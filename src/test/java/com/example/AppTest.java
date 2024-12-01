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
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the App class.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AppTest {

    private Robot robot; // Robot for simulating user actions

    /**
     * Set up the test environment before running any tests.
     */
    @BeforeAll
    public void setUp() {
        // Removed headless property
        System.out.println("Headless mode: " + System.getProperty("java.awt.headless"));
        FailOnThreadViolationRepaintManager.install();
        robot = BasicRobot.robotWithNewAwtHierarchy();
    }

    /**
     * Clean up after all tests have run.
     */
    @AfterAll
    public void tearDown() {
        if (robot != null) {
            robot.cleanUp();
        }
    }

    /**
     * The only original test
     */
    @Test
    void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    /**
     * Test the App constructor.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testAppConstructor() throws Exception {
        final App[] app = new App[1];
        SwingUtilities.invokeAndWait(() -> app[0] = new App());
        assertNotNull(app[0].frame);
        assertNotNull(app[0].textArea);
        assertTrue(app[0].frame.isVisible());
    }

    /**
     * Test the initializeFrame method.
     * @throws Exception if an error occurs during the test.
     */
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

    /**
     * Test the initializeTextArea method.
     * @throws Exception if an error occurs during the test.
     */
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

    /**
     * Test the initializeMenuBar method.
     * @throws Exception if an error occurs during the test.
     */
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

    /**
     * Test the windowClosing method.
     * @throws Exception if an error occurs during the test.
     */
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

    /**
     * Test the kill method with unsaved changes.
     * @throws Exception if an error occurs during the test.
     */
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

    /**
     * Test the kill method with unsaved changes.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testKillWithUnsavedChanges() throws Exception {
        final App[] app = new App[1];
        FrameFixture window;

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setVisible(true);
        });

        // Create FrameFixture for AssertJ Swing
        window = new FrameFixture(robot, app[0].frame);
        window.show();

        // Add text to trigger unsaved changes state
        window.textBox().setText("Some unsaved text");
        robot.waitForIdle();

        // Verify title contains asterisk indicating unsaved changes
        assertTrue(app[0].frame.getTitle().contains("*"), "Title should indicate unsaved changes");

        // Trigger window closing
        window.close();
        robot.waitForIdle();

        // Find and handle the confirmation dialog
        DialogFixture dialog = WindowFinder.findDialog(JDialog.class)
                .withTimeout(5000)
                .using(robot);

        // Click "No" on the save confirmation dialog
        dialog.button(new GenericTypeMatcher<>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return "No".equals(button.getText());
            }
        }).click();

        // Verify the frame is closed
        assertFalse(app[0].frame.isVisible(), "Frame should be closed after clicking No");

        // Clean up
        window.cleanUp();
    }

    /**
     * Test the documentListener method.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testDocumentListener() throws Exception {
        final App[] app = new App[1];
        FrameFixture window;

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setVisible(true);
        });

        // Create FrameFixture for AssertJ Swing
        window = new FrameFixture(robot, app[0].frame);
        window.show();

        // Type text into the text area
        window.textBox().enterText("New text");
        robot.waitForIdle();

        // Verify the title was updated with asterisk
        assertEquals("notepad - Untitled.txt *", app[0].frame.getTitle());

        // Clean up
        window.cleanUp();
    }

    /**
     * Test the handleMouseWheelEventZoomIn method.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testHandleMouseWheelEventZoomIn() throws Exception {
        final App[] app = new App[1];
        FrameFixture window;

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setVisible(true);
        });

        // Create FrameFixture for AssertJ Swing
        window = new FrameFixture(robot, app[0].frame);
        window.show();

        // Get initial font size
        int initialFontSize = app[0].textArea.getFont().getSize();

        // Create and dispatch mouse wheel event
        SwingUtilities.invokeAndWait(() -> {
            MouseWheelEvent event = new MouseWheelEvent(
                    app[0].textArea,
                    MouseEvent.MOUSE_WHEEL,
                    System.currentTimeMillis(),
                    MouseEvent.ALT_DOWN_MASK,
                    0, 0, 0, false,
                    MouseWheelEvent.WHEEL_UNIT_SCROLL,
                    1, -1
            );
            app[0].handleMouseWheelEvent(event);
        });

        robot.waitForIdle();

        // Verify font size increased
        assertEquals(initialFontSize + 1, app[0].textArea.getFont().getSize());

        // Clean up
        window.cleanUp();
    }

    /**
     * Test the handleMouseWheelEventZoomOut method.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testSaveToPath() throws Exception {
        final App[] app = new App[1];
        FrameFixture window;

        // Create temporary file
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setVisible(true);
            app[0].path = tempFile.getAbsolutePath();
        });

        // Create FrameFixture for AssertJ Swing
        window = new FrameFixture(robot, app[0].frame);
        window.show();

        // Enter text into the text area
        window.textBox().setText("Sample text");
        robot.waitForIdle();

        // Save the file
        SwingUtilities.invokeAndWait(() -> app[0].saveToPath());
        robot.waitForIdle();

        // Verify file content
        String content = Files.readString(tempFile.toPath());
        assertEquals("Sample text", content);

        // Clean up
        window.cleanUp();
    }

    /**
     * Test the openFile method.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testOpenFile() throws Exception {
        final App[] app = new App[1];

        // Create temporary file with content
        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), "Test content".getBytes());

        // Mock JFileChooser to return the tempFile when showOpenDialog is called
        JFileChooser mockFileChooser = mock(JFileChooser.class);
        when(mockFileChooser.showOpenDialog(any())).thenReturn(JFileChooser.APPROVE_OPTION);
        when(mockFileChooser.getSelectedFile()).thenReturn(tempFile);

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].fileChooser = mockFileChooser; // Inject the mock
            app[0].frame.setVisible(true);
        });

        // Trigger the open action
        SwingUtilities.invokeAndWait(() -> app[0].miOpen.doClick());
        robot.waitForIdle();

        // Verify the file content and metadata
        assertEquals("Test content\n", app[0].textArea.getText());
        assertEquals(tempFile.getName(), app[0].defaultTitle);
        assertEquals("notepad - " + tempFile.getName(), app[0].frame.getTitle());
        assertEquals(tempFile.getAbsolutePath(), app[0].path);

        // Clean up
        if (app[0].frame.isVisible()) {
            SwingUtilities.invokeAndWait(() -> app[0].frame.dispose());
        }
    }

    /**
     * Test the updateFrameTitle method.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testUpdateFrameTitle() throws Exception {
        final App[] app = new App[1];
        FrameFixture window;

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setVisible(true);
        });

        // Create FrameFixture for AssertJ Swing
        window = new FrameFixture(robot, app[0].frame);
        window.show();

        // Test with modified flag true
        SwingUtilities.invokeAndWait(() -> app[0].updateFrameTitle(true));
        robot.waitForIdle();
        assertEquals("notepad - Untitled.txt *", app[0].frame.getTitle());

        // Test with modified flag false
        SwingUtilities.invokeAndWait(() -> app[0].updateFrameTitle(false));
        robot.waitForIdle();
        assertEquals("notepad - Untitled.txt", app[0].frame.getTitle());

        // Clean up
        window.cleanUp();
    }

    /**
     * Test the resetEditor method.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testResetEditor() throws Exception {
        final App[] app = new App[1];
        FrameFixture window;

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setVisible(true);

            // Set initial state
            app[0].textArea.setText("Some text");
            app[0].defaultTitle = "ChangedTitle.txt";
            app[0].path = "/path/to/file.txt";
        });

        // Create FrameFixture for AssertJ Swing
        window = new FrameFixture(robot, app[0].frame);
        window.show();

        // Reset the editor
        SwingUtilities.invokeAndWait(() -> app[0].resetEditor());
        robot.waitForIdle();

        // Verify the reset state
        assertEquals("", app[0].textArea.getText());
        assertEquals("Untitled.txt", app[0].defaultTitle);
        assertEquals("notepad - Untitled.txt", app[0].frame.getTitle());
        assertEquals("", app[0].path);

        // Clean up
        window.cleanUp();
    }

    /**
     * Test the handleKeyTyped method.
     * @throws Exception if an error occurs during the test.
     */
    @Test
    void testTypingInTextArea() throws Exception {
        final App[] app = new App[1];
        FrameFixture window;

        // Initialize the app on the EDT
        SwingUtilities.invokeAndWait(() -> {
            app[0] = new App();
            app[0].frame.setVisible(true);
        });

        // Create FrameFixture for AssertJ Swing
        window = new FrameFixture(robot, app[0].frame);
        window.show();

        // Type text into the text area
        window.textBox().enterText("Hello World");
        robot.waitForIdle();

        // Verify the text content
        assertEquals("Hello World", app[0].textArea.getText());

        // Clean up
        window.cleanUp();
    }
}
