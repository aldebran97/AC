package com.aldebran.text.preprocess;

import java.io.File;
import java.io.IOException;

/**
 * 词语 相似词语 追加器
 *
 * @author aldebran
 */
public class WordSimilarityProcess extends WordParentProcess {
    public WordSimilarityProcess(File file) throws IOException {
        super(file);
    }

    public WordSimilarityProcess() {

    }
}
