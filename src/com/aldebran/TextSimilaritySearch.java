package com.aldebran;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 相似检索，由于是内存中的，所以只适合少量数据
 *
 * @author aldebran
 * @since 2023-06-18
 */
public class TextSimilaritySearch implements Serializable {

    public static final long serialVersionUID = 1L;

    public static List<ReplaceInfo> replaceInfos = new ArrayList<>();

    public static Pattern wordPattern = Pattern.compile("[a-z]+");

    static {

        // （1）源串小写 + 词语处理
        replaceInfos.add(new WordReplaceInfo());

        // （2）数字替换
        replaceInfos.add(
                new ReplaceInfo("A",
                        Pattern.compile("\\d+(\\.\\d+)?"),
                        Pattern.compile("\\d+(\\.\\d+)?")));

        // （3）停止词
        // 标点符号
        replaceInfos.add(
                new ReplaceInfo("",
                        Pattern.compile("[·|\\u3002|\\uff1f|\\uff01|\\uff0c|\\u3001|\\uff1b|\\uff1a|\\u201c|\\u201d|\\u2018|\\u2019|\\uff08|\\uff09|\\u300a|\\u300b|\\u3008|\\u3009|\\u3010|\\u3011|\\u300e|\\u300f|\\u300c|\\u300d|\\ufe43|\\ufe44|\\u3014|\\u3015|\\u2026|\\u2014|\\uff5e|\\ufe4f|\\uffe5|\\uff0d \\uff3f|\\u002d]|\\p{Punct}"),
                        Pattern.compile("[·|\\u3002|\\uff1f|\\uff01|\\uff0c|\\u3001|\\uff1b|\\uff1a|\\u201c|\\u201d|\\u2018|\\u2019|\\uff08|\\uff09|\\u300a|\\u300b|\\u3008|\\u3009|\\u3010|\\u3011|\\u300e|\\u300f|\\u300c|\\u300d|\\ufe43|\\ufe44|\\u3014|\\u3015|\\u2026|\\u2014|\\uff5e|\\ufe4f|\\uffe5|\\uff0d \\uff3f|\\u002d]|\\p{Punct}")));

        // 没用的
        replaceInfos.add(
                new ReplaceInfo("",
                        Pattern.compile("[的了么吗]"),
                        Pattern.compile("[的了么吗]")));


        // (4)相似词语
        replaceInfos.add(
                new ReplaceInfo("快乐",
                        Pattern.compile("(高兴)|(兴高采烈)"),
                        Pattern.compile("(高兴)|(兴高采烈)")));
        // lots of
    }

    private AC ac = new AC();

    private Map<String, Set<String>> gramTextIdsMap = new HashMap<>();

    private Map<String, Text> idTextMap = new HashMap<>();

    public int hitCount1_2; // 不同问题的(n,0.6)不同，检索和查重是不同的

    public int n = 2;

    public String libName;

    public TextSimilaritySearch(String libName, int hitCount1_2, int n) {
        this.libName = libName;
        this.n = n;
        this.hitCount1_2 = hitCount1_2;
    }

    public void addText(String text, String title, String id) {
        Text textObj = textProcess(text);
        textObj.id = id;
        textObj.title = title;
        List<String> grams = nGram(textObj, n);
        for (String gram : grams) {
            Set<String> set = gramTextIdsMap.get(gram);
            if (set == null) {
                set = new HashSet<>();
                gramTextIdsMap.put(gram, set);
            }
            set.add(id);
        }
        ac.addWords(grams);

        idTextMap.put(textObj.id, textObj);
    }

    public void update() {
        ac.update();
    }


    public Text queryById(String id) {
        return idTextMap.get(id);
    }


    public static List<String> textToGramUnits(Text textObj) {
        String[] sp = textObj.result.split("B");
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

    public static List<String> nGram(Text textObj, int n) {
        List<String> gramUnits = textToGramUnits(textObj);
        List<String> result = new ArrayList<>();
        for (int i = 0; i < gramUnits.size() - n; i++) {
            String item = "";
            for (int j = 0; j < n; j++) {
                item += gramUnits.get(i + j);
            }
            result.add(item);
        }
        return result;
    }

    public static Text textProcess(String text) {
        Text textObj = new Text();
        textObj.source = text;
        text = text.toLowerCase();
        for (ReplaceInfo replaceInfo : replaceInfos) {
            text = replaceInfo.replace(text);
        }
        textObj.result = text;
        return textObj;
    }

    public List<SimilaritySearchResult> similaritySearch(String text, int topK) {

        List<AC.MatchResult> mrs = ac.indexOf(text);
        // 不记录重复计数
        List<SimilaritySearchResult> result = new ArrayList<>();

        Map<String, Integer> textIdCountMap = new HashMap<>();
        for (AC.MatchResult mr : mrs) {
            String gram = mr.word;
            Set<String> textIds = gramTextIdsMap.get(gram);
            for (String textId : textIds) {
                Integer count = textIdCountMap.get(textId);
                if (count == null) {
                    count = 0;
                }
                count++;
                textIdCountMap.put(textId, count);
            }
        }


        TreeSet<Map.Entry<String, Integer>> treeSet = new TreeSet(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o1.getValue() < o2.getValue() ? 1 : 0;
            }

        });

        treeSet.addAll(textIdCountMap.entrySet());

        for (Map.Entry<String, Integer> entry : treeSet) {
            if (result.size() > topK) break;
            Text textObj = queryById(entry.getKey());
            int count = entry.getValue();
            SimilaritySearchResult similaritySearchResult = new SimilaritySearchResult();
            similaritySearchResult.id = textObj.id;
            similaritySearchResult.text = textObj.source;
            similaritySearchResult.title = textObj.title;

            // TODO calc score
            similaritySearchResult.score = count;
            result.add(similaritySearchResult);
        }


        return result;
    }
}
