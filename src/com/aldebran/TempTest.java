package com.aldebran;

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

    static void regexTest4() {

        String regex = "[·|\\u3002|\\uff1f|\\uff01|\\uff0c|\\u3001|\\uff1b|\\uff1a|\\u201c|\\u201d|\\u2018|\\u2019|\\uff08|\\uff09|\\u300a|\\u300b|\\u3008|\\u3009|\\u3010|\\u3011|\\u300e|\\u300f|\\u300c|\\u300d|\\ufe43|\\ufe44|\\u3014|\\u3015|\\u2026|\\u2014|\\uff5e|\\ufe4f|\\uffe5|\\uff0d \\uff3f|\\u002d]|\\p{Punct}";

        ReplaceInfo replaceInfo = new ReplaceInfo("",
                Pattern.compile(regex),
                Pattern.compile(regex));

        System.out.println(replaceInfo.replace("!！。.你好Hello<《（("));
    }

    static void textProcess5() {
        System.out.println(TextSimilaritySearch.textProcess("《蝶恋花·答李淑一》" +
                "伊凡一世 thumb|right|伊凡一世 伊凡一世·丹尼洛维奇（钱袋）（，），是莫斯科大公（约1325年－1340年3月31日在位），" +
                "亚历山大·涅夫斯基幼子丹尼尔·亚历山德罗维奇之子" +
                "水调歌头 水调歌头，词牌名。亦称《花犯念奴》、《元会曲》。此调是截取《水调歌》大曲开头一章的创新之作。"));
    }

    static void gramUnits6() {
        Text text = TextSimilaritySearch.textProcess("伊凡一世 thumb|right|伊凡一世  莫斯科大公（约1325年－1340年3月31日在位）");

        System.out.println(TextSimilaritySearch.textToGramUnits(text));

        System.out.println(TextSimilaritySearch.nGram(text, 2));
    }

    static void tryTextSimilaritySearch() {

        TextSimilaritySearch textSimilaritySearch = new TextSimilaritySearch("test", 3, 2);

        textSimilaritySearch.addText("伊凡一世 thumb|right|伊凡一世  莫斯科大公（约1325年－1340年3月31日在位）", "", "1");

        textSimilaritySearch.addText("水调歌头 水调歌头，词牌名。亦称《花犯念奴》、《元会曲》。", "", "2");

        System.out.println(textSimilaritySearch.queryById("1"));

        textSimilaritySearch.update();

        System.out.println(textSimilaritySearch.similaritySearch("伊凡一世词牌名", 10));
    }

}
