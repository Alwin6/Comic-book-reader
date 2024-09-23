package com.alba.tracer;

public class ObjectProperties {
    public Vector3 color;
    public double emission;
    public double metallicness;
    public double reflectiveness;
    public double smoothness;
    public double transparency;
    public Texture texture;

    public ObjectProperties(Vector3 color, double emission, double metallicness, double reflectiveness, double smoothness, double transparency, Texture texture) {
        this.color = color;
        this.emission = emission;
        this.metallicness = metallicness;
        this.reflectiveness = reflectiveness;
        this.smoothness = smoothness;
        this.transparency = transparency;
        this.texture = texture;
    }

    public ObjectProperties(Vector3 color, double emission, double metallicness, double reflectiveness, double smoothness, double transparency) {
        this.color = color;
        this.emission = emission;
        this.metallicness = metallicness;
        this.reflectiveness = reflectiveness;
        this.smoothness = smoothness;
        this.transparency = transparency;
        this.texture = new Texture();
    }

    public ObjectProperties(Vector3 color) {
        this.color = color;
        this.emission = 0.0;
        this.metallicness = 0.0;
        this.reflectiveness = 0.0;
        this.smoothness = 0.0;
        this.transparency = 0;
        this.texture = new Texture();
    }

    public ObjectProperties() {
        this.color = new Vector3(0, 0, 0);
        this.emission = 0.0;
        this.metallicness = 0.0;
        this.reflectiveness = 0.0;
        this.smoothness = 0.0;
        this.transparency = 0;
        this.texture = new Texture();
    }



}
