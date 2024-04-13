package main.java.com.aldebran.text.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    public static String readFileString(File file, Charset charset) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (FileInputStream fileIn = new FileInputStream(file);
             BufferedInputStream bIn = new BufferedInputStream(fileIn);
        ) {
            int data;
            while ((data = bIn.read()) != -1) {
                byteArrayOutputStream.write(data);
            }
        }
        byteArrayOutputStream.close();
        return byteArrayOutputStream.toString(charset.toString());
    }

    public static String readFileString(File file) throws IOException {
        return readFileString(file, StandardCharsets.UTF_8);
    }

    public static File createFolder(String folderName) throws IOException {
        File file = new File(folderName);
        if (file.isFile()) {
            throw new IOException("目标位置是文件，创建目录失败：" + folderName);
        }
        if (!file.exists() && !file.mkdirs()) {
            throw new IOException("创建目录失败：" + folderName);
        }
        return file;
    }
}
