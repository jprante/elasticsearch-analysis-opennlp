package org.xbib.elasticsearch.index.analysis.opennlp;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Random;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.automaton.CharacterRunAutomaton;
import org.apache.lucene.util.automaton.RegExp;

public class MockTokenizer extends Tokenizer {

    public static final CharacterRunAutomaton WHITESPACE =
            new CharacterRunAutomaton(new RegExp("[^ \t\r\n]+").toAutomaton());

    public static final CharacterRunAutomaton KEYWORD =
            new CharacterRunAutomaton(new RegExp(".*").toAutomaton());

    public static final CharacterRunAutomaton SIMPLE =
             new CharacterRunAutomaton(new RegExp("[A-Za-zªµºÀ-ÖØ-öø-ˁ一-鿌]+").toAutomaton());

    private static final int DEFAULT_MAX_TOKEN_LENGTH = Integer.MAX_VALUE;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private final CharacterRunAutomaton runAutomaton;
    private final boolean lowerCase;
    private final int maxTokenLength;

    private int state;

    private int off = 0;

    private int bufferedCodePoint = -1; // -1 indicates empty buffer
    private int bufferedOff = -1;

    private enum State {
        SETREADER,       // consumer set a reader input either via ctor or via reset(Reader)
        RESET,           // consumer has called reset()
        INCREMENT,       // consumer is consuming, has called incrementToken() == true
        INCREMENT_FALSE, // consumer has called incrementToken() which returned false
        END,             // consumer has called end() to perform end of stream operations
        CLOSE            // consumer has called close() to release any resources
    };

    private State streamState = State.CLOSE;
    private int lastOffset = 0;

    private final Random random = new Random();

    public MockTokenizer() {
        this(WHITESPACE, true);
    }

    public MockTokenizer(AttributeFactory factory) {
        this(factory, WHITESPACE, true);
    }

    public MockTokenizer(CharacterRunAutomaton runAutomaton, boolean lowerCase) {
        this(runAutomaton, lowerCase, DEFAULT_MAX_TOKEN_LENGTH);
    }

    public MockTokenizer(CharacterRunAutomaton runAutomaton, boolean lowerCase, int maxTokenLength) {
        this(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY, runAutomaton, lowerCase, maxTokenLength);
    }

    public MockTokenizer(AttributeFactory factory, CharacterRunAutomaton runAutomaton, boolean lowerCase) {
        this(factory, runAutomaton, lowerCase, DEFAULT_MAX_TOKEN_LENGTH);
    }

    public MockTokenizer(AttributeFactory factory, CharacterRunAutomaton runAutomaton, boolean lowerCase, int maxTokenLength) {
        super(factory);
        this.runAutomaton = runAutomaton;
        this.lowerCase = lowerCase;
        this.state = runAutomaton.getInitialState();
        this.maxTokenLength = maxTokenLength;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (streamState != State.RESET && streamState != State.INCREMENT) {
            throw new IllegalStateException("incrementToken() called while in wrong state: " + streamState);
        }
        clearAttributes();
        for (;;) {
            int startOffset;
            int cp;
            if (bufferedCodePoint >= 0) {
                cp = bufferedCodePoint;
                startOffset = bufferedOff;
                bufferedCodePoint = -1;
            } else {
                startOffset = off;
                cp = readCodePoint();
            }
            if (cp < 0) {
                break;
            } else if (isTokenChar(cp)) {
                int endOffset;
                do {
                    char chars[] = Character.toChars(normalize(cp));
                    for (char aChar : chars) {
                        termAtt.append(aChar);
                    }
                    endOffset = off;
                    if (termAtt.length() >= maxTokenLength) {
                        break;
                    }
                    cp = readCodePoint();
                } while (cp >= 0 && isTokenChar(cp));
                if (termAtt.length() < maxTokenLength) {
                    bufferedCodePoint = cp;
                    bufferedOff = endOffset;
                } else {
                    bufferedCodePoint = -1;
                }
                int correctedStartOffset = correctOffset(startOffset);
                int correctedEndOffset = correctOffset(endOffset);
                if (correctedStartOffset < 0) {
                    throw new IllegalStateException("invalid start offset: " + correctedStartOffset + ", before correction: " + startOffset);
                }
                if (correctedEndOffset < 0) {
                    throw new IllegalStateException("invalid end offset: " + correctedEndOffset + ", before correction: " + endOffset);
                }
                if (correctedStartOffset < lastOffset) {
                    throw new IllegalStateException("start offset went backwards: " + correctedStartOffset + ", before correction: " + startOffset + ", lastOffset: " + lastOffset);
                }
                lastOffset = correctedStartOffset;
                if (correctedEndOffset < correctedStartOffset) {
                    throw new IllegalStateException("end offset: " + correctedEndOffset + " is before start offset: " + correctedStartOffset);
                }
                offsetAtt.setOffset(correctedStartOffset, correctedEndOffset);
                if (state == -1 || runAutomaton.isAccept(state)) {
                    // either we hit a reject state (longest match), or end-of-text, but in an accept state
                    streamState = State.INCREMENT;
                    return true;
                }
            }
        }
        streamState = State.INCREMENT_FALSE;
        return false;
    }

    private int readCodePoint() throws IOException {
        int ch = readChar();
        if (ch < 0) {
            return ch;
        } else {
            if (Character.isLowSurrogate((char) ch)) {
                throw new IllegalStateException("unpaired low surrogate: " + Integer.toHexString(ch));
            }
            off++;
            if (Character.isHighSurrogate((char) ch)) {
                int ch2 = readChar();
                if (ch2 >= 0) {
                    off++;
                    if (!Character.isLowSurrogate((char) ch2)) {
                        throw new IllegalStateException("unpaired high surrogate: " + Integer.toHexString(ch) + ", followed by: " + Integer.toHexString(ch2));
                    }
                    return Character.toCodePoint((char) ch, (char) ch2);
                } else {
                    throw new IllegalStateException("stream ends with unpaired high surrogate: " + Integer.toHexString(ch));
                }
            }
            return ch;
        }
    }

    private int readChar() throws IOException {
        switch(random.nextInt(10)) {
            case 0: {
                char c[] = new char[1];
                int ret = input.read(c);
                return ret < 0 ? ret : c[0];
            }
            case 1: {
                char c[] = new char[2];
                int ret = input.read(c, 1, 1);
                return ret < 0 ? ret : c[1];
            }
            case 2: {
                char c[] = new char[1];
                CharBuffer cb = CharBuffer.wrap(c);
                int ret = input.read(cb);
                return ret < 0 ? ret : c[0];
            }
            default:
                return input.read();
        }
    }

    private boolean isTokenChar(int c) {
        if (state < 0) {
            state = runAutomaton.getInitialState();
        }
        state = runAutomaton.step(state, c);
        return state >= 0;
    }

    private int normalize(int c) {
        return lowerCase ? Character.toLowerCase(c) : c;
    }

    @Override
    public void reset() throws IOException {
        try {
            super.reset();
            state = runAutomaton.getInitialState();
            lastOffset = off = 0;
            bufferedCodePoint = -1;
            if (streamState == State.RESET) {
                throw new IllegalStateException("double reset()");
            }
        } finally {
            streamState = State.RESET;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
            if (!(streamState == State.END || streamState == State.CLOSE)) {
                throw new IllegalStateException("close() called in wrong state: " + streamState);
            }
        } finally {
            streamState = State.CLOSE;
        }
    }

    @Override
    public void end() throws IOException {
        try {
            super.end();
            int finalOffset = correctOffset(off);
            offsetAtt.setOffset(finalOffset, finalOffset);
            if (streamState != State.INCREMENT_FALSE) {
                throw new IllegalStateException("end() called before incrementToken() returned false!");
            }
        } finally {
            streamState = State.END;
        }
    }

}
