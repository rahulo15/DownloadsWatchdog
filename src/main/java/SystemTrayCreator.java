import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SystemTrayCreator {
    private static final Logger LOGGER = Logger.getLogger(SystemTrayCreator.class.getName());
    private final AtomicBoolean isPaused;
    private final List<Path> foldersToWatch;
    private final WatchDogService watchDogService;

    public SystemTrayCreator(AtomicBoolean isPaused, List<Path> foldersToWatch) {
        this.isPaused = isPaused;
        this.foldersToWatch = foldersToWatch;
        this.watchDogService = new WatchDogService();
    }

    public void SystemTrayLoader() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray not supported!");
            return;
        }

        final SystemTray tray = SystemTray.getSystemTray();

        Image image = this.GenerateImage();

        PopupMenu popup = new PopupMenu();

        MenuItem exitItem = new MenuItem("Exit");

        exitItem.addActionListener(e -> {
            LOGGER.info("Service has been stopped.");
            System.exit(0);
        });

        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "DownloadsWatchdog", popup);
        trayIcon.setImageAutoSize(true);

        class CleanupHandler implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean newState = !isPaused.get();
                isPaused.set(newState);
                LOGGER.info(newState ? "Service is paused." : "Service has been resumed.");
                trayIcon.displayMessage("DownloadsWatchDog!",
                        newState ? "Service is paused." : "Service has been resumed.",
                        TrayIcon.MessageType.INFO);
                if(!isPaused.get()) {
                    System.out.println("--- 🧹 Starting Cleanup ---");
                    LOGGER.info("--- 🧹 Starting Cleanup ---");
                    for (Path folder : foldersToWatch) {
                        if (Files.exists(folder)) {
                            System.out.println("Processing: " + folder);
                            LOGGER.info("Processing: " + folder);
                            watchDogService.performInitialCleanup(folder);
                        } else {
                            System.out.println("⚠️ Warning: Folder not found: " + folder);
                            LOGGER.warning("⚠️ Warning: Folder not found: " + folder);
                        }
                    }
                    LOGGER.info("--- ✅ Cleanup Complete ---");
                }
            }
        };

        trayIcon.addActionListener(new CleanupHandler());

        try {
            tray.add(trayIcon);
            LOGGER.info("Tray icon added.");
        } catch (AWTException e) {
            LOGGER.warning("TrayIcon could not be added.");
        }
    }

    private java.awt.Image GenerateImage() {
        int width = 16, height = 16;
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw the white of the eye (an oval)
        g.setColor(Color.WHITE);
        g.fillOval(1, 3, 14, 10);

        // 2. Draw the outer outline (dark gray)
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new java.awt.BasicStroke(1.5f));
        g.drawOval(1, 3, 14, 10);

        // 3. Draw the iris/pupil (blue circle in the middle)
        g.setColor(new Color(0, 120, 215)); // A nice blue
        g.fillOval(5, 5, 6, 6);

        // 4. Optional: A tiny white reflection dot to make it look glossy
        g.setColor(Color.WHITE);
        g.fillOval(6, 6, 2, 2);

        g.dispose();
        return img;
    }
}