package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;

import java.io.StringReader;

import static org.elasticsearch.common.settings.Settings.settingsBuilder;

public class ChunkingTest extends BaseOpenNLPAnalysis {

    private final static String SENTENCES = "Sentence number 1 has 6 words. Sentence number 2, 5 words.";
    private final static String[] SENTENCES_punc = {"Sentence", "number", "1", "has", "6", "words", ".", "Sentence", "number", "2", ",", "5", "words", "."};
    //static String SENTENCES_chunks[] = {"I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "I-NP", "O", "O", "B-PP", "B-NP", "O", "B-NP", "I-NP", "O"};
    private final static String SENTENCES_chunks[] = {"B-NP", "I-NP", "I-NP", "I-NP", "B-NP", "I-NP", "O", "B-VP", "B-PP", "B-NP", "O", "B-NP", "I-NP", "O"};

    /*@Override
    protected Settings getSettings() {
        return settingsBuilder()
                .put("sentenceModel", "build/en-test-sent.bin")
                .put("tokenizerModel", "build/en-test-tokenizer.bin")
                .put("posTaggerModel", "build/en-test-pos-maxent.bin")
                .put("chunkerModel", "build/en-test-chunker.bin")
                .build();
    }*/

    @Override
    protected void analyze() throws Exception {
        StringReader inputReader = new StringReader(SENTENCES);
        Tokenizer t = tokenizerFactory.create();
        t.setReader(inputReader);
        TokenStream ts = filterFactory.create(t);
        ts.reset();
        walkTerms(ts, "chunks", SENTENCES_punc, SENTENCES_chunks);
        //walkTerms(ts, "chunks", SENTENCES_punc, SENTENCES_chunks);
    }
}
