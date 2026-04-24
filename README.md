# Polyglot Plugin Loader

A Java app that loads and runs plugins written in Java or Python. Drop a `.jar` or `.py` file in the `plugins/` folder and it just works.

## Architecture

**On startup:**
```
PluginLoader scans plugins/ folder
        │
        ├──▶ JavaPluginLoader ──▶ loads .jar files
        │
        └──▶ PythonPluginLoader ──▶ loads .py files
                    │
                    ▼
            PluginRegistry (stores all plugins)
```

**On each command:**
```
CLI (interactive prompt)
        │
        ▼
    JobManager (creates async job, runs in thread pool)
        │
        ▼
    PluginExecutor (finds plugin in registry, validates, executes)
        │
        ▼
    ExecutionHistory (saves result with job ID)
```

**How it all fits together:**
1. Startup: plugins get loaded into the registry
2. User types a command
3. JobManager kicks off a job, hands back an ID right away
4. PluginExecutor does the actual work in the background
5. Result gets saved to history
6. User can check on it with `job <id>` whenever they want

## Design decisions

- **Async job execution** - plugins run in a thread pool (4 threads), so you can fire off multiple at once. You get a job ID back right away, no waiting around.
- **Execution history** - every run (good or bad) gets saved to `executions.json` with its job ID. Nice for debugging.
- **Python via subprocess** - way simpler than embedding Jython. Works with whatever Python you've got. If Python blows up, Java keeps on trucking.
- **Stateless plugins** - each run starts fresh, no memory of past calls. Keeps things simple.
- **30 second timeout** - plugins can't hog resources forever.
- **Input validation** - constraints like `int:min=0,max=100` get checked before we even try to run.

## Running it

```bash
# build it
mvn clean package

# fire it up
java -jar target/plugin-loader.jar
```

## Commands

Once you're in:

```
help                          Show all commands
list                          List all plugins
list --category=math          Filter by category
run-plugin <name> --k=v       Kick off a plugin job
jobs                          See all jobs and their status
job <id>                      Check on a specific job
history                       Look at past executions
exit                          Head out
```

## Example session

```
> list
Available plugins:

  [math]
    Name: doubler
    Inputs: {value=int:min=0,max=1000}

  [text]
    Name: greet
    Inputs: {name=string:default=World}

> run-plugin doubler --value=5
Job submitted: a1b2c3d4
Use 'job a1b2c3d4' to check status

> jobs
Jobs (1):
  a1b2c3d4  COMPLETED  doubler

> job a1b2c3d4
Job: a1b2c3d4
Plugin: doubler
Status: COMPLETED
Result:
{
  "status": "SUCCESS",
  "outputs": {"result": 10}
}

> history
Execution history (1 records):

1. [a1b2c3d4] 2024-04-24T19:30:00Z
   Plugin: doubler
   Inputs: {value=5}
   Status: success
   Output: {result=10}

> exit
Bye!
```

## Example plugins

### Python (in `plugins/`)

**doubler.py** - doubles a number, nothing fancy
```python
def get_metadata():
    return {
        "name": "doubler",
        "version": "1.0",
        "category": "math",
        "inputs": {"value": "int:min=0,max=1000"},
        "outputs": {"result": "int"}
    }

def execute(inputs):
    return {"result": inputs["value"] * 2}
```

**greet.py** - says howdy
```python
def get_metadata():
    return {
        "name": "greet",
        "version": "1.0",
        "category": "text",
        "inputs": {
            "name": "string:default=World,minlength=1,maxlength=50",
            "excited": "boolean:required=false,default=false"
        },
        "outputs": {"message": "string"}
    }

def execute(inputs):
    name = inputs["name"]
    excited = inputs.get("excited", False)
    if excited:
        return {"message": f"Hello, {name}!!!"}
    return {"message": f"Hello, {name}."}
```

### Java (in `example-plugins/`)

**MultiplierPlugin.java** - multiplies two numbers
```java
public class MultiplierPlugin implements Plugin {
    public String getName() { return "multiplier"; }
    public Map<String, String> getInputs() {
        return Map.of("a", "int", "b", "int");
    }
    public Map<String, Object> execute(Map<String, Object> inputs) {
        int a = ((Number) inputs.get("a")).intValue();
        int b = ((Number) inputs.get("b")).intValue();
        return Map.of("product", a * b);
    }
}
```

Compile it to a jar and toss it in `plugins/`.

## Bonus: distributed execution

If we wanted to run plugins across multiple machines, I'd go with a coordinator-worker setup:

```
┌─────────────┐       ┌─────────────┐
│ Coordinator │──────▶│  Worker A   │
│  (router)   │──────▶│  Worker B   │
└─────────────┘       └─────────────┘
```

**How it works:**
- Workers run an HTTP server with `/execute` (run a plugin) and `/health` (report what they've got)
- Coordinator pings workers to know who's alive and who has what
- Request comes in, coordinator picks a worker and forwards it
- Worker times out? Try another one

**What we'd need to change:**
- `PluginExecutor.execute()` becomes an HTTP call instead of local
- Add a `WorkerRegistry` to keep track of workers
- Request IDs for tracing and making sure retries don't run twice

**The tricky bits:**
- Partial failure: 2 of 3 workers succeed, what do we do?
- Idempotency: retries shouldn't double-run
- No shared state between workers, coordinator's the boss

Since plugins are stateless, any worker with the plugin can handle any request. Makes load balancing real straightforward.
