package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.junit.Assert;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class TokenStreamContents extends Assert {

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[], int posLengths[], Integer finalOffset,
                                                 boolean offsetsAreCorrect) throws IOException {
        assertNotNull(output);
        CheckClearAttributesAttribute checkClearAtt = ts.addAttribute(CheckClearAttributesAttribute.class);

        assertTrue(ts.hasAttribute(CharTermAttribute.class));
        CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);

        OffsetAttribute offsetAtt = null;
        if (startOffsets != null || endOffsets != null || finalOffset != null) {
            assertTrue(ts.hasAttribute(OffsetAttribute.class));
            offsetAtt = ts.getAttribute(OffsetAttribute.class);
        }

        TypeAttribute typeAtt = null;
        if (types != null) {
            assertTrue(ts.hasAttribute(TypeAttribute.class));
            typeAtt = ts.getAttribute(TypeAttribute.class);
        }

        PositionIncrementAttribute posIncrAtt = null;
        if (posIncrements != null) {
            assertTrue(ts.hasAttribute(PositionIncrementAttribute.class));
            posIncrAtt = ts.getAttribute(PositionIncrementAttribute.class);
        }

        PositionLengthAttribute posLengthAtt = null;
        if (posLengths != null) {
            assertTrue(ts.hasAttribute(PositionLengthAttribute.class));
            posLengthAtt = ts.getAttribute(PositionLengthAttribute.class);
        }

        // Maps position to the start/end offset:
        final Map<Integer, Integer> posToStartOffset = new HashMap<Integer, Integer>();
        final Map<Integer, Integer> posToEndOffset = new HashMap<Integer, Integer>();

        ts.reset();
        int pos = -1;
        int lastStartOffset = 0;
        for (int i = 0; i < output.length; i++) {
            // extra safety to enforce, that the state is not preserved and also assign bogus values
            ts.clearAttributes();
            termAtt.setEmpty().append("bogusTerm");
            if (offsetAtt != null) {
                offsetAtt.setOffset(14584724, 24683243);
            }
            if (typeAtt != null) {
                typeAtt.setType("bogusType");
            }
            if (posIncrAtt != null) {
                posIncrAtt.setPositionIncrement(45987657);
            }
            if (posLengthAtt != null) {
                posLengthAtt.setPositionLength(45987653);
            }

            checkClearAtt.getAndResetClearCalled(); // reset it, because we called clearAttribute() before
            assertTrue(ts.incrementToken());
            assertTrue(checkClearAtt.getAndResetClearCalled());

            assertEquals(output[i], termAtt.toString());
            if (startOffsets != null) {
                assertEquals(startOffsets[i], offsetAtt.startOffset());
            }
            if (endOffsets != null) {
                assertEquals(endOffsets[i], offsetAtt.endOffset());
            }
            if (types != null) {
                assertEquals(types[i], typeAtt.type());
            }
            if (posIncrements != null) {
                assertEquals(posIncrements[i], posIncrAtt.getPositionIncrement());
            }
            if (posLengths != null) {
                assertEquals(posLengths[i], posLengthAtt.getPositionLength());
            }

            // we can enforce some basic things about a few attributes even if the caller doesn't check:
            if (offsetAtt != null) {
                final int startOffset = offsetAtt.startOffset();
                final int endOffset = offsetAtt.endOffset();
                if (finalOffset != null) {
                    assertTrue(startOffset <= finalOffset);
                    assertTrue(endOffset <= finalOffset);
                }

                if (offsetsAreCorrect) {
                    assertTrue(offsetAtt.startOffset() >= lastStartOffset);
                    lastStartOffset = offsetAtt.startOffset();
                }

                if (offsetsAreCorrect && posLengthAtt != null && posIncrAtt != null) {
                    // Validate offset consistency in the graph, ie
                    // all tokens leaving from a certain pos have the
                    // same startOffset, and all tokens arriving to a
                    // certain pos have the same endOffset:
                    final int posInc = posIncrAtt.getPositionIncrement();
                    pos += posInc;

                    final int posLength = posLengthAtt.getPositionLength();

                    if (!posToStartOffset.containsKey(pos)) {
                        // First time we've seen a token leaving from this position:
                        posToStartOffset.put(pos, startOffset);
                        //System.out.println("  + s " + pos + " -> " + startOffset);
                    } else {
                        // We've seen a token leaving from this position
                        // before; verify the startOffset is the same:
                        //System.out.println("  + vs " + pos + " -> " + startOffset);
                        assertEquals(posToStartOffset.get(pos).intValue(), startOffset);
                    }

                    final int endPos = pos + posLength;

                    if (!posToEndOffset.containsKey(endPos)) {
                        // First time we've seen a token arriving to this position:
                        posToEndOffset.put(endPos, endOffset);
                        //System.out.println("  + e " + endPos + " -> " + endOffset);
                    } else {
                        // We've seen a token arriving to this position
                        // before; verify the endOffset is the same:
                        //System.out.println("  + ve " + endPos + " -> " + endOffset);
                        assertEquals(posToEndOffset.get(endPos).intValue(), endOffset);
                    }
                }
            }
            if (posIncrAtt != null) {
                if (i == 0) {
                    assertTrue(posIncrAtt.getPositionIncrement() >= 1);
                } else {
                    assertTrue(posIncrAtt.getPositionIncrement() >= 0);
                }
            }
            if (posLengthAtt != null) {
                assertTrue(posLengthAtt.getPositionLength() >= 1);
            }
        }
        assertFalse(ts.incrementToken());
        ts.end();
        if (finalOffset != null) {
            assertEquals(finalOffset.intValue(), offsetAtt.endOffset());
        }
        if (offsetAtt != null) {
            assertTrue(offsetAtt.endOffset() >= 0);
        }
        ts.close();
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[], int posLengths[], Integer finalOffset) throws IOException {
        assertTokenStreamContents(ts, output, startOffsets, endOffsets, types, posIncrements, posLengths, finalOffset, true);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[], Integer finalOffset) throws IOException {
        assertTokenStreamContents(ts, output, startOffsets, endOffsets, types, posIncrements, null, finalOffset);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[]) throws IOException {
        assertTokenStreamContents(ts, output, startOffsets, endOffsets, types, posIncrements, null, null);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output) throws IOException {
        assertTokenStreamContents(ts, output, null, null, null, null, null, null);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, String[] types) throws IOException {
        assertTokenStreamContents(ts, output, null, null, types, null, null, null);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int[] posIncrements) throws IOException {
        assertTokenStreamContents(ts, output, null, null, null, posIncrements, null, null);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[]) throws IOException {
        assertTokenStreamContents(ts, output, startOffsets, endOffsets, null, null, null, null);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], Integer finalOffset) throws IOException {
        assertTokenStreamContents(ts, output, startOffsets, endOffsets, null, null, null, finalOffset);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], int[] posIncrements) throws IOException {
        assertTokenStreamContents(ts, output, startOffsets, endOffsets, null, posIncrements, null, null);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], int[] posIncrements, Integer finalOffset) throws IOException {
        assertTokenStreamContents(ts, output, startOffsets, endOffsets, null, posIncrements, null, finalOffset);
    }

    public static void assertTokenStreamContents(TokenStream ts, String[] output, int startOffsets[], int endOffsets[], int[] posIncrements, int[] posLengths, Integer finalOffset) throws IOException {
        assertTokenStreamContents(ts, output, startOffsets, endOffsets, null, posIncrements, posLengths, finalOffset);
    }

    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[]) throws IOException {
        assertTokenStreamContents(a.tokenStream("dummy", new StringReader(input)), output, startOffsets, endOffsets, types, posIncrements, null, input.length());
    }

    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[], int posLengths[]) throws IOException {
        assertTokenStreamContents(a.tokenStream("dummy", new StringReader(input)), output, startOffsets, endOffsets, types, posIncrements, posLengths, input.length());
    }

    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[], int posLengths[], boolean offsetsAreCorrect) throws IOException {
        assertTokenStreamContents(a.tokenStream("dummy", new StringReader(input)), output, startOffsets, endOffsets, types, posIncrements, posLengths, input.length(), offsetsAreCorrect);
    }

    public static void assertAnalyzesTo(Analyzer a, String input, String[] output) throws IOException {
        assertAnalyzesTo(a, input, output, null, null, null, null, null);
    }

    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, String[] types) throws IOException {
        assertAnalyzesTo(a, input, output, null, null, types, null, null);
    }

    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, int[] posIncrements) throws IOException {
        assertAnalyzesTo(a, input, output, null, null, null, posIncrements, null);
    }

    public static void assertAnalyzesToPositions(Analyzer a, String input, String[] output, int[] posIncrements, int[] posLengths) throws IOException {
        assertAnalyzesTo(a, input, output, null, null, null, posIncrements, posLengths);
    }

    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[]) throws IOException {
        assertAnalyzesTo(a, input, output, startOffsets, endOffsets, null, null, null);
    }

    public static void assertAnalyzesTo(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[], int[] posIncrements) throws IOException {
        assertAnalyzesTo(a, input, output, startOffsets, endOffsets, null, posIncrements, null);
    }

    public static void assertAnalyzesToReuse(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[], String types[], int posIncrements[]) throws IOException {
        assertTokenStreamContents(a.tokenStream("dummy", new StringReader(input)), output, startOffsets, endOffsets, types, posIncrements, null, input.length());
    }

    public static void assertAnalyzesToReuse(Analyzer a, String input, String[] output) throws IOException {
        assertAnalyzesToReuse(a, input, output, null, null, null, null);
    }

    public static void assertAnalyzesToReuse(Analyzer a, String input, String[] output, String[] types) throws IOException {
        assertAnalyzesToReuse(a, input, output, null, null, types, null);
    }

    public static void assertAnalyzesToReuse(Analyzer a, String input, String[] output, int[] posIncrements) throws IOException {
        assertAnalyzesToReuse(a, input, output, null, null, null, posIncrements);
    }

    public static void assertAnalyzesToReuse(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[]) throws IOException {
        assertAnalyzesToReuse(a, input, output, startOffsets, endOffsets, null, null);
    }

    public static void assertAnalyzesToReuse(Analyzer a, String input, String[] output, int startOffsets[], int endOffsets[], int[] posIncrements) throws IOException {
        assertAnalyzesToReuse(a, input, output, startOffsets, endOffsets, null, posIncrements);
    }

    public interface CheckClearAttributesAttribute extends Attribute {

        boolean getAndResetClearCalled();
    }

    public static final class CheckClearAttributesAttributeImpl extends AttributeImpl implements CheckClearAttributesAttribute {

        private boolean clearCalled = false;

        @Override
        public boolean getAndResetClearCalled() {
            try {
                return clearCalled;
            } finally {
                clearCalled = false;
            }
        }

        @Override
        public void clear() {
            clearCalled = true;
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof CheckClearAttributesAttributeImpl
                    && ((CheckClearAttributesAttributeImpl) other).clearCalled == this.clearCalled);
        }

        @Override
        public int hashCode() {
            return 76137213 ^ Boolean.valueOf(clearCalled).hashCode();
        }

        @Override
        public void copyTo(AttributeImpl target) {
            ((CheckClearAttributesAttributeImpl) target).clear();
        }
    }
}
