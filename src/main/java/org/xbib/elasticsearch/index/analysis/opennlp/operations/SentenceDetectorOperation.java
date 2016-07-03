package org.xbib.elasticsearch.index.analysis.opennlp.operations;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.Span;

import java.io.IOException;

public class SentenceDetectorOperation {

    private final SentenceDetectorME sentenceSplitter;

    public SentenceDetectorOperation(SentenceModel model) throws IOException {
        sentenceSplitter = new SentenceDetectorME(model);
    }

    public synchronized Span[] splitSentences(String line) {
        return sentenceSplitter.sentPosDetect(line);
    }

}
