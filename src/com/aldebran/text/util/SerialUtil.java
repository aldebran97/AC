package com.aldebran.text.util;

import com.aldebran.text.similarity.TextSimilaritySearch;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 序列化工具
 *
 * @author aldebran
 * @since 2023-09-25 20:46:00
 */
public class SerialUtil {

    public static File save(List<Object> objs, File outFile) throws IOException {
        try (FileOutputStream fO = new FileOutputStream(outFile);
             BufferedOutputStream bO = new BufferedOutputStream(fO);
             ObjectOutputStream oO = new ObjectOutputStream(bO);
        ) {
            for (Object obj : objs) {
                oO.writeObject(obj);
            }
        }
        return outFile;
    }

    public static File saveOne(Object obj, File outFile) throws IOException {
        return save(Arrays.asList(obj), outFile);
    }

    public static List<Object> load(File inFile) throws Exception {
        try (
                FileInputStream fileInputStream = new FileInputStream(inFile);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        ) {
            List list = new ArrayList();
            Object obj = null;
            while ((obj = objectInputStream.readObject()) != null) {
                list.add(obj);
            }
            return list;
        }
    }

    public static Object loadOne(File inFile) throws Exception {
        return load(inFile).get(0);
    }
}
