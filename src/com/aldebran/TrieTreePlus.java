package com.aldebran;

import java.util.*;
import java.util.function.Consumer;

/**
 * 词语查找树
 * <p>
 * 相比于com.aldebran.TrieTree实现
 * 每个结点不仅保存失配指针，还保存祖先的失配指针
 * 词尾结点保存词内容
 * 因此空间消耗更大，时间消耗更低
 *
 * @author aldebran
 * @since 2023-01-30
 */
public class TrieTreePlus {

    // 词语查找树结点定义
    public static class TrieTreeNode {
        public String charContent; // 字符内容，之所以不用char，因为考虑了特殊字符，两字节无法表示的情况
        public List<TrieTreeNode> children; // 子结点
        public TrieTreeNode mismatchPointer; // 失配结点
        public TrieTreeNode ancestorMismatchPointer; // 自己以及祖先中离根结点最近结点的失配结点
        public int ancestorMismatchMove; // 失配时指针向前移动量
        public TrieTreeNode parent; // 父结点
        public String word; // 是否可作为词尾，用于处理包含关系

        public Map<String, TrieTreeNode> childContentChildMap = new HashMap<>(); // value不用index

        public TrieTreeNode(String charContent, List<TrieTreeNode> children, TrieTreeNode mismatchPointer, TrieTreeNode parent) {
            this.charContent = charContent;
            this.children = children;
            this.mismatchPointer = mismatchPointer;
            this.parent = parent;
        }

        @Override
        public String toString() {
            return "TrieTreeNode{" +
                    "charContent='" + charContent + '\'' +
                    ", parent=" + (parent == null ? "null" : parent.charContent) +
                    ", mismatchPointer=" + (mismatchPointer == null ? "null" : mismatchPointer.charContent) +
                    ", word=" + word +
                    '}';
        }
    }

    // 用于输出词语
    public interface WordWriter {
        void write(String word);
    }

    public static class MatchResult {
        public String word;
        public int index;

        public MatchResult(String word, int index) {
            this.word = word;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MatchResult that = (MatchResult) o;
            return index == that.index && Objects.equals(word, that.word);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word, index);
        }

        @Override
        public String toString() {
            return "MatchResult{" +
                    "word='" + word + '\'' +
                    ", index=" + index +
                    '}';
        }
    }

    public TrieTreeNode root = new TrieTreeNode("", new ArrayList<>(), null, null);

    // 添加词
    public void addWords(List<String> words) {
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            TrieTreeNode current = root;
            for (int j = 0; j < word.length(); j++) {
                String charContent = word.substring(j, j + 1);
                TrieTreeNode find = null;
                // 字查找可转换为二分插入、查找或者其他带有索引的方法
                find = current.childContentChildMap.get(charContent);

                if (find == null) {
                    find = new TrieTreeNode(charContent, new ArrayList<>(), null, current);
                    current.children.add(find);
                    current.childContentChildMap.put(charContent, find);
                }
                current = find;
                if (j == word.length() - 1) current.word = word;
            }

        }
    }


    // 遍历
    public void traverse_(Consumer<TrieTreeNode> pVisit) {
        Queue<TrieTreeNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            TrieTreeNode c = queue.remove();
            pVisit.accept(c);
            for (TrieTreeNode child : c.children) {
                queue.add(child);
            }
        }
    }

    public void traverse(Consumer<String> pVisit) {
        traverse_(trieTreeNode -> pVisit.accept(trieTreeNode.charContent));
    }

    // 更新失配结点
    public void update() {
        traverse_(trieTreeNode -> {
            if (trieTreeNode != root) {
                if (trieTreeNode.mismatchPointer == null) {
                    String first = trieTreeNode.charContent;
                    for (TrieTreeNode child : root.children) {
                        if (child.charContent.equals(first) && child != trieTreeNode) {
                            trieTreeNode.mismatchPointer = child;
                            break;
                        }
                    }
                }

                int move = 0;
                int max = -1;

                TrieTreeNode c1 = trieTreeNode;
                TrieTreeNode lastMismatchPointer = null;
                while (c1 != root) {
                    if (c1.mismatchPointer != null) {
                        lastMismatchPointer = c1.mismatchPointer;
                        if (move > max) {
                            max = move;
                        }
                    }
                    c1 = c1.parent;
                    move++;
                }

                if (max == -1) {
                    trieTreeNode.ancestorMismatchPointer = root;
                    trieTreeNode.ancestorMismatchMove = 0;
                } else {
                    trieTreeNode.ancestorMismatchPointer = lastMismatchPointer;
                    trieTreeNode.ancestorMismatchMove = max;
                }

            }
        });
    }

    // 特里树转词
    public void toWords(WordWriter wordWriter) {
        traverse_(node -> {
                    if (node.word != null) wordWriter.write(node.word);
                }
        );
    }

    public List<String> toWordsList() {
        List<String> result = new ArrayList<>();
        toWords(word -> {
            result.add(word);
        });
        return result;
    }

    // 是否包含词
    public boolean containsWord(String word) {
        TrieTreeNode current = root;
        for (int i = 0; i < word.length(); i++) {
            String charContent = word.substring(i, i + 1);
            TrieTreeNode find = null;
            // 字查找可转换为二分插入、查找或者其他带有索引的方法
            find = current.childContentChildMap.get(charContent);

            if (find == null) {
                if (current.mismatchPointer == null) return false;
                current = current.mismatchPointer;
                i -= 1;
            } else {
                current = find;
            }

        }
        return current.word != null;
    }

    public List<MatchResult> indexOf(String text) {

        List<MatchResult> results = new ArrayList<>();

        int p = 0;
        TrieTreeNode current = root;
        while (p < text.length()) {
            String charContent = text.substring(p, p + 1);
            TrieTreeNode find = current.childContentChildMap.get(charContent);
            if (find != null) {
                current = find;
                p++;
            } else {
                if (current == root) {
                    p++;
                } else {
                    current = current.ancestorMismatchPointer;
                    p -= current.ancestorMismatchMove;
                }
            }

            if (current.word != null) results.add(new MatchResult(current.word, p - current.word.length()));
        }

        return results;
    }
}
