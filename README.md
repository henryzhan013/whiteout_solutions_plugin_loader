# Polyglot Plugin Loader

A Java app that loads and runs plugins written in Java or Python. Drop a `.jar` or `.py` file in the `plugins/` folder and it just works.

## How it works

```
CLI → PluginLoader → finds .jar and .py files → PluginExecutor → runs them
```

- Java plugins: loaded via `URLClassLoader` at runtime
- Python plugins: executed as subprocesses, communicate via JSON

## Architecture - the whole shebang

Alright, let me walk y'all through how this thing is put together.

### Core stuff (`pluginloader.core`)

**Plugin.java** - This here's the interface every plugin's gotta follow. Got 5 main methods: `getName()`, `getVersion()`, `getInputs()`, `getOutputs()`, and `execute()`. Don't matter if it's Java or Python, they all gotta act like this interface.

**PluginRegistry.java** - Think of it like a phonebook for plugins. When we load 'em up, they get registered here. Need to find a plugin by name? Ask the registry. Want all the math plugins? Registry handles that too.

**PluginLoader.java** - This ol' boy scans the `plugins/` folder, figures out what's a `.jar` and what's a `.py`, and hands 'em off to the right loader. It's the foreman, delegates the real work.

**PluginExecutor.java** - When you wanna actually run a plugin, this is your guy. Takes the plugin name and inputs, validates everything's kosher, runs it, and wraps up the result nice and pretty.

### Loaders (`pluginloader.loader`)

**JavaPluginLoader.java** - Loads up `.jar` files using `URLClassLoader`. Does some reflection magic to find classes that implement our Plugin interface.

**PythonPluginLoader.java** - For `.py` files. Doesn't actually run Python itself, just creates a wrapper.

**PythonPlugin.java** - The wrapper for Python plugins. When you call `execute()`, it fires up a Python subprocess, passes the inputs as JSON, and reads back the result.

### Models (`pluginloader.model`)

**ExecutionResult.java** - Just a container for what comes back when you run a plugin. Got a status (success/error), the plugin name, and the output.

**PluginMetadata.java** - Holds info about a plugin - name, version, category, what inputs it takes, what outputs it gives.

**PluginType.java** - Enum for the types we support: int, float, string, boolean. Knows how to convert strings to these types.

### Validation (`pluginloader.validation`)

**InputConstraint.java** - Parses stuff like `int:min=0,max=100` and validates inputs against it. Makes sure you ain't passing garbage to a plugin.

**PluginValidator.java** - Checks that a plugin's metadata makes sense before we try to run it.

### Utilities (`pluginloader.util`)

**JsonUtil.java** - Wrapper around Gson. Converts objects to JSON and back. Nothing fancy.

**PythonRunner.java** - Actually runs Python subprocesses. Handles the timeout (30 seconds, then we kill it), reads stdout, all that mess.

**AppLogger.java** - Simple logging helper. Prints stuff to console with timestamps.

### CLI (`pluginloader.cli`)

**CliParser.java** - Figures out what command you're running (`list`, `run-plugin`) and pulls out the arguments.

**PluginPrinter.java** - Formats the output nice and pretty for the terminal.

## Design decisions

**Python via subprocess** - Could've used Jython or GraalPython, but subprocess is way simpler. Works with whatever Python you got installed. If Python crashes, Java keeps truckin'.

**Stateless plugins** - Every run is independent. Plugin don't remember nothing from last time. Keeps things simple.

**30 second timeout** - Plugins can't hang forever. Take too long, we pull the plug.

**JSON everywhere** - Inputs, outputs, errors - all JSON. Easy to work with, works across languages.

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
