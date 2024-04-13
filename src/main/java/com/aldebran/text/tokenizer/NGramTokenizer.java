package main.java.com.aldebran.text.tokenizer;

import com.aldebran.text.preprocess.WordProcess;
import com.aldebran.text.util.CheckUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * n-gram分词器 不用于训练模型
 *
 * @author aldebran
 */
public class NGramTokenizer extends Tokenizer {

    public int n;

    private final Pattern englishFullWordPattern = Pattern.compile("^[a-z]+$");

    public NGramTokenizer(int n, List<WordProcess> wordProcesses) {
        super(wordProcesses);
        CheckUtil.Assert(n > 0);
        this.n = n;
    }

    public NGramTokenizer() {
        this(2, null);
    }

    @Override
    public List<String> tokenize(List<String> units) {
        System.out.println("units: " + units);
        List<String> result = new ArrayList<>();
        for (int i = 0; i <= units.size() - n; i++) {
            String item = "";

            String unit = units.get(i);

            if (englishFullWordPattern.matcher(unit).find() && n > 1) { // 英文单词本身会追加
                result.add(unit);
            }

            for (int j = 0; j < n; j++) {
                item += units.get(i + j);
            }
//            CheckUtil.Assert(item.length() >= n); // 英文单词可能多字符，但至少为n
            result.add(item);
        }
        for (int i = Math.max(units.size() - n + 1, 0); i < units.size(); i++) {
            String unit = units.get(i);

            if (englishFullWordPattern.matcher(unit).find() && n > 1) { // 英文单词本身会追加
                result.add(unit);
            }
        }

        return result;
    }

    @Override
    public void save(File file) throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(file);
             BufferedOutputStream bOut = new BufferedOutputStream(fileOut);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bOut);
        ) {
            objectOutputStream.writeObject(this);
        }
    }

    @Override
    public void load(File file) throws IOException {
        try (FileInputStream fileIn = new FileInputStream(file);
             BufferedInputStream bIn = new BufferedInputStream(fileIn);
             ObjectInputStream oIn = new ObjectInputStream(bIn);
        ) {
            try {
                NGramTokenizer readObject = (NGramTokenizer) oIn.readObject();
                this.n = readObject.n;
                this.textPreprocess = readObject.textPreprocess;
            } catch (ClassNotFoundException e) {
                throw new IOException("度对象失败！", e);
            }
        }

    }

    public void update() {

    }
}
