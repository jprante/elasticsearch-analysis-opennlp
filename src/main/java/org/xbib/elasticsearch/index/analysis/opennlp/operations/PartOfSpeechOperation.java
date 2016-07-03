package org.xbib.elasticsearch.index.analysis.opennlp.operations;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;

import java.io.IOException;

public class PartOfSpeechOperation {

    private final POSTagger tagger;

    public PartOfSpeechOperation(POSModel model) throws IOException {
        this.tagger = new POSTaggerME(model);
    }

    public synchronized String[] getPOSTags(String[] words) {
        return tagger.tag(words);
    }
}
