package com.aldebran.text.util;

import com.aldebran.text.ac.AC;
import com.aldebran.text.similarity.TextSimilaritySearch;

import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * 检测工具，大多数方法仅在测试中用，不在正式运行时
 *
 * @author aldebran
 * @since 2023-09-27
 */
public class CheckUtil {

    public static String acDetail(AC ac) {
        StringBuilder sb = new StringBuilder();
        sb.append(ac.getClass());
        sb.append(ac.nextId);
        ac.traverse_(acNode -> {
            sb.append(acNode.id);
            sb.append(acNode.charContent);
            sb.append(acNode.word);
            sb.append(acNode.parent);
            sb.append(acNode.mismatchPointer);
            for (AC.ACNode child : acNode.childContentChildMap.values().stream().sorted(new Comparator<AC.ACNode>() {
                @Override
                public int compare(AC.ACNode o1, AC.ACNode o2) {
                    if (o1.id == o2.id) return 0;
                    return o1.id < o2.id ? -1 : 1;
                }
            }).collect(Collectors.toList())) {
                sb.append(child.id);
//                sb.append(acNode.childContentChildMap.keySet().stream().sorted().collect(Collectors.toList()));
                assert acNode.childContentChildMap.containsKey(child.charContent);
            }
        });

        return sb.toString();
    }

    public static boolean acEquals(AC ac1, AC ac2) {
        return acDetail(ac1).equals(acDetail(ac2));
    }

    public static boolean textSimilarityEquals(TextSimilaritySearch lib1, TextSimilaritySearch lib2) {
        return acEquals(lib1.contentAC, lib2.contentAC)
                && acEquals(lib1.titleAC, lib2.titleAC)
                && lib1.scoreCalculator.equals(lib2.scoreCalculator)
                && lib1.avgIdfGrowthCalculator.equals(lib2.avgIdfGrowthCalculator);
    }

    public static void Assert(boolean value) {
        if (!value) throw new RuntimeException("false表达式");
    }

    public static boolean closeDouble(double d1, double d2) {
        return Math.abs(d1 - d2) < 0.0001;
    }
}
