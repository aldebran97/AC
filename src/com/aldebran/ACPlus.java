package com.aldebran;

import java.util.List;

/**
 * AC自动机
 * 在匹配时可处理包含词
 *
 * @author aldebran
 * @since 2023-01-30
 */
public class ACPlus extends AC {

    @Override
    protected void dealFind(List<MatchResult> results, ACNode find, int p) {
        while (find != null) {
            if (find.word != null) {
                results.add(new MatchResult(find.word, p - find.word.length()));
            }
            find = find.mismatchPointer;
        }
    }
}
