package main.java.com.aldebran.text.score;

import com.aldebran.text.util.CheckUtil;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 单字段 BM25得分计算器
 *
 * @author aldebran
 */
public class BM25FieldScoreCalculator implements Serializable {

    public double criticalHitCount = Double.NaN; // 用户指定：临界的内容命中个数

    public double criticalIdf = Double.NaN; // 自动统计：平均IDF

    public double criticalTf = Double.NaN; // 自动统计：平均TF

    public double criticalWordsCount = Double.NaN; // 自动统计：平均文本长度

    public double bm25K = Double.NaN; // 用户指定：bm25 tf重要性

    public double bm25B = Double.NaN; // 用户指定：bm25 短文本的重要性

    public double itemScoreDiff = Double.NaN; // 用户指定：单项得分区分度

    public double k = Double.NaN; // 用户指定：字段权重

    // 获取该字段得分的默认值
    public double getFieldDefaultValue() {
        return criticalHitCount * (criticalIdf * criticalTf * (bm25K + 1) / (criticalTf + bm25K)) * itemScoreDiff * k;
    }

    // 获取该字段单项词得分的默认值
    public double getItemDefaultValue() {
        return criticalIdf * criticalTf * (bm25K + 1) / (criticalTf + bm25K);
    }

    // BM25
    public double calc(double[] idfs, double[] tfs) {
        CheckUtil.Assert(idfs.length == tfs.length);
        double sum = 0;
        for (int i = 0; i < idfs.length; i++) {
            double idf = idfs[i];
            double tf = tfs[i];
            sum += idf * tf * (bm25K + 1) / (tf + bm25K * (1 - bm25B + bm25B * idfs.length / criticalWordsCount));
        }
        sum *= itemScoreDiff * k;
        return sum;
    }

    public void tryAssert() {
        for (Double v : Arrays.asList(criticalHitCount, criticalIdf, criticalTf, criticalWordsCount, bm25K, itemScoreDiff, k)) {
            CheckUtil.legalDouble(v);
        }
    }

}
