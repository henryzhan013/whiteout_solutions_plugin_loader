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
Coordinator (routes requests) → Workers (run plugins)
```

Workers expose `/execute` and `/health` endpoints. Coordinator tracks which worker has which plugins, adds timeouts and retries.

The tricky parts: handling partial failures and making retries idempotent. For a stateless system like this, prioritize availability - any worker with the plugin can handle any request.
