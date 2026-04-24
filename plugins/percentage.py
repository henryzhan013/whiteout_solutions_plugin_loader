def get_metadata():
    return {
        "name": "percentage",
        "version": "1.0",
        "category": "math",
        "minLoaderVersion": "1.0.0",
        "inputs": {
            "value": "float:min=0,max=100",
            "total": "float:min=0.01"
        },
        "outputs": {"percent": "float"}
    }

def execute(inputs):
    value = inputs["value"]
    total = inputs["total"]
    return {"percent": round((value / total) * 100, 2)}
