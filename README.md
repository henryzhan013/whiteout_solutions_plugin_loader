# Polyglot Plugin Loader

A Java app that loads and runs plugins written in Java or Python. Drop a `.jar` or `.py` file in the `plugins/` folder and it just works.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                      CLI                            │
│            (parses commands, prints output)         │
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

**The flow:** CLI receives a command → loader finds plugins in `plugins/` folder → registry stores them → executor validates inputs and runs the plugin → result comes back as JSON.

**Java plugins** get loaded via `URLClassLoader` and reflection. **Python plugins** run as subprocesses - we pass inputs as JSON, they return outputs as JSON.

## Design decisions

- **Python via subprocess** - simpler than embedding Jython. Works with any Python version. If Python crashes, Java keeps going.
- **Stateless plugins** - every run is independent, no memory between calls. Easier to reason about.
- **30 second timeout** - plugins can't hang forever.
- **JSON everywhere** - inputs, outputs, errors. Works across languages.

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
        "inputs": {"name": "string:default=World"},
        "outputs": {"message": "string"}
    }

def execute(inputs):
    return {"message": f"Hello, {inputs['name']}!"}
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

## Running it

```bash
# build
mvn clean package

# list plugins
java -jar target/polyglot-plugin-loader-1.0-SNAPSHOT-jar-with-dependencies.jar list

# run a plugin
java -jar target/polyglot-plugin-loader-1.0-SNAPSHOT-jar-with-dependencies.jar run-plugin doubler --value 5
```

Output:
```json
{
  "status": "success",
  "plugin": "doubler",
  "output": {"result": 10}
}
```

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
