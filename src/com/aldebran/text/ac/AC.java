package com.aldebran.text.ac;

import com.aldebran.text.util.CheckUtil;
import com.aldebran.text.util.ContinuousSerialUtil;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * AC自动机 = KMP + TrieTree
 * 此类不处理包含词，处理包含词请看com.aldebran.ACPlus类
 *
 * @author aldebran
 * @since 2023-01-30
 */
public class AC implements Serializable {

    // AC自动机结点定义
    public static class ACNode implements Serializable {
        public String charContent; // 字符内容，之所以不用char，因为考虑了特殊字符，两字节无法表示的情况
        public ACNode mismatchPointer; // 失配结点
        public ACNode parent; // 父结点
        public String word; // 是否可作为词尾，用于处理包含关系

        public long id = 0;

        public Map<String, ACNode> childContentChildMap = new HashMap<>(); // value不用index

        public ACNode(String charContent, ACNode mismatchPointer, ACNode parent, long id) {
            this.charContent = charContent;
            this.mismatchPointer = mismatchPointer;
            this.parent = parent;
            this.id = id;
            if (parent != null) {
                parent.childContentChildMap.put(charContent, this);
            }
        }

        @Override
        public String toString() {
            return "ACNode{" +
                    "id='" + id + '\'' +
                    "charContent='" + charContent + '\'' +
                    ", parentId=" + (parent == null ? "null" : parent.id) +
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

    public ACNode root = new ACNode("", null, null, 0);

    public long nextId = 1;

    // 添加词
    public void addWords(List<String> words) {
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            ACNode current = root;
            for (int j = 0; j < word.length(); j++) {
                String charContent = word.substring(j, j + 1);
                ACNode find = null;
                // 字查找可转换为二分插入、查找或者其他带有索引的方法
                find = current.childContentChildMap.get(charContent);

                if (find == null) {
                    find = new ACNode(charContent, null, current, nextId++);
                }
                current = find;
                if (j == word.length() - 1) current.word = word;
            }

        }
    }

    // 遍历
    public void traverse_(Consumer<ACNode> pVisit) {
        Queue<ACNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            ACNode c = queue.remove();
            pVisit.accept(c);
            for (ACNode child : c.childContentChildMap.values()) {
                queue.add(child);
            }
        }
    }

    public void traverse(Consumer<String> pVisit) {
        traverse_(trieTreeNode -> pVisit.accept(trieTreeNode.charContent));
    }

    // 更新失配结点
    public void update() {
        traverse_(node -> {
            if (node != root) {
                if (node.parent == root) {
                    node.mismatchPointer = root;
                } else {
                    ACNode find = node.parent.mismatchPointer.childContentChildMap.get(node.charContent);
                    if (find != null) {
                        node.mismatchPointer = find;
                    } else {
                        find = root.childContentChildMap.get(node.charContent);
                        if (find != null) {
                            node.mismatchPointer = find;
                        } else {
                            node.mismatchPointer = root;
                        }
                    }
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
        ACNode current = root;
        for (int i = 0; i < word.length(); i++) {
            String charContent = word.substring(i, i + 1);
            ACNode find = null;
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

    // 正向匹配
    public List<MatchResult> indexOf(String text) {
        List<MatchResult> results = new ArrayList<>();
        int p = 0;
        ACNode current = root;
        while (p < text.length()) {
            String charContent = text.substring(p, p + 1);
            ACNode find = current.childContentChildMap.get(charContent);
            if (find != null) {
                current = find;
                p++;
            } else {
                if (current == root) {
                    p++;
                } else {
                    current = current.mismatchPointer;
                }
            }
            dealFind(results, find, p);
        }

        return results;
    }

    protected void dealFind(List<MatchResult> results, ACNode find, int p) {
        if (find != null && find.word != null) {
            results.add(new MatchResult(find.word, p - find.word.length()));
        }
    }

    public static File save(AC ac, File outFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 1 * 1024 * 1024);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream);
        ) {
            ContinuousSerialUtil.saveAC(objectOutputStream, ac, 10 * 10000);
        }
        return outFile;
    }

    public static AC load(File inFile) throws Exception {
        try (FileInputStream fileInputStream = new FileInputStream(inFile);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream, 1 * 1024 * 1024);
             ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
        ) {
            return ContinuousSerialUtil.loadAC(objectInputStream);
        }
    }

    // 运行时不调用，仅用于测试
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return CheckUtil.acEquals(this, (AC) o);
    }
}
