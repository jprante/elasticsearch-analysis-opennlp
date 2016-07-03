package org.xbib.elasticsearch.index.analysis.opennlp;

import opennlp.tools.chunker.ChunkSampleStream;
import opennlp.tools.chunker.ChunkerFactory;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.namefind.BioCodec;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSDictionary;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.WordTagSampleStream;
import opennlp.tools.sentdetect.SentenceDetectorFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.sentdetect.SentenceSampleStream;
import opennlp.tools.tokenize.TokenSample;
import opennlp.tools.tokenize.TokenSampleStream;
import opennlp.tools.tokenize.TokenizerFactory;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.SequenceCodec;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.model.ModelType;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

public class Trainer {

    public static void trainSentences(final String inResource, String outFile) throws IOException {
        InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                return Trainer.class.getResourceAsStream(inResource);
            }
        };
        SentenceSampleStream samples = new SentenceSampleStream(new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8));
        TrainingParameters trainingParameters = new TrainingParameters();
        trainingParameters.put(TrainingParameters.ALGORITHM_PARAM, ModelType.MAXENT.name());
        trainingParameters.put(TrainingParameters.ITERATIONS_PARAM, "100");
        trainingParameters.put(TrainingParameters.CUTOFF_PARAM, "0");
        SentenceDetectorFactory sentenceDetectorFactory = SentenceDetectorFactory.create(null, "en", true, null, ".?!".toCharArray());
        SentenceModel sentdetectModel = SentenceDetectorME.train("en", samples, sentenceDetectorFactory, trainingParameters);
        //.train("en", samples, true, null, 100, 0);
        samples.close();
        FileOutputStream out = new FileOutputStream(outFile);
        sentdetectModel.serialize(out);
        out.close();
    }

    public static void trainTokenizer(final String inResource, String outFile) throws IOException {
        InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                return Trainer.class.getResourceAsStream(inResource);
            }
        };
        ObjectStream<TokenSample> samples = new TokenSampleStream(new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8));
        TrainingParameters trainingParameters = new TrainingParameters();
        trainingParameters.put(TrainingParameters.ITERATIONS_PARAM, "100");
        trainingParameters.put(TrainingParameters.CUTOFF_PARAM, "5");
        String subclassname = null;
        String langcode = "en";
        Dictionary dict = null;
        Pattern alphanumericpattern = null;

        opennlp.tools.tokenize.TokenizerFactory tokenizerFactory = TokenizerFactory.create(subclassname, langcode, dict, true, alphanumericpattern);
        TokenizerModel model = TokenizerME.train(samples, tokenizerFactory, trainingParameters);
        //TokenizerME.train("en", samples, true, 5, 100);
        samples.close();
        FileOutputStream out = new FileOutputStream(outFile);
        model.serialize(out);
        out.close();
    }

    public static void trainPOS(final String inResource, String outFile) throws IOException {
        InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                return Trainer.class.getResourceAsStream(inResource);
            }
        };
        WordTagSampleStream samples = new WordTagSampleStream(new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8));
        TrainingParameters trainingParameters = new TrainingParameters();
        trainingParameters.put(TrainingParameters.ALGORITHM_PARAM, ModelType.MAXENT.name());
        trainingParameters.put(TrainingParameters.ITERATIONS_PARAM, "100");
        trainingParameters.put(TrainingParameters.CUTOFF_PARAM, "5");
        Dictionary ngramDictionary = null;
        POSDictionary posDictionary = null;
        POSTaggerFactory posTaggerFactory = POSTaggerFactory.create(null, ngramDictionary, posDictionary);
        POSModel model = POSTaggerME.train("en", samples, trainingParameters, posTaggerFactory);
        //POSTaggerME.train("en", samples, ModelType.MAXENT, null, null, 5, 100);
        samples.close();
        FileOutputStream out = new FileOutputStream(outFile);
        model.serialize(out);
        out.close();
    }

    public static void trainChunker(final String inResource, String outFile) throws IOException {
        InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                return Trainer.class.getResourceAsStream(inResource);
            }
        };
        ChunkSampleStream samples = new ChunkSampleStream(new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8));
        TrainingParameters trainingParameters = new TrainingParameters();
        trainingParameters.put(TrainingParameters.ITERATIONS_PARAM, "70");
        trainingParameters.put(TrainingParameters.CUTOFF_PARAM, "1");

        ChunkerFactory chunkerFactory = ChunkerFactory.create(null);
        ChunkerModel model = ChunkerME.train("en", samples, trainingParameters, chunkerFactory);
        //ChunkerME.train("en", samples, 1, 70);
        samples.close();
        FileOutputStream out = new FileOutputStream(outFile);
        model.serialize(out);
        out.close();
    }

    public static void trainNameFinder(final String inResource, String outFile) throws IOException {
        InputStreamFactory inputStreamFactory = new InputStreamFactory() {
            @Override
            public InputStream createInputStream() throws IOException {
                return Trainer.class.getResourceAsStream(inResource);
            }
        };
        InputStream in = Trainer.class.getResourceAsStream(inResource);
        NameSampleDataStream samples = new NameSampleDataStream(new PlainTextByLineStream(inputStreamFactory, StandardCharsets.UTF_8));
        TrainingParameters trainingParameters = new TrainingParameters();
        trainingParameters.put(TrainingParameters.ITERATIONS_PARAM, "5");
        trainingParameters.put(TrainingParameters.CUTOFF_PARAM, "200");
        byte[] featureGeneratorBytes = null;
        Map<String, Object> resources = Collections.<String, Object>emptyMap();
        SequenceCodec<String> seqCodec = new BioCodec();
        TokenNameFinderFactory tokenNameFinderFactory = TokenNameFinderFactory.create(null, featureGeneratorBytes, resources, seqCodec);
        TokenNameFinderModel model = NameFinderME.train("en", "person", samples, trainingParameters, tokenNameFinderFactory);
        //NameFinderME.train("en", "person", samples, Collections.<String, Object>emptyMap(), 200, 5);
        samples.close();
        FileOutputStream out = new FileOutputStream(outFile);
        model.serialize(out);
        out.close();
    }
}
