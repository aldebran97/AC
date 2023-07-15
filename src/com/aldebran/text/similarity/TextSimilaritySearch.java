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

    // 用于计算得分的各种参数
    public double criticalContentHitCount = 1;

    public double criticalTitleHitCount = 1;

    public double criticalScore = 0.5;

    public int n = 2;

    public double contentGrowRate = 5;

    public double titleGrowthRate = 5;

    public double decayRate = 0.5;

    public double avgIdf;

    public Map<String, Double> gramIdfMap = new HashMap<>();

    public ScoreCalculator scoreCalculator = new ScoreCalculator();


//    public ScoreCalculator2 scoreCalculator = new ScoreCalculator2();

    public String libName;

    // quick query
    public Map<String, FullText> idTextMap = new HashMap<>();

    public Map<String, Set<String>> contentGramTextIdsMap = new HashMap<>();

    public Map<String, Set<String>> titleGramTextIdsMap = new HashMap<>();

    public AC titleAC = new AC();

    public AC contentAC = new AC();

    public double titleGramsCountSum = 0;

    public double contentGramsCountSum = 0;

    public TextSimilaritySearch(double criticalContentHitCount,
                                double criticalTitleHitCount,
                                double criticalScore,
                                double contentGrowRate,
                                double titleGrowthRate,
                                double decayRate,
                                int n,
                                String libName) {
        this.criticalContentHitCount = criticalContentHitCount;
        this.criticalTitleHitCount = criticalTitleHitCount;
        this.criticalScore = criticalScore;
        this.contentGrowRate = contentGrowRate;
        this.titleGrowthRate = titleGrowthRate;
        this.decayRate = decayRate;
        this.n = n;
        this.libName = libName;

    }

    // 库插入新文章后要更新
    public void update() {
        processIdf();
        contentAC.update();
        titleAC.update();
        changeScoreArgs();
    }

    public int textsCount() {
        return idTextMap.size();
    }

    public double idf(String gram) {
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

        for (String gram : s) {
            double idf = idf(gram);
            gramIdfMap.put(gram, idf);
            idfSum += idf;
        }

        avgIdf = idfSum / gramsCount;
    }

    public void changeArgs(double criticalContentHitCount,
                           double criticalTitleHitCount,
                           double criticalScore,
                           double contentGrowRate,
                           double titleGrowthRate,
                           double decayRate) {
        this.criticalContentHitCount = criticalContentHitCount;
        this.criticalTitleHitCount = criticalTitleHitCount;
        this.criticalScore = criticalScore;
        this.contentGrowRate = contentGrowRate;
        this.titleGrowthRate = titleGrowthRate;
        this.decayRate = decayRate;
        changeScoreArgs();
    }

    public void changeScoreArgs() {
        scoreCalculator.update(criticalContentHitCount,
                criticalTitleHitCount,
                criticalScore,
                contentGrowRate,
                titleGrowthRate,
                contentGramsCountSum / textsCount(),
                titleGramsCountSum / textsCount(),
                avgIdf
        );
//        scoreCalculator.update(criticalContentHitCount,
//                criticalTitleHitCount,
//                criticalScore,
//                contentGrowRate,
//                titleGrowthRate,
//                avgIdf
//        );
    }

    public void addText(String content, String title, String id, double weight) {

        FullText fullText = new FullText();
        fullText.articleWeight = weight;
        fullText.id = id;

        fullText.contentText = TextProcessor.textProcess(content);
        fullText.titleText = TextProcessor.textProcess(title);

        fullText.contentText.sourceText = content;
        fullText.titleText.sourceText = title;

        fullText.contentText.gramCountMap = new HashMap<>();
        fullText.titleText.gramCountMap = new HashMap<>();

        List<String> contentGrams = TextProcessor.nGram(fullText.contentText, n);
        List<String> titleGrams = TextProcessor.nGram(fullText.titleText, n);

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

        BasicText basicText = TextProcessor.textProcess(text);

        String gPString = String.join("", TextProcessor.textToGramUnits(basicText));

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

            // 基于内容
            double contentIdfSum = 0;

            for (String contentGram : fullText.contentText.gramCountMap.keySet()) {
                double idf = gramIdfMap.get(contentGram);
                double this_score = 0;
                int libContentGramsCount = fullText.contentText.gramCountMap.get(contentGram);

                // 考虑衰减率
                if (idf < this.avgIdf) {
                    idf *= decayRate;
                }

                Integer gTextGramsCount = textMatchInfo.hitContentGramCountMap.get(contentGram);

                if (gTextGramsCount == null || gTextGramsCount == 0) {
                    this_score = idf;
                } else {
                    double log_a = 5;
                    this_score = MMath.log(log_a, libContentGramsCount + log_a - 1)
                            * contentGrowRate
                            * MMath.log(log_a, gTextGramsCount + log_a - 1) * idf;
                }
                contentIdfSum += this_score;

            }

            double contentAvgIdf = contentIdfSum / fullText.contentText.totalGramsCount;

            // 基于标题
            double titleIdfSum = 0;

            for (String titleGram : fullText.titleText.gramCountMap.keySet()) {
                double idf = gramIdfMap.get(titleGram);
                double this_score = 0;
                int libTitleGramsCount = fullText.titleText.gramCountMap.get(titleGram);

                // 考虑衰减率
//                if (idf < this.avgIdf) {
//                    idf *= decayRate;
//                }

                Integer gTextGramsCount = textMatchInfo.hitTitleGramCountMap.get(titleGram);

                if (gTextGramsCount == null || gTextGramsCount == 0) {
                    this_score = idf;
                } else {
                    double log_a = 5;
                    this_score = MMath.log(log_a, libTitleGramsCount + log_a - 1)
                            * titleGrowthRate
                            * MMath.log(log_a, gTextGramsCount + log_a - 1) * idf;
                }
                titleIdfSum += this_score;

            }

            double titleAvgIdf = titleIdfSum / fullText.titleText.totalGramsCount;

            double scoreX = (contentAvgIdf + titleAvgIdf) / 2;

            double score = scoreCalculator.score(scoreX);

            // 过滤无效文章，可去除此部分
            if (fullText.contentText.totalGramsCount <
                    this.contentGramsCountSum / this.textsCount() * 0.5) {
                score *= 0.1;
            }

//            double score = scoreCalculator.score(scoreX,
//                    fullText.titleText.totalGramsCount,
//                    fullText.contentText.totalGramsCount);

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

    private double weightScore(double score, double weight) {
        // (0.5,1) (1,3)
        // k = 4, b = -1
        // 可以采用非线性
        double k = weight * 4 - 1;
        return k * score;
    }
}
