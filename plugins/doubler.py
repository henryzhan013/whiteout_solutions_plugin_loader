def get_metadata():
    return {
        "name": "doubler",
        "version": "1.0",
        "category": "math",
        "minLoaderVersion": "1.0.0",
        "inputs": {"value": "int:min=0,max=1000"},
        "outputs": {"result": "int"}
    }

def execute(inputs):
    return {"result": inputs["value"] * 2}
