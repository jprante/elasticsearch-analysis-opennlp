package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.payloads.DelimitedPayloadTokenFilter;
import org.apache.lucene.analysis.payloads.IdentityEncoder;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class PayloadsFilterTest extends Assert {

    @Test
    public void testDelimitedPayloads() throws Exception {
        String test = "The quick|JJ red|JJ fox|NN jumped|VB over the lazy|JJ brown|JJ dogs|NN";
        MockTokenizer mockTokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
        mockTokenizer.setReader(new StringReader(test));
        DelimitedPayloadTokenFilter filter = new DelimitedPayloadTokenFilter(mockTokenizer,
                DelimitedPayloadTokenFilter.DEFAULT_DELIMITER, new IdentityEncoder());
        filter.reset();
        CharTermAttribute termAtt = filter.getAttribute(CharTermAttribute.class);
        PayloadAttribute payAtt = filter.getAttribute(PayloadAttribute.class);

        assertTermEquals("The", filter, termAtt, payAtt, null);
        assertTermEquals("quick", filter, termAtt, payAtt, "JJ".getBytes("UTF-8"));
        assertTermEquals("red", filter, termAtt, payAtt, "JJ".getBytes("UTF-8"));
        assertTermEquals("fox", filter, termAtt, payAtt, "NN".getBytes("UTF-8"));
        assertTermEquals("jumped", filter, termAtt, payAtt, "VB".getBytes("UTF-8"));
        assertTermEquals("over", filter, termAtt, payAtt, null);
        assertTermEquals("the", filter, termAtt, payAtt, null);
        assertTermEquals("lazy", filter, termAtt, payAtt, "JJ".getBytes("UTF-8"));
        assertTermEquals("brown", filter, termAtt, payAtt, "JJ".getBytes("UTF-8"));
        assertTermEquals("dogs", filter, termAtt, payAtt, "NN".getBytes("UTF-8"));

        assertFalse(filter.incrementToken());
    }

    @Test
    public void testKeepPayloads() throws Exception {
        String test = "The quick|JJ red|JJ fox|NN jumped|VB over the lazy|JJ brown|JJ dogs|NN";
        MockTokenizer mockTokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
        mockTokenizer.setReader(new StringReader(test));
        DelimitedPayloadTokenFilter baseFilter = new DelimitedPayloadTokenFilter(mockTokenizer,
                DelimitedPayloadTokenFilter.DEFAULT_DELIMITER, new IdentityEncoder());
        byte[][] payloads = {
                "VB".getBytes(StandardCharsets.UTF_8),
                "NN".getBytes(StandardCharsets.UTF_8)
        };
        FilterPayloadsFilter filter = new FilterPayloadsFilter(baseFilter, payloads, true);
        filter.reset();
        CharTermAttribute termAtt = filter.getAttribute(CharTermAttribute.class);
        PayloadAttribute payAtt = filter.getAttribute(PayloadAttribute.class);
        assertTermEquals("fox", filter, termAtt, payAtt, "NN".getBytes("UTF-8"));
        assertTermEquals("jumped", filter, termAtt, payAtt, "VB".getBytes("UTF-8"));
        assertTermEquals("dogs", filter, termAtt, payAtt, "NN".getBytes("UTF-8"));
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testFilterPayloads() throws Exception {
        String test = "The quick|JJ red|JJ fox|NN jumped|VB over the lazy|JJ brown|JJ dogs|NN";
        MockTokenizer mockTokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
        mockTokenizer.setReader(new StringReader(test));
        DelimitedPayloadTokenFilter baseFilter = new DelimitedPayloadTokenFilter(mockTokenizer,
                DelimitedPayloadTokenFilter.DEFAULT_DELIMITER, new IdentityEncoder());
        byte[][] payloads = {
                "VB".getBytes("UTF-8"),
                "NN".getBytes("UTF-8")
        };
        FilterPayloadsFilter filter = new FilterPayloadsFilter(baseFilter, payloads, false);
        filter.reset();
        CharTermAttribute termAtt = filter.getAttribute(CharTermAttribute.class);
        PayloadAttribute payAtt = filter.getAttribute(PayloadAttribute.class);
        assertTermEquals("The", filter, termAtt, payAtt, null);
        assertTermEquals("quick", filter, termAtt, payAtt, "JJ".getBytes("UTF-8"));
        assertTermEquals("red", filter, termAtt, payAtt, "JJ".getBytes("UTF-8"));
        assertTermEquals("over", filter, termAtt, payAtt, null);
        assertTermEquals("the", filter, termAtt, payAtt, null);
        assertTermEquals("lazy", filter, termAtt, payAtt, "JJ".getBytes("UTF-8"));
        assertTermEquals("brown", filter, termAtt, payAtt, "JJ".getBytes("UTF-8"));
        assertFalse(filter.incrementToken());
    }

    @Test
    public void testStripPayloads() throws Exception {
        String test = "The quick|JJ red|JJ fox|NN jumped|VB over the lazy|JJ brown|JJ dogs|NN";
        MockTokenizer mockTokenizer = new MockTokenizer(MockTokenizer.WHITESPACE, false);
        mockTokenizer.setReader(new StringReader(test));
        DelimitedPayloadTokenFilter baseFilter = new DelimitedPayloadTokenFilter(mockTokenizer,
                DelimitedPayloadTokenFilter.DEFAULT_DELIMITER, new IdentityEncoder());
        StripPayloadsTokenFilter filter = new StripPayloadsTokenFilter(baseFilter);
        filter.reset();
        CharTermAttribute termAtt = filter.getAttribute(CharTermAttribute.class);
        PayloadAttribute payAtt = filter.getAttribute(PayloadAttribute.class);
        assertTermPayload("The", filter, termAtt, payAtt);
        assertTermPayload("quick", filter, termAtt, payAtt);
        assertTermPayload("red", filter, termAtt, payAtt);
        assertTermPayload("fox", filter, termAtt, payAtt);
        assertTermPayload("jumped", filter, termAtt, payAtt);
        assertTermPayload("over", filter, termAtt, payAtt);
        assertTermPayload("the", filter, termAtt, payAtt);
        assertTermPayload("lazy", filter, termAtt, payAtt);
        assertTermPayload("brown", filter, termAtt, payAtt);
        assertTermPayload("dogs", filter, termAtt, payAtt);
        assertFalse(filter.incrementToken());
    }

    private void assertTermPayload(String expected, TokenStream stream, CharTermAttribute termAtt, PayloadAttribute payAtt) throws Exception {
        assertTrue(stream.incrementToken());
        assertEquals(expected, termAtt.toString());
        BytesRef payload = payAtt.getPayload();
        assertEquals(null, payload);
    }

    private void assertTermEquals(String expected, TokenStream stream, CharTermAttribute termAtt, PayloadAttribute payAtt, byte[] expectPay) throws Exception {
        assertTrue(stream.incrementToken());
        assertEquals(expected, termAtt.toString());
        BytesRef payload = payAtt.getPayload();
        if (payload != null) {
            assertTrue(payload.length == expectPay.length);
            for (int i = 0; i < expectPay.length; i++) {
                assertTrue(expectPay[i] == payload.bytes[payload.offset + i]);
            }
        } else {
            assertTrue(expectPay == null);
        }
    }
}
