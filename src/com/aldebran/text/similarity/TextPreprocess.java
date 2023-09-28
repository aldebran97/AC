package com.aldebran.text.similarity;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.replacePolicy.ReplaceInfo;
import com.aldebran.text.replacePolicy.WordReplaceInfo;
import com.aldebran.text.util.CheckUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本预处理
 *
 * @author aldebran
 * @since 2023-09-23
 */
public class TextPreprocess implements Serializable {

    // 替换原则
    // （1）停止词 减少干扰
    // （2）相似词，增加召回率
    // （3）简称换全称，带来更多信息，增加召回率
    // （4）额外追加父类信息，父类level不能太高，否则精确率会降低
    public Map<String, String> replaceMap = new HashMap<>();

    public ACPlus replaceAC = new ACPlus();
    public static Pattern whiteCharsPattern = Pattern.compile("\\s+");
    public static Pattern wordPattern = Pattern.compile("[a-z]+");

    public List<ReplaceInfo> replaceInfos = new ArrayList<>();

    {

        // （2）词语处理
        replaceInfos.add(new WordReplaceInfo());

        // （3）数字替换
        replaceInfos.add(new ReplaceInfo("A", Pattern.compile("\\d+(\\.\\d+)?"), Pattern.compile("\\d+(\\.\\d+)?")));

    }

    public String preprocess(String origin) {
        String resultText = replaceMapConvert(origin);
        for (ReplaceInfo replaceInfo : replaceInfos) {
            resultText = replaceInfo.replace(resultText);
        }
        // （4）去除空白字符
        resultText = whiteCharsPattern.matcher(resultText).replaceAll("");
        return resultText;
    }

    public void loadReplaceMapFromFile(String filePath) throws IOException {
        try (FileReader fileReader = new FileReader(filePath);
             BufferedReader bufferedReader = new BufferedReader(fileReader);
        ) {
            String line = null;
            List<String> words = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#")) continue;
                if (line.isEmpty()) continue;
                List<String> list = new ArrayList<>();
                for (String s : whiteCharsPattern.split(line)) {
                    if (!s.isEmpty()) {
                        list.add(line);
                    }
                }
                String word = list.get(0).toLowerCase();
                String replacement = list.size() > 1 ? list.get(1).toLowerCase() : "";
                replaceMap.put(word, replacement);
                words.add(word);
            }
            replaceAC.addWords(words);
            replaceAC.update();
        }
    }

    // (1) 转换表替换，不要处理空白字符。
    public String replaceMapConvert(String origin) {
        String result = origin.toLowerCase();
        List<AC.MatchResult> mrs = replaceAC.indexOf(origin);
        if (mrs.isEmpty()) {
            return result;
        }
        int st = 0;
        StringBuilder sb = new StringBuilder();
        for (AC.MatchResult mr : mrs) {
            if (mr.index > st) {
                sb.append(result.substring(st, mr.index));
                sb.append(replaceMap.get(mr.word));
                st = mr.index + mr.word.length();
            }
        }
        if (st < origin.length()) {
            sb.append(result.substring(st));
        }
        return sb.toString();
    }

    public BasicText textProcess(String text) {
        BasicText basicText = new BasicText();
        basicText.sourceText = text;
        basicText.resultText = preprocess(text);
        return basicText;
    }

    public List<String> textToGramUnits(BasicText basicText) {
        String[] sp = basicText.resultText.split("B");
        List<String> result = new ArrayList<>();

        for (String s : sp) {
            Matcher matcher = wordPattern.matcher(s);
            if (matcher.find()) {
                int st = matcher.start();
                int ed = matcher.end();
                if (st == 0) {
                    String word = s.substring(st, ed);
                    result.add(word);
                    for (int i = ed; i < s.length(); i++) {
                        result.add(s.substring(i, i + 1));
                    }
                } else {
                    for (int i = 0; i < st; i++) {
                        result.add(s.substring(i, i + 1));
                    }
                    String word = s.substring(st);
                    result.add(word);
                }
            } else {
                for (int i = 0; i < s.length(); i++) {
                    result.add(s.substring(i, i + 1));
                }
            }

        }
        return result;
    }

    public List<String> nGram(BasicText basicText, int n) {
        List<String> gramUnits = textToGramUnits(basicText);
        List<String> result = new ArrayList<>();
        for (int i = 0; i <= gramUnits.size() - n; i++) {
            String item = "";
            for (int j = 0; j < n; j++) {
                item += gramUnits.get(i + j);
            }
            result.add(item);
        }
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextPreprocess that = (TextPreprocess) o;
        return Objects.equals(replaceMap, that.replaceMap) && CheckUtil.acEquals(replaceAC, that.replaceAC) && Objects.equals(replaceInfos, that.replaceInfos);
    }
}
