package com.aldebran.text.similarity;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextLibManagement {
    private Map<String, TextSimilaritySearch> nameLibMap = new HashMap<>();

    private File libsFolder;

    public TextLibManagement(File libsFolder) {
        this.libsFolder = libsFolder;
    }

    public void releaseLibFromMemory(String libName) {
        nameLibMap.remove(libName);
    }

    public void releaseLibsFromMemory(Collection<String> libNames) {
        for (String libName : libNames) {
            releaseLibFromMemory(libName);
        }
    }

    public void loadLibFromDisk(String libName) throws Exception {
        TextSimilaritySearch lib = TextSimilaritySearch.load(new File(libsFolder, libName));
        nameLibMap.put(libName, lib);
    }

    public void loadLibsFromDisk(Collection<String> libNames) throws Exception {
        for (String libName : libNames) {
            loadLibFromDisk(libName);
        }
    }


    public void saveLib(String libName) throws IOException {
        TextSimilaritySearch lib = nameLibMap.get(libName);
        TextSimilaritySearch.save(lib, new File(libsFolder, libName));
    }

    public void saveLibs(Collection<String> libNames) throws IOException {
        for (String libName : libNames) {
            saveLib(libName);
        }
    }

    public List<SimilaritySearchResult> similaritySearch(
            String libName, String text, int topK) {
        return nameLibMap.get(libName).similaritySearch(text, topK);
    }

    public Text getTextById(String libName, String id) {
        return nameLibMap.get(libName).queryById(id);
    }
}
