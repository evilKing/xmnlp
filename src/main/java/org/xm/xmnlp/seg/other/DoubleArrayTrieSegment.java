package org.xm.xmnlp.seg.other;

import org.xm.xmnlp.collection.ahocorasick.AhoCorasickDoubleArrayTrie;
import org.xm.xmnlp.collection.trie.DoubleArrayTrie;
import org.xm.xmnlp.corpus.tag.Nature;
import org.xm.xmnlp.dictionary.CoreDictionary;
import org.xm.xmnlp.dictionary.CustomDictionary;
import org.xm.xmnlp.seg.domain.Term;
import org.xm.xmnlp.seg.nshortpath.AtomNode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 使用DoubleArrayTrie实现的最长分词器
 * 基于词典分词，使用DoubleArrayTrie数据结构直接查找词典分词
 * Created by mingzai on 2016/7/30.
 */
public class DoubleArrayTrieSegment extends DictionaryBasedSegment {
    public DoubleArrayTrieSegment() {
        super();
        tokenizerConfig.useCustomDictionary = false;
    }

    @Override
    protected List<Term> segSentence(char[] sentence) {
        char[] charArray = sentence;
        final int[] wordNet = new int[charArray.length];
        Arrays.fill(wordNet, 1);
        final Nature[] natureArray = tokenizerConfig.speechTagging ? new Nature[charArray.length] : null;
        DoubleArrayTrie<CoreDictionary.Attribute>.Searcher searcher =
                CoreDictionary.trie.getSearcher(sentence, 0);
        while (searcher.next()) {
            int length = searcher.length;
            if (length > wordNet[searcher.begin]) {
                wordNet[searcher.begin] = length;
                if (tokenizerConfig.speechTagging) {
                    natureArray[searcher.begin] = searcher.value.nature[0];
                }
            }
        }
        if (tokenizerConfig.useCustomDictionary) {
            CustomDictionary.parseText(charArray, new AhoCorasickDoubleArrayTrie.IHit<CoreDictionary.Attribute>() {
                @Override
                public void hit(int begin, int end, CoreDictionary.Attribute value) {
                    int length = end - begin;
                    if (length > wordNet[begin]) {
                        wordNet[begin] = length;
                        if (tokenizerConfig.speechTagging) {
                            natureArray[begin] = value.nature[0];
                        }
                    }
                }
            });
        }
        LinkedList<Term> termLinkedList = new LinkedList<Term>();
        if (tokenizerConfig.speechTagging) {
            getSpeechTagging(charArray, wordNet, natureArray);
        }
        for (int i = 0; i < wordNet.length; ) {
            Term term = new Term(new String(charArray, i, wordNet[i]), tokenizerConfig.speechTagging ? (natureArray[i] == null ? Nature.nz : natureArray[i]) : null);
            term.offset = i;
            termLinkedList.add(term);
            i += wordNet[i];
        }
        return termLinkedList;
    }

    public static void getSpeechTagging(char[] charArray, int[] wordNet, Nature[] natureArray) {
        for (int i = 0; i < natureArray.length; ) {
            if (natureArray[i] == null) {
                int j = i + 1;
                for (; j < natureArray.length; ++j) {
                    if (natureArray[j] != null) break;
                }
                List<AtomNode> atomNodeList = quickAtomSegment(charArray, i, j);
                for (AtomNode atomNode : atomNodeList) {
                    if (atomNode.sWord.length() >= wordNet[i]) {
                        wordNet[i] = atomNode.sWord.length();
                        natureArray[i] = atomNode.getNature();
                        i += wordNet[i];
                    }
                }
                i = j;
            } else {
                ++i;
            }
        }
    }


}
