package com.aldebran.text.similarity;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * 相似库管理工具
 *
 * @author aldebran
 * @since 2023-06-18
 */
public class TextLibManagement {
    private Map<String, TextSimilaritySearch> nameLibMap = new HashMap<>();

    private File libsFolder;

    public TextLibManagement(File libsFolder) {
        this.libsFolder = libsFolder;
    }

    public Set<String> getLibNames() {
        return nameLibMap.keySet();
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

    public List<SimilaritySearchResult> similaritySearch(
            Collection<String> libNames, String text, int topK) {
        PriorityQueue<SimilaritySearchResult> priorityQueue = new PriorityQueue<>(new Comparator<SimilaritySearchResult>() {
            @Override
            public int compare(SimilaritySearchResult o1, SimilaritySearchResult o2) {
                return Double.compare(o1.score, o2.score);
            }
        });
        for (String libName : libNames) {
            List<SimilaritySearchResult> similaritySearchResults = nameLibMap.get(libName).similaritySearch(text, topK);
            for (SimilaritySearchResult similaritySearchResult : similaritySearchResults) {
                if (priorityQueue.size() < topK) {
                    priorityQueue.add(similaritySearchResult);
                } else if (priorityQueue.peek().score < similaritySearchResult.score) {
                    priorityQueue.poll();
                    priorityQueue.add(similaritySearchResult);
                }
            }
        }

        List<SimilaritySearchResult> result = new LinkedList<>();
        while (!priorityQueue.isEmpty()) {
            result.add(0, priorityQueue.poll());
        }

        return result;
    }


    public Text getTextById(String libName, String id) {
        return nameLibMap.get(libName).queryById(id);
    }

    public boolean isLibLoaded(String libName) {
        return nameLibMap.containsKey(libName);
    }
}
