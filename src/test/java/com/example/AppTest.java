package com.example;

import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.swing.*;

import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class AppTest extends AssertJSwingJUnitTestCase {
    /**
     * Sets up headless mode for testing.
     */
    @BeforeClass
    public static void setUpHeadless() {
//        System.setProperty("java.awt.headless", "true");
        System.out.println("Headless mode: " + System.getProperty("java.awt.headless"));
    }

    /**
     * Initializes the Swing environment for testing.
     */
    @Override
    protected void onSetUp() {
        // Initialize GUI testing
        FailOnThreadViolationRepaintManager.install();
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    /**
     * Tests the constructor of the App class.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    public void testAppConstructor() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> app[0] = new App());
        assertNotNull(app[0].frame);
        assertNotNull(app[0].textArea);
        assertTrue(app[0].frame.isVisible());
    }

    /**
     * Tests the initialization of the frame.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    public void testInitializeFrame() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> app[0] = new App());
        JFrame frame = app[0].frame;

        assertEquals("notepad - Untitled.txt", frame.getTitle());
        assertEquals(WindowConstants.DO_NOTHING_ON_CLOSE, frame.getDefaultCloseOperation());
        assertEquals(500, frame.getWidth());
        assertEquals(500, frame.getHeight());
        assertTrue(frame.isResizable());
        assertTrue(frame.isVisible());
    }

    /**
     * Tests the initialization of the text area.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    public void testInitializeTextArea() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> app[0] = new App());

        JTextArea textArea = app[0].textArea;
        assertTrue(textArea.getLineWrap());
        assertTrue(textArea.getWrapStyleWord());

        // Check that textArea is added to a JScrollPane
        JScrollPane scrollPane = (JScrollPane) textArea.getParent().getParent();
        assertNotNull(scrollPane);
        assertEquals(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, scrollPane.getVerticalScrollBarPolicy());
    }

    /**
     * Tests the initialization of the menu bar.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    public void testInitializeMenuBar() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> app[0] = new App());

        JMenuBar menuBar = app[0].frame.getJMenuBar();
        assertNotNull(menuBar);
        assertEquals(2, menuBar.getMenuCount());

        JMenu fileMenu = menuBar.getMenu(0);
        assertEquals("File", fileMenu.getText());

        JMenu editMenu = menuBar.getMenu(1);
        assertEquals("Edit", editMenu.getText());
    }

    @Test
    public void testKeyboardShortcuts() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].textArea.setText("Test content");
            app[0].textArea.requestFocusInWindow();
        });

        // Simulate Ctrl+S
        robot().pressAndReleaseKeys(KeyEvent.VK_CONTROL, KeyEvent.VK_S);

        // Verify the frame title and content
        assertTrue(app[0].frame.getTitle().contains("notepad"));
        assertNotNull(app[0].textArea.getText());
        assertEquals("Test contents", app[0].textArea.getText());
    }

    @Test
    public void testWindowClosing() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].textArea.setText("Test content");
        });

        // Create and trigger window closing event
        WindowEvent windowClosing = new WindowEvent(app[0].frame, WindowEvent.WINDOW_CLOSING);

        // Use robot to handle the dialog that appears
        SwingUtilities.invokeLater(() -> {
            for (WindowListener listener : app[0].frame.getWindowListeners()) {
                listener.windowClosing(windowClosing);
            }
        });

        robot().pressKey(KeyEvent.VK_ESCAPE);
        robot().releaseKey(KeyEvent.VK_ESCAPE);

        assertTrue(app[0].frame.isVisible());
        assertEquals(WindowConstants.DO_NOTHING_ON_CLOSE, app[0].frame.getDefaultCloseOperation());
    }

    @Test
    public void testKillWithoutUnsavedChanges() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setTitle("notepad - Untitled.txt");
        });

        // Simulate kill without unsaved changes
        int result = app[0].kill();

        // Verify the result
        assertEquals(0, result);
        assertFalse(app[0].frame.isVisible());
    }
}
