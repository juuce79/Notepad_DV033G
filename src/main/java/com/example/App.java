package com.example;

import java.io.*;
import java.awt.event.*;
import java.awt.Font;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

public class App extends JFrame implements ActionListener {
    private static final String APP_TITLE_PREFIX = "notepad - ";
    private static final String DEFAULT_TITLE_CONSTANT = "Untitled.txt";
    private static final int SCROLL_INCREMENT = 20;

    JTextArea textArea;
    JFrame frame;
    String path = "";
    String defaultTitle = DEFAULT_TITLE_CONSTANT;
    JFileChooser fileChooser;

    // Menu Items
    private JMenuItem miNew;
    private JMenuItem miOpen;
    private JMenuItem miSave;

    public App() {
        initializeFrame();
        initializeTextArea();
        initializeMenuBar();
        initializeKeyboardShortcuts();
        initializeWindowListener();
        initializeDocumentListener();
        initializeMouseWheelListeners();
        initializeFileChooser();
    }

    /**
     * Initializes the main application frame.
     */
    private void initializeFrame() {
        frame = new JFrame(APP_TITLE_PREFIX + defaultTitle);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setResizable(true);
        frame.setVisible(true);
    }

    /**
     * Initializes the text area and adds it to the frame within a scroll pane.
     */
    private void initializeTextArea() {
        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane);
    }

    /**
     * Initializes the menu bar with "File" and "Edit" menus and their respective menu items.
     */
    private void initializeMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        miNew = new JMenuItem("New");
        miOpen = new JMenuItem("Open");
        miSave = new JMenuItem("Save");
        JMenuItem miSaveAs = new JMenuItem("Save as");
        JMenuItem miPrint = new JMenuItem("Print");
        JMenuItem miClose = new JMenuItem("Close");

        // Add action listeners
        ActionListener menuListener = this;
        JMenuItem[] fileMenuItems = { miNew, miOpen, miSave, miSaveAs, miPrint, miClose};
        for (JMenuItem item : fileMenuItems) {
            item.addActionListener(menuListener);
            fileMenu.add(item);
        }

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem miCut = new JMenuItem("Cut");
        JMenuItem miCopy = new JMenuItem("Copy");
        JMenuItem miPaste = new JMenuItem("Paste");

        JMenuItem[] editMenuItems = {miCut, miCopy, miPaste};
        for (JMenuItem item : editMenuItems) {
            item.addActionListener(menuListener);
            editMenu.add(item);
        }

        // Add menus to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        frame.setJMenuBar(menuBar);
    }

    /**
     * Initializes keyboard shortcuts for common actions like Save, Open, and New.
     */
    private void initializeKeyboardShortcuts() {
        // Save Shortcut
        textArea.getInputMap().put(KeyStroke.getKeyStroke("control S"), "save");
        textArea.getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                miSave.doClick();
            }
        });

        // Open Shortcut
        textArea.getInputMap().put(KeyStroke.getKeyStroke("control O"), "open");
        textArea.getActionMap().put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                miOpen.doClick();
            }
        });

        // New Shortcut
        textArea.getInputMap().put(KeyStroke.getKeyStroke("control N"), "new");
        textArea.getActionMap().put("new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                miNew.doClick();
            }
        });
    }

    /**
     * Initializes window listener to handle the window closing event.
     */
    private void initializeWindowListener() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (kill() == 1) {
                    frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                }
            }
        });
    }

    /**
     * Initializes document listener to detect changes in the text area.
     */
    private void initializeDocumentListener() {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateFrameTitle(true);
            }

            public void removeUpdate(DocumentEvent e) {
                updateFrameTitle(true);
            }

            public void insertUpdate(DocumentEvent e) {
                updateFrameTitle(true);
            }
        });
    }

    /**
     * Initializes mouse wheel listeners for scrolling and zooming functionalities.
     */
    private void initializeMouseWheelListeners() {
        textArea.addMouseWheelListener(this::handleMouseWheelEvent);
    }

    /**
     * Handles mouse wheel events for scrolling and zooming.
     *
     * @param e the mouse wheel event
     */
    void handleMouseWheelEvent(MouseWheelEvent e) {
        if (e.isAltDown()) {
            handleZoom(e);
        } else {
            handleScroll(e);
        }
    }

    /**
     * Handles scrolling based on mouse wheel rotation.
     *
     * @param e the mouse wheel event
     */
    private void handleScroll(MouseWheelEvent e) {
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, textArea);
        if (scrollPane != null) {
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            int value = verticalScrollBar.getValue();
            if (e.getWheelRotation() < 0) {
                verticalScrollBar.setValue(value - SCROLL_INCREMENT);
            } else {
                verticalScrollBar.setValue(value + SCROLL_INCREMENT);
            }
        }
    }

    /**
     * Handles zooming in and out based on mouse wheel rotation.
     *
     * @param e the mouse wheel event
     */
    private void handleZoom(MouseWheelEvent e) {
        int currentSize = textArea.getFont().getSize();
        if (e.getWheelRotation() < 0) {
            textArea.setFont(new Font("Arial", Font.PLAIN, currentSize + 1));
        } else {
            if (currentSize > 1) {
                textArea.setFont(new Font("Arial", Font.PLAIN, currentSize - 1));
            }
        }
    }

    /**
     * Initializes the file chooser with specific file filters.
     */
    private void initializeFileChooser() {
        String[] descriptions = { "*.txt", "*.docx", "*.doc", "*.java", "*.py", "*.c", "*.cpp",
                "*.html", "*.css", "*.js", "*.json", "*.xml", "*.yml", "*.yaml",
                "*.md", "*.markdown", "*.sql" };
        String[] extensions = { "txt", "docx", "doc", "java", "py", "c", "cpp",
                "html", "css", "js", "json", "xml", "yml", "yaml",
                "md", "markdown", "sql" };

        fileChooser = new JFileChooser("C:");
        for (int i = 0; i < descriptions.length; i++) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(descriptions[i], extensions[i]);
            fileChooser.addChoosableFileFilter(filter);
        }
        fileChooser.setAcceptAllFileFilterUsed(false);
    }

    /**
     * Updates the frame title based on whether the document has unsaved changes.
     *
     * @param modified indicates if the document has been modified
     */
    private void updateFrameTitle(boolean modified) {
        if (modified) {
            frame.setTitle(APP_TITLE_PREFIX + defaultTitle + " *");
        } else {
            frame.setTitle(APP_TITLE_PREFIX + defaultTitle);
        }
    }

    /**
     * Handles menu item actions based on their command.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "New":
                newFile();
                break;
            case "Open":
                open();
                break;
            case "Save":
                saveToPath();
                break;
            case "Save as":
                save();
                break;
            case "Print":
                printFile();
                break;
            case "Cut":
                textArea.cut();
                break;
            case "Copy":
                textArea.copy();
                break;
            case "Paste":
                textArea.paste();
                break;
            case "Close":
                kill();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + command);
        }
    }

    /**
     * Creates a new file, prompting the user to save if there are unsaved changes.
     */
    public void newFile() {
        if (frame.getTitle().contains("*")) {
            int choice = JOptionPane.showConfirmDialog(frame,
                    "Do you want to save changes to " + defaultTitle + "?",
                    "Confirm Save",
                    JOptionPane.YES_NO_CANCEL_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                saveToPath();
                resetEditor();
            } else if (choice == JOptionPane.NO_OPTION) {
                resetEditor();
            }
            // If CANCEL_OPTION, do nothing
        } else {
            resetEditor();
        }
    }

    /**
     * Resets the editor to the default state.
     */
    private void resetEditor() {
        textArea.setText("");
        defaultTitle = DEFAULT_TITLE_CONSTANT;
        updateFrameTitle(false);
        path = "";
    }

    /**
     * Saves the current document to the existing path.
     */
    public void saveToPath() {
        if (path.isEmpty()) {
            save();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
            textArea.write(bw);
            updateFrameTitle(false);
        } catch (IOException evt) {
            JOptionPane.showMessageDialog(frame, "Saving error: " + evt.getMessage());
        }
    }

    /**
     * Opens the save dialog to save the current document as a new file.
     */
    public void save() {
        int result = fileChooser.showSaveDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(selectedFile, false))) {
                textArea.write(bw);
                defaultTitle = selectedFile.getName();
                updateFrameTitle(false);
                path = selectedFile.getAbsolutePath();
            } catch (IOException evt) {
                JOptionPane.showMessageDialog(frame, "Saving error: " + evt.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Cancelled saving");
        }
    }

    /**
     * Opens a file selected by the user.
     */
    void open() {
        int result = fileChooser.showOpenDialog(frame);
        File selectedFile;

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            StringBuilder content = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    content.append(line).append("\n");
                }

                textArea.setText(content.toString());
                defaultTitle = selectedFile.getName();
                updateFrameTitle(false);
                path = selectedFile.getAbsolutePath();
            } catch (IOException evt) {
                JOptionPane.showMessageDialog(frame, "Error opening file: " + evt.getMessage());
            }
        }

    }

    /**
     * Prints the current document.
     */
    void printFile() {
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please save or open a file first");
            return;
        }
        try {
            boolean printed = textArea.print();
            if (printed) {
                JOptionPane.showMessageDialog(frame, "Printing completed");
            } else {
                JOptionPane.showMessageDialog(frame, "Printing cancelled");
            }
        } catch (Exception evt) {
            JOptionPane.showMessageDialog(frame, "Printing error: " + evt.getMessage());
        }
    }

    /**
     * Prompts the user to save changes before exiting the application.
     *
     * @return 1 if the user chooses to cancel the exit, otherwise exits the application.
     */
    public int kill() {
        if (!frame.getTitle().contains("*")) {
            frame.dispose();
            return 0;
        }

        int choice = JOptionPane.showConfirmDialog(frame,
                "Do you want to save the file before exiting?",
                "Confirm Exit",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            save();
            frame.dispose();
            return 0;
        } else if (choice == JOptionPane.NO_OPTION) {
            frame.dispose();
            return 0;
        }

        return 1;
    }

    /**
     * Launches the application.
     */
    private static void launch() {
        SwingUtilities.invokeLater(App::new);
    }

    public static void main(String[] args) {
        launch();
    }
}
