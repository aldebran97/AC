package com.aldebran.text.tokenizer;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 有向无环图分词器 不用于训练模型
 *
 * @author aldebran
 */
public class DAGTokenizer extends Tokenizer implements Serializable {

    public void addText(String text) {
        addTexts(Arrays.asList(text));
    }

    public void addTexts(List<String> texts) {

    }

    public static DAGTokenizer load(File file) {
        return null;
    }

    public static void save(DAGTokenizer dagTokenizer) {

    }

    @Override
    public List<String> tokenize(List<String> units) {
        return null;
    }
}
