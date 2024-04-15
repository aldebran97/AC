package com.aldebran.text.tokenizer;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.preprocess.WordProcess;
import com.aldebran.text.util.ContinuousSerialUtil;
import com.aldebran.text.util.FileUtils;
import com.aldebran.text.util.StatisticsUtil;
import com.aldebran.text.util.CheckUtil;

import java.io.*;
import java.util.*;

/**
 * 有向无环图分词器 不用于训练模型
 * 但它需要在大量文本上做统计（这里也称为训练了）
 *
 * @author aldebran
 */
public class DAGTokenizer extends Tokenizer {

    public List<Integer> gramsLengths; // n-gram中n的大小，支持多种长度

    private int maxGramLength = Integer.MIN_VALUE;

    private transient final List<NGramTokenizer> nGramTokenizers = new ArrayList<>();

    public int decreaseInterval; // 每间隔多少句子，缩减一次词表。采用固定的缩减策略，仅保留TF-IDF不小于平均值的gram

    public int documentsCount; // 文章个数

    private transient Map<String, Integer> gramCountMap = new HashMap<>();

//    private final Map<String, Integer> gramDocumentsCountMap = new HashMap<>();

    private double tfK;

    private double lengthK;

    private transient Map<String, Double> gramTfMap = new HashMap<>();

    private WordScoreStatistics wordScoreStatistics;

    private transient ACPlus acPlus;

    private boolean updateScore;

    private boolean updateAC;

    public DAGTokenizer(List<Integer> gramsLengths, int decreaseInterval, File vocabFile, List<WordProcess> wordProcesses) {
        super(wordProcesses);
        this.gramsLengths = gramsLengths;
        this.decreaseInterval = decreaseInterval;
        initNGramTokenizers();
    }

    private void initNGramTokenizers() {
        nGramTokenizers.clear();
        for (int n : gramsLengths) {
            if (n > maxGramLength) {
                maxGramLength = n;
            }
            NGramTokenizer nGramTokenizer = new NGramTokenizer(n, null);
            nGramTokenizer.textPreprocess = this.textPreprocess;
            nGramTokenizers.add(nGramTokenizer);
        }
    }

    public DAGTokenizer() {
        this(Arrays.asList(2, 3, 4), -1, null, null);
    }


    public void train(String text) {
        train(Arrays.asList(text));
    }

    // 词语统计
    public void train(List<String> texts) {
        updateScore = false;
        updateAC = false;
        for (String text : texts) {
            for (NGramTokenizer nGramTokenizer : nGramTokenizers) {
                List<String> grams = nGramTokenizer.tokenize(text);
                for (String gram : grams) {
                    int c = gramCountMap.getOrDefault(gram, 0);
                    gramCountMap.put(gram, c + 1);
                }
//                for (String gram : new HashSet<>(grams)) {
//                    int c = gramDocumentsCountMap.getOrDefault(gram, 0);
//                    gramDocumentsCountMap.put(gram, c + 1);
//                }
            }
            documentsCount += 1;
            if (documentsCount > 0 && decreaseInterval > 0 && documentsCount % decreaseInterval == 0) {
                decrease();
            }
        }
    }

    // 减小词表
    public void decrease() {
        updateScore();
        for (WordScore wordScore : wordScoreStatistics.wordScores) {
            if (wordScore.score < wordScoreStatistics.basicStatistics.arithmeticAverage) {
                removeGram(wordScore.gram);
            }
        }
        updateScore = false;
        updateAC = false;
    }

    private void removeGram(String gram) {
        gramCountMap.remove(gram);
//        gramDocumentsCountMap.remove(gram);
        gramTfMap.remove(gram);
    }

    // 计算gram得分并排序
    public void updateScore() {
        if (updateScore) return;
        gramTfMap.clear();
        wordScoreStatistics = new WordScoreStatistics();
//        int N = documentsCount;
        double[] tfs = new double[gramCountMap.size()];
        double[] lengths = new double[gramCountMap.size()];
//        double[] idfs = new double[gramCountMap.size()];
        int i = 0;
        List<String> grams = new ArrayList<>(gramCountMap.keySet());
        for (String gram : grams) {
//            int nt = gramDocumentsCountMap.get(gram);
//            double idf = Math.log1p(N * 1.0 / nt);
//            if (StatisticsUtil.closeTo(idf, 0)) {
//                idf = 0.0001;
//            }
            double tf = gramCountMap.get(gram) * 1.0; // 后面要归一化
//            idfs[i] = idf;
            tfs[i] = tf;
            lengths[i] = gram.length() * 1.0 / maxGramLength;
            i++;
        }
//        idfArray = StatisticsUtil.maxMinNormalization(idfArray);
        tfs = StatisticsUtil.maxMinNormalization(tfs);
        lengths = StatisticsUtil.maxMinNormalization(lengths);

        StatisticsUtil.BasicStatistics tfBasicStatistics = StatisticsUtil.basicStatistics(tfs);
        StatisticsUtil.BasicStatistics lengthBasicStatistics = StatisticsUtil.basicStatistics(lengths);

        this.lengthK = 1;
        this.tfK = lengthBasicStatistics.arithmeticAverage / tfBasicStatistics.arithmeticAverage;
        System.out.printf("recommend tf_k : length_k = %s%n", this.tfK);
        System.out.printf("real tf_k : length_k = %s%n", this.tfK / this.lengthK);

        for (i = 0; i < grams.size(); i++) {
            String gram = grams.get(i);
            double tf = tfs[i];
            wordScoreStatistics.wordScores.add(new WordScore(gram, tf));
            gramTfMap.put(gram, tf);
        }

        wordScoreStatistics.basicStatistics = StatisticsUtil.basicStatistics(tfs);

        updateScore = true;
    }

    // 更新AC自动机
    public void updateAC() {
        if (updateAC) return;
        acPlus = new ACPlus();
        acPlus.addWords(new ArrayList<>(gramCountMap.keySet()));
        acPlus.update();
        updateAC = true;
    }

    public void update() {
        updateScore();
        updateAC();
    }

    @Override
    public List<String> tokenize(List<String> units) {
//        System.out.println(units);
        String sentence = String.join("", units);
//        System.out.println("sentence: " + sentence);
        int n = sentence.length() + 1;
//        System.out.println("matrix size: " + n + 1);
        Link[][] adjacentMatrix = new Link[n + 1][n + 1]; // 假设分隔段不长，采用邻接矩阵方式存储，空间复杂度O(n^2)
        // 构造图
        for (int i = 0; i < sentence.length(); i++) {
            String unit = sentence.substring(i, i + 1);
//            System.out.println("unit: " + unit);
            double tf = wordScoreStatistics.basicStatistics.arithmeticAverage;
            double score = -(lengthK / maxGramLength + tfK * tf) / (lengthK + tfK);
            adjacentMatrix[i][i + 1] = new Link(unit, score);
        }

        for (AC.MatchResult mr : acPlus.indexOf(sentence)) {
            double tf = gramTfMap.get(mr.word);
            double score = -(lengthK * mr.word.length() * 1.0 / maxGramLength + tfK * tf) / (lengthK + tfK);

//            System.out.println(mr);
            adjacentMatrix[mr.index][mr.index + mr.word.length()] = new Link(mr.word, score);
        }
//        System.out.println(Arrays.deepToString(adjacentMatrix));
        // Djikstra算法求最短路径
        boolean[] visit = new boolean[n];
        double[] minD = new double[n];
        for (int i = 0; i < minD.length; i++) {
            minD[i] = Double.MAX_VALUE;
        }
        // 源点ID=0
        minD[0] = 0;
        int minIndex = 0;
        int[] prior = new int[n];
        prior[0] = -1;

        while (minIndex >= 0) {
            visit[minIndex] = true;
            // 更新最小距离
            for (int j = 0; j < adjacentMatrix[minIndex].length; j++) {
                Link link = adjacentMatrix[minIndex][j];
                if (link != null) {
                    if (link.weight + minD[minIndex] < minD[j]) {
                        minD[j] = link.weight + minD[minIndex];
                        prior[j] = minIndex;
                    }
                }
            }
            // 选出最小距离点
            minIndex = -1;
            Double minDistance = Double.MAX_VALUE;
            for (int i = 0; i < minD.length; i++) {
                double d = minD[i];
                if (!visit[i] && d < minDistance) {
                    minDistance = d;
                    minIndex = i;
                }
            }
        }
//        System.out.println(Arrays.toString(minD));
//        System.out.println(Arrays.toString(prior));
        List<String> result = new LinkedList<>();
        int last = n - 1;
        while (last > 0) {
            int p = prior[last];
            Link link = adjacentMatrix[p][last];
            result.add(0, link.gram);
            CheckUtil.Assert(!link.gram.isEmpty());
            last = p;
        }

        return result;
    }

    @Override
    public void save(File file) throws IOException, InterruptedException {
        int defaultUnitSize = 1000;
        FileUtils.createFolder(file.getAbsolutePath());
        File objectFile = new File(file, "object");
        try (FileOutputStream fileOut = new FileOutputStream(objectFile);
             BufferedOutputStream bOut = new BufferedOutputStream(fileOut);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bOut);
        ) {
            objectOutputStream.writeObject(this);
        }
        File gramCountMapFile = new File(file, "gramCountMap");
        try (FileOutputStream fileOut = new FileOutputStream(gramCountMapFile);
             BufferedOutputStream bOut = new BufferedOutputStream(fileOut);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bOut);
        ) {
            ContinuousSerialUtil.saveHashMap(objectOutputStream, (HashMap) gramCountMap, defaultUnitSize);
        }
        File gramTfMapFile = new File(file, "gramTfMapFile");
        try (FileOutputStream fileOut = new FileOutputStream(gramTfMapFile);
             BufferedOutputStream bOut = new BufferedOutputStream(fileOut);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bOut);
        ) {
            ContinuousSerialUtil.saveHashMap(objectOutputStream, (HashMap) gramTfMap, defaultUnitSize);
        }

        File acPlusFile = new File(file, "acPlus");
        ACPlus.save(acPlus, acPlusFile, true);
    }

    @Override
    public void load(File file) throws IOException, ClassNotFoundException, InterruptedException {
        int defaultUnitSize = 1000;
        FileUtils.createFolder(file.getAbsolutePath());
        File objectFile = new File(file, "object");
        try (FileInputStream fileIn = new FileInputStream(objectFile);
             BufferedInputStream bIn = new BufferedInputStream(fileIn);
             ObjectInputStream oIn = new ObjectInputStream(bIn);
        ) {
            DAGTokenizer dagTokenizer = (DAGTokenizer) oIn.readObject();
            this.tfK = dagTokenizer.tfK;
            this.lengthK = dagTokenizer.lengthK;
            this.textPreprocess = dagTokenizer.textPreprocess;
            this.maxGramLength = dagTokenizer.maxGramLength;
            this.decreaseInterval = dagTokenizer.decreaseInterval;
            this.wordScoreStatistics = dagTokenizer.wordScoreStatistics;
            this.updateAC = dagTokenizer.updateAC;
            this.updateScore = dagTokenizer.updateScore;
            this.documentsCount = dagTokenizer.documentsCount;
        }
        File gramCountMapFile = new File(file, "gramCountMap");
        try (FileInputStream fileIn = new FileInputStream(gramCountMapFile);
             BufferedInputStream bIn = new BufferedInputStream(fileIn);
             ObjectInputStream oIn = new ObjectInputStream(bIn);
        ) {
            this.gramCountMap = ContinuousSerialUtil.readHashMap(oIn);
        }
        File gramTfMapFile = new File(file, "gramTfMapFile");
        try (FileInputStream fileIn = new FileInputStream(gramTfMapFile);
             BufferedInputStream bIn = new BufferedInputStream(fileIn);
             ObjectInputStream oIn = new ObjectInputStream(bIn);
        ) {
            this.gramTfMap = ContinuousSerialUtil.readHashMap(oIn);
        }
        File acPlusFile = new File(file, "acPlus");
        this.acPlus = ACPlus.load(acPlusFile, true);
        initNGramTokenizers();
    }
}

class WordScore implements Serializable {
    String gram;

    double score; // gram 归一化得分

    public WordScore(String gram, double score) {
        this.gram = gram;
        this.score = score;
    }

    @Override
    public String toString() {
        return "WordScore{" +
                "gram='" + gram + '\'' +
                ", score=" + score +
                '}';
    }
}

// 词语得分统计
class WordScoreStatistics implements Serializable {

    public List<WordScore> wordScores = new ArrayList<>();

    public StatisticsUtil.BasicStatistics basicStatistics;

}

// 边的定义
class Link {

    public String gram;

    public double weight;

    public Link(String gram, double weight) {
        this.gram = gram;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Link{" +
                "gram='" + gram + '\'' +
                ", weight=" + weight +
                '}';
    }
}