package main.java.com.aldebran.text.text;

import java.io.Serializable;
import java.util.Map;

/**
 * 单字段文本
 *
 * @author aldebran
 * @since 2023-07-15
 */
public class BasicText implements Serializable {

    public String sourceText;

    public String resultText;

    public Map<String, Integer> wordCountMap;

    public int totalWordsCountRepeat;

    public double avgIdf = Double.NaN;

    public double avgTf = Double.NaN;

    public double getTf(String word) {
        return wordCountMap.get(word) * 1.0 / totalWordsCountRepeat;
    }


    @Override
    public String toString() {
        return "BasicText{" +
                "sourceText='" + sourceText + '\'' +
                ", resultText='" + resultText + '\'' +
                ", wordCountMap=" + wordCountMap +
                ", totalWordsCountRepeat=" + totalWordsCountRepeat +
                ", avgIdf=" + avgIdf +
                ", avgTf=" + avgTf +
                '}';
    }
}
