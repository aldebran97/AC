package com.aldebran.text.preprocess;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 英语词根处理
 *
 * @author aldebran
 */
public class EnglishRootProcess implements WordProcess {

    private ACPlus prefixACPlus = new ACPlus();

    private ACPlus postfixACPlus = new ACPlus();

    private ACPlus containsACPlus = new ACPlus();

    private final Pattern whiteCharsPattern = Pattern.compile("\\s+"); // 空白识别正则

    public EnglishRootProcess(File file) throws IOException {
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = whiteCharsPattern.matcher(line).replaceAll("");
                if (line.isEmpty()) continue;
                if (line.startsWith("###")) {
                    postfixACPlus.addWords(Arrays.asList(line.replace("#", "")));
                } else if (line.endsWith("###")) {
                    prefixACPlus.addWords(Arrays.asList(line.replace("#", "")));
                } else if (!line.startsWith("#")) {
                    containsACPlus.addWords(Arrays.asList(line));
                }
            }
        }
        postfixACPlus.update();
        prefixACPlus.update();
        containsACPlus.update();
    }

    @Override
    public List<String> getAppendWords(String word) {
        Set<String> set = new HashSet<>();
        for (AC.MatchResult mr : postfixACPlus.indexOf(word)) {
            if (mr.index + mr.word.length() == word.length()) {
                set.add(mr.word);
            }
        }
        for (AC.MatchResult mr : prefixACPlus.indexOf(word)) {
            if (mr.index == 0) {
                set.add(mr.word);
            }
        }

        for (AC.MatchResult mr : containsACPlus.indexOf(word)) {
            set.add(mr.word);
        }
        return new ArrayList<>(set);
    }

    @Override
    public void save(File file) throws IOException, InterruptedException {
        
    }

    @Override
    public void load(File file) throws IOException, ClassNotFoundException, InterruptedException {

    }

}
