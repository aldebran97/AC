package com.aldebran.text.preprocess;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.util.ContinuousSerialUtil;
import com.aldebran.text.util.FileUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 词语父类追加器
 *
 * @author aldebran
 */
public class WordParentProcess implements WordProcess {

    private Map<String, List<String>> wordParentsMap = new HashMap<>();

    private ACPlus acPlus = new ACPlus();

    public WordParentProcess(File file) throws IOException {
        JSONObject root = JSON.parseObject(FileUtils.readFileString(file));
        Queue<JSONObject> queue = new LinkedList<>();
        queue.add(root);
        Map<String, String> wordParentMap = new HashMap<>();
        while (!queue.isEmpty()) {
            JSONObject v = queue.poll();
            for (Map.Entry<String, Object> entry : v.entrySet()) {
                String p = entry.getKey();
                JSONObject childJSON = (JSONObject) entry.getValue();
                if (childJSON != null) {
                    for (String c : childJSON.keySet()) {
                        wordParentMap.put(c, p);
                    }
                    queue.add(childJSON);
                }
            }

        }
        acPlus.addWords(new ArrayList<>(wordParentMap.keySet()));
        acPlus.update();
        for (Map.Entry<String, String> entry : wordParentMap.entrySet()) {
            String c = entry.getKey();
            String p = entry.getValue();
            while (p != null) {
                List<String> ps = wordParentsMap.getOrDefault(c, new ArrayList<>());
                wordParentsMap.put(c, ps);
                ps.add(p);
                p = wordParentMap.get(p);
            }
        }

    }

    public WordParentProcess() {

    }

    @Override
    public List<String> getAppendWords(String word) {
        Set<String> set = new HashSet<>();
        for (AC.MatchResult mr : acPlus.indexOf(word)) {
            for (String s : wordParentsMap.get(mr.word)) {
                set.add(s);
            }
        }
        return new ArrayList<>(set);
    }

    @Override
    public void save(File file) throws IOException, InterruptedException {
        FileUtils.createFolder(file.getAbsolutePath());
        int defaultUnitSize = 1000;
        File wordParentsMapFile = new File(file, "wordParentsMap");
        try (FileOutputStream fileOut = new FileOutputStream(wordParentsMapFile);
             BufferedOutputStream bOut = new BufferedOutputStream(fileOut);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bOut);
        ) {
            ContinuousSerialUtil.saveHashMap(objectOutputStream, (HashMap) wordParentsMap, defaultUnitSize);
        }
        File acFolder = new File(file, "acPlus");

        AC.save(acPlus, acFolder, true);

    }

    @Override
    public void load(File file) throws IOException, ClassNotFoundException, InterruptedException {
        File wordParentsMapFile = new File(file, "wordParentsMap");
        File acFolder = new File(file, "acPlus");
        try (FileInputStream fileIn = new FileInputStream(wordParentsMapFile);
             BufferedInputStream bIn = new BufferedInputStream(fileIn);
             ObjectInputStream oIn = new ObjectInputStream(bIn);
        ) {
            wordParentsMap = ContinuousSerialUtil.readHashMap(oIn);
        }
        acPlus = ACPlus.load(acFolder, true);
    }
}
