package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;

import java.io.StringReader;

import static org.elasticsearch.common.settings.Settings.settingsBuilder;

public class NoBreakTest extends BaseOpenNLPAnalysis {

    private final static String NO_BREAK = "No period";
    private final static String[] NO_BREAK_terms = {"No", "period"};
    private final static int[] NO_BREAK_startOffsets = {0, 3};
    private final static int[] NO_BREAK_endOffsets = {2, 9};

    /*@Override
    protected Settings getSettings() {
        return settingsBuilder()
                .put("sentenceModel", "build/en-test-sent.bin")
                .put("tokenizerModel", "build/en-test-tokenizer.bin")
                .build();
    }*/

    @Override
    protected void analyze() throws Exception {
        StringReader inputReader = new StringReader(NO_BREAK);
        Tokenizer t = tokenizerFactory.create();
        t.setReader(inputReader);
        TokenStream ts = filterFactory.create(t);
        TokenStreamContents.assertTokenStreamContents(ts, NO_BREAK_terms, NO_BREAK_startOffsets, NO_BREAK_endOffsets);
    }
}
