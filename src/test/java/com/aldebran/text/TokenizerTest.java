package test.java.com.aldebran.text;

import com.aldebran.text.tokenizer.DAGTokenizer;
import com.aldebran.text.tokenizer.NGramTokenizer;
import com.aldebran.text.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tokenizer测试
 *
 * @author aldebran
 */
public class TokenizerTest {

    private static String resourceFolderName = "C:\\user_dir\\code\\tokenizer\\train";

    private static List<String> texts = Arrays.asList(
            "中国国家图书馆",
            "图书馆分为总馆南区、总馆北区和古籍馆，总馆南区主楼为双塔形高楼，采用双重檐形式，孔雀蓝琉璃瓦大屋顶，淡乳灰色的瓷砖外墙。",
            "其中古籍文献近200万册，数字资源总量超过1000TB，是亚洲规模最大的图书馆，居世界国家图书馆第三位；"
    );

    public static void main(String[] args) throws Exception {
//        testNGramTokenizer();
        testDAGTokenizer();
    }

    private static List<String> readDocuments() throws IOException {
        List<String> results = new ArrayList<>();
        for (File file : new File(resourceFolderName).listFiles()) {
            if (file.getName().endsWith(".txt")) {
                results.add(FileUtils.readFileString(file));
            }
        }
        return results;
    }


    public static void testNGramTokenizer() throws IOException {
        NGramTokenizer nGramTokenizer = new NGramTokenizer();
        File tokenizerFile = new File("./tokenizer");
        nGramTokenizer.save(tokenizerFile);
        nGramTokenizer = new NGramTokenizer();
        nGramTokenizer.load(tokenizerFile);
        for (String text : texts) {
            System.out.println(nGramTokenizer.tokenize(text));
        }
    }

    public static void testDAGTokenizer() throws Exception {
        DAGTokenizer dagTokenizer = new DAGTokenizer();
        List<String> ds = readDocuments();
        dagTokenizer.train(ds);
//        dagTokenizer.decrease();
        dagTokenizer.update();

        File tokenizerFile = new File("./tokenizer");
        dagTokenizer.save(tokenizerFile);
        dagTokenizer = new DAGTokenizer();
        dagTokenizer.load(tokenizerFile);
        for (String text : texts) {
            System.out.println(text);
            System.out.println(dagTokenizer.tokenize(text));
        }
    }
}
