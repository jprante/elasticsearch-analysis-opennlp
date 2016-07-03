package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

import java.io.StringReader;

public class NamesTest extends BaseOpenNLPAnalysis {

    private final static String NAMES2 = "Royal Flash is a tale about Harry Flashman.";
    //private final static String[] NAMES2_OUT = {null, null, null, null, null, null, null, "PERSON", null};
    private final static String[] NAMES2_OUT = {null, null, null, null, null, null, null, "I-NP", null};

    /*@Override
    protected Settings getSettings() {
        return settingsBuilder()
                .put("sentenceModel", "build/en-test-sent.bin")
                .put("tokenizerModel", "build/en-test-tokenizer.bin")
                .put("nerTaggerModels", "build/en-test-ner-person.bin")
                .build();
    }*/

    @Override
    protected void analyze() throws Exception {
        StringReader inputReader = new StringReader(NAMES2);
        Tokenizer t = tokenizerFactory.create();
        t.setReader(inputReader);
        TokenStream ts = filterFactory.create(t);
        ts.reset();
        walkTerms(ts, "names", null, NAMES2_OUT);
    }
}
