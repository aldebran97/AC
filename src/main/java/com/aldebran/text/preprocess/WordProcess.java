package main.java.com.aldebran.text.preprocess;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * 词语处理，获取应该在处理中追加的词语
 *
 * @author aldebran
 */
public interface WordProcess extends Serializable {

    List<String> getAppendWords(String word);

    void save(File file) throws IOException, InterruptedException;

    void load(File file) throws IOException, ClassNotFoundException, InterruptedException;

}
