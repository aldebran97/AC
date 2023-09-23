package com.aldebran.text.similarity;

import com.aldebran.text.ac.AC;
import com.aldebran.text.util.MMath;

import java.io.*;
import java.util.*;

/**
 * 文本相似搜索
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class TextSimilaritySearch implements Serializable {

    /**
     * 用于计算得分的各种参数
     */
    public double criticalContentHitCount = 1; // 临界情况内容期望命中的gram个数

    public double criticalTitleHitCount = 1; // 临界情况标题期望命中的gram个数
    public double criticalScore = 0.5; // 临界情况期望的分值
    public int n = 2;  // n-gram中n的取值

    public double contentK = 1; // 内容权重

    public double titleK = 1; // 标题权重

    public double hitGramsCountLogA = 8; // gram命中时对数量进行log运算的底数

    public double gramsCountLogA = 50; // 使得长度短的匹配文本有一些优势

    public double idfGrowthK = 10; // idf差距明显化


    /**
     * 用于计算得分的各种统计值
     */
    public double gramAvgIdf; // gram平均IDf

    public double gramMinIdf; // gram最小IDf

    public double gramMaxIdf; // gram最大IDF

    public double maxTitleAvgIdf; // title平均IDF的最大值

    public double minTitleAvgIdf; // title平均IDF的最小值

    public double avgTitleAvgIdf; // title平均IDF的平均值

    public double maxContentAvgIdf; // content平均IDf的最大值

    public double minContentAvgIdf; // content平均IDf的最小值

    public double avgContentAvgIdf; // content平均IDf的平均值

    public double titleIdfRate; // 为了让title和content变得可以比较而增加的一个比率，取值为avgContentAvgIdf/avgTitleAvgIdf

    // 基础增加值
    public double basicGrowthValue;

    // 标题grams个数总和
    public double titleGramsCountSum = 0;

    // 内容grams个数总和
    public double contentGramsCountSum = 0;


    /**
     * 得分计算相关
     */
    public ScoreCalculator scoreCalculator = new ScoreCalculator();

    public AvgIdfGrowthCalculator avgIdfGrowthCalculator = new AvgIdfGrowthCalculator();

    /**
     * 存储结构
     */
    public Map<String, Double> gramIdfMap = new HashMap<>();

    public String libName;

    // quick query
    public Map<String, FullText> idTextMap = new HashMap<>();

    public Map<String, Set<String>> contentGramTextIdsMap = new HashMap<>();

    public Map<String, Set<String>> titleGramTextIdsMap = new HashMap<>();

    public AC titleAC = new AC();

    public AC contentAC = new AC();

    public TextPreprocess textPreprocess = new TextPreprocess();


    public TextSimilaritySearch(double criticalContentHitCount,
                                double criticalTitleHitCount,
                                double criticalScore,
                                double contentK,
                                double titleK,
                                double hitGramsCountLogA,
                                double gramsCountLogA,
                                double idfGrowthK,
                                int n,
                                String libName) {
        this.criticalContentHitCount = criticalContentHitCount;
        this.criticalTitleHitCount = criticalTitleHitCount;
        this.criticalScore = criticalScore;
        this.contentK = contentK;
        this.titleK = titleK;
        this.hitGramsCountLogA = hitGramsCountLogA;
        this.gramsCountLogA = gramsCountLogA;
        this.idfGrowthK = idfGrowthK;
        this.n = n;
        this.libName = libName;

    }

    // 库插入新文章后要更新
    public void update() {
        processIdf();
        contentAC.update();
        titleAC.update();
        generateFullTextsAvgIdf();
        changeScoreArgs();
    }

    public int textsCount() {
        return idTextMap.size();
    }

    private double idf(String gram) {
        Set<String> s = new HashSet<>();
        Set<String> ids = titleGramTextIdsMap.get(gram);
        if (ids != null) {
            s.addAll(ids);
        }
        ids = contentGramTextIdsMap.get(gram);
        if (ids != null) {
            s.addAll(ids);
        }
        int n = s.size();
        int d = textsCount();
        return MMath.log2((d + 1.0) / (n + 1));
    }


    public void processIdf() {
        gramIdfMap.clear();
        Set<String> s = new HashSet<>();
        s.addAll(contentGramTextIdsMap.keySet());
        s.addAll(titleGramTextIdsMap.keySet());

        double idfSum = 0;
        long gramsCount = s.size();
        gramMaxIdf = Double.MIN_VALUE;
        gramMinIdf = Double.MAX_VALUE;

        for (String gram : s) {
            double idf = idf(gram);
//            assert MMath.legalPositiveDoubleValue(idf);
            if (idf < gramMinIdf) {
                gramMinIdf = idf;
            }
            if (idf > gramMaxIdf) {
                gramMaxIdf = idf;
            }
            gramIdfMap.put(gram, idf);
            idfSum += idf;
        }

        gramAvgIdf = idfSum / gramsCount;
//        assert MMath.legalPositiveDoubleValue(gramAvgIdf);
//        assert MMath.legalPositiveDoubleValue(gramMaxIdf);
//        assert MMath.legalPositiveDoubleValue(gramMinIdf);
        System.out.println("gramAvgIdf: " + gramAvgIdf);
        System.out.println("gramMaxIdf: " + gramMaxIdf);
        System.out.println("gramMinIdf: " + gramMinIdf);
    }

    public void changeArgs(double criticalContentHitCount,
                           double criticalTitleHitCount,
                           double criticalScore,
                           double contentK,
                           double titleK,
                           double hitGramsCountLogA,
                           double gramsCountLogA,
                           double idfGrowthK) {
        this.criticalContentHitCount = criticalContentHitCount;
        this.criticalTitleHitCount = criticalTitleHitCount;
        this.criticalScore = criticalScore;
        this.contentK = contentK;
        this.titleK = titleK;
        this.hitGramsCountLogA = hitGramsCountLogA;
        this.gramsCountLogA = gramsCountLogA;
        this.idfGrowthK = idfGrowthK;
        changeScoreArgs();
//        generateFullTextsAvgIdf();
    }

    private void changeScoreArgs() {
        scoreCalculator.update(criticalContentHitCount,
                criticalTitleHitCount,
                criticalScore,
                avgContentAvgIdf,
                basicGrowthValue,
                contentK,
                titleK,
                (contentGramsCountSum + titleGramsCountSum) / textsCount(),
                gramsCountLogA,
                idfGrowthK
        );
        avgIdfGrowthCalculator.update(basicGrowthValue, gramAvgIdf, gramMinIdf, gramMaxIdf, titleIdfRate, idfGrowthK);
    }

    public void addText(String content, String title, String id, double weight) {

        FullText fullText = new FullText();
        fullText.articleWeight = weight;
        fullText.id = id;

        fullText.contentText = textPreprocess.textProcess(content);
        fullText.titleText = textPreprocess.textProcess(title);

        fullText.contentText.sourceText = content;
        fullText.titleText.sourceText = title;

        fullText.contentText.gramCountMap = new HashMap<>();
        fullText.titleText.gramCountMap = new HashMap<>();

        List<String> contentGrams = textPreprocess.nGram(fullText.contentText, n);
        List<String> titleGrams = textPreprocess.nGram(fullText.titleText, n);

        if (contentGrams.isEmpty() || titleGrams.isEmpty()) {
            // ignore too short text
//            System.out.printf("ignore, id: %s, title: %s, content: %s%n", id, title, content);
            return;
        }

        if (contentGrams.get(0).length() < n || titleGrams.get(0).length() < n) {
            // ignore too short text
//            System.out.printf("ignore, id: %s, title: %s, content: %s%n", id, title, content);
            return;
        }

        // 处理内容
        for (String gram : contentGrams) {
            Integer c = fullText.contentText.gramCountMap.get(gram);
            if (c == null) {
                c = 0;
            }
            c++;
            fullText.contentText.gramCountMap.put(gram, c);
            Set<String> set = contentGramTextIdsMap.get(gram);
            if (set == null) {
                set = new HashSet<>();
                contentGramTextIdsMap.put(gram, set);
            }
            set.add(id);
        }
        fullText.contentText.totalGramsCount = contentGrams.size();
        contentAC.addWords(contentGrams);
        contentGramsCountSum += contentGrams.size();

        // 处理标题
        for (String gram : titleGrams) {
            Integer c = fullText.titleText.gramCountMap.get(gram);
            if (c == null) {
                c = 0;
            }
            c++;
            fullText.titleText.gramCountMap.put(gram, c);
            Set<String> set = titleGramTextIdsMap.get(gram);
            if (set == null) {
                set = new HashSet<>();
                titleGramTextIdsMap.put(gram, set);
            }
            set.add(id);
        }
        fullText.titleText.totalGramsCount = titleGrams.size();
        titleAC.addWords(contentGrams);
        titleGramsCountSum += titleGrams.size();

        idTextMap.put(id, fullText);

    }


    public List<SimilaritySearchResult> similaritySearch(String text, int topK) {

        List<SimilaritySearchResult> result = new LinkedList<>();

        BasicText basicText = textPreprocess.textProcess(text);

        String gPString = String.join("", textPreprocess.textToGramUnits(basicText));

        List<AC.MatchResult> contentMRs = contentAC.indexOf(gPString);
        List<AC.MatchResult> titleMRs = titleAC.indexOf(gPString);

        Map<String, TextMatchInfo> idMatchInfoMap = new HashMap<>();

        // 处理内容
        for (AC.MatchResult mr : contentMRs) {
            String hitGram = mr.word;
            Set<String> textIds = contentGramTextIdsMap.get(hitGram);
            if (textIds != null) {
                for (String textId : textIds) {
                    TextMatchInfo textMatchInfo = idMatchInfoMap.get(textId);
                    if (textMatchInfo == null) {
                        textMatchInfo = new TextMatchInfo();
                        textMatchInfo.text = idTextMap.get(textId);
                        idMatchInfoMap.put(textId, textMatchInfo);
                    }
                    Integer c = textMatchInfo.hitContentGramCountMap.get(hitGram);
                    if (c == null) {
                        c = 0;
                    }
                    c++;
                    textMatchInfo.hitContentGramCountMap.put(hitGram, c);
                }
            }
        }

        // 处理标题
        for (AC.MatchResult mr : titleMRs) {
            String hitGram = mr.word;
            Set<String> textIds = titleGramTextIdsMap.get(hitGram);
            if (textIds != null) {
                for (String textId : textIds) {
                    TextMatchInfo textMatchInfo = idMatchInfoMap.get(textId);
                    if (textMatchInfo == null) {
                        textMatchInfo = new TextMatchInfo();
                        textMatchInfo.text = idTextMap.get(textId);
                        idMatchInfoMap.put(textId, textMatchInfo);
                    }
                    Integer c = textMatchInfo.hitTitleGramCountMap.get(hitGram);
                    if (c == null) {
                        c = 0;
                    }
                    c++;
                    textMatchInfo.hitTitleGramCountMap.put(hitGram, c);
                }
            }
        }

        PriorityQueue<SimilaritySearchResult> priorityQueue = new PriorityQueue<>(new Comparator<SimilaritySearchResult>() {
            @Override
            public int compare(SimilaritySearchResult o1, SimilaritySearchResult o2) {
                return Double.compare(o1.score, o2.score);
            }
        });

        for (TextMatchInfo textMatchInfo : idMatchInfoMap.values()) {
            FullText fullText = textMatchInfo.text;
//            System.out.printf("title: %s, init title score: %s, init content score: %s%n",
//                    fullText.titleText.resultText, fullText.contentText.basicTextAvgIdf, fullText.titleText.basicTextAvgIdf);

            if (Double.isNaN(fullText.contentText.basicTextAvgIdf)) {
                System.out.println("content NaN");
                System.out.println(fullText.contentText);
            }

            if (Double.isNaN(fullText.titleText.basicTextAvgIdf)) {
                System.out.println("content NaN");
                System.out.println(fullText.titleText);
            }

            // 基于内容
            double thisContentAvgIdf = fullText.contentText.basicTextAvgIdf;

            for (String hitContentGram : textMatchInfo.hitContentGramCountMap.keySet()) {
                int gTextGramsCount = textMatchInfo.hitContentGramCountMap.get(hitContentGram);
                int libContentGramsCount = fullText.contentText.gramCountMap.get(hitContentGram);
                double idf = gramIdfMap.get(hitContentGram);
                double this_growth_value = avgIdfGrowthCalculator.calc(idf, false);

                thisContentAvgIdf += MMath.log(hitGramsCountLogA, libContentGramsCount + hitGramsCountLogA - 1)
                        * MMath.log(hitGramsCountLogA, gTextGramsCount + hitGramsCountLogA - 1) * this_growth_value;
            }


            // 基于标题
            double thisTitleAvgIdf = fullText.titleText.basicTextAvgIdf * titleIdfRate;

            for (String hitTitleGram : textMatchInfo.hitTitleGramCountMap.keySet()) {
                int gTextGramsCount = textMatchInfo.hitTitleGramCountMap.get(hitTitleGram);
                int libTitleGramsCount = fullText.titleText.gramCountMap.get(hitTitleGram);
                double idf = gramIdfMap.get(hitTitleGram);

                double this_growth_value = avgIdfGrowthCalculator.calc(idf, true);

                thisTitleAvgIdf += MMath.log(hitGramsCountLogA, libTitleGramsCount + hitGramsCountLogA - 1)
                        * MMath.log(hitGramsCountLogA, gTextGramsCount + hitGramsCountLogA - 1) * this_growth_value;

            }

            double scoreX = (contentK * thisContentAvgIdf + titleK * thisTitleAvgIdf) / (contentK + titleK)
                    * fullText.articleWeight;

            // 除去平均长度的log值

            scoreX /= MMath.log(gramsCountLogA, fullText.totalGramsCount + gramsCountLogA);

            double score = scoreCalculator.score(scoreX);

            SimilaritySearchResult similaritySearchResult = new SimilaritySearchResult();

            similaritySearchResult.id = fullText.id;
            similaritySearchResult.score = score;
            similaritySearchResult.text = fullText.contentText.sourceText;
            similaritySearchResult.title = fullText.titleText.sourceText;

            if (Double.isNaN(similaritySearchResult.score)) continue;

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

    // 列出所有文章，生成器方法
    public Iterable<FullText> listAll() {


        return new Iterable<FullText>() {
            @Override
            public Iterator<FullText> iterator() {
                return new Iterator<FullText>() {

                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public FullText next() {
                        return null;
                    }
                };
            }
        };
    }

    public FullText queryById(String id) {
        return idTextMap.get(id);
    }

    public int contentWordsCount() {
        return contentGramTextIdsMap.size();
    }

    public int titleWordsCount() {
        return titleGramTextIdsMap.size();
    }

    // 生成BasicText的平均IDF
    private void generateBasicTextAvgIdf(BasicText basicText) {
        basicText.basicTextAvgIdf = 0;
        for (String gram : basicText.gramCountMap.keySet()) {
            int count = basicText.gramCountMap.get(gram);
            double idf = gramIdfMap.get(gram);
            basicText.basicTextAvgIdf += idf * count;
        }
        basicText.basicTextAvgIdf /= basicText.totalGramsCount;
    }

    // 生成所有FullText的平均IDF和基础增加值
    private void generateFullTextsAvgIdf() {

        maxTitleAvgIdf = Double.MIN_VALUE;
        minTitleAvgIdf = Double.MAX_VALUE;
        double titleAvgIdfSum = 0;

        maxContentAvgIdf = Double.MIN_VALUE;
        minContentAvgIdf = Double.MAX_VALUE;
        double contentAvgIdfSum = 0;

        for (FullText fullText : idTextMap.values()) {
            generateBasicTextAvgIdf(fullText.titleText);
            generateBasicTextAvgIdf(fullText.contentText);

            if (fullText.titleText.basicTextAvgIdf < minTitleAvgIdf) {
                minTitleAvgIdf = fullText.titleText.basicTextAvgIdf;
            }
            if (fullText.titleText.basicTextAvgIdf > maxTitleAvgIdf) {
                maxTitleAvgIdf = fullText.titleText.basicTextAvgIdf;
            }

            titleAvgIdfSum += fullText.titleText.basicTextAvgIdf;

            if (fullText.contentText.basicTextAvgIdf < minContentAvgIdf) {
                minContentAvgIdf = fullText.contentText.basicTextAvgIdf;
            }
            if (fullText.contentText.basicTextAvgIdf > maxContentAvgIdf) {
                maxContentAvgIdf = fullText.contentText.basicTextAvgIdf;
            }

            contentAvgIdfSum += fullText.contentText.basicTextAvgIdf;

            fullText.totalGramsCount = fullText.titleText.totalGramsCount + fullText.contentText.totalGramsCount;
//            System.out.println(fullText.totalGramsCount);
        }

        avgTitleAvgIdf = titleAvgIdfSum / idTextMap.size();
        System.out.println("avgTitleAvgIdf: " + avgTitleAvgIdf);
        avgContentAvgIdf = contentAvgIdfSum / idTextMap.size();
        System.out.println("avgContentAvgIdf: " + avgContentAvgIdf);
        titleIdfRate = avgContentAvgIdf / avgTitleAvgIdf;
        System.out.println("titleIdfRate: " + titleIdfRate);

        // 基础增加值采用极差
        basicGrowthValue = maxContentAvgIdf - minContentAvgIdf;
        System.out.println("basicGrowthValue: " + basicGrowthValue);
    }

    public Statistics getStatistics() {
        Statistics statistics = new Statistics();
        statistics.totalWordsCount = gramIdfMap.size();
        statistics.textsCount = textsCount();
        double originTextLength = 0;
        for (FullText fullText : idTextMap.values()) {
            originTextLength += fullText.titleText.sourceText.length();
            originTextLength += fullText.contentText.sourceText.length();
        }
        statistics.avgOriginTextLength = originTextLength / statistics.textsCount;
        return statistics;
    }


}
