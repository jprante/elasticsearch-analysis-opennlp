package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.ChunkerOperation;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.NamedEntityRecognitionOperation;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.PartOfSpeechOperation;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class OpenNLPTokenFilter extends TokenFilter {

    private final static String SENTENCE_BREAK = "[.?!]";

    private final boolean doPOS;
    private final boolean doChunking;
    private final boolean doNER;

    private int finalOffset;

    private List<AttributeSource> tokenAttrs;
    private boolean first;
    private int indexToken;

    private PartOfSpeechOperation posTaggerOp;
    private ChunkerOperation chunkerOp;
    private List<NamedEntityRecognitionOperation> nerTaggerOps;

    public OpenNLPTokenFilter(TokenStream input,
                              PartOfSpeechOperation posTaggerOp,
                              ChunkerOperation chunkerOp,
                              List<NamedEntityRecognitionOperation> nerTaggerOps) throws IOException {
        super(input);
        this.tokenAttrs = new ArrayList<>();
        this.first = true;
        this.indexToken = 0;
        this.nerTaggerOps = new ArrayList<>();
        this.posTaggerOp = posTaggerOp;
        this.chunkerOp = chunkerOp;
        this.nerTaggerOps = nerTaggerOps;
        this.doChunking = chunkerOp != null;
        this.doPOS = !doChunking && posTaggerOp != null;
        this.doNER = nerTaggerOps != null;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        if (first) {
            String[] words = walkTokens();
            if (words.length == 0) {
                return false;
            }
            createTags(words);
            first = false;
            indexToken = 0;
        }
        if (indexToken == tokenAttrs.size()) {
            return false;
        }
        AttributeSource as = tokenAttrs.get(indexToken);
        Iterator<? extends Class<? extends Attribute>> it = as.getAttributeClassesIterator();
        while (it.hasNext()) {
            Class<? extends Attribute> attrClass = it.next();
            if (!hasAttribute(attrClass)) {
                addAttribute(attrClass);
            }
        }
        as.copyTo(this);
        indexToken++;
        return true;
    }

    private String[] walkTokens() throws IOException {
        List<String> wordList = new ArrayList<>();
        while (input.incrementToken()) {
            CharTermAttribute textAtt = input.getAttribute(CharTermAttribute.class);
            OffsetAttribute offsetAtt = input.getAttribute(OffsetAttribute.class);
            char[] buffer = textAtt.buffer();
            String word = new String(buffer, 0, offsetAtt.endOffset() - offsetAtt.startOffset());
            wordList.add(word);
            AttributeSource attrs = input.cloneAttributes();
            tokenAttrs.add(attrs);
        }
        String[] words = new String[wordList.size()];
        for (int i = 0; i < words.length; i++) {
            words[i] = wordList.get(i);
        }
        return words;
    }

    private void createTags(String[] words) {
        String[] appended = appendDot(words);
        if (doPOS) {
            String[] tags = posTaggerOp.getPOSTags(appended);
            appendPayloads(tags, words.length);
        } else if (doChunking) {
            String[] pos = posTaggerOp.getPOSTags(appended);
            String[] tags = chunkerOp.getChunks(words, pos, null);
            appendPayloads(tags, words.length);
        }
        if (doNER) {
            for (NamedEntityRecognitionOperation op : nerTaggerOps) {
                appendPayloads(op.createAll(appended), words.length);
            }
        }
    }

    private String[] appendDot(String[] words) {
        int nWords = words.length;
        String lastWord = words[nWords - 1];
        if (lastWord.length() != 1) {
            return words;
        }
        if (lastWord.matches(SENTENCE_BREAK)) {
            return words;
        }
        words = Arrays.copyOf(words, nWords + 1);
        words[nWords] = ".";
        return words;
    }

    private void appendPayloads(String[] tags, int length) {
        for (int i = 0; i < length; i++) {
            AttributeSource attrs = tokenAttrs.get(i);
            if (tags[i] != null) {
                try {
                    PayloadAttribute payloadAtt = attrs.hasAttribute(PayloadAttribute.class) ? attrs.getAttribute(PayloadAttribute.class) : attrs.addAttribute(PayloadAttribute.class);
                    BytesRef bytesRef = new BytesRef(tags[i].toUpperCase(Locale.getDefault()).getBytes("UTF-8"));
                    payloadAtt.setPayload(bytesRef);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public final void end() {
        clearAttributes();
        OffsetAttribute offsetAtt = getAttribute(OffsetAttribute.class);
        offsetAtt.setOffset(finalOffset, finalOffset);
        tokenAttrs.clear();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        clearAttributes();
        indexToken = 0;
        finalOffset = 0;
    }
}
