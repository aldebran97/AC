package com.aldebran.text.similarity;

/**
 * 统计结果
 *
 * @author aldebran
 * @since 2023-07-24
 */
public class Statistics {
    public double avgOriginTextLength;

    public long totalWordsCount;

    public int textsCount;

    @Override
    public String toString() {
        return "Statistics{" +
                "avgOriginTextLength=" + avgOriginTextLength +
                ", totalWordsCount=" + totalWordsCount +
                ", textsCount=" + textsCount +
                '}';
    }
}
