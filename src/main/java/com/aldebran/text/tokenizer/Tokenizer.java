package main.java.com.aldebran.text.tokenizer;

import com.aldebran.text.preprocess.TextPreprocess;
import com.aldebran.text.preprocess.WordProcess;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * tokenizer接口定义 不用于训练模型
 *
 * @author aldebran
 */
public abstract class Tokenizer implements Serializable {

    public TextPreprocess textPreprocess = new TextPreprocess();

    public transient List<WordProcess> wordProcesses;

    public Tokenizer(List<WordProcess> wordProcesses) {
        this.wordProcesses = wordProcesses;
    }

    public Tokenizer() {

    }

    protected abstract List<String> tokenize(List<String> units); // 单句分词算法

    public List<String> tokenize(String string) { // 多句分词算法
        List<String> result = new ArrayList<>();
        for (List<String> sentenceUnits : textPreprocess.preprocess(string)) {
//            System.out.println(sentenceUnits);
            result.addAll(tokenize(sentenceUnits));
        }
        if (wordProcesses != null) {
            for (WordProcess wordProcess : wordProcesses) {
                List<String> result2 = new ArrayList<>();
                for (String word : result) {
                    result2.add(word);
                    result2.addAll(wordProcess.getAppendWords(word));
                }
                result = result2;
            }
        }

        return result;
    }


    public abstract void save(File file) throws IOException, InterruptedException; // 保存Tokenizer

    public abstract void load(File file) throws IOException, ClassNotFoundException, InterruptedException; // 加载Tokenizer

    public abstract void update(); // 我认为，Tokenizer内部状态发生变化后，更新后才能使用
}
