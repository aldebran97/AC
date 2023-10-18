package com.aldebran.text.similarity;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 相似库管理工具
 *
 * @author aldebran
 * @since 2023-06-18
 */
public class TextLibManagement {
    private Map<String, TextSimilaritySearch> nameLibMap = new ConcurrentHashMap<>();

    private File libsFolder;

    public TextLibManagement(File libsFolder) {
        if (!libsFolder.isDirectory()) {
            assert libsFolder.mkdirs();
        }
        this.libsFolder = libsFolder;
    }

    public Set<String> getLibNames() {
        return nameLibMap.keySet();
    }

    // 从内存中释放相似库
    public void releaseLibFromMemory(String libName) {
        nameLibMap.remove(libName);
    }

    public void releaseLibsFromMemory(Collection<String> libNames) {
        for (String libName : libNames) {
            releaseLibFromMemory(libName);
        }
    }

    // 加载相似库
    public void loadLibFromDisk(String libName) throws Exception {
        TextSimilaritySearch lib = TextSimilaritySearch.load(new File(libsFolder, libName), true);
        nameLibMap.put(libName, lib);
    }

    public void loadLibsFromDisk(Collection<String> libNames) throws Exception {
        for (String libName : libNames) {
            loadLibFromDisk(libName);
        }
    }

    // 获得磁盘中存在的相似库
    public Set<String> getDiskLibNames() {
        return Arrays.stream(libsFolder.listFiles())
                .filter(it -> new File(it, "idTextMap").isFile())
                .map(it -> it.getName()).collect(Collectors.toSet());
    }


    // 持久化相似库
    public void saveLib(String libName) throws IOException, InterruptedException {
        TextSimilaritySearch lib = nameLibMap.get(libName);
        TextSimilaritySearch.save(lib, new File(libsFolder, libName), true);
    }

    public void saveLibs(Collection<String> libNames) throws IOException, InterruptedException {
        for (String libName : libNames) {
            saveLib(libName);
        }
    }

    // 相似检索
    public List<SimilaritySearchResult> similaritySearch(
            String libName, String text, int topK) {
        return nameLibMap.get(libName).similaritySearch(text, topK);
    }

    // 相似检索，支持跨库检索
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

    // 根据ID查询
    public FullText getTextById(String libName, String id) {
        return nameLibMap.get(libName).queryById(id);
    }

    // 返回库是否加载
    public boolean isLibLoaded(String libName) {
        return nameLibMap.containsKey(libName);
    }

    public Map<String, Boolean> isLibsLoaded(Collection<String> libNames) {
        Map<String, Boolean> result = new HashMap<>();
        for (String libName : libNames) {
            result.put(libName, isLibLoaded(libName));
        }
        return result;
    }

    // 返回已加载的库名称
    public Set<String> getLoadedLibNames() {
        return nameLibMap.keySet();
    }

    // 将库加入管理中
    public void manageLib(TextSimilaritySearch lib) {
        nameLibMap.put(lib.libName, lib);
    }

    public void manageLibs(Collection<TextSimilaritySearch> libs) {
        for (TextSimilaritySearch lib : libs) {
            manageLib(lib);
        }
    }

    // 获取库
    public TextSimilaritySearch getLib(String libName) {
        return nameLibMap.get(libName);
    }

}
