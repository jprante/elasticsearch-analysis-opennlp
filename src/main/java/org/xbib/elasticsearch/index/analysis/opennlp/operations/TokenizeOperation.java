package org.xbib.elasticsearch.index.analysis.opennlp.operations;

import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class TokenizeOperation {

    private final Tokenizer tokenizer;

    public TokenizeOperation(TokenizerModel model) {
        tokenizer = new TokenizerME(model);
    }

    public synchronized Span[] getTerms(String sentence) {
        return tokenizer.tokenizePos(sentence);
    }
}
