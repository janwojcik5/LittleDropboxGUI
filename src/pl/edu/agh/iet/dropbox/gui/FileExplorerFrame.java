package pl.edu.agh.iet.dropbox.gui;

import javax.swing.*;

public class FileExplorerFrame extends JFrame {

    private JTree fileTree;
    private JButton addFile;
    private String userLogin;

    public FileExplorerFrame(String userLogin) {

        super("Your Files");

        setSize(400,300);

    }
}
