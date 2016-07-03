package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.Arrays;

public final class FilterPayloadsFilter extends FilteringTokenFilter {

    private final PayloadAttribute payloadAtt;
    private final byte[][] payloads;
    private final boolean keep;

    public FilterPayloadsFilter(TokenStream input, byte[][] payloads, boolean keep) {
        super(input);
        this.payloadAtt = addAttribute(PayloadAttribute.class);
        this.payloads = payloads;
        this.keep = keep;
    }

    @Override
    protected boolean accept() throws IOException {
        BytesRef p = payloadAtt.getPayload();
        if (p == null && keep) {
            return false;
        } else if (p == null) {
            return true;
        } else {
            byte[] key = Arrays.copyOfRange(p.bytes, p.offset, p.offset + p.length);
            int n = 0;
            while (n < payloads.length) {
                if (Arrays.equals(key, payloads[n])) {
                    break;
                }
                n++;
            }
            return (n < payloads.length) == keep;
        }
    }

}
