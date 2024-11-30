package com.example;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    private Robot robot;

    @BeforeAll
    public void setUp() {
        // Removed headless property
        System.out.println("Headless mode: " + System.getProperty("java.awt.headless"));
        FailOnThreadViolationRepaintManager.install();
        robot = BasicRobot.robotWithNewAwtHierarchy();
    }

    @AfterAll
    public void tearDown() {
        if (robot != null) {
            robot.cleanUp();
        }
    }

    @Test
    void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    void testAppConstructor() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> app[0] = new App());
        assertNotNull(app[0].frame);
        assertNotNull(app[0].textArea);
        assertTrue(app[0].frame.isVisible());
    }

    @Test
    void testInitializeFrame() throws Exception {
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

    @Test
    void testInitializeTextArea() throws Exception {
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

    @Test
    void testInitializeMenuBar() throws Exception {
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
    void testKeyboardShortcuts() throws Exception {
        final App[] app = new App[1];

        // Initialize the app on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].textArea.setText("Test content");
            app[0].textArea.requestFocusInWindow();
        });

        // Simulate Ctrl+S key press correctly
        robot.pressKey(KeyEvent.VK_CONTROL);
        robot.pressAndReleaseKey(KeyEvent.VK_S);
        robot.releaseKey(KeyEvent.VK_CONTROL);

        // Verify the frame title and content
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(app[0].frame.getTitle().contains("notepad"), "Frame title should contain 'notepad'.");
            assertNotNull(app[0].textArea.getText(), "Text area should not be null.");
            assertEquals("Test content", app[0].textArea.getText(), "Text area content should remain unchanged.");
        });
    }

    @Test
    void testWindowClosing() throws Exception {
        final App[] app = new App[1];
        FrameFixture window;

        // Initialize the app on the EDT and create FrameFixture
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setVisible(true);
        });

        // Create FrameFixture for AssertJ Swing
        window = new FrameFixture(robot, app[0].frame);
        window.show(); // Ensure the frame is visible

        // Add some text to trigger the "unsaved changes" state
        window.textBox().setText("Some unsaved text");
        robot.waitForIdle();

        // Trigger window closing event
        window.close();
        robot.waitForIdle();

        // Wait for dialog using WindowFinder with specific timeout
        DialogFixture dialog = WindowFinder.findDialog(JDialog.class)
                .withTimeout(10000)
                .using(robot);

        // Find and click the Cancel button by text
        dialog.button(new GenericTypeMatcher<>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return "Cancel".equals(button.getText());
            }
        }).click();

        // Verify that the frame is still visible and hasn't closed
        assertTrue(app[0].frame.isVisible(), "Frame should remain visible after canceling window closing.");
        assertEquals(WindowConstants.DO_NOTHING_ON_CLOSE, app[0].frame.getDefaultCloseOperation(),
                "Default close operation should be DO_NOTHING_ON_CLOSE.");

        // Clean up
        window.cleanUp();
    }

    @Test
    void testKillWithoutUnsavedChanges() throws Exception {
        final App[] app = new App[1];

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setTitle("notepad - Untitled.txt");
        });

        // Simulate kill without unsaved changes
        final int[] result = new int[1];
        SwingUtilities.invokeAndWait(() -> result[0] = app[0].kill());

        // Verify the result
        assertEquals(0, result[0]);
        SwingUtilities.invokeAndWait(() -> assertFalse(app[0].frame.isVisible()));
    }
}
