package com.aldebran.text.tokenizer;

import com.aldebran.text.util.CheckUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * n-gram分词器 不用于训练模型
 *
 * @author aldebran
 */
public class NGramTokenizer extends Tokenizer {


    public int n;

    public NGramTokenizer(int n) {
        CheckUtil.Assert(n > 0);
        this.n = n;
    }

    @Override
    public List<String> tokenize(List<String> units) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i <= units.size() - n; i++) {
            String item = "";
            for (int j = 0; j < n; j++) {
                item += units.get(i + j);
            }
            result.add(item);
        }
        return result;
    }
}
