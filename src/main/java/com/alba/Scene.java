import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;

public class Scene extends JPanel implements KeyListener {
    private int width;
    private int height;
    private Vector3 backgroundColor;
    private Camera camera;
    private Sphere[] spheres;
    private Light[] lights;
    private Vector3 ambientLight;
    private BufferedImage image;
    private boolean altPressed = false;
    private Point lastMousePosition;
    private boolean rotating = false;
    private Robot robot;  // For centering the cursor
    private Point centerPoint;  // Center of the panel for cursor locking
    private boolean showSettings = false;  // Flag to show/hide settings panel
    private Render renderer = new Render(null);
    private HDRLoader hdrLoader;
    private boolean useHDR = false; // Flag to use HDR if loaded
    private int renderMethod = 0;

    private BufferedImage imgR; // Image R
    private BufferedImage imgS; // Image S
    // UI Components for Settings
    public JTextField resolutionWidthField;
    public JTextField resolutionHeightField;
    public JTextField ambientRField;
    public JTextField ambientGField;
    public JTextField ambientBField;
    public JTextField backgroundRField;
    public JTextField backgroundGField;
    public JTextField backgroundBField;
    public JTextField FOVField;
    public JTextField HDRfield;
    public JPanel settingsPanel;  // Container for all settings

    public Scene(int width, int height, Sphere[] spheres, Light[] lights, Vector3 ambientLight) {
        this.width = width;
        this.height = height;
        this.backgroundColor = new Vector3(0, 0, 0);
        double aspectRatio = (double) width / height;
        double fov = 90.0;  // Field of view of 90 degrees

        this.camera = new Camera(aspectRatio, fov);
        this.spheres = spheres;
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
            imgR = ImageIO.read(new File("src/main/resources/R.png"));
            imgS = ImageIO.read(new File("src/main/resources/S.png"));
        } catch (IOException e) {
            System.err.println("Failed to load images.");
            e.printStackTrace();
        }

        try {
            hdrLoader = new HDRLoader("src/main/resources/Assets/.hdr");
            useHDR = true; // Set flag to use HDR for rendering

            // Initialize the renderer with HDRLoader
            renderer = new Render(hdrLoader);

        } catch (IOException e) {
            System.out.println("HDR file not found or failed to load, using default background.");
            // Initialize without HDRLoader
            useHDR = false;
            renderer = new Render(null);
        }



        // Initialize the UI components for settings
        initUIComponents(width, height);

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

                        if (rBounds.contains(e.getPoint())) {
                            renderMethod = 0;
                            render();
                            repaint();
                        } else if (sBounds.contains(e.getPoint())) {
                            renderMethod = 1;
                            render();
                            repaint();
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



    public void initUIComponents(int width, int height) {
        setLayout(null);  // Use absolute positioning for overlay components
	    Font customFont = new Font("Arial", Font.PLAIN, 12);
        // Create settings panel container
        settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsPanel.setBounds(55, 20, 150, 230);  // Position the panel
        settingsPanel.setBorder(BorderFactory.createTitledBorder(null, "Scene", TitledBorder.LEFT, TitledBorder.TOP, customFont, Color.white));  // Add a border with title
        settingsPanel.setBackground(new Color(123, 114, 100, 255));  // Semi-transparent background
        settingsPanel.setVisible(false);  // Hidden by default
        add(settingsPanel);

        // Set a custom font for all components in the settings panel
        

        // Add UI components to the settings panel
        // Resolution settings
        JLabel resolutionLabel = new JLabel("Resolution            ");
	    resolutionLabel.setForeground(Color.white);
        resolutionLabel.setFont(customFont);
        settingsPanel.add(resolutionLabel);

        resolutionWidthField = createTextField("640", e -> updateResolution(), customFont);
        settingsPanel.add(resolutionWidthField);
        resolutionHeightField = createTextField("360", e -> updateResolution(), customFont);
        settingsPanel.add(resolutionHeightField);

        // Ambient light settings
        JLabel ambientLabel = new JLabel("Ambient Light      ");
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
          
        JLabel HDRLabel = new JLabel("HDR                         ");
	    HDRLabel.setForeground(Color.white);
        HDRLabel.setFont(customFont);
        settingsPanel.add(HDRLabel);

        HDRfield = createTextField("", e -> loadHDR(), customFont);
        HDRfield.setColumns(11);
        settingsPanel.add(HDRfield);

        // Fov settings
        JLabel FOVLabel = new JLabel("FOV");
	    FOVLabel.setForeground(Color.white);
        FOVLabel.setFont(customFont);
        settingsPanel.add(FOVLabel);

        FOVField = createTextField("90", e -> updateFOV(), customFont);
        settingsPanel.add(FOVField);
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
        String r = HDRfield.getText();
        // Update ambient light
        try {
            hdrLoader = new HDRLoader("src/main/resources/Assets/"+r+".hdr");
            useHDR = true; // Set flag to use HDR for rendering

            // Initialize the renderer with HDRLoader
            renderer = new Render(hdrLoader);

        } catch (IOException e) {
            System.out.println("HDR file not found or failed to load, using default background.");
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
        // Parse background color values and set the background accordingly
        double f = Double.parseDouble(FOVField.getText());
        double aspectRatio = (double) width / height;
        double fov = f;  // Field of view of 90 degrees

        this.camera = new Camera(aspectRatio, fov, camera.getOrigin(), camera.getLookAt(), camera.getOrientation());
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

        // Draw the image scaled to the current size of the panel
        g.drawImage(image, 0, 0, panelWidth, panelHeight, null);

        // Draw images R and S in the bottom-left corner
        if (imgR != null) {
            g.drawImage(imgR, 5, panelHeight - imgR.getHeight() - 5, this);
        }
        if (imgS != null) {
            g.drawImage(imgS, imgR.getWidth() + 10, panelHeight - imgS.getHeight() - 5, this);
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
        double rotateSpeed = 5.0; // Adjust the speed of camera rotation

        int keycode = e.getKeyCode();
        if (keycode == KeyEvent.VK_W) camera.moveForward(moveSpeed);  // Move forward 
        if (keycode == KeyEvent.VK_S) camera.moveBackward(moveSpeed); // Move backward
        if (keycode == KeyEvent.VK_A) camera.moveLeft(moveSpeed); // Move left
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
                    Vector3 color = renderer.traceRay(ray, spheres, lights, ambientLight, backgroundColor);

                    int r = (int) (255.99 * color.x);
                    int g = (int) (255.99 * color.y);
                    int b = (int) (255.99 * color.z);

                    int rgb = (r << 16) | (g << 8) | b;
                    image.setRGB(i, j, rgb);
                }
            }
        } else {
            for (int j = 0; j < height; j+=2) {
                for (int i = 0; i < width; i+=2) {
                    double u = (double) i / (width - 1);
                    double v = (double) (height - j - 1) / (height - 1);
                    Ray ray = camera.getRay(u, v);
                    Vector3 color = renderer.traceRayBasic(ray, spheres, lights, ambientLight, backgroundColor);

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

    public static void main(String[] args) {
        Sphere[] spheres = {
                   new Sphere(new Vector3(0, -2, 3), 2, 
                   new ObjectProperties(new Vector3(1, 0, 1), 0, .1, .3, .4, .3, new Texture("bricks.jpg"))),
                   new Sphere(new Vector3(0, 2, 3), 1,
                   new ObjectProperties( new Vector3(1, 0, 1), 100, .1, .2, .3, .1)),
                   new Sphere(new Vector3(0, -504, 0), 500,
                   new ObjectProperties(new Vector3(.5, .5, .5))),
        };

        Light[] lights = {
            new Light(new Vector3(1, 5, 0), new Vector3(1, 1, 1), 1), // White light

        };


        Vector3 ambientLight = new Vector3(0.1, 0.1, 0.1); // Low-intensity ambient light

        Scene scene = new Scene(640, 360, spheres, lights, ambientLight);
        scene.render();

        JFrame frame = new JFrame("GPTracer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(scene.width, scene.height);
        frame.add(scene);

        frame.setVisible(true);
        frame.setResizable(true);  // Allow the window to be resizable
    }
}
