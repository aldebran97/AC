package com.aldebran.text;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.similarity.TextSimilaritySearch;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * 相关测试
 *
 * @author aldebran
 * @since 2023-01-30
 */
public class TempTest {

    public static void main(String[] args) throws Exception {
        tryTextSimilaritySearch();
//        trySave();
    }

    static void simpleTest1() {
        AC trieTree = new AC();
        trieTree.addWords(Arrays.asList("12348", "2344", "38"));
        trieTree.update();

        trieTree.traverse_(str -> System.out.println(str));

        System.out.println(trieTree.containsWord("12348"));
        System.out.println(trieTree.containsWord("2344"));
        System.out.println(trieTree.containsWord("23"));
        System.out.println(trieTree.containsWord("38"));
        System.out.println(trieTree.toWordsList());


        System.out.println(trieTree.indexOf("0012343382344038"));
    }

    static void simpleTest2() {
        AC trieTree = new AC();
        trieTree.addWords(Arrays.asList("ABCABCABDABC", "BD", "CD"));
        trieTree.update();

        trieTree.traverse_(str -> System.out.println(str));

        System.out.println(trieTree.indexOf("00ABCAABDABDABCD"));
    }

    static void simpleTest3() {
        AC trieTree = new AC();
        trieTree.addWords(Arrays.asList("ABCEAFBABCD", "EA", "FB", "F", "B"));
        trieTree.update();

        System.out.println(trieTree.indexOf("ABCEAFBABCQEA"));

        trieTree = new ACPlus();
        trieTree.addWords(Arrays.asList("ABCEAFBABCD", "EA", "FB", "F", "B"));
        trieTree.update();

        System.out.println(trieTree.indexOf("ABCEAFBABCQEA"));
    }


    static String text1 = "《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。";

    static String title1 = "《梦游天姥吟留别》";

    static String text2 = "《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。";

    static String title2 = "《水调歌头·文字觑天巧》";

    static String text3 = "伊凡一世富于谋略，为达到自己的目的不择手段，狡猾而残忍。他利用莫斯科优越的地理优势，利用以往积累的财力贿赂金帐汗国统治阶层，又站在对清算封建分裂势力有利的教会一方，抑制以特维尔王公为首的莫斯科邻近各公国。";

    static String title3 = "伊凡一世";

    static void tryTextSimilaritySearch() throws Exception {

        TextSimilaritySearch textSimilaritySearch = new TextSimilaritySearch(
                3,
                3,
                0.5,
                1,
                2,
                2,
                200,
                10,
                2,
                "test");
        textSimilaritySearch.textPreprocess.loadReplaceMapFromFile("./replace.txt");

        textSimilaritySearch.addText(text1, title1, "1", 1);

        textSimilaritySearch.addText(text2, title2, "2", 1);

        textSimilaritySearch.addText(text3, title3, "3", 1);

//        System.out.println(textSimilaritySearch.queryById("1"));

        textSimilaritySearch.update();

        System.out.println(textSimilaritySearch.similaritySearch(
                "《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。" +
                        "辛弃疾的《水调歌头》在此之后。", 10));
    }

    static void trySave() throws Exception {

        TextSimilaritySearch textSimilaritySearch = new TextSimilaritySearch(
                3,
                3,
                0.5,
                1,
                2,
                2,
                200,
                10,
                2,
                "test");

        textSimilaritySearch.textPreprocess.loadReplaceMapFromFile("./replace.txt");

        textSimilaritySearch.addText(text1, title1, "1", 1);

        textSimilaritySearch.addText(text2, title2, "2", 1);

        textSimilaritySearch.addText(text3, title3, "3", 1);

        textSimilaritySearch.update();

        File outFile = TextSimilaritySearch.save(textSimilaritySearch, new File("./test-lib"));

        System.out.println(outFile.getAbsolutePath());

        textSimilaritySearch = TextSimilaritySearch.load(outFile);

        System.out.println(textSimilaritySearch.queryById("1"));

        System.out.println(textSimilaritySearch.similaritySearch(
                "《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。" +
                        "辛弃疾的《水调歌头》在此之后。", 10));
    }

}
