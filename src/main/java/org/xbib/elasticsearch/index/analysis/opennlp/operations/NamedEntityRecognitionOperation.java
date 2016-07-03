package org.xbib.elasticsearch.index.analysis.opennlp.operations;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

public class NamedEntityRecognitionOperation {

    private final TokenNameFinder nameFinder;

    public NamedEntityRecognitionOperation(TokenNameFinderModel model) {
        this.nameFinder = new NameFinderME(model);
    }

    public String[] createAll(String[] words) {
        Span[] nerSpans;
        synchronized(nameFinder) {
            nerSpans = nameFinder.find(words);
            nameFinder.clearAdaptiveData();
        }
        String[] nerTags = new String[words.length];
        if (nerSpans.length == 0) {
            return nerTags;
        }
        String tag = nerSpans[0].getType();
        for (Span tagged : nerSpans) {
            for (int j = tagged.getStart(); j < tagged.getEnd(); j++) {
                nerTags[j] = tag;
            }
        }
        return nerTags;
    }
}
