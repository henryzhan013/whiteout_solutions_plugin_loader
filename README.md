# Polyglot Plugin Loader

A Java app that loads and runs plugins written in Java or Python. Drop a `.jar` or `.py` file in the `plugins/` folder and it just works.

## How it works

```
CLI → PluginLoader → finds .jar and .py files → PluginExecutor → runs them
```

- Java plugins: loaded via `URLClassLoader` at runtime
- Python plugins: executed as subprocesses, communicate via JSON

## Project structure

```
src/main/java/pluginloader/
├── cli/          # command line stuff
├── core/         # plugin interface, registry, loader, executor
├── loader/       # java and python specific loaders
├── model/        # data classes
├── util/         # json, logging, python runner
└── validation/   # input validation
```

## Why these decisions?

**Python via subprocess instead of embedding Jython/GraalPython**
- way simpler
- works with any python version you have installed
- if python crashes, java keeps running

**Type constraints like `int:min=0,max=100`**
- validates inputs before running the plugin
- gives clear error messages when something's wrong

**30 second timeout on python**
- plugins can't hang forever and freeze the app

## Example plugins

### Python (in `plugins/`)

**doubler.py** - doubles a number
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

**greet.py** - says hello
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
    // ... other methods
}
```

Compile it to a jar and drop it in `plugins/`.

## Running it

**Build:**
```bash
mvn clean package
```

**List plugins:**
```bash
java -jar target/polyglot-plugin-loader-1.0-SNAPSHOT-jar-with-dependencies.jar list
```

**Run a plugin:**
```bash
java -jar target/polyglot-plugin-loader-1.0-SNAPSHOT-jar-with-dependencies.jar run-plugin doubler --value 5
```

Output is JSON:
```json
{
  "status": "success",
  "plugin": "doubler",
  "output": {"result": 10}
}
```

## Bonus: distributed execution

If I wanted to run plugins across multiple machines, I'd go with a coordinator-worker setup:

```
Coordinator (receives requests, routes them)
     ↓
Workers (machines that actually run plugins)
```

The main changes:
- Workers expose HTTP endpoints (`/execute`, `/health`)
- Coordinator keeps track of which worker has which plugins
- Add timeouts and retries for when workers fail
- Use request IDs so you can trace what happened

The tricky parts are handling partial failures (some workers succeed, some don't) and making sure retries don't run the same plugin twice (idempotency).

For a stateless system like this, I'd prioritize availability over consistency - any worker with the plugin can handle any request, so just route around failures.
