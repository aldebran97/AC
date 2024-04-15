package com.aldebran.text.preprocess;

import com.aldebran.text.Constants;
import com.aldebran.text.replacePolicy.ReplaceInfo;
import com.aldebran.text.replacePolicy.WordReplaceInfo;

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

    // 预处理原则
    // （1）停止词分割
    // （2）数值模糊化，确定最小子单元：【1】对于英文来说，最小单元是单词【2】对于中文来说，最小单元是汉字【3】对于数字来说，最小单元是数字。
    // （3）相似语义部分由WordProcess处理：属类处理 + 相似处理 + 英文词根处理

    private final Pattern whiteCharsPattern = Pattern.compile("\\s+"); // 空白识别正则
    private final Pattern wordPattern = Pattern.compile("[a-z]+"); // 单词识别正则

    private final String splitSegmentChars = "·`。.？?！!，,、\\；;：:“\"”‘'’（(）)《》〈<〉>【[】]『』「」﹃﹄〔〕…—-～﹏_￥$|=和的"; // 片段分割字符

//    private final String splitSentenceChars = "!！?？。.";

    private final Pattern splitSegmentPattern = Pattern.compile("并且|而且|成为|分为|或者|或许|不是|使得"); // 片段分割正则

    private final Pattern numberPattern = Pattern.compile("\\d+(\\.\\d+)*"); // 数值识别正则

    private List<ReplaceInfo> replaceInfos = Arrays.asList(
            new ReplaceInfo(Constants.NUMBER_REPLACEMENT, numberPattern, numberPattern),
            new WordReplaceInfo()
    );

    private String processSegment(String segment) {
//        System.out.println("processSegment before: " + segment);
        String resultText = segment;
        // （1）词语处理（2）数字替换
        for (ReplaceInfo replaceInfo : replaceInfos) {
            resultText = replaceInfo.replace(resultText);
        }
        // （3）去除空白字符
        resultText = whiteCharsPattern.matcher(resultText).replaceAll("");
//        System.out.println("processSegment after: " + resultText);
        return resultText;
    }

    // 句子分割
    private List<String> splitByChars(String chars, String text) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            String c = text.substring(i, i + 1);
            if (chars.contains(c)) {
                if (!c.equals(".")) {
                    if (current.length() != 0) {
                        result.add(current.toString());
                        current = new StringBuilder();
                    }
                } else {
                    int st = Math.max(i - 1, 0);
                    int ed = Math.min(i + 1, text.length());
                    String s = text.substring(st, ed);
                    if (!numberPattern.matcher(s).find()) {
                        if (current.length() != 0) {
                            result.add(current.toString());
                            current = new StringBuilder();
                        }
                    } else {
                        current.append(c);
                    }
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() != 0) {
            result.add(current.toString());
        }
        return result;
    }

    private List<String> spiltSegments(String text) {
        List<String> result = new ArrayList<>();
        for (String segment : splitByChars(splitSegmentChars, text)) {
            result.add(segment);
        }
        return result;
    }

    private static String toText(List<List<String>> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> strings : list) {
            stringBuilder.append(String.join("", strings));
        }
        return stringBuilder.toString();
    }


//    public List<List<String>> preprocessAsSentences(String text) {
//        List<List<String>> result = new ArrayList<>();
//        for (String sentence : splitByChars(splitSentenceChars, text)) {
//            sentence = processSentence(sentence);
//            if (sentence.length() > 0) {
//                List<String> units = sentenceToUnits(sentence);
//                result.add(units);
//            }
//
//        }
//        return result;
//    }

    public List<List<String>> preprocess(String text) {
        List<List<String>> result = new ArrayList<>();
        for (String segment1 : splitSegmentPattern.split(text)) {
            for (String segment2 : spiltSegments(segment1)) {
                segment2 = processSegment(segment2);
                if (segment2.length() > 0) {
                    List<String> units = sentenceToUnits(segment2);
                    result.add(units);
                }
            }
        }
        return result;
    }

    public String preprocessToText(String text) {
        return toText(preprocess(text));
    }


    // 预处理后的文本转换成单元（中文一个字一个单元，英文一个词一个单元）
    private List<String> sentenceToUnits(String preprocessText) {
        String[] sp = preprocessText.split("B");
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
                    String word = s.substring(st, ed);
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

}
