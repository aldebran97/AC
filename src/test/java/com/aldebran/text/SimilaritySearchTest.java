package com.aldebran.text;

import com.aldebran.text.similarity.SimilaritySearchResult;
import com.aldebran.text.similarity.TextSimilaritySearch;
import com.aldebran.text.tokenizer.NGramTokenizer;

import java.io.File;

/**
 * 相似检索测试
 *
 * @author aldebran
 * @since 2023-01-30
 */
public class SimilaritySearchTest {

    public static void main(String[] args) throws Exception {
        tryTextSimilaritySearch();
    }


    static String text1 = "《梦游天姥吟留别》是唐代大诗人李白的诗作。这是一首记梦诗，也是一首游仙诗。此诗以记梦为由，抒写了对光明、自由的渴求，对黑暗现实的不满，表现了诗人蔑视权贵、不卑不屈的叛逆精神。";

    static String title1 = "《梦游天姥吟留别》";

    static String text2 = "《水调歌头·文字觑天巧》是南宋诗人辛弃疾创作的一首词。上片写李子永家亭榭风流华美，有浓郁的田园风味，但不能因此不忧虑世事。";

    static String title2 = "《水调歌头·文字觑天巧》";

    static String text3 = "伊凡一世富于谋略，为达到自己的目的不择手段，狡猾而残忍。他利用莫斯科优越的地理优势，利用以往积累的财力贿赂金帐汗国统治阶层，又站在对清算封建分裂势力有利的教会一方，抑制以特维尔王公为首的莫斯科邻近各公国。";

    static String title3 = "伊凡一世";

    static void tryTextSimilaritySearch() throws Exception {

        File libFolder = new File("./test-lib");

        TextSimilaritySearch textSimilaritySearch = new TextSimilaritySearch(
                3,
                3,
                0.5,
                1,
                3,
                2,
                0.1,
                10,
                new NGramTokenizer(2, null),
                "test",
                libFolder);

        textSimilaritySearch.addText(text1, title1, "1", 1);

        textSimilaritySearch.addText(text2, title2, "2", 1);

        textSimilaritySearch.addText(text3, title3, "3", 1);

        textSimilaritySearch.allowMultiThreadsSearch = false;

        System.out.println(textSimilaritySearch.queryById("1"));

        textSimilaritySearch.update();

        for (SimilaritySearchResult result : textSimilaritySearch.similaritySearch(
                "《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。" +
                        "辛弃疾的《水调歌头》在此之后。", 10)) {
            System.out.printf("title: %s, score: %s, text: %s, id: %s%n", result.title, result.score, result.content, result.id);
        }

        TextSimilaritySearch.save(textSimilaritySearch, textSimilaritySearch.libFolder, true);

        TextSimilaritySearch textSimilaritySearch2 = TextSimilaritySearch.load(libFolder, true);

        textSimilaritySearch2.tokenizer = new NGramTokenizer(2, null);

        for (SimilaritySearchResult result : textSimilaritySearch2.similaritySearch(
                "《梦游天姥吟留别》作于李白出翰林之后。唐玄宗天宝三载（744），李白在长安受到权贵的排挤，被放出京，返回东鲁（在今山东）家园。" +
                        "辛弃疾的《水调歌头》在此之后。", 10)) {
            System.out.printf("title: %s, score: %s, text: %s, id: %s%n", result.title, result.score, result.content, result.id);
        }


    }

}

