package com.aldebran.text.similarity;

import com.aldebran.text.util.MMath;

import java.io.Serializable;

/**
 * 得分计算器
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class ScoreCalculator implements Serializable {

    public double a;
    public double b;

    public void update(double criticalContentHitCount,
                       double criticalTitleHitCount,
                       double criticalScore,
                       double textAvgIdf,
                       double basicGrowthValue,
                       double contentK,
                       double titleK,
                       double gramsAvgCount,
                       double gramsCountLogA,
                       double idfGrowthK) {
        this.b = -1;


        // 标题得分期望

        double titleScoreExpect = textAvgIdf + criticalTitleHitCount * basicGrowthValue * idfGrowthK;


        System.out.println("titleScoreExpect: " + titleScoreExpect);

        // 内容得分期望
        double contentScoreExpect = textAvgIdf + criticalContentHitCount * basicGrowthValue * idfGrowthK;

        System.out.println("contentScoreExpect: " + contentScoreExpect);

        double finalScoreExpect = (contentK * contentScoreExpect + titleK * titleScoreExpect) / (contentK + titleK);

        // 除去平均长度的log值

        finalScoreExpect /= MMath.log(gramsCountLogA, gramsAvgCount + gramsCountLogA);

        System.out.println("finalScoreExpect: " + finalScoreExpect);


        this.a = (1.0 / (criticalScore - 1) - b) / finalScoreExpect;

    }


    public double score(double sum) {
        return 1.0 / (a * sum + b) + 1;
    }


}
