package test.java.com.aldebran.text;

import com.aldebran.text.preprocess.EnglishRootProcess;
import com.aldebran.text.preprocess.WordParentProcess;
import com.aldebran.text.preprocess.WordSimilarityProcess;

import java.io.File;
import java.io.IOException;

/**
 * 词语处理器测试
 *
 * @author aldebran
 */
public class WordProcessTest {

    public static void main(String[] args) throws Exception {
//        testWordParentProcess();
        testWordSimilarityProcess();
//        testEnglishRootProcess();
    }


    private static void testWordParentProcess() throws Exception {
        WordParentProcess wordParentProcess = new WordParentProcess(new File("./word_parent.json"));
        System.out.println(wordParentProcess.getAppendWords("豹子"));
    }

    private static void testWordSimilarityProcess() throws Exception {
        File testWordProcessFile = new File("./word_process");
        WordSimilarityProcess wordSimilarityProcess = new WordSimilarityProcess(new File("./word_similarity.json"));
        wordSimilarityProcess.save(testWordProcessFile);
        wordSimilarityProcess = new WordSimilarityProcess();
        wordSimilarityProcess.load(testWordProcessFile);
        System.out.println(wordSimilarityProcess.getAppendWords("高兴"));
    }

    private static void testEnglishRootProcess() throws Exception {
        EnglishRootProcess englishRootProcess = new EnglishRootProcess(new File("./english_root.txt"));
        System.out.println(englishRootProcess.getAppendWords("knowable"));
        System.out.println(englishRootProcess.getAppendWords("population"));
    }

}
