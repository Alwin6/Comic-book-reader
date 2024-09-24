package com.alba;

import javax.swing.*;

public class Main {

    public static void main(String[] args){

        JFrame frame = new JFrame("Comic book reader");
        frame.setSize(800, 600);    // Window Size
        frame.setLayout(null);      // Just set it to null
        frame.setVisible(true);     // Default is false, that is why we need to set visibility to true

        JLabel label = new JLabel("Hello");            // Initialize the label with a text
        label.setBounds(100, 100, 100, 25);     // Sets the absolute position and size of the component to the window
        frame.add(label);

        JTextField text = new JTextField();            // Initialize the text field without any text value
        text.setBounds(100, 200, 100, 25);  // Sets the absolute position and size of the component to the window
        frame.add(text);

        JButton button = new JButton("CLICK ME");       // Initialize the button with a text
        button.setBounds(100, 300, 100, 25);    // Sets the absolute position and size of the component to the window
        frame.add(button);

        frame.revalidate();
        frame.repaint();

        // On default, the close [x] button will do nothing so
        // we have to set it to JFrame.EXIT_ON_CLOSE which will
        // close the window properly
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        com.alba.tracer.Scene.main(args);
    }
}
