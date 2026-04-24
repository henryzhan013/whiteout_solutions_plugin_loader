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
    else:
        return {"message": f"Hello, {name}."}
