package com.aldebran.text.similarity;

import java.io.Serializable;
import java.util.Map;

/**
 * 最小文章单元
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class BasicText implements Serializable {

    public String sourceText;

    public String resultText;

    public Map<String, Integer> gramCountMap;

    public int totalGramsCount;

    public double basicTextAvgIdf;

    @Override
    public String toString() {
        return "BasicText{" +
                "sourceText='" + sourceText + '\'' +
                ", resultText='" + resultText + '\'' +
                ", gramCountMap=" + gramCountMap +
                ", totalGramsCount=" + totalGramsCount +
                '}';
    }
}
