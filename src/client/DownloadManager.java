package client;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DownloadManager {
    private static String currentRoot;

    public static void createDownloadFolder() {
        currentRoot = "Inbox/" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        new File(currentRoot).mkdirs();
        System.out.println("下载目录: " + currentRoot);
    }

    public static String getRoot() {
        return currentRoot;
    }
}