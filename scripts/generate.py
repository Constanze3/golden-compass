
def item_files():
    for i in range(35):
        with open(f"golden_compass_{i:02}.json", "w") as file:
            data = f"""{{
  "parent": "minecraft:item/generated",
  "textures": {{
    "layer0": "golden_compass:item/golden_compass_{i:02}"
  }}
}}"""
    
            file.write(data);

def rename():
    import os
    import re

    pattern = re.compile(r"(?P<name>[a-zA-Z_]+)(?P<number>\d+)\.(?P<extension>[a-z]+)");

    for filename in os.listdir("."):
        match = pattern.match(filename)
        if match:
            name = match.group("name")
            number = int(match.group("number"))
            extension = match.group("extension")

            os.rename(filename, f"{name}_{(number - 1):02}.{extension}")


rename()