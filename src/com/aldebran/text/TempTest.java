package com.aldebran.text;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.similarity.SimilaritySearchResult;
import com.aldebran.text.similarity.TextSimilaritySearch;
import com.aldebran.text.util.CheckUtil;

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
//        acTest1();
//        acTest2();
//        acPlusTest();
    }

    // AC基础测试1
    static void acTest1() throws Exception {
        AC ac = new AC();

        // 添加若干词
        ac.addWords(Arrays.asList("12348", "2344", "38"));
        // 更新失配指针
        ac.update();

        // 遍历测试
        ac.traverse(str -> System.out.println(str));

        // 包含测试
        System.out.println(ac.containsWord("12348"));
        System.out.println(ac.containsWord("2344"));
        System.out.println(ac.containsWord("23"));
        System.out.println(ac.containsWord("38"));

        // 转词列表测试
        System.out.println(ac.toWordsList());

        // 匹配测试
        System.out.println(ac.indexOf("0012343382344038"));

        File acLibFile = new File("./test-ac");

        // 导出测试
        AC.save(ac, acLibFile);

        // 导入测试
        AC ac2 = AC.load(acLibFile);

        // 验证ac库是否完全相同，CheckUtil.acEquals仅在测试使用，不要在正式环境上使用
        System.out.println(CheckUtil.acEquals(ac2, ac));

    }

    // AC基础测试2
    static void acTest2() {
        AC ac = new AC();
        ac.addWords(Arrays.asList("ABCABCABDABC", "BD", "CD"));
        ac.update();

        ac.traverse_(str -> System.out.println(str));

        System.out.println(ac.indexOf("00ABCAABDABDABCD"));
    }

    // ACPlus测试，用于处理含有包含关系的词库
    static void acPlusTest() throws Exception {
        AC ac = new AC();
        ac.addWords(Arrays.asList("ABCEAFBABCD", "EA", "FB", "F", "B")); // 词库含有包含关系
        ac.update();

        System.out.println(ac.indexOf("ABCEAFBABCQEA")); // AC不能正确处理，需要使用ACPlus

        AC acPlus = new ACPlus();
        acPlus.addWords(Arrays.asList("ABCEAFBABCD", "EA", "FB", "F", "B"));
        acPlus.update();

        System.out.println(acPlus.indexOf("ABCEAFBABCQEA"));

        File acLibFile = new File("./test-ac");

        ACPlus.save(acPlus, acLibFile);

        AC ac2 = ACPlus.load(acLibFile); // 加载ACPlus库应该用ACPlus.load导入，不能用AC.load

        System.out.println(CheckUtil.acEquals(ac2, acPlus)); // CheckUtil.acEquals仅在测试使用，不要在正式环境上使用
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

        for (SimilaritySearchResult result : textSimilaritySearch.similaritySearch(
                "《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。" +
                        "辛弃疾的《水调歌头》在此之后。", 10)) {
            System.out.printf("title: %s, score: %s, text: %s, id: %s%n", result.title, result.score, result.text, result.id);
        }

        File outFile = TextSimilaritySearch.save(textSimilaritySearch, new File("./test-lib"));

        TextSimilaritySearch textSimilaritySearch2 = TextSimilaritySearch.load(outFile);

        System.out.println(textSimilaritySearch.simpleEquals(textSimilaritySearch2));

//        for (SimilaritySearchResult result : textSimilaritySearch2.similaritySearch(
//                "《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。" +
//                        "辛弃疾的《水调歌头》在此之后。", 10)) {
//            System.out.printf("title: %s, score: %s, text: %s, id: %s%n", result.title, result.score, result.text, result.id);
//        }
    }

}
