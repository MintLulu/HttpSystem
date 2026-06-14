package client;

import java.io.File;
import java.io.FileOutputStream;

public class FileUtil {
    public static void saveFile(String relativePath, byte[] data) throws Exception {
        File file = new File(DownloadManager.getRoot() + relativePath);
        File parent = file.getParentFile();
        if(parent != null){
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
        System.out.println("已保存: " + file.getPath());
    }
}