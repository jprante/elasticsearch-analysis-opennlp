package org.xbib.elasticsearch.index.analysis.opennlp.operations;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;

import java.io.IOException;

public class ChunkerOperation {

    private final ChunkerME chunker;

    public ChunkerOperation(ChunkerModel chunkerModel) throws IOException {
        chunker = new ChunkerME(chunkerModel);
    }

    public synchronized String[] getChunks(String[] words, String[] tags, double[] probs) {
        String[] chunks = chunker.chunk(words, tags);
        if (probs != null) {
            chunker.probs(probs);
        }
        return chunks;
    }
}
