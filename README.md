# Polyglot Plugin Loader

A Java app that loads and runs plugins written in Java or Python. Drop a `.jar` or `.py` file in the `plugins/` folder and it just works.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Interactive CLI                   │
│              (REPL loop, parses commands)           │
└─────────────────────────┬───────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────┐
│                    JobManager                       │
│         (async execution, thread pool)              │
└─────────────────────────┬───────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────┐
│                      Core                           │
│  PluginLoader → PluginRegistry → PluginExecutor    │
│  (finds plugins)  (stores them)   (runs them)      │
└────────────┬────────────────────────────┬───────────┘
             │                            │
┌────────────▼──────────┐    ┌────────────▼───────────┐
│   JavaPluginLoader    │    │   PythonPluginLoader   │
│ (URLClassLoader)      │    │ (subprocess + JSON)    │
└───────────────────────┘    └────────────────────────┘
```

**The flow:**
1. CLI receives a command
2. JobManager creates an async job
3. PluginExecutor validates inputs and runs the plugin
4. Result is stored in history with job ID
5. User can check job status anytime

## Design decisions

- **Async job execution** - plugins run in a thread pool (4 threads), so multiple can run in parallel. You get a job ID back immediately.
- **Execution history** - every execution (success or failure) is saved to `executions.json` with its job ID.
- **Python via subprocess** - simpler than embedding Jython. Works with any Python version. If Python crashes, Java keeps going.
- **Stateless plugins** - every run is independent, no memory between calls.
- **30 second timeout** - plugins can't hang forever.
- **Input validation** - type constraints like `int:min=0,max=100` are validated before execution.

## Running it

```bash
# build
mvn clean package

# start interactive mode
java -jar target/plugin-loader.jar
```

## Commands

Once in the interactive prompt:

```
help                          Show all commands
list                          List all plugins
list --category=math          List plugins in a category
run-plugin <name> --k=v       Submit a job to run a plugin
jobs                          List all jobs with status
job <id>                      Get job details and result
history                       Show execution history
exit                          Quit
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

**doubler.py**
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

**greet.py**
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

**MultiplierPlugin.java**
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

Compile to a jar and drop in `plugins/`.

## Bonus: distributed execution

To run plugins across multiple machines, I'd use a coordinator-worker setup:

```
┌─────────────┐       ┌─────────────┐
│ Coordinator │──────▶│  Worker A   │
│  (router)   │──────▶│  Worker B   │
└─────────────┘       └─────────────┘
```

**How it works:**
- Workers run an HTTP server with `/execute` (run a plugin) and `/health` (report status + available plugins)
- Coordinator polls workers periodically to know who's alive and who has what
- When a request comes in, coordinator picks a worker that has the plugin and forwards the request
- If a worker times out, retry on a different one

**Key changes to the code:**
- `PluginExecutor.execute()` becomes an HTTP call instead of a local call
- Add a `WorkerRegistry` to track available workers
- Add request IDs for tracing and to prevent duplicate execution on retry

**The hard parts:**
- Partial failure: what if 2 of 3 workers succeed? Return partial results or fail everything?
- Idempotency: if we retry, make sure the plugin doesn't run twice (use request IDs)
- No shared state: workers don't talk to each other, coordinator is the single source of truth

Since plugins are stateless, any worker with the plugin can handle any request - makes load balancing simple.
