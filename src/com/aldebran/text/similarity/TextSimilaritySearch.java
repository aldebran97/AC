package com.aldebran.text.similarity;

import com.aldebran.text.ac.AC;
import com.aldebran.text.replacePolicy.ReplaceInfo;
import com.aldebran.text.replacePolicy.WordReplaceInfo;

import java.io.*;
import java.util.*;
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

    public AC ac = new AC();

    private Map<String, Set<String>> gramTextIdsMap = new HashMap<>();

    private Map<String, Text> idTextMap = new HashMap<>();

    // 不同问题的(criticalHitCount * avg_idf , criticalScore)不同，检索和查重是不同的
    public int criticalHitCount = 3;

    public double criticalScore = 0.5;

    public int n = 2;

    public double decayRate = 0.01;

    public double growthRate = 3;

    public double avgIdf = 0;

    public String libName;

    public double a;

    public double b;

    public TextSimilaritySearch(String libName,
                                int criticalHitCount,
                                double criticalScore,
                                int n,
                                double decayRate,
                                double growthRate) {
        this.libName = libName;
        this.n = n;
        regenerateArgs(criticalHitCount, criticalScore, decayRate, growthRate);
    }

    public void regenerateArgs(int criticalHitCount,
                               double criticalScore,
                               double decayRate,
                               double growthRate) {
        this.criticalHitCount = criticalHitCount;
        this.criticalScore = criticalScore;
        this.decayRate = decayRate;
        this.growthRate = growthRate;
        this.b = -1;
        this.a = (1.0 / (criticalScore - 1) - b) / (avgIdf * criticalHitCount +
                criticalHitCount * 0.7 * (growthRate * avgIdf + avgIdf) / 2);
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
        avgIdf = getAvgIdf();
    }


    private double getAvgIdf() {
        double sum = 0;
        for (String gram : gramTextIdsMap.keySet()) {
            double idf = idf(gram);
//            System.out.printf("gram: %s, idf: %s%n", gram, idf);
            sum += idf;
        }
        return sum / gramTextIdsMap.size();
    }


    public double idf(String gram) {
        int n = gramTextIdsMap.get(gram).size();
        int d = textsCount();
        return Math.log((d + 1.0) / (n + 1));
    }


    public Text queryById(String id) {
        return idTextMap.get(id);
    }

    public int textsCount() {
        return idTextMap.size();
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
        for (int i = 0; i <= gramUnits.size() - n; i++) {
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

        Text gText = TextSimilaritySearch.textProcess(text);

        String gPString = String.join("", TextSimilaritySearch.textToGramUnits(gText));

        List<AC.MatchResult> mrs = ac.indexOf(gPString);
        // 不记录重复计数
        List<SimilaritySearchResult> result = new LinkedList<>();

        Map<String, List<String>> textIdGramsMap = new HashMap<>();

        for (AC.MatchResult mr : mrs) {
            String gram = mr.word;
            Set<String> textIds = gramTextIdsMap.get(gram);
            for (String textId : textIds) {
                List<String> grams = textIdGramsMap.get(textId);
                if (grams == null) {
                    grams = new ArrayList<>();
                    textIdGramsMap.put(textId, grams);
                }
                grams.add(gram);
            }

        }

        PriorityQueue<SimilaritySearchResult> priorityQueue = new PriorityQueue<>(new Comparator<SimilaritySearchResult>() {
            @Override
            public int compare(SimilaritySearchResult o1, SimilaritySearchResult o2) {
                return Double.compare(o1.score, o2.score);
            }
        });

        Map<String, Double> gramIdfMap = new HashMap<>();


        for (String textId : textIdGramsMap.keySet()) {
            List<String> grams = textIdGramsMap.get(textId);
            Map<String, Integer> gramCountMap = new HashMap<>();
            Text textObj = queryById(textId);
            for (String gram : grams) {
                Integer count = gramCountMap.get(gram);
                if (count == null) {
                    count = 0;
                }
                count++;
                gramCountMap.put(gram, count);
            }

            SimilaritySearchResult similaritySearchResult = new SimilaritySearchResult();
            similaritySearchResult.id = textObj.id;
            similaritySearchResult.text = textObj.source;
            similaritySearchResult.title = textObj.title;
            similaritySearchResult.score = 0;

            for (String gram : gramCountMap.keySet()) {
                Pattern pattern = Pattern.compile(gram);
                Matcher matcher = pattern.matcher(textObj.result);
                int count = gramCountMap.get(gram);
                while (matcher.find()) {
                    count++;
                }

                Double idf = gramIdfMap.get(gram);
                if (idf == null) {
                    idf = idf(gram);
                    gramIdfMap.put(gram, idf);
                }

                if (idf < avgIdf) {
                    idf *= decayRate;
                }

                double this_score = count / 2.0 * idf;

                similaritySearchResult.score += this_score; // 基本文本内容

            }

            // 基于标题
            Text titleObj = textProcess(textObj.title);
            if (titleObj.result.length() >= n) {

                // start
//                // gram标题中出现次数
//                Map<String, Integer> titleGramTitleCountMap = new HashMap<>();
//                // gram命中次数
//                Map<String, Integer> titleGramHitCountMap = new HashMap<>();
//
//
//                List<String> titleGrams = nGram(titleObj, n);
//                AC titleAC = new AC();
//
//                for (String titleGram : titleGrams) {
//                    Integer gramCount = titleGramTitleCountMap.get(titleGram);
//                    if (gramCount == null) {
//                        gramCount = 0;
//                    }
//                    gramCount++;
//                    titleGramTitleCountMap.put(titleGram, gramCount);
//
//                }
//
//                titleAC.addWords(titleGrams);
//
//                titleAC.update();
//
//                for (AC.MatchResult matchResult : titleAC.indexOf(gPString)) {
//                    String matchTitleGram = matchResult.word;
//                    Integer hitCount = titleGramHitCountMap.get(matchTitleGram);
//                    if (hitCount == null) {
//                        hitCount = 0;
//                    }
//                    hitCount++;
//                    titleGramHitCountMap.put(matchTitleGram, hitCount);
//                }
//
//                double this_idf = 0;
//                int matchTimes = 0;
//
//                for (String titleGram : titleGramTitleCountMap.keySet()) {
//                    Double idf = gramIdfMap.get(titleGram);
//                    if (idf == null) {
//                        idf = idf(titleGram);
//                        gramIdfMap.put(titleGram, idf);
//                    }
//                    if (titleGramHitCountMap.containsKey(titleGram)) {
//                        idf *= growthRate;
//                    }
//                    this_idf += titleGramTitleCountMap.get(titleGram) * idf;
//                }
//                double this_avg_idf = this_idf / titleGrams.size(); // 加权平均值
//
//                for (String titleGram : titleGramHitCountMap.keySet()) {
//                    Integer hitCount = titleGramHitCountMap.get(titleGram);
//                    matchTimes += hitCount * titleGramTitleCountMap.get(titleGram);
//                }
//
//                similaritySearchResult.score += matchTimes * this_avg_idf;
                // end


                List<String> titleGrams = nGram(titleObj, n);
                double this_idf = 0;
                int totalTimes = 0;
                for (String titleGram : titleGrams) {
                    Double idf = gramIdfMap.get(titleGram);
                    if (idf == null) {
                        idf = idf(titleGram);
                        gramIdfMap.put(titleGram, idf);
                    }
                    Matcher matcher = Pattern.compile(titleGram).matcher(gPString);
                    int times = 0;
                    while (matcher.find()) times++;
                    if (times != 0) {
                        idf *= times * growthRate;
                    }
                    this_idf += idf;
                    totalTimes += times;
                }
                double this_avg_idf = this_idf / titleGrams.size();
                similaritySearchResult.score += totalTimes * this_avg_idf;
//                System.out.println("title: " + titleObj.result + " avg_idf: " + this_avg_idf + " count: " + totalTimes);

            }


            similaritySearchResult.score = score(similaritySearchResult.score);

            if (priorityQueue.size() < topK) {
                priorityQueue.add(similaritySearchResult);
            } else if (priorityQueue.peek().score < similaritySearchResult.score) {
                priorityQueue.poll();
                priorityQueue.add(similaritySearchResult);
            }
        }


        while (!priorityQueue.isEmpty()) {
            result.add(0, priorityQueue.poll());
        }

        return result;
    }

    private double score(double sum) {
        return 1.0 / (a * sum + b) + 1;
    }

    public int wordsCount() {
        return gramTextIdsMap.size();
    }

    public static File save(TextSimilaritySearch textLib, File outFile) throws IOException {
        try (FileOutputStream fO = new FileOutputStream(outFile);
             BufferedOutputStream bO = new BufferedOutputStream(fO);
             ObjectOutputStream oO = new ObjectOutputStream(bO);
        ) {
            oO.writeObject(textLib);
        }
        return outFile;
    }

    public static TextSimilaritySearch load(File inFile) throws Exception {
        try (
                FileInputStream fileInputStream = new FileInputStream(inFile);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        ) {
            return (TextSimilaritySearch) objectInputStream.readObject();
        }
    }

}
