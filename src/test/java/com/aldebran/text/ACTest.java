package com.aldebran.text;

import com.aldebran.text.ac.AC;
import com.aldebran.text.ac.ACPlus;
import com.aldebran.text.util.CheckUtil;

import java.io.File;
import java.util.Arrays;

public class ACTest {

    public static void main(String[] args) throws Exception {
//        acTest1();
//        acTest2();
        acPlusTest();
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
        AC.save(ac, acLibFile, false);

        // 导入测试
        AC ac2 = AC.load(acLibFile, false);

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

        ACPlus.save(acPlus, acLibFile, false);

        AC ac2 = ACPlus.load(acLibFile, false); // 加载ACPlus库应该用ACPlus.load导入，不能用AC.load

        System.out.println(CheckUtil.acEquals(ac2, acPlus)); // CheckUtil.acEquals仅在测试使用，不要在正式环境上使用
    }

}
