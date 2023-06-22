package com.aldebran.text.similarity;

import com.aldebran.text.ac.AC;
import com.aldebran.text.replacePolicy.ReplaceInfo;
import com.aldebran.text.replacePolicy.WordReplaceInfo;
import com.aldebran.text.util.MMath;

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
        replaceInfos.add(
                new ReplaceInfo("介绍",
                        Pattern.compile("(简介)|((基本|主要)?信息)"),
                        Pattern.compile("(简介)|((基本|主要)?信息)")));
        // lots of
    }

    public AC ac = new AC();

    private Map<String, Set<String>> gramTextIdsMap = new HashMap<>();

    private Map<String, Text> idTextMap = new HashMap<>();

    // 不同问题的(criticalHitCount * avg_idf , criticalScore)不同，检索和查重是不同的
    public double criticalContentHitCountPerGram = 1;

    public double criticalTitleHitCountPerGram = 1;

    public double criticalScore = 0.5;

    public int n = 2;

    public double contentGrowRate = 5;

    public double titleGrowthRate = 5;

    public double decayRate = 0.5;

    public double avgIdf = 0;

    public String libName;

    public double a;

    public double b;

    public TextSimilaritySearch(String libName,
                                double criticalContentHitCountPerGram,
                                double criticalTitleHitCountPerGram,
                                double criticalScore,
                                int n,
                                double contentGrowRate,
                                double titleGrowRate,
                                double decayRate) {
        this.libName = libName;
        this.n = n;
        regenerateArgs(criticalContentHitCountPerGram, criticalTitleHitCountPerGram,
                criticalScore, contentGrowRate, titleGrowRate, decayRate);
    }

    public void regenerateArgs(double criticalContentHitCountPerGram,
                               double criticalTitleHitCountPerGram,
                               double criticalScore,
                               double contentGrowRate,
                               double titleGrowthRate,
                               double decayRate) {
        this.criticalContentHitCountPerGram = criticalContentHitCountPerGram;
        this.criticalTitleHitCountPerGram = criticalTitleHitCountPerGram;
        this.criticalScore = criticalScore;
        this.contentGrowRate = contentGrowRate;
        this.titleGrowthRate = titleGrowthRate;
        this.decayRate = decayRate;
        this.b = -1;
        this.a = (1.0 / (criticalScore - 1) - b) / (
                ((0.8 * avgIdf + 0.2 * contentGrowRate * avgIdf) * criticalContentHitCountPerGram
                        + (avgIdf + avgIdf * titleGrowthRate) / 2 * criticalTitleHitCountPerGram) / 2
        );
    }


    public void addText(String text, String title, String id, double weight) {
        Text textObj = textProcess(text);
        textObj.id = id;
        textObj.title = title;
        textObj.contentWeight = weight;
        List<String> grams = nGram(textObj, n);
        textObj.gramCountMap = new HashMap<>();
        for (String gram : grams) {
            Integer c = textObj.gramCountMap.get(gram);
            if (c == null) {
                c = 0;
            }
            c++;
            textObj.gramCountMap.put(gram, c);
            Set<String> set = gramTextIdsMap.get(gram);
            if (set == null) {
                set = new HashSet<>();
                gramTextIdsMap.put(gram, set);
            }
            set.add(id);
        }
        textObj.totalGramsCount = grams.size();

        ac.addWords(grams);

        idTextMap.put(textObj.id, textObj);
    }

    public void update() {
        ac.update();
        avgIdf = getAvgIdf();
        regenerateArgs(this.criticalContentHitCountPerGram,
                this.criticalTitleHitCountPerGram,
                this.criticalScore,
                this.contentGrowRate,
                this.titleGrowthRate,
                this.decayRate);
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
        return MMath.log2((d + 1.0) / (n + 1));
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
            Text textObj = queryById(textId);

            Map<String, Integer> contentHitGramCountMap = new HashMap<>();
            for (String contentHitGram : textIdGramsMap.get(textId)) {
                Integer c = contentHitGramCountMap.get(contentHitGram);
                if (c == null) {
                    c = 0;
                }
                c++;
                contentHitGramCountMap.put(contentHitGram, c);
            }

            SimilaritySearchResult similaritySearchResult = new SimilaritySearchResult();
            similaritySearchResult.id = textObj.id;
            similaritySearchResult.text = textObj.source;
            similaritySearchResult.title = textObj.title;
            similaritySearchResult.score = 0;

            double contentScore = 0.0;

            double contentIdfSum = 0.0;

            double contentAvgIdf = 0.0;

            // 基于内容

            for (String libContentGram : textObj.gramCountMap.keySet()) {
                // 库文章gram命中数量
                Integer gTextGramsCountPerGram = contentHitGramCountMap.get(libContentGram);
                if (gTextGramsCountPerGram == null) {
                    gTextGramsCountPerGram = 0;
                }
                int libContentGramsCount = textObj.gramCountMap.get(libContentGram);

                Double idf = gramIdfMap.get(libContentGram);
                if (idf == null) {
                    idf = idf(libContentGram);
                    gramIdfMap.put(libContentGram, idf);
                }

                if (idf < avgIdf) {
                    idf *= decayRate;
                }

                double this_score = 0;
                if (gTextGramsCountPerGram == 0) {
                    this_score = idf;
                } else {
//                    this_score = gTextGramsCountPerGram * idf;
                    double log_a = 8;
                    this_score = contentGrowRate * MMath.log(log_a, gTextGramsCountPerGram + log_a - 1) * idf;
                }

                contentIdfSum += libContentGramsCount * this_score; // 基本文本内容
            }

            contentAvgIdf = contentIdfSum / textObj.totalGramsCount;

            contentScore = contentAvgIdf * contentWeightK(textObj.contentWeight);

            // contentScore期望是 0.2*contentGrowRate*avg_idf + 0.8*avg_idf

            // 基于标题
            Text titleObj = textProcess(textObj.title);

            double titleScore = 0.0;

            if (titleObj.result.length() >= n) {

                List<String> titleGrams = nGram(titleObj, n);
                double titleGramsIdfSum = 0;
                for (String titleGram : titleGrams) {
                    Double idf = gramIdfMap.get(titleGram);
                    if (idf == null) {
                        idf = idf(titleGram);
                        gramIdfMap.put(titleGram, idf);
                    }
                    Matcher matcher = Pattern.compile(titleGram).matcher(gPString);
                    int gTextGramsCountPerGram = 0;
                    while (matcher.find()) gTextGramsCountPerGram++;
                    if (gTextGramsCountPerGram != 0) {
                        idf *= gTextGramsCountPerGram * titleGrowthRate;
                    }
                    titleGramsIdfSum += idf;
                }
                double titleGramsAvgIdf = titleGramsIdfSum / titleGrams.size();
                titleScore = titleGramsAvgIdf;
            }

            // titleScore的期望值假设是 (avg_idf + avg_idf * 1 * growthRate) /2

            similaritySearchResult.score = (contentScore + titleScore) / 2;

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

    private double contentWeightK(double weight) {
        // (0.5,1) (1,3)
        // k = 4, b = -1
        // 可以采用非线性
        return weight * 4 - 1;
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
