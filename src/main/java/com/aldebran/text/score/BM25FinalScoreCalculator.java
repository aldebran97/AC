package com.aldebran.text.score;

import com.aldebran.text.util.CheckUtil;

import java.io.Serializable;
import java.util.Arrays;

/**
 * BM25算法最终得分计算器
 *
 * @author aldebran
 */
public class BM25FinalScoreCalculator implements Serializable {

    public BM25FieldScoreCalculator titleFieldScoreCalculator = new BM25FieldScoreCalculator(); // 标题得分计算器

    public BM25FieldScoreCalculator contentFieldScoreCalculator = new BM25FieldScoreCalculator(); // 内容得分计算器

    public double contentTitleItemScoreRate = Double.NaN;

    public double criticalScore = Double.NaN;

    private double a = Double.NaN;
    private double b = Double.NaN;

    public void update() {
        this.b = -1;

        contentTitleItemScoreRate = contentFieldScoreCalculator.getItemDefaultValue() / titleFieldScoreCalculator.getItemDefaultValue();

        double finalScoreExpect = (contentFieldScoreCalculator.getItemDefaultValue() + titleFieldScoreCalculator.getFieldDefaultValue() * contentTitleItemScoreRate)
                / (contentFieldScoreCalculator.k + titleFieldScoreCalculator.k);

//        System.out.println("finalScoreExpect: " + finalScoreExpect);

        this.a = (1.0 / (criticalScore - 1) - b) / finalScoreExpect;

        tryAssert();

    }


    private double score(double sum) {
        return 1.0 / (a * sum + b) + 1;
    }

    public double calc(double[] titleIdfs, double[] titleTfs, double[] contentIdfs, double[] contentTfs) {
        return score((contentFieldScoreCalculator.calc(contentIdfs, contentTfs)
                + titleFieldScoreCalculator.calc(titleIdfs, titleTfs) * contentTitleItemScoreRate)
                / (contentFieldScoreCalculator.k + titleFieldScoreCalculator.k));
    }

    public void tryAssert() {
        titleFieldScoreCalculator.tryAssert();
        contentFieldScoreCalculator.tryAssert();
        for (Double v : Arrays.asList(contentTitleItemScoreRate, criticalScore, a, b)) {
            CheckUtil.legalDouble(v);
        }
    }

}
