package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.StringReader;

public class POSTaggingTest extends BaseOpenNLPAnalysis {

    private final static String SENTENCES = "Sentence number 1 has 6 words. Sentence number 2, 5 words.";
    private final static String[] SENTENCES_punc = {"Sentence", "number", "1", "has", "6", "words", ".", "Sentence", "number", "2", ",", "5", "words", "."};
    private final static String[] SENTENCES_posTags = {"NNS", "NN", "CD", "NNS", "CD", "NNS", ".", "VBD", "IN", "CD", ",", "CD", "NNS", "."};

    public String getResource() {
        return "/org/xbib/elasticsearch/index/analysis/opennlp/pos-tagging-analysis.json";
    }

    @Override
    protected void analyze() throws Exception {
        StringReader inputReader = new StringReader(SENTENCES);
        Tokenizer t = tokenizerFactory.create();
        t.setReader(inputReader);
        TokenStream ts = filterFactory.create(t);
        ts.reset();
        walkTerms(ts, "pos", SENTENCES_punc, SENTENCES_posTags);
        //walkTerms(ts, "pos", SENTENCES_punc, SENTENCES_posTags);
    }
}
