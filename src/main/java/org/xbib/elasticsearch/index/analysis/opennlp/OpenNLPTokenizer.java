package org.xbib.elasticsearch.index.analysis.opennlp;

import opennlp.tools.util.Span;
import org.apache.lucene.analysis.Tokenizer;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.SentenceDetectorOperation;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.TokenizeOperation;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.util.Arrays;

public final class OpenNLPTokenizer extends Tokenizer {

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private Span[] sentences;
    private Span[][] words;
    private Span[] wordSet;
    private int correctOffset;
    private boolean first;
    private int indexSentence ;
    private int indexWord;
    private char[] fullText;

    private SentenceDetectorOperation sentenceOp = null;
    private TokenizeOperation tokenizerOp = null;

    public OpenNLPTokenizer(SentenceDetectorOperation sentenceOp, TokenizeOperation tokenizerOp) throws IOException {
        termAtt.resizeBuffer(DEFAULT_BUFFER_SIZE);
        if (sentenceOp == null && tokenizerOp == null) {
            throw new IllegalArgumentException("need one or both of Sentence Detector and Tokenizer");
        }
        this.sentenceOp = sentenceOp;
        this.tokenizerOp = tokenizerOp;
        this.first = true;
        this.indexSentence = 0;
        this.indexWord = 0;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (first) {
            loadAll();
            restartAtBeginning();
            first = false;
        }
        if (sentences.length == 0) {
            return false;
        }
        int sentenceOffset = sentences[indexSentence].getStart();
        if (wordSet == null) {
            wordSet = words[indexSentence];
        }
        clearAttributes();
        while (indexSentence < sentences.length) {
            while (indexWord == wordSet.length) {
                indexSentence++;
                if (indexSentence < sentences.length) {
                    wordSet = words[indexSentence];
                    indexWord = 0;
                    sentenceOffset = sentences[indexSentence].getStart();
                } else {
                    return false;
                }
            }
            Span sentence = sentences[indexSentence];
            Span word = wordSet[indexWord];
            int spot = sentence.getStart() + word.getStart();
            termAtt.setEmpty();
            int termLength = word.getEnd() - word.getStart();
            if (termAtt.buffer().length < termLength) {
                termAtt.resizeBuffer(termLength);
            }
            termAtt.setLength(termLength);
            correctOffset = correctOffset(sentenceOffset + word.getEnd());
            offsetAtt.setOffset(correctOffset(word.getStart() + sentenceOffset), correctOffset);
            System.arraycopy(fullText, spot, termAtt.buffer(), 0, termLength);
            indexWord++;
            return true;
        }
        return false;
    }

    private void restartAtBeginning() throws IOException {
        indexWord = 0;
        indexSentence = 0;
        indexWord = 0;
        correctOffset = 0;
        wordSet = null;
    }

    private void loadAll() throws IOException {
        fillBuffer();
        detectSentences();
        words = new Span[sentences.length][];
        for (int i = 0; i < sentences.length; i++) {
            splitWords(i);
        }
    }

    private void fillBuffer() throws IOException {
        int offset = 0;
        int size = 16 * 1024;
        fullText = new char[size];
        int length = input.read(fullText);
        while (length == size) {
            fullText = Arrays.copyOf(fullText, offset + size);
            offset += size;
            length = input.read(fullText, offset, size);
        }
        fullText = Arrays.copyOf(fullText, offset + length);
    }

    private void detectSentences() throws IOException {
        sentences = sentenceOp.splitSentences(new String(fullText));
    }

    private void splitWords(int i) {
        Span current = sentences[i];
        String sentence = String.copyValueOf(fullText, current.getStart(), current.getEnd() - current.getStart());
        words[i] = tokenizerOp.getTerms(sentence);
    }

    @Override
    public final void end() {
        offsetAtt.setOffset(correctOffset, correctOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        clearAttributes();
        restartAtBeginning();
    }
}
