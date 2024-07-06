package com.aldebran.text.similarity;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.score.BM25FinalScoreCalculator;
import com.aldebran.text.text.BasicText;
import com.aldebran.text.text.FullText;
import com.aldebran.text.text.ShowText;
import com.aldebran.text.text.TextType;
import com.aldebran.text.tokenizer.NGramTokenizer;
import com.aldebran.text.tokenizer.Tokenizer;
import com.aldebran.text.util.CheckUtil;
import com.aldebran.text.util.ContinuousSerialUtil;
import com.aldebran.text.util.FileUtils;
import com.aldebran.text.util.MMath;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.aldebran.text.Constants.*;

/**
 * 文本相似搜索
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class TextSimilaritySearch implements Serializable {

    /**
     * 统计参数 + 得分计算
     */
    // BM25 得分计算器
    private BM25FinalScoreCalculator bm25FinalScoreCalculator = new BM25FinalScoreCalculator();

    public HashMap<String, Double> wordIdfMap = new HashMap<>();

    // 标题词数总和
    public double titleWordsCountSum = 0;

    // 内容词数总和
    public double contentWordsCountSum = 0;

    /**
     * 存储结构
     */
    public String libName;

    public transient File libFolder;

    public transient File dataFolder;

    // quick query
    public transient HashMap<String, FullText> idTextMap = new HashMap<>();

    public transient HashMap<String, Set<String>> contentWordTextIdsMap = new HashMap<>();

    public transient HashMap<String, Set<String>> titleWordTextIdsMap = new HashMap<>();

    public transient ACPlus titleAC = new ACPlus();

    public transient ACPlus contentAC = new ACPlus();

    /**
     * 分词器
     */
    public transient Tokenizer tokenizer;

    /**
     * 多线程配置
     */
    // 启用多线程搜索的最小文本数量
    public int multipleThreadSearchMinTextsCount = MULTIPLE_THREAD_SEARCH_MIN_TEXTS_COUNT;

    // 是否允许多线程
    public boolean allowMultiThreadsSearch = false;

    public TextSimilaritySearch(double criticalContentHitCount, // 内容词临界命中个数
                                double criticalTitleHitCount, // 标题词临界命中个数
                                double criticalScore, // 临界得分
                                double contentK, // 内容权重
                                double titleK, // 标题权重
                                double bm25K, // bm25算法中的k
                                double bm25B, // bm25算法中的b
                                double itemScoreDiff, // 得分区分度
                                Tokenizer tokenizer, // 分词器
                                String libName, // 库名
                                File libFolder // 库存储路径
    ) {
        modifyParams(criticalContentHitCount, criticalTitleHitCount, criticalScore, contentK, titleK, bm25K, bm25B, itemScoreDiff);
        this.tokenizer = tokenizer;
        this.libName = libName;
        setSaveFolder(libFolder);
    }

    public TextSimilaritySearch(String libName, File libFolder) {
        this(
                CRITICAL_CONTENT_HIT_COUNT, CRITICAL_TITLE_HIT_COUNT, CRITICAL_SCORE, CONTENT_K, TITLE_K, BM25_K, BM25_B, ITEM_SCORE_DIFF,
                new NGramTokenizer(2, null), libName, libFolder
        );
    }

    public void setSaveFolder(File folder) {
        this.libFolder = folder;
        this.dataFolder = new File(libFolder, "data");
        this.dataFolder.mkdirs();
    }

    public void modifyParams(
            double criticalContentHitCount, // 内容词临界命中个数
            double criticalTitleHitCount, // 标题词临界命中个数
            double criticalScore, // 临界得分
            double contentK, // 内容权重
            double titleK, // 标题权重
            double bm25K, // bm25算法中的k
            double bm25B, // bm25算法中的b
            double itemScoreDiff // 得分区分度
    ) {
        this.bm25FinalScoreCalculator.contentFieldScoreCalculator.criticalHitCount = criticalContentHitCount;
        this.bm25FinalScoreCalculator.contentFieldScoreCalculator.k = contentK;

        this.bm25FinalScoreCalculator.titleFieldScoreCalculator.criticalHitCount = criticalTitleHitCount;
        this.bm25FinalScoreCalculator.titleFieldScoreCalculator.k = titleK;

        this.bm25FinalScoreCalculator.titleFieldScoreCalculator.bm25B = bm25B;
        this.bm25FinalScoreCalculator.contentFieldScoreCalculator.bm25B = bm25B;

        this.bm25FinalScoreCalculator.titleFieldScoreCalculator.bm25K = bm25K;
        this.bm25FinalScoreCalculator.contentFieldScoreCalculator.bm25K = bm25K;

        this.bm25FinalScoreCalculator.titleFieldScoreCalculator.itemScoreDiff = itemScoreDiff;
        this.bm25FinalScoreCalculator.contentFieldScoreCalculator.itemScoreDiff = itemScoreDiff;

        this.bm25FinalScoreCalculator.criticalScore = criticalScore;
    }


    // 库插入新文章后要更新
    public void update() {
        generateWordIdfMap(); // 生成每个词的IDF
        contentAC.update(); // 更新内容词的AC自动机
        titleAC.update(); // 更新标题词的AC自动机
        calcStatisticsParams(); // 生成统计类参数
        bm25FinalScoreCalculator.update(); // 更新得分计算器
    }

    public int textsCount() {
        return idTextMap.size();
    }

    private double idf(String gram) {
        Set<String> s = new HashSet<>();
        Set<String> ids = titleWordTextIdsMap.get(gram);
        if (ids != null) {
            s.addAll(ids);
        }
        ids = contentWordTextIdsMap.get(gram);
        if (ids != null) {
            s.addAll(ids);
        }
        int n = s.size();
        int d = textsCount();
        return MMath.log2(d * 1.0 / n);
    }

    // 计算每个词的IDF
    public void generateWordIdfMap() {
        wordIdfMap.clear();

        for (Set<String> set : Arrays.asList(contentWordTextIdsMap.keySet(), titleWordTextIdsMap.keySet())) {
            for (String word : set) {
                if (!wordIdfMap.containsKey(word)) {
                    double idf = idf(word);
                    CheckUtil.legalDouble(idf);
                    wordIdfMap.put(word, idf);
                }

            }
        }
    }

    private BasicText getBasicText(String id, String srcText, String dstText, List<String> words, TextType textType) throws IOException {
        BasicText basicText = new BasicText();
        basicText.totalWordsCountRepeat = words.size();
        basicText.wordCountMap = new HashMap<>();

        for (String word : words) {
            basicText.wordCountMap.put(word, basicText.wordCountMap.getOrDefault(word, 0) + 1);

            Map<String, Set<String>> textIdsMap = textType == TextType.TITLE ? titleWordTextIdsMap : contentWordTextIdsMap;

            Set<String> set = textIdsMap.getOrDefault(word, new HashSet<>());
            textIdsMap.put(word, set);
            set.add(id);
        }

        basicText.avgTf = 1.0 / basicText.wordCountMap.size();
        return basicText;
    }


    private void writeTextToDisk(String id, String title, String pTitle, String content, String pContent, HashMap<String, Object> metaData)
            throws IOException {
        if (metaData == null) {
            metaData = new HashMap<>();
        }
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> titleData = new HashMap<>();
        Map<String, Object> contentData = new HashMap<>();
        titleData.put("src", title);
        titleData.put("dst", pTitle);
        contentData.put("src", content);
        contentData.put("dst", pContent);
//        data.put("id", id);
        data.put("title", titleData);
        data.put("content", contentData);
        data.put("metaData", metaData);
        File file = new File(dataFolder, String.format("%s.json", id));
        FileUtils.writeJSON(file, data);
    }


    private JSONObject readTextFromDisk(String id) throws IOException {
        File file = new File(dataFolder, String.format("%s.json", id));
        return FileUtils.readJSONObject(file);
    }

    private void fillResults(List<SimilaritySearchResult> results) throws IOException {
        for (SimilaritySearchResult result : results) {
            JSONObject data = readTextFromDisk(result.id);
            result.content = data.getJSONObject("content").getString("src");
            result.title = data.getJSONObject("title").getString("src");
            result.metaData = data.getJSONObject("metaData");
        }
    }

    private void fillShowResult(ShowText showText) throws IOException {
        JSONObject data = readTextFromDisk(showText.id);
        showText.content = data.getJSONObject("content").getString("src");
        showText.title = data.getJSONObject("title").getString("src");
        showText.metaData = data.getJSONObject("metaData");
    }


    public void addText(String content, String title, String id, double weight, HashMap<String, Object> metaData) throws IOException {

        FullText fullText = new FullText();
        fullText.articleWeight = weight;
        fullText.id = id;

        String dstContent = tokenizer.textPreprocess.preprocessToText(content);
        String dstTitle = tokenizer.textPreprocess.preprocessToText(title);

        List<String> contentWords = tokenizer.tokenize(content);
        List<String> titleWords = tokenizer.tokenize(title);

//        System.out.println("contentWords: " + contentWords);
//        System.out.println("titleWords: " + titleWords);

        if (contentWords.isEmpty() || titleWords.isEmpty()) {
            // ignore too short text
//            System.out.printf("ignore, id: %s, title: %s, content: %s%n", id, title, content);
            return;
        }

        fullText.contentText = getBasicText(id, content, dstContent, contentWords, TextType.CONTENT);
        fullText.titleText = getBasicText(id, title, dstTitle, titleWords, TextType.TITLE);
        fullText.totalWordsCountRepeat = contentWords.size() + titleWords.size();

        writeTextToDisk(id, title, dstTitle, content, dstContent, metaData);

        contentAC.addWords(contentWords);
        contentWordsCountSum += contentWords.size();

        titleAC.addWords(titleWords);
        titleWordsCountSum += titleWords.size();

        idTextMap.put(id, fullText);

    }

    public List<SimilaritySearchResult> similaritySearch(String text, int topK) throws Exception {
        double textsCount = textsCount();
        if (textsCount <= multipleThreadSearchMinTextsCount || !allowMultiThreadsSearch) {
            return similaritySearchSingleThread(text, topK);
        } else {
            return similaritySearchMultipleThread(text, topK);
        }
    }


    private void getMatchInfoSingleThread(Map<String, MultipleFieldMatchInfo> idMatchInfoMap,
                                          String processedText,
                                          TextType textType
    ) {
        ACPlus acPlus = textType == TextType.TITLE ? titleAC : contentAC;
        Map<String, Set<String>> wordTextIdsMap = textType == TextType.TITLE ? titleWordTextIdsMap : contentWordTextIdsMap;
        List<AC.MatchResult> mrs = acPlus.indexOf(processedText);
//        System.out.printf("text: %s, mrs: %s, text type: %s%n", processedText, mrs, textType);
        getMatchInfoSingleThread(idMatchInfoMap, wordTextIdsMap, mrs, textType);
        postProcessMatchInfo(idMatchInfoMap.values());
    }

    private void getMatchInfoSingleThread(Map<String, MultipleFieldMatchInfo> idMatchInfoMap,
                                          Map<String, Set<String>> wordTextIdsMap,
                                          List<AC.MatchResult> mrs,
                                          TextType textType) {
        for (AC.MatchResult mr : mrs) {
            String hitWord = mr.word;
            Set<String> textIds = wordTextIdsMap.get(hitWord);
            if (textIds != null) {
                for (String textId : textIds) {
                    MultipleFieldMatchInfo textMatchInfo = idMatchInfoMap.get(textId);
                    if (textMatchInfo == null) {
                        textMatchInfo = new MultipleFieldMatchInfo();
                        textMatchInfo.text = idTextMap.get(textId);
                        textMatchInfo.titleMatchInfo.basicText = textMatchInfo.text.titleText;
                        textMatchInfo.contentMatchInfo.basicText = textMatchInfo.text.contentText;
                        idMatchInfoMap.put(textId, textMatchInfo);
                    }
                    SingleFieldMatchInfo singleFieldMatchInfo = textType == TextType.TITLE ? textMatchInfo.titleMatchInfo : textMatchInfo.contentMatchInfo;
                    singleFieldMatchInfo.hitWordCountMap.put(hitWord, singleFieldMatchInfo.hitWordCountMap.getOrDefault(hitWord, 0) + 1);
                }
            }
        }
    }

    private void postProcessMatchInfo(Collection<MultipleFieldMatchInfo> multipleFieldMatchInfos) {
        for (MultipleFieldMatchInfo multipleFieldMatchInfo : multipleFieldMatchInfos) {
            for (SingleFieldMatchInfo singleFieldMatchInfo : Arrays.asList(multipleFieldMatchInfo.titleMatchInfo, multipleFieldMatchInfo.contentMatchInfo)) {
                List<String> words = new ArrayList<>(singleFieldMatchInfo.hitWordCountMap.keySet());
                singleFieldMatchInfo.idfs = new double[words.size()];
                singleFieldMatchInfo.tfs = new double[words.size()];
                for (int i = 0; i < words.size(); i++) {
                    String word = words.get(i);
                    singleFieldMatchInfo.idfs[i] = wordIdfMap.get(word);
                    singleFieldMatchInfo.tfs[i] = singleFieldMatchInfo.basicText.getTf(word);
                }
            }
        }
    }


    private List<Thread> getMatchInfoMultipleThread(Map<String, MultipleFieldMatchInfo> idMatchInfoMap,
                                                    String processedText,
                                                    TextType textType,
                                                    int startThreadsNum
    ) {
        CheckUtil.Assert(startThreadsNum >= 1);
        List<Thread> threads = new ArrayList<>();
        ACPlus acPlus = textType == TextType.TITLE ? titleAC : contentAC;
        Map<String, Set<String>> wordTextIdsMap = textType == TextType.TITLE ? titleWordTextIdsMap : contentWordTextIdsMap;
        List<AC.MatchResult> mrs = acPlus.indexOf(processedText);
        int mrSt = 0;
        int mrStUnit = (int) Math.ceil(mrs.size() * 1.0 / startThreadsNum);
        while (mrSt < mrs.size()) {

            int i = mrSt;
            int j = mrSt + mrStUnit > mrs.size() ? mrs.size() : mrSt + mrStUnit;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    List<AC.MatchResult> thisMRs = mrs.subList(i, j);
                    Map<String, MultipleFieldMatchInfo> thisIdMatchInfoMap = new HashMap<>();
                    getMatchInfoSingleThread(thisIdMatchInfoMap, wordTextIdsMap, thisMRs, textType);
                    // combine
                    synchronized (idMatchInfoMap) {
                        for (Map.Entry<String, MultipleFieldMatchInfo> entry : thisIdMatchInfoMap.entrySet()) {
                            String textId = entry.getKey();
                            MultipleFieldMatchInfo thisTextMatchInfo = entry.getValue();
                            MultipleFieldMatchInfo totalTextMatchInfo = idMatchInfoMap.get(textId);
                            if (totalTextMatchInfo == null) {
                                idMatchInfoMap.put(textId, thisTextMatchInfo);
                            } else {
                                SingleFieldMatchInfo thisSingleFieldMatchInfo = textType == TextType.TITLE ? thisTextMatchInfo.titleMatchInfo : thisTextMatchInfo.contentMatchInfo;
                                SingleFieldMatchInfo totalSingleFieldMatchInfo = textType == TextType.TITLE ? totalTextMatchInfo.titleMatchInfo : totalTextMatchInfo.contentMatchInfo;

                                for (Map.Entry<String, Integer> stringIntegerEntry : thisSingleFieldMatchInfo.hitWordCountMap.entrySet()) {
                                    String hitWord = stringIntegerEntry.getKey();
                                    int thisC = stringIntegerEntry.getValue();
                                    totalSingleFieldMatchInfo.hitWordCountMap.put(hitWord, totalSingleFieldMatchInfo.hitWordCountMap.getOrDefault(hitWord, 0) + thisC);
                                }
                            }
                        }
                    }
                }
            });

            threads.add(thread);
            thread.start();
            mrSt = j;
        }
        return threads;
    }

    private List<SimilaritySearchResult> similaritySearchSingleThread(String text, int topK) throws IOException {

        String gPString = tokenizer.textPreprocess.preprocessToText(text);

//        System.out.println("gPString: " + gPString);

        Map<String, MultipleFieldMatchInfo> idMatchInfoMap = new HashMap<>();

        // 处理内容
        getMatchInfoSingleThread(idMatchInfoMap, gPString, TextType.CONTENT);

        // 处理标题
        getMatchInfoSingleThread(idMatchInfoMap, gPString, TextType.TITLE);

        List<SimilaritySearchResult> results = sort(idMatchInfoMap.values(), topK);

        fillResults(results);

        return results;

    }

    // 整理好后的排序
    private List<SimilaritySearchResult> sort(Collection<MultipleFieldMatchInfo> multipleFieldMatchInfos, int topK) throws IOException {

        List<SimilaritySearchResult> result = new LinkedList<>();


        PriorityQueue<SimilaritySearchResult> priorityQueue = new PriorityQueue<>(new Comparator<SimilaritySearchResult>() {
            @Override
            public int compare(SimilaritySearchResult o1, SimilaritySearchResult o2) {
                return Double.compare(o1.score, o2.score);
            }
        });

        for (MultipleFieldMatchInfo textMatchInfo : multipleFieldMatchInfos) {
            FullText fullText = textMatchInfo.text;
//            System.out.printf("title: %s, init title score: %s, init content score: %s%n",
//                    fullText.titleText.resultText, fullText.contentText.basicTextAvgIdf, fullText.titleText.basicTextAvgIdf);

            CheckUtil.legalDouble(fullText.contentText.avgTf);
            CheckUtil.legalDouble(fullText.contentText.avgIdf);
            CheckUtil.legalDouble(fullText.titleText.avgTf);
            CheckUtil.legalDouble(fullText.titleText.avgIdf);

            double score = bm25FinalScoreCalculator.calc(textMatchInfo.titleMatchInfo.idfs, textMatchInfo.titleMatchInfo.tfs,
                    textMatchInfo.contentMatchInfo.idfs, textMatchInfo.contentMatchInfo.tfs, fullText.articleWeight);


            SimilaritySearchResult similaritySearchResult = new SimilaritySearchResult();

            similaritySearchResult.id = fullText.id;
            similaritySearchResult.score = score;
//            similaritySearchResult.content = fullText.contentText.sourceText;
//            similaritySearchResult.title = fullText.titleText.sourceText;
//            similaritySearchResult.content = readUnitTextFromDisk(fullText.id, TextType.CONTENT).get("srcText").toString();
//            similaritySearchResult.title = readUnitTextFromDisk(fullText.id, TextType.TITLE).get("srcText").toString();

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

    private void waitThreads(List<Thread> threads) {
        // 等待数量统计线程结束
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        threads.clear();
    }

    private List<SimilaritySearchResult> similaritySearchMultipleThread(String text, int topK) throws Exception {
        double textsCount = textsCount();
        int startThreadsNum = (int) Math.ceil(textsCount / multipleThreadSearchMinTextsCount);

//        System.out.printf("threads count: %s%n", startThreadsNum);

        List<SimilaritySearchResult> result = new LinkedList<>();

        String gPString = tokenizer.textPreprocess.preprocessToText(text);

        List<Thread> threads = new ArrayList<>();

        Map<String, MultipleFieldMatchInfo> idMatchInfoMap = new HashMap<>();

//        long getMatchInfoSt = System.currentTimeMillis();

        threads.addAll(getMatchInfoMultipleThread(idMatchInfoMap, gPString, TextType.TITLE, startThreadsNum));
        threads.addAll(getMatchInfoMultipleThread(idMatchInfoMap, gPString, TextType.CONTENT, startThreadsNum));

        // 等待数量统计线程结束
        waitThreads(threads);

//        long getMatchInfoEd = System.currentTimeMillis();

//        System.out.printf("get match info time usage: %ss, match count: %s%n", (getMatchInfoEd - getMatchInfoSt) / 1000.0,
//                idMatchInfoMap.size());

//        long sortSt = System.currentTimeMillis();

        // 相似匹配
        PriorityQueue<SimilaritySearchResult> priorityQueue = new PriorityQueue<>(new Comparator<SimilaritySearchResult>() {
            @Override
            public int compare(SimilaritySearchResult o1, SimilaritySearchResult o2) {
                return Double.compare(o1.score, o2.score);
            }
        });

        List<Exception> exceptions = new ArrayList<>();

        List<MultipleFieldMatchInfo> textMatchInfos = idMatchInfoMap.values().stream().collect(Collectors.toList());
        int textMatchInfoUnit = (int) Math.ceil(textMatchInfos.size() * 1.0 / startThreadsNum);
        int textMatchInfoSt = 0;
        while (textMatchInfoSt < textMatchInfos.size()) {
            int i = textMatchInfoSt;
            int j = textMatchInfoSt + textMatchInfoUnit > textMatchInfos.size() ? textMatchInfos.size() : textMatchInfoSt + textMatchInfoUnit;

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    List<MultipleFieldMatchInfo> subList = textMatchInfos.subList(i, j);
                    postProcessMatchInfo(subList);

                    List<SimilaritySearchResult> thisResult = null;
                    try {
                        thisResult = sort(textMatchInfos.subList(i, j), topK);
                    } catch (IOException e) {
                        exceptions.add(e);
                        thisResult = new ArrayList<>();
                    }


                    synchronized (priorityQueue) {
                        for (int k = thisResult.size() - 1; k >= 0; k--) {
                            SimilaritySearchResult result = thisResult.get(k);
                            if (priorityQueue.size() < topK) {
                                priorityQueue.add(result);
                            } else if (priorityQueue.peek().score < result.score) {
                                priorityQueue.poll();
                                priorityQueue.add(result);
                            }
                        }
                    }
                }
            });
            threads.add(thread);
            thread.start();
            textMatchInfoSt = j;
        }

        // 等待得分计算-排序线程结束
        waitThreads(threads);

//        long sortEd = System.currentTimeMillis();

//        System.out.printf("sort time usage: %ss%n", (sortEd - sortSt) / 1000.0);

        if (!exceptions.isEmpty()) {
            throw new Exception("子线程计算得分+排序出现异常！", exceptions.get(0));
        }

        while (!priorityQueue.isEmpty()) {
            result.add(0, priorityQueue.poll());
        }

        fillResults(result);

        return result;
    }

    public static File save(TextSimilaritySearch textLib, File folder, boolean allowMultipleThreads) throws IOException, InterruptedException {
        int defaultUnitSize = 1000;
        if (!allowMultipleThreads) {
            ContinuousSerialUtil.saveTextSimilaritySearchSingleThread(folder, textLib, defaultUnitSize);
        } else {
            ContinuousSerialUtil.saveTextSimilaritySearchMultipleThreads(folder, textLib, defaultUnitSize);
        }

        return folder;
    }

    public static TextSimilaritySearch load(File folder, boolean allowMultipleThreads) throws Exception {

        if (!allowMultipleThreads) {
            return ContinuousSerialUtil.loadTextSimilaritySearchSingleThread(folder);
        } else {
            return ContinuousSerialUtil.loadTextSimilaritySearchMultipleThreads(folder);
        }
    }

    // 列出所有文章，生成器方法
    public Iterable<ShowText> listAll() {


        return new Iterable<ShowText>() {

            @Override
            public Iterator<ShowText> iterator() {
                return new Iterator<ShowText>() {

                    final HashMap<String, FullText> map = idTextMap;

                    final Iterator<String> iterator = map.keySet().iterator();

                    @Override
                    public boolean hasNext() {

                        return iterator.hasNext();
                    }

                    @Override
                    public ShowText next() {
                        try {
                            return queryById(iterator.next());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }
        };
    }

    public ShowText queryById(String id) throws IOException {
        if (!idTextMap.containsKey(id)) {
            return null;
        }
        ShowText showText = new ShowText();
        showText.id = id;
        fillShowResult(showText);
        return showText;
    }

    public boolean containsText(String id) {
        return idTextMap.containsKey(id);
    }

    public double avgFullTextLength() {
        return (contentWordsCountSum + titleWordsCountSum * 1.0) / textsCount();
    }

    // 生成BasicText的平均IDF
    private void generateBasicTextAvgIdf(BasicText basicText) {
        basicText.avgIdf = 0.0;
        for (String gram : basicText.wordCountMap.keySet()) {
            int count = basicText.wordCountMap.get(gram);
            double idf = wordIdfMap.get(gram);
            basicText.avgIdf += idf * count;
        }
        basicText.avgIdf /= basicText.totalWordsCountRepeat;
    }

    // 计算统计参数
    private void calcStatisticsParams() {

        double titleCriticalIdfSum = 0;
        double titleCriticalTfSum = 0;
        double contentCriticalIdfSum = 0;
        double contentCriticalTfSum = 0;
        double titleWordsSum = 0;
        double contentWordsSum = 0;


        for (FullText fullText : idTextMap.values()) {
            generateBasicTextAvgIdf(fullText.titleText);
            generateBasicTextAvgIdf(fullText.contentText);

            titleCriticalIdfSum += fullText.titleText.avgIdf;
            titleCriticalTfSum += fullText.titleText.avgTf;
            contentCriticalIdfSum += fullText.contentText.avgIdf;
            contentCriticalTfSum += fullText.contentText.avgTf;

            titleWordsSum += fullText.titleText.totalWordsCountRepeat;
            contentWordsSum += fullText.contentText.totalWordsCountRepeat;

        }

        bm25FinalScoreCalculator.titleFieldScoreCalculator.criticalIdf = titleCriticalIdfSum / idTextMap.size();
        bm25FinalScoreCalculator.titleFieldScoreCalculator.criticalTf = titleCriticalTfSum / idTextMap.size();
        bm25FinalScoreCalculator.titleFieldScoreCalculator.criticalWordsCount = titleWordsSum / idTextMap.size();

        bm25FinalScoreCalculator.contentFieldScoreCalculator.criticalIdf = contentCriticalIdfSum / idTextMap.size();
        bm25FinalScoreCalculator.contentFieldScoreCalculator.criticalTf = contentCriticalTfSum / idTextMap.size();
        bm25FinalScoreCalculator.contentFieldScoreCalculator.criticalWordsCount = contentWordsSum / idTextMap.size();

    }


}
