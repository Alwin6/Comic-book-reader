package com.alba.tracer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class SceneManager {
    public Vector3 backgroundColor;
    public Shape[] shapes;
    public Light[] lights;
    public Vector3 ambientLight;
    public HDRLoader hdrLoader;
    public int sampleCount = 4;
    public int reflections = 150;

    public SceneManager(Shape[] shapes, Light[] lights, Vector3 backgroundColor, Vector3 ambientLight, HDRLoader hdrLoader, int sampleCount, int reflections) {
        this.shapes = shapes; // a shape contains, Vector3 origin, scale, rotation. String objectType, ObjectProperties properties
                              // object properties are accessed with .properties, an ObjectProperties contains:
                              // Vector3 color, double emission, metallicness, reflectiveness, smoothness, transparency. Texture texture
                              // .properties.texture.filename returns the filename of the texture
        this.lights = lights; // a light contains, Vector3, position, color. double intensity, radius
        this.backgroundColor = backgroundColor;
        this.ambientLight = ambientLight;
        this.hdrLoader = hdrLoader; // hdrLoader.filename gives the filename
        this.sampleCount = sampleCount;
        this.reflections = reflections;
    }


    public static void Export(String filePath, SceneManager scene) throws IOException {
        // Create a JSONObject to store the scene data
        JSONObject sceneData = new JSONObject();

        // Export background color
        sceneData.put("backgroundColor", vectorToJson(scene.backgroundColor));

        // Export ambient light
        sceneData.put("ambientLight", vectorToJson(scene.ambientLight));

        // Export HDR file
        if (scene.hdrLoader != null && scene.hdrLoader.filename != null) {
            sceneData.put("hdrLoader", scene.hdrLoader.filename);
        }

        // Export sample count and reflections
        sceneData.put("sampleCount", scene.sampleCount);
        sceneData.put("reflections", scene.reflections);

        // Export shapes
        JSONArray shapesArray = new JSONArray();
        for (Shape shape : scene.shapes) {
            JSONObject shapeData = new JSONObject();
            shapeData.put("origin", vectorToJson(shape.origin));
            shapeData.put("scale", vectorToJson(shape.scale));
            shapeData.put("rotation", vectorToJson(shape.rotation));
            shapeData.put("objectType", shape.objectType);

            // Export object properties
            JSONObject properties = new JSONObject();
            properties.put("color", vectorToJson(shape.properties.color));
            properties.put("emission", shape.properties.emission);
            properties.put("metallicness", shape.properties.metallicness);
            properties.put("reflectiveness", shape.properties.reflectiveness);
            properties.put("smoothness", shape.properties.smoothness);
            properties.put("transparency", shape.properties.transparency);

            // Export texture if available
            if (shape.properties.texture != null && shape.properties.texture.filename != null) {
                properties.put("texture", shape.properties.texture.filename);
            }

            shapeData.put("properties", properties);
            shapesArray.put(shapeData);
        }
        sceneData.put("shapes", shapesArray);

        // Export lights
        JSONArray lightsArray = new JSONArray();
        for (Light light : scene.lights) {
            JSONObject lightData = new JSONObject();
            lightData.put("position", vectorToJson(light.position));
            lightData.put("color", vectorToJson(light.color));
            lightData.put("intensity", light.intensity);
            lightData.put("radius", light.radius);
            lightsArray.put(lightData);
        }
        sceneData.put("lights", lightsArray);

        // Write the JSON object to a file
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(sceneData.toString(4));  // 4 is for pretty print indentation
            file.flush();
        }
    }

    public void Import(String filePath) throws IOException {
        // Open and read the JSON file
        try (FileReader reader = new FileReader(filePath)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject sceneData = new JSONObject(tokener);

            // Parse background color
            this.backgroundColor = jsonToVector(sceneData.getJSONObject("backgroundColor"));

            // Parse ambient light
            this.ambientLight = jsonToVector(sceneData.getJSONObject("ambientLight"));

            // Parse sample count and reflections
            this.sampleCount = sceneData.getInt("sampleCount");
            this.reflections = sceneData.getInt("reflections");

            // Parse HDRLoader if present
            if (sceneData.has("hdrLoader")) {
                String hdrFilePath = sceneData.getString("hdrLoader");
                this.hdrLoader = new HDRLoader(hdrFilePath);
            } else {
                this.hdrLoader = null;
            }

            // Parse shapes
            JSONArray shapesArray = sceneData.getJSONArray("shapes");
            this.shapes = new Shape[shapesArray.length()];
            for (int i = 0; i < shapesArray.length(); i++) {
                JSONObject shapeData = shapesArray.getJSONObject(i);

                // Shape properties
                String objectType = shapeData.getString("objectType");
                Vector3 origin = jsonToVector(shapeData.getJSONObject("origin"));
                Vector3 scale = jsonToVector(shapeData.getJSONObject("scale"));
                Vector3 rotation = jsonToVector(shapeData.getJSONObject("rotation"));

                // ObjectProperties
                JSONObject propertiesData = shapeData.getJSONObject("properties");
                Vector3 color = jsonToVector(propertiesData.getJSONObject("color"));
                double emission = propertiesData.getDouble("emission");
                double metallicness = propertiesData.getDouble("metallicness");
                double reflectiveness = propertiesData.getDouble("reflectiveness");
                double smoothness = propertiesData.getDouble("smoothness");
                double transparency = propertiesData.getDouble("transparency");

                // Texture if available
                Texture texture = null;
                if (propertiesData.has("texture")) {
                    String textureFileName = propertiesData.getString("texture");
                    texture = new Texture(textureFileName);
                }

                ObjectProperties properties = new ObjectProperties(color, emission, metallicness, reflectiveness, smoothness, transparency, texture);
                this.shapes[i] = new Shape(objectType, origin, scale, rotation, properties);
            }

            // Parse lights
            JSONArray lightsArray = sceneData.getJSONArray("lights");
            this.lights = new Light[lightsArray.length()];
            for (int i = 0; i < lightsArray.length(); i++) {
                JSONObject lightData = lightsArray.getJSONObject(i);

                Vector3 position = jsonToVector(lightData.getJSONObject("position"));
                Vector3 color = jsonToVector(lightData.getJSONObject("color"));
                double intensity = lightData.getDouble("intensity");
                double radius = lightData.getDouble("radius");

                this.lights[i] = new Light(position, color, intensity, radius);
            }
        }
    }

    // Utility method to convert Vector3 to JSONObject
    private static JSONObject vectorToJson(Vector3 vector) {
        JSONObject vectorJson = new JSONObject();
        vectorJson.put("x", vector.x);
        vectorJson.put("y", vector.y);
        vectorJson.put("z", vector.z);
        return vectorJson;
    }

    // Utility method to convert a JSONObject to Vector3
    private static Vector3 jsonToVector(JSONObject json) {
        return new Vector3(json.getDouble("x"), json.getDouble("y"), json.getDouble("z"));
    }
}
