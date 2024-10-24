package com.alba.tracer;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.io.IOException;
import java.io.File;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class Scene extends JPanel implements KeyListener {
    private int width, height;
    private Vector3 backgroundColor;
    private Camera camera;
    private Shape[] shapes;
    private Light[] lights;
    private Vector3 ambientLight;
    private BufferedImage image;
    private boolean altPressed = false;
    private boolean rotating = false;
    private Robot robot;  // For centering the cursor
    private Point centerPoint;  // Center of the panel for cursor locking
    private boolean showSettings = false;  // Flag to show/hide settings panel
    private Render renderer = new Render(null);
    private HDRLoader hdrLoader;
    private boolean useHDR = false; // Flag to use HDR if loaded
    private int renderMethod = 0;
    private int sampleCount = 4;
    private int selectedObject = -1;
    private int selectedObjectType = 0;
    private int reflections = 150;

    private BufferedImage imgR, imgS, imgE; // Image R

    // UI Components for Settings
    public JTextField resolutionWidthField, resolutionHeightField;
    public JTextField ambientRField, ambientGField, ambientBField;
    public JTextField backgroundRField, backgroundGField, backgroundBField;
    public JTextField FOVField, HDRField, sampleCountField, reflectionsField;
    public JTextField exportSceneField, importSceneField;
    public JPanel settingsPanel;  // Container for all settings

    // UI components for edit panel
    public JTextField objectT, originX, originY, originZ;
    public JTextField scaleX, scaleY, scaleZ;
    public JTextField rotationX, rotationY, rotationZ;
    public JTextField colorR, colorG, colorB;
    public JTextField emissivity, metallicness, reflectiveness, smoothness, transparency, texture;
    public JPanel editPanel;  // Container for all settings

    //UI components for light edit panel
    public JTextField lOriginX, lOriginY, lOriginZ;
    public JTextField lColorR, lColorG, lColorB;
    public JTextField intensity, radius;
    public JPanel lightEditPanel;

    public JTextField shapeName;
    public JPanel addPanel;


    public Scene(int width, int height, Shape[] shapes, Light[] lights, Vector3 ambientLight) {
        this.width = width;
        this.height = height;
        this.backgroundColor = new Vector3(0, 0, 0);
        double aspectRatio = (double) width / height;
        double fov = 90.0;  // Field of view of 90 degrees

        this.camera = new Camera(aspectRatio, fov);
        this.shapes = shapes;
        this.lights = lights;
        this.ambientLight = ambientLight;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        setFocusable(true);  // For key events to work
        addKeyListener(this);

        try {
            robot = new Robot();  // Initialize Robot for mouse control
        } catch (AWTException e) {
            e.printStackTrace();
        }

        try {
            imgR = ImageIO.read(new File("src/main/resources/tracer/R.png"));
            imgS = ImageIO.read(new File("src/main/resources/tracer/S.png"));
            imgE = ImageIO.read(new File("src/main/resources/tracer/E.png"));
        } catch (IOException e) {
            System.err.println("Failed to load images.");
            e.printStackTrace();
        }

        try {
            hdrLoader = new HDRLoader("src/main/resources/tracer/Assets/.hdr");
            useHDR = true; // Set flag to use HDR for rendering

            // Initialize the renderer with HDRLoader
            renderer = new Render(hdrLoader);

        } catch (IOException e) {
            // System.out.println("HDR file not found or failed to load, using default background.");
            // Initialize without HDRLoader
            useHDR = false;
            renderer = new Render(null);
        }


        // Initialize the UI components for settings
        initUIComponents(width);
        // Mouse listener to handle mouse press and release events
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                if (altPressed) {
                    rotating = true;
                    centerPoint = e.getLocationOnScreen();  // Set centerPoint to the current cursor position
                    setCursor(createInvisibleCursor());  // Hide cursor
                } else {
                    // If click is within the axis area (50x50 in the top-left corner), toggle settings visibility
                    if (e.getX() < 50 && e.getY() < 50 && !rotating) {
                        showSettings = !showSettings;
                        toggleSettingsVisibility(showSettings);
                        repaint();  // Repaint to update the display
                    } else {
                        // Check if click is within the bounds of imgR or imgS
                        Rectangle rBounds = new Rectangle(5, getHeight() - imgR.getHeight() - 5, imgR.getWidth(), imgR.getHeight());
                        Rectangle sBounds = new Rectangle(imgR.getWidth() + 10, getHeight() - imgS.getHeight() - 5, imgS.getWidth(), imgS.getHeight());
                        Rectangle eBounds = new Rectangle(imgR.getWidth() * 2 + 15, getHeight() - imgE.getHeight() - 5, imgE.getWidth(), imgE.getHeight());

                        if (rBounds.contains(e.getPoint())) {
                            renderMethod = 0;
                            render();
                            repaint();
                        } else if (sBounds.contains(e.getPoint())) {
                            renderMethod = 1;
                            render();
                            repaint();
                        } else if (eBounds.contains(e.getPoint())) {
                            renderMethod = 2;
                            render();
                            repaint();
                        }else {
                            // clicking objects
                            int panelWidth = getWidth();
                            int panelHeight = getHeight();


                            double u = (double) e.getX() / (panelWidth - 1);
                            double v = (double) (panelHeight - e.getY() - 1) / (panelHeight - 1);

                            Ray ray = camera.getRay(u, v);
                            int previous = selectedObject;
                            int previousSelectedObjectType = selectedObjectType;
                            Vector2 selection = select(ray);
                            selectedObject = (int)selection.x;
                            selectedObjectType = (int)selection.y;
                            if (previous == selectedObject && selectedObjectType == previousSelectedObjectType) {editPanel.setVisible(false);selectedObject=-1;}
                            editPanel.setVisible(selectedObject != -1 && selectedObjectType == 0);
                            lightEditPanel.setVisible(selectedObject != -1 && selectedObjectType == 1);

                            if (selectedObject != -1) {
                                if (selectedObjectType == 0) {
                                    GetValuesOfSelectedObject();
                                } else {
                                    GetValuesOfSelectedLight();
                                }
                            }

                            render(); repaint();

                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (rotating) {
                    rotating = false;
                    setCursor(Cursor.getDefaultCursor());  // Show cursor
                }
            }
        });

        // Mouse motion listener to handle dragging (rotation)
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (rotating) {
                    // Get current mouse position on screen
                    Point currentMousePosition = e.getLocationOnScreen();

                    // Calculate the difference (delta) from the original cursor position (centerPoint)
                    int deltaX = currentMousePosition.x - centerPoint.x;
                    int deltaY = currentMousePosition.y - centerPoint.y;

                    // Rotate the camera based on mouse movement
                    camera.rotateY(-deltaX * 0.1);  // Yaw rotation
                    camera.rotateX(-deltaY * 0.1);  // Pitch rotation

                    // Re-render the scene and repaint
                    render();
                    repaint();

                    // Move the cursor back to the locked position (centerPoint)
                    robot.mouseMove(centerPoint.x, centerPoint.y);
                }
            }
        });

        // Key listener to detect when Alt is pressed or released
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ALT) {
                    altPressed = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ALT) {
                    altPressed = false;
                    if (rotating) {
                        rotating = false;
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }
        });
    }

    public Vector2 select(Ray ray) {
        return renderer.traceFind(ray, shapes, lights, renderMethod);
    }

    public void GetValuesOfSelectedObject() {
        objectT.setText(shapes[selectedObject].objectType);
        originX.setText(String.valueOf(shapes[selectedObject].origin.x));
        originY.setText(String.valueOf(shapes[selectedObject].origin.y));
        originZ.setText(String.valueOf(shapes[selectedObject].origin.z));
        scaleZ.setText(String.valueOf(shapes[selectedObject].scale.z));
        scaleX.setText(String.valueOf(shapes[selectedObject].scale.x));
        scaleY.setText(String.valueOf(shapes[selectedObject].scale.y));
        rotationX.setText(String.valueOf(shapes[selectedObject].rotation.x));
        rotationY.setText(String.valueOf(shapes[selectedObject].rotation.y));
        rotationZ.setText(String.valueOf(shapes[selectedObject].rotation.z));
        colorR.setText(String.valueOf(shapes[selectedObject].properties.color.x));
        colorG.setText(String.valueOf(shapes[selectedObject].properties.color.y));
        colorB.setText(String.valueOf(shapes[selectedObject].properties.color.z));
        emissivity.setText(String.valueOf(shapes[selectedObject].properties.emission));
        metallicness.setText(String.valueOf(shapes[selectedObject].properties.metallicness));
        smoothness.setText(String.valueOf(shapes[selectedObject].properties.smoothness));
        reflectiveness.setText(String.valueOf(shapes[selectedObject].properties.reflectiveness));
        transparency.setText(String.valueOf(shapes[selectedObject].properties.transparency));
        texture.setText(shapes[selectedObject].properties.texture.filename);
    }

    public void GetValuesOfSelectedLight() {
        lOriginX.setText(String.valueOf(lights[selectedObject].position.x));
        lOriginY.setText(String.valueOf(lights[selectedObject].position.y));
        lOriginZ.setText(String.valueOf(lights[selectedObject].position.z));
        lColorR.setText(String.valueOf(lights[selectedObject].color.x));
        lColorG.setText(String.valueOf(lights[selectedObject].color.y));
        lColorB.setText(String.valueOf(lights[selectedObject].color.z));
        intensity.setText(String.valueOf(lights[selectedObject].intensity));
        radius.setText(String.valueOf(lights[selectedObject].radius));
    }

    public void initUIComponents(int width) {
        setLayout(null);  // Use absolute positioning for overlay components
	    Font customFont = new Font("Arial", Font.PLAIN, 12);
        // Create settings panel container
        settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsPanel.setBounds(55, 20, 250, 245);  // Position the panel
        settingsPanel.setBorder(BorderFactory.createTitledBorder(null, "Scene", TitledBorder.LEFT, TitledBorder.TOP, customFont, Color.white));  // Add a border with title
        settingsPanel.setBackground(new Color(123, 114, 100, 255));  // Semi-transparent background
        settingsPanel.setVisible(false);  // Hidden by default
        add(settingsPanel);

        // Add UI components to the settings panel
        // Resolution settings
        JLabel resolutionLabel = new JLabel("Resolution                            ");
	    resolutionLabel.setForeground(Color.white);
        resolutionLabel.setFont(customFont);
        settingsPanel.add(resolutionLabel);

        resolutionWidthField = createTextField("640", e -> updateResolution(), customFont);
        settingsPanel.add(resolutionWidthField);
        resolutionHeightField = createTextField("360", e -> updateResolution(), customFont);
        settingsPanel.add(resolutionHeightField);

        // Ambient light settings
        JLabel ambientLabel = new JLabel("Ambient Light        ");
	    ambientLabel.setForeground(Color.white);
        ambientLabel.setFont(customFont);
        settingsPanel.add(ambientLabel);

        ambientRField = createTextField("0.1", e -> updateAmbientLight(), customFont);
        settingsPanel.add(ambientRField);
        ambientGField = createTextField("0.1", e -> updateAmbientLight(), customFont);
        settingsPanel.add(ambientGField);
        ambientBField = createTextField("0.1", e -> updateAmbientLight(), customFont);
        settingsPanel.add(ambientBField);

        // Background color settings
        JLabel backgroundLabel = new JLabel("Background Color");
	    backgroundLabel.setForeground(Color.white);
        backgroundLabel.setFont(customFont);
        settingsPanel.add(backgroundLabel);

        backgroundRField = createTextField("0", e -> updateBackgroundColor(), customFont);
        settingsPanel.add(backgroundRField);
        backgroundGField = createTextField("0", e -> updateBackgroundColor(), customFont);
        settingsPanel.add(backgroundGField);
        backgroundBField = createTextField("0", e -> updateBackgroundColor(), customFont);
        settingsPanel.add(backgroundBField);

        JLabel HDRLabel = new JLabel("HDR                          ");
	    HDRLabel.setForeground(Color.white);
        HDRLabel.setFont(customFont);
        settingsPanel.add(HDRLabel);

        HDRField = createTextField("", e -> loadHDR(), customFont);
        HDRField.setColumns(11);
        settingsPanel.add(HDRField);

        // Fov settings
        JLabel FOVLabel = new JLabel("FOV                                                       ");
	    FOVLabel.setForeground(Color.white);
        FOVLabel.setFont(customFont);
        settingsPanel.add(FOVLabel);

        FOVField = createTextField("90", e -> updateFOV(), customFont);
        settingsPanel.add(FOVField);
        // Fov settings
        JLabel SampleLabel = new JLabel("Light Samples                                    ");
        SampleLabel.setForeground(Color.white);
        SampleLabel.setFont(customFont);
        settingsPanel.add(SampleLabel);

        sampleCountField = createTextField("4", e -> {sampleCount = Integer.parseInt(sampleCountField.getText());render();repaint();}, customFont);
        settingsPanel.add(sampleCountField);
        // Fov settings definitely right?
        JLabel reflectionsLabel = new JLabel("Ray Depth                                            ");
        reflectionsLabel.setForeground(Color.white);
        reflectionsLabel.setFont(customFont);
        settingsPanel.add(reflectionsLabel);

        reflectionsField = createTextField("150", e -> {reflections = Integer.parseInt(reflectionsField.getText());render();repaint();}, customFont);
        settingsPanel.add(reflectionsField);

        JLabel saveField = new JLabel("Export Scene           ");
        saveField.setForeground(Color.white);
        saveField.setFont(customFont);
        settingsPanel.add(saveField);

        exportSceneField = createTextField("", e -> {
            SceneManager export = new SceneManager(shapes, lights, backgroundColor, ambientLight, hdrLoader, sampleCount, reflections);
            try {
                SceneManager.Export(exportSceneField.getText(), export);
            } catch (IOException ex) {
                System.err.println("fuck");
            }
        }, customFont);
        exportSceneField.setColumns(11);
        settingsPanel.add(exportSceneField);

        JLabel loadField = new JLabel("Import Scene          ");
        loadField.setForeground(Color.white);
        loadField.setFont(customFont);
        settingsPanel.add(loadField);

        importSceneField = createTextField("", e -> {
            SceneManager imp = new SceneManager(shapes, lights, backgroundColor, ambientLight, hdrLoader, sampleCount, reflections);

            try {
                imp.Import(importSceneField.getText());
                ambientLight = imp.ambientLight;
                hdrLoader = imp.hdrLoader;
                if (hdrLoader != null) {
                    renderer = new Render(hdrLoader);
                } else {
                    renderer = new Render(null);
                }
                sampleCount = imp.sampleCount;
                reflections = imp.reflections;
                shapes = imp.shapes;
                lights = imp.lights;
                backgroundColor = imp.backgroundColor;
                ambientRField.setText(String.valueOf(ambientLight.x));
                ambientGField.setText(String.valueOf(ambientLight.y));
                ambientBField.setText(String.valueOf(ambientLight.z));
                backgroundRField.setText(String.valueOf(backgroundColor.x));
                backgroundGField.setText(String.valueOf(backgroundColor.y));
                backgroundBField.setText(String.valueOf(backgroundColor.z));
                if (hdrLoader != null) {
                    HDRField.setText(hdrLoader.filename);
                } else {
                    HDRField.setText("");
                }
                sampleCountField.setText(String.valueOf(sampleCount));
                reflectionsField.setText(String.valueOf(reflections));

                render();
                repaint();
            } catch (IOException ex) {
                System.err.println("fuck");
            }


        }, customFont);
        importSceneField.setColumns(11);
        settingsPanel.add(importSceneField);


        // --------------------------------------------------------------------------------------------------------------

        setLayout(null);  // Use absolute positioning for overlay components
        // Create edit panel container
        editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        editPanel.setBounds(width - 220, 20, 200, 300);  // Position the panel
        editPanel.setBorder(BorderFactory.createTitledBorder(null, "Object", TitledBorder.LEFT, TitledBorder.TOP, customFont, Color.white));  // Add a border with title
        editPanel.setBackground(new Color(123, 114, 100, 255));  // Semi-transparent background
        editPanel.setVisible(false);  // Hidden by default
        add(editPanel);

        JLabel meshLabel = new JLabel("Mesh        ");
        meshLabel.setForeground(Color.white);
        meshLabel.setFont(customFont);
        editPanel.add(meshLabel);

        objectT = createTextField("sphere", e -> updateShape(), customFont);
        objectT.setColumns(11);
        editPanel.add(objectT);


        JLabel originLabel = new JLabel("Location ");
        originLabel.setForeground(Color.white);
        originLabel.setFont(customFont);
        editPanel.add(originLabel);

        originX = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(originX);
        originY = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(originY);
        originZ = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(originZ);

        JLabel scaleLabel = new JLabel("Scale      ");
        scaleLabel.setForeground(Color.white);
        scaleLabel.setFont(customFont);
        editPanel.add(scaleLabel);

        scaleX = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(scaleX);
        scaleY = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(scaleY);
        scaleZ = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(scaleZ);

        JLabel RotationLabel = new JLabel("Rotation ");
        RotationLabel.setForeground(Color.white);
        RotationLabel.setFont(customFont);
        editPanel.add(RotationLabel);

        rotationX = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(rotationX);
        rotationY = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(rotationY);
        rotationZ = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(rotationZ);

        JLabel colorLabel = new JLabel("Color      ");
        colorLabel.setForeground(Color.white);
        colorLabel.setFont(customFont);
        editPanel.add(colorLabel);

        colorR = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(colorR);
        colorG = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(colorG);
        colorB = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(colorB);

        JLabel emissivityLabel = new JLabel("Emissivity                            ");
        emissivityLabel.setForeground(Color.white);
        emissivityLabel.setFont(customFont);
        editPanel.add(emissivityLabel);

        emissivity = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(emissivity);

        JLabel metallicnessLabel = new JLabel("Metallicness                       ");
        metallicnessLabel.setForeground(Color.white);
        metallicnessLabel.setFont(customFont);
        editPanel.add(metallicnessLabel);

        metallicness = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(metallicness);

        JLabel ReflectivenessLabel = new JLabel("Reflectiveness                   ");
        ReflectivenessLabel.setForeground(Color.white);
        ReflectivenessLabel.setFont(customFont);
        editPanel.add(ReflectivenessLabel);

        reflectiveness = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(reflectiveness);

        JLabel SmoothnessLabel = new JLabel("Smoothness                      ");
        SmoothnessLabel.setForeground(Color.white);
        SmoothnessLabel.setFont(customFont);
        editPanel.add(SmoothnessLabel);

        smoothness = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(smoothness);

        JLabel TransparencyLabel = new JLabel("Transparency                     ");
        TransparencyLabel.setForeground(Color.white);
        TransparencyLabel.setFont(customFont);
        editPanel.add(TransparencyLabel);

        transparency = createTextField("0", e -> updateShape(), customFont);
        editPanel.add(transparency);

        JLabel textureLabel = new JLabel("Texture    ");
        textureLabel.setForeground(Color.white);
        textureLabel.setFont(customFont);
        editPanel.add(textureLabel);

        texture = createTextField("", e -> updateShape(), customFont);
        texture.setColumns(11);
        editPanel.add(texture);

        // -----------------------------

        setLayout(null);  // Use absolute positioning for overlay components
        // Create edit panel container
        lightEditPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lightEditPanel.setBounds(width - 220, 20, 200, 125);  // Position the panel
        lightEditPanel.setBorder(BorderFactory.createTitledBorder(null, "Light", TitledBorder.LEFT, TitledBorder.TOP, customFont, Color.white));  // Add a border with title
        lightEditPanel.setBackground(new Color(123, 114, 100, 255));  // Semi-transparent background
        lightEditPanel.setVisible(false);  // Hidden by default
        add(lightEditPanel);


        JLabel loriginLabel = new JLabel("Location ");
        loriginLabel.setForeground(Color.white);
        loriginLabel.setFont(customFont);
        lightEditPanel.add(loriginLabel);

        lOriginX = createTextField("0", e -> updatelight(), customFont);
        lightEditPanel.add(lOriginX);
        lOriginY = createTextField("0", e -> updatelight(), customFont);
        lightEditPanel.add(lOriginY);
        lOriginZ = createTextField("0", e -> updatelight(), customFont);
        lightEditPanel.add(lOriginZ);

        JLabel lcolorLabel = new JLabel("Color      ");
        lcolorLabel.setForeground(Color.white);
        lcolorLabel.setFont(customFont);
        lightEditPanel.add(lcolorLabel);

        lColorR = createTextField("0", e -> updatelight(), customFont);
        lightEditPanel.add(lColorR);
        lColorG = createTextField("0", e -> updatelight(), customFont);
        lightEditPanel.add(lColorG);
        lColorB = createTextField("0", e -> updatelight(), customFont);
        lightEditPanel.add(lColorB);

        JLabel intensityLabel = new JLabel("Intensity                               ");
        intensityLabel.setForeground(Color.white);
        intensityLabel.setFont(customFont);
        lightEditPanel.add(intensityLabel);

        intensity = createTextField("0", e -> updatelight(), customFont);
        lightEditPanel.add(intensity);

        JLabel radiusLabel = new JLabel("Radius                                 ");
        radiusLabel.setForeground(Color.white);
        radiusLabel.setFont(customFont);
        lightEditPanel.add(radiusLabel);

        radius = createTextField("0", e -> updatelight(), customFont);
        lightEditPanel.add(radius);

        // -----------------------------

        setLayout(null);  // Use absolute positioning for overlay components
        // Create edit panel container
        addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.setBounds(0, 0, 150, 50);  // Position the panel
        addPanel.setBorder(BorderFactory.createTitledBorder(null, "Add", TitledBorder.LEFT, TitledBorder.TOP, customFont, Color.white));  // Add a border with title
        addPanel.setBackground(new Color(123, 114, 100, 255));  // Semi-transparent background
        addPanel.setVisible(false);  // Hidden by default
        add(addPanel);

        shapeName = createTextField("", e -> {
            if (!shapeName.getText().isEmpty()) {
                if (!shapeName.getText().equals("light")) {
                    Shape[] newShapes = Arrays.copyOf(shapes, shapes.length + 1);
                    newShapes[newShapes.length - 1] = new Shape(shapeName.getText(), new Vector3(0, 0, 0), new Vector3(1, 1, 1), new Vector3(0, 0, 0), new ObjectProperties(new Vector3(.5, .5, .5)));
                    shapes = newShapes;

                    selectedObject = shapes.length - 1;
                    selectedObjectType = 0;
                    GetValuesOfSelectedObject();
                    editPanel.setVisible(true);
                    lightEditPanel.setVisible(false);
                    render();
                    repaint();
                    addPanel.setVisible(false);
                } else {
                    Light[] newLights = Arrays.copyOf(lights, lights.length + 1);
                    newLights[newLights.length - 1] = new Light(new Vector3(0, 0, 0), new Vector3(1, 1, 1), 1, 0);
                    lights = newLights;

                    selectedObject = lights.length - 1;
                    selectedObjectType = 1;
                    GetValuesOfSelectedLight();
                    editPanel.setVisible(false);
                    lightEditPanel.setVisible(true);
                    render();
                    repaint();
                    addPanel.setVisible(false);
                }

            } else {
                addPanel.setVisible(false);
            }

        }, customFont);
        shapeName.setColumns(11);

        addPanel.add(shapeName);

    }

    public void updateShape() {
        shapes[selectedObject] = new Shape(objectT.getText(),
                                  new Vector3(Double.parseDouble(originX.getText()), Double.parseDouble(originY.getText()), Double.parseDouble(originZ.getText())),
                                  new Vector3(Double.parseDouble(scaleX.getText()), Double.parseDouble(scaleY.getText()), Double.parseDouble(scaleZ.getText())),
                                  new Vector3(Double.parseDouble(rotationX.getText()), Double.parseDouble(rotationY.getText()), Double.parseDouble(rotationZ.getText())),
                                  new ObjectProperties(
                                          new Vector3(Double.parseDouble(colorR.getText()), Double.parseDouble(colorG.getText()), Double.parseDouble(colorB.getText())),
                                          Double.parseDouble(emissivity.getText()),
                                          Double.parseDouble(metallicness.getText()),
                                          Double.parseDouble(reflectiveness.getText()),
                                          Double.parseDouble(smoothness.getText()),
                                          Double.parseDouble(transparency.getText()),
                                          new Texture(texture.getText())
                                  )
        );
        render();
        repaint();
    }

    public void updatelight() {
        lights[selectedObject] = new Light(new Vector3(Double.parseDouble(lOriginX.getText()), Double.parseDouble(lOriginY.getText()), Double.parseDouble(lOriginZ.getText())),
                                           new Vector3(Double.parseDouble(lColorR.getText()), Double.parseDouble(lColorG.getText()), Double.parseDouble(lColorB.getText())),
                                           Double.parseDouble(intensity.getText()),
                                           Double.parseDouble(radius.getText())
        );
        render();
        repaint();
    }

    public JTextField createTextField(String text, ActionListener actionListener, Font font) {
        JTextField textField = new JTextField(text);
        textField.addActionListener(actionListener);  // Update when pressing Enter
        textField.setPreferredSize(new Dimension(40, 20));
        textField.setFont(font);  // Set the custom font
        return textField;
    }

    public void toggleSettingsVisibility(boolean visible) {
        settingsPanel.setVisible(visible);  // Show/hide the settings panel
    }

    public void updateResolution() {
        // Parse width and height
        int newWidth = Integer.parseInt(resolutionWidthField.getText());
        int newHeight = Integer.parseInt(resolutionHeightField.getText());
        // Update resolution
        this.width = newWidth;
        this.height = newHeight;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        double aspectRatio = (double) width / height;
	    double f = Double.parseDouble(FOVField.getText());
        this.camera = new Camera(aspectRatio, f, camera.getOrigin(), camera.getLookAt(), camera.getOrientation());
        render();
        repaint();
    }

    public void updateAmbientLight() {
        // Parse ambient light values
        double r = Double.parseDouble(ambientRField.getText());
        double g = Double.parseDouble(ambientGField.getText());
        double b = Double.parseDouble(ambientBField.getText());
        // Update ambient light
        this.ambientLight = new Vector3(r, g, b);
        render();
        repaint();
    }

    public void loadHDR() {
        // Parse ambient light values
        String r = HDRField.getText();
        // Update ambient light
        try {
            hdrLoader = new HDRLoader("src/main/resources/tracer/Assets/"+r+".hdr");
            useHDR = true; // Set flag to use HDR for rendering

            // Initialize the renderer with HDRLoader
            renderer = new Render(hdrLoader);

        } catch (IOException e) {
            System.err.println("HDR file not found or failed to load, using default background");
            // Initialize without HDRLoader
            useHDR = false;
            renderer = new Render(null);
        }
        render();
        repaint();
    }

    public void updateBackgroundColor() {
        // Parse background color values and set the background accordingly
        double r = Double.parseDouble(backgroundRField.getText());
        double g = Double.parseDouble(backgroundGField.getText());
        double b = Double.parseDouble(backgroundBField.getText());
        backgroundColor = new Vector3(r, g, b);
        render();
        repaint();
    }
    public void updateFOV() {
        this.camera = new Camera((double) width / height, Double.parseDouble(FOVField.getText()), camera.getOrigin(), camera.getLookAt(), camera.getOrientation());
        render();
        repaint();
    }

    public Cursor createInvisibleCursor() {
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.createCustomCursor(cursorImg, new Point(0, 0), "InvisibleCursor");
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Get the current size of the JPanel
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        editPanel.setBounds(panelWidth - 220, 20, 200, 300);
        lightEditPanel.setBounds(panelWidth - 220, 20, 200, 125);// Position the panel

        // Draw the image scaled to the current size of the panel
        g.drawImage(image, 0, 0, panelWidth, panelHeight, null);

        // Draw images R and S in the bottom-left corner
        if (imgR != null) {
            g.drawImage(imgR, 5, panelHeight - imgR.getHeight() - 5, this);
        }
        if (imgS != null) {
            assert imgR != null;
            g.drawImage(imgS, imgR.getWidth() + 10, panelHeight - imgS.getHeight() - 5, this);
        }
        if (imgE != null) {
            assert imgR != null;
            assert imgS != null;
            g.drawImage(imgE, imgR.getWidth() * 2 + 15, panelHeight - imgE.getHeight() - 5, this);
        }

        // Draw camera orientation
        drawOrientation(g);
    }

    private void drawOrientation(Graphics g) {
        int x = 25;  // Starting x position for drawing
        int y = 25;  // Starting y position for drawing

        int axisLength = 20;  // Length of each axis line

        // Draw X-axis (Red)
        g.setColor(Color.RED);
        g.drawLine(x, y, x + (int) (axisLength * camera.getRight().x), y + (int) (axisLength * camera.getRight().y));

        // Draw Y-axis (Green)
        g.setColor(Color.GREEN);
        g.drawLine(x, y, x + (int) (axisLength * camera.getUp().x), y + (int) (axisLength * camera.getUp().y));

        // Draw Z-axis (Blue)
        g.setColor(Color.BLUE);
        g.drawLine(x, y, x + (int) (axisLength * camera.getForward().x), y + (int) (axisLength * camera.getForward().y));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        double moveSpeed = 0.1;  // Adjust the speed of camera movement

        int keycode = e.getKeyCode();
        if (keycode == KeyEvent.VK_W) camera.moveForward(moveSpeed);  // Move forward
        if (keycode == KeyEvent.VK_S) camera.moveBackward(moveSpeed); // Move backward
        if (keycode == KeyEvent.VK_A) {
            if (!altPressed) {
                camera.moveLeft(moveSpeed);
            } else {
                Point f = MouseInfo.getPointerInfo().getLocation();
                addPanel.setBounds( f.x + 10, f.y + 5, 150, 50);
                addPanel.setVisible(true);
                shapeName.setText("");
            }
        } // Move left
        if (keycode == KeyEvent.VK_D) camera.moveRight(moveSpeed); // Move right
        if (keycode == KeyEvent.VK_SPACE) camera.moveUp(moveSpeed); // Move up
        if (keycode == KeyEvent.VK_SHIFT) camera.moveDown(moveSpeed); // Move down

        // Re-render the scene after moving the camera
        render();
        repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // No action needed
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // No action needed
    }

    public void render() {
        if (renderMethod == 0) {
            for (int j = 0; j < height; j++) {
                for (int i = 0; i < width; i++) {
                    double u = (double) i / (width - 1);
                    double v = (double) (height - j - 1) / (height - 1);
                    Ray ray = camera.getRay(u, v);
                    Vector3 color = renderer.traceRay(ray, shapes, lights, ambientLight, backgroundColor, sampleCount, selectedObject, reflections, selectedObjectType);

                    int r = (int) (255.99 * color.x);
                    int g = (int) (255.99 * color.y);
                    int b = (int) (255.99 * color.z);

                    int rgb = (r << 16) | (g << 8) | b;
                    image.setRGB(i, j, rgb);
                }
            }
        } else if (renderMethod == 1) {
            for (int j = 0; j < height; j+=2) {
                for (int i = 0; i < width; i+=2) {
                    double u = (double) i / (width - 1);
                    double v = (double) (height - j - 1) / (height - 1);
                    Ray ray = camera.getRay(u, v);
                    Vector3 color = renderer.traceRayBasic(ray, shapes, backgroundColor, selectedObject, selectedObjectType);

                    int r = (int) (255.99 * color.x);
                    int g = (int) (255.99 * color.y);
                    int b = (int) (255.99 * color.z);

                    int rgb = (r << 16) | (g << 8) | b;
                    image.setRGB(i, j, rgb);
                    image.setRGB(i+1, j, rgb);
                    image.setRGB(i+1, j+1, rgb);
                    image.setRGB(i, j+1, rgb);
                }
            }
        } else {
            for (int j = 0; j < height; j+=2) {
                for (int i = 0; i < width; i+=2) {
                    double u = (double) i / (width - 1);
                    double v = (double) (height - j - 1) / (height - 1);
                    Ray ray = camera.getRay(u, v);
                    Vector3 color = renderer.traceRayEdit(ray, shapes, backgroundColor, selectedObject, lights, selectedObjectType);

                    int r = (int) (255.99 * color.x);
                    int g = (int) (255.99 * color.y);
                    int b = (int) (255.99 * color.z);

                    int rgb = (r << 16) | (g << 8) | b;
                    image.setRGB(i, j, rgb);
                    image.setRGB(i+1, j, rgb);
                    image.setRGB(i+1, j+1, rgb);
                    image.setRGB(i, j+1, rgb);
                }
            }
        }
    }

    public static void init() {

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignore) {}

        Shape[] shapes = {
                   new Shape("sphere", new Vector3(0, -2, 3), new Vector3(2, 1, .2), new Vector3(30, 90,20),
                   new ObjectProperties(new Vector3(1, 0, 1), 2, .1, .3, .4, .3, new Texture("bricks.jpg"))),
                   new Shape("sphere", new Vector3(0, 2, 3), new Vector3(1, 1, 1), new Vector3(0, 0,0 ),
                   new ObjectProperties( new Vector3(1, 0, 1), 0, .1, .2, .3, .1)),
                   new Shape("cube", new Vector3(0,2,-3), new Vector3(1, 1, 1), new Vector3(0, 0, 0),
                   new ObjectProperties( new Vector3(1, .5, .3), 0, 0, .3, 1, 0)),
                   new Shape("sphere", new Vector3(0, -4, 0), new Vector3(500, 0.001, 500), new Vector3(0, 0,0),
                   new ObjectProperties(new Vector3(.5, .5, .5))),
        };

        Light[] lights = {
            new Light(new Vector3(1, 5, 0), new Vector3(1, 1, 1), 1, 0), // White light
        };


        Vector3 ambientLight = new Vector3(0.1, 0.1, 0.1); // Low-intensity ambient light

        Scene scene = new Scene(640, 360, shapes, lights, ambientLight);
        scene.render();

        JFrame frame = new JFrame("GPTracer");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Only close this window
        frame.setSize(656, 399);
        frame.add(scene);

        frame.setVisible(true);
        frame.setResizable(true);  // Allow the window to be resizable
    }
}