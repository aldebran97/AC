package com.aldebran.text.tokenizer;

import com.aldebran.text.preprocess.TextPreprocess;

import java.io.Serializable;
import java.util.List;

/**
 * tokenizer接口定义 不用于训练模型
 *
 * @author aldebran
 */
public abstract class Tokenizer implements Serializable {

    public TextPreprocess textPreprocess = new TextPreprocess();

    public abstract List<String> tokenize(List<String> units);

    public List<String> tokenize(String string, Object... args) {
        return tokenize(textPreprocess.textToUnits(textPreprocess.preprocess(string)));
    }
}
