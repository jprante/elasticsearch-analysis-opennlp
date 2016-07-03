package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;

import java.io.IOException;

public final class StripPayloadsTokenFilter extends TokenFilter {

    public StripPayloadsTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            PayloadAttribute payloadAtt = getAttribute(PayloadAttribute.class);
            if (payloadAtt != null) {
                payloadAtt.setPayload(null);
            }
            return true;
        }
        return false;
    }

}
