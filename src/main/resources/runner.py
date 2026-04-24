#!/usr/bin/env python3
import sys
import json
import importlib.util

def load_plugin(plugin_path):
    spec = importlib.util.spec_from_file_location("plugin", plugin_path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module

def main():
    if len(sys.argv) < 3:
        print(json.dumps({"error": "Usage: runner.py <plugin_path> <action> [json_inputs]"}))
        sys.exit(1)

    plugin_path = sys.argv[1]
    action = sys.argv[2]

    try:
        plugin = load_plugin(plugin_path)

        if action == "metadata":
            metadata = plugin.get_metadata()
            print(json.dumps(metadata))

        elif action == "execute":
            if len(sys.argv) < 4:
                print(json.dumps({"error": "Missing json_inputs for execute action"}))
                sys.exit(1)

            inputs = json.loads(sys.argv[3])
            result = plugin.execute(inputs)
            print(json.dumps(result))

        else:
            print(json.dumps({"error": f"Unknown action: {action}"}))
            sys.exit(1)

    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

if __name__ == "__main__":
    main()
