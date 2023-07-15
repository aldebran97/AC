package com.aldebran.text.similarity;

import com.aldebran.text.replacePolicy.ReplaceInfo;
import com.aldebran.text.replacePolicy.WordReplaceInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本处理
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class TextProcessor implements Serializable {

    public static List<ReplaceInfo> replaceInfos = new ArrayList<>();

    public static Pattern wordPattern = Pattern.compile("[a-z]+");

    static {

        // （1）源串小写 + 词语处理
        replaceInfos.add(new WordReplaceInfo());

        // （2）数字替换
        replaceInfos.add(new ReplaceInfo("A", Pattern.compile("\\d+(\\.\\d+)?"), Pattern.compile("\\d+(\\.\\d+)?")));

        // （3）停止词
        // 标点符号
        replaceInfos.add(new ReplaceInfo("", Pattern.compile("[·|\\u3002|\\uff1f|\\uff01|\\uff0c|\\u3001|\\uff1b|\\uff1a|\\u201c|\\u201d|\\u2018|\\u2019|\\uff08|\\uff09|\\u300a|\\u300b|\\u3008|\\u3009|\\u3010|\\u3011|\\u300e|\\u300f|\\u300c|\\u300d|\\ufe43|\\ufe44|\\u3014|\\u3015|\\u2026|\\u2014|\\uff5e|\\ufe4f|\\uffe5|\\uff0d \\uff3f|\\u002d]|\\p{Punct}"), Pattern.compile("[·|\\u3002|\\uff1f|\\uff01|\\uff0c|\\u3001|\\uff1b|\\uff1a|\\u201c|\\u201d|\\u2018|\\u2019|\\uff08|\\uff09|\\u300a|\\u300b|\\u3008|\\u3009|\\u3010|\\u3011|\\u300e|\\u300f|\\u300c|\\u300d|\\ufe43|\\ufe44|\\u3014|\\u3015|\\u2026|\\u2014|\\uff5e|\\ufe4f|\\uffe5|\\uff0d \\uff3f|\\u002d]|\\p{Punct}")));

        // 没用的
        replaceInfos.add(new ReplaceInfo("", Pattern.compile("[的了么吗\\s]"), Pattern.compile("[的了么吗\\s]")));


        // (4)相似词语
        replaceInfos.add(new ReplaceInfo("快乐", Pattern.compile("(高兴)|(兴高采烈)"), Pattern.compile("(高兴)|(兴高采烈)")));
        replaceInfos.add(new ReplaceInfo("介绍", Pattern.compile("(简介)|((基本|主要)?信息)"), Pattern.compile("(简介)|((基本|主要)?信息)")));
        // lots of
    }

    public static BasicText textProcess(String text) {
        BasicText basicText = new BasicText();
        basicText.sourceText = text;
        text = text.toLowerCase();
        for (ReplaceInfo replaceInfo : replaceInfos) {
            text = replaceInfo.replace(text);
        }
        basicText.resultText = text;
        return basicText;
    }

    public static List<String> textToGramUnits(BasicText basicText) {
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

    public static List<String> nGram(BasicText basicText, int n) {
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

}
