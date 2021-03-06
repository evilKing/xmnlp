package org.xm.xmnlp.recognition.person;

import org.xm.xmnlp.corpus.tag.Nature;
import org.xm.xmnlp.dictionary.BaseSearcher;
import org.xm.xmnlp.dictionary.CoreDictionary;
import org.xm.xmnlp.dictionary.person.JapanesePersonDictionary;
import org.xm.xmnlp.dictionary.person.NRConstant;
import org.xm.xmnlp.seg.domain.Vertex;
import org.xm.xmnlp.seg.domain.WordNet;
import org.xm.xmnlp.util.Predefine;

import java.util.List;
import java.util.Map;

/**
 * 日本人名识别
 */
public class JapanesePersonRecognition {
    /**
     * 执行识别
     *
     * @param segResult      粗分结果
     * @param wordNetOptimum 粗分结果对应的词图
     * @param wordNetAll     全词图
     */
    public static void recognition(List<Vertex> segResult, WordNet wordNetOptimum, WordNet wordNetAll) {
        StringBuilder sbName = new StringBuilder();
        int appendTimes = 0;
        char[] charArray = wordNetAll.charArray;
        BaseSearcher searcher = JapanesePersonDictionary.getSearcher(charArray);
        Map.Entry<String, Character> entry;
        int activeLine = 1;
        int preOffset = 0;
        while ((entry = searcher.next()) != null) {
            Character label = entry.getValue();
            String key = entry.getKey();
            int offset = searcher.getOffset();
            if (preOffset != offset) {
                if (appendTimes > 1 && sbName.length() > 2) // 日本人名最短为3字
                {
                    insertName(sbName.toString(), activeLine, wordNetOptimum, wordNetAll);
                }
                sbName.setLength(0);
                appendTimes = 0;
            }
            if (appendTimes == 0) {
                if (label == JapanesePersonDictionary.X) {
                    sbName.append(key);
                    ++appendTimes;
                    activeLine = offset + 1;
                }
            } else {
                if (label == JapanesePersonDictionary.M) {
                    sbName.append(key);
                    ++appendTimes;
                } else {
                    if (appendTimes > 1 && sbName.length() > 2) {
                        insertName(sbName.toString(), activeLine, wordNetOptimum, wordNetAll);
                    }
                    sbName.setLength(0);
                    appendTimes = 0;
                }
            }
            preOffset = offset + key.length();
        }
        if (sbName.length() > 0) {
            if (appendTimes > 1) {
                insertName(sbName.toString(), activeLine, wordNetOptimum, wordNetAll);
            }
        }
    }

    /**
     * 是否是bad case
     *
     * @param name
     * @return
     */
    public static boolean isBadCase(String name) {
        Character label = JapanesePersonDictionary.get(name);
        if (label == null) return false;
        return label.equals(JapanesePersonDictionary.A);
    }

    /**
     * 插入日本人名
     *
     * @param name
     * @param activeLine
     * @param wordNetOptimum
     * @param wordNetAll
     */
    private static void insertName(String name, int activeLine, WordNet wordNetOptimum, WordNet wordNetAll) {
        if (isBadCase(name)) return;
        wordNetOptimum.insert(activeLine, new Vertex(Predefine.TAG_PEOPLE, name, new CoreDictionary.Attribute(Nature.nrj), NRConstant.WORD_ID), wordNetAll);
    }
}
