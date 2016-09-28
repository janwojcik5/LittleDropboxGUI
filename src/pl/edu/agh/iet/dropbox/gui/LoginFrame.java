package pl.edu.agh.iet.dropbox.gui;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField userNameField;
    private JTextField passwordField;

    private JLabel userLabel;
    private JLabel passwordLabel;

    private JButton okButton;

    public LoginFrame() {
        super("Login");

        userLabel=new JLabel("user");
        userLabel.setBounds(10,10,80,25);

        passwordLabel=new JLabel("password");
        passwordLabel.setBounds(10, 40, 80, 25);

        userNameField=new JTextField(20);
        userNameField.setBounds(100, 10, 160, 25);

        passwordField=new JPasswordField(20);
        passwordField.setBounds(100, 40, 160, 25);

        okButton=new JButton("OK");
        okButton.setBounds(10, 80, 80, 25);

        BoxLayout layout=new BoxLayout(getContentPane(),BoxLayout.Y_AXIS);
        this.setLayout(layout);

        add(userLabel);
        add(userNameField);
        add(passwordLabel);
        add(passwordField);
        add(okButton);

        setSize(300,150);
        show();

    }


}
