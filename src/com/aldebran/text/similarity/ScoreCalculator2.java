package com.aldebran.text.similarity;

import java.io.Serializable;

/**
 * 得分计算器2
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class ScoreCalculator2 implements Serializable {

    double criticalContentHitCount;

    double criticalTitleHitCount;

    double criticalScore;

    double contentGrowRate;

    double titleGrowthRate;
    double avgIdf;


    public void update(double criticalContentHitCount,
                       double criticalTitleHitCount,
                       double criticalScore,
                       double contentGrowRate,
                       double titleGrowthRate,
                       double avgIdf) {
        this.criticalContentHitCount = criticalContentHitCount;
        this.criticalTitleHitCount = criticalTitleHitCount;
        this.criticalScore = criticalScore;
        this.contentGrowRate = contentGrowRate;
        this.titleGrowthRate = titleGrowthRate;
        this.avgIdf = avgIdf;
    }

    public double score(double sum, double titleGramsCount, double contentGramsCount) {
        double b = -1;


        // 标题得分期望
        double titleGramGrowP = (criticalTitleHitCount + 1) / (titleGramsCount + 1);

        if (titleGramGrowP > 1) {
            titleGramGrowP = 1;
        }

        double titleScoreExpect = titleGramGrowP * titleGrowthRate * avgIdf + (1 - titleGramGrowP) * avgIdf;

//        System.out.println("titleScoreExpect: " + titleScoreExpect);

        // 内容得分期望
        double contentGramGrowP = (criticalContentHitCount + 1) / (contentGramsCount + 1);

        if (contentGramGrowP > 1) {
            contentGramGrowP = 1;
        }

        double contentScoreExpect = contentGramGrowP * contentGrowRate * avgIdf + (1 - contentGramGrowP) * avgIdf;

//        System.out.println("contentScoreExpect: " + contentScoreExpect);

        double a = (1.0 / (criticalScore - 1) - b) / ((titleScoreExpect + contentScoreExpect) / 2);
        return 1.0 / (a * sum + b) + 1;
    }
}
