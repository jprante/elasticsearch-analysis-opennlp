package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;

import java.io.StringReader;

import static org.elasticsearch.common.settings.Settings.settingsBuilder;

public class PassThroughTest extends BaseOpenNLPAnalysis {

    private final static String SENTENCES = "Sentence number 1 has 6 words. Sentence number 2, 5 words.";
    private final static String[] SENTENCES_punc = {"Sentence", "number", "1", "has", "6", "words", ".", "Sentence", "number", "2", ",", "5", "words", "."};
    private final static String[] SENTENCES_nopunc = {"Sentence", "number", "1", "has", "6", "words", ".", "Sentence", "number", "2", ",", "5", "words", "."};
    private final static int[] SENTENCES_startOffsets = {0, 9, 16, 18, 22, 24, 29, 31, 40, 47, 48, 50, 52, 57};
    private final static int[] SENTENCES_endOffsets = {8, 15, 17, 21, 23, 29, 30, 39, 46, 48, 49, 51, 57, 58};


    /*@Override
    protected Settings getSettings() {
        return settingsBuilder()
                .put("sentenceModel", "build/en-test-sent.bin")
                .put("tokenizerModel", "build/en-test-tokenizer.bin")
                .build();
    }*/

    @Override
    protected void analyze() throws Exception {
        StringReader inputReader = new StringReader(SENTENCES);
        Tokenizer t = tokenizerFactory.create();
        t.setReader(inputReader);
        TokenStream ts = filterFactory.create(t);
        ts.reset();
        walkTerms(ts, "none", SENTENCES_punc, null);
        //walkTerms(ts, "none", SENTENCES_punc, null);
        TokenStreamContents.assertTokenStreamContents(ts, SENTENCES_punc, SENTENCES_startOffsets, SENTENCES_endOffsets);
    }
}
