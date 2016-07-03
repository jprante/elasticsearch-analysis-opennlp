package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalysisService;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.instanceOf;

/**
 * Needs the OpenNLP Tokenizer because it creates full streams of punctuation.
 * The POS, Chunking and NER models are based on this tokenization.
 *
 * Tagging models are created from tiny test data in
 * contrib/opennlp/test-files/training and are not very accurate. Chunking in
 * particular is garbage. NER training generally recognizes sentences that end
 * with "Flashman." The period is required.
 */
public abstract class BaseOpenNLPAnalysis extends Assert {

    OpenNLPTokenizerFactory tokenizerFactory;
    OpenNLPTokenFilterFactory filterFactory;

    @Before
    public void train() throws IOException {
        Trainer.trainSentences("/sentences.txt", "build/en-test-sent.bin");
        Trainer.trainTokenizer("/tokenizer.txt", "build/en-test-tokenizer.bin");
        Trainer.trainPOS("/pos.txt", "build/en-test-pos-maxent.bin");
        Trainer.trainChunker("/chunks.txt", "build/en-test-chunker.bin");
        Trainer.trainNameFinder("/ner/ner_flashman.txt", "build/en-test-ner-person.bin");
    }

    protected String getResource() {
        return "/org/xbib/elasticsearch/index/analysis/opennlp/analysis.json";
    }

    protected abstract void analyze() throws Exception;

    @Test
    public void testOpenNLPAnalysis() throws Exception {
        AnalysisService analysisService = MapperTestUtils.analysisService(getResource());
        TokenizerFactory tokenizerFactory = analysisService.tokenizer("opennlp");
        MatcherAssert.assertThat(tokenizerFactory, instanceOf(OpenNLPTokenizerFactory.class));
        this.tokenizerFactory = (OpenNLPTokenizerFactory) tokenizerFactory;
        TokenFilterFactory filterFactory = analysisService.tokenFilter("opennlp");
        MatcherAssert.assertThat(filterFactory, instanceOf(OpenNLPTokenFilterFactory.class));
        this.filterFactory = (OpenNLPTokenFilterFactory) filterFactory;

        TokenFilterFactory filterpayloadsfilterFactory = analysisService.tokenFilter("filterpayloads");
        MatcherAssert.assertThat(filterpayloadsfilterFactory, instanceOf(FilterPayloadsTokenFilterFactory.class));
        TokenFilterFactory strippayloadsfilterFactory = analysisService.tokenFilter("strippayloads");
        MatcherAssert.assertThat(strippayloadsfilterFactory, instanceOf(StripPayloadsTokenFilterFactory.class));

        analyze();
    }

    void walkTerms(TokenStream ts, String op, String[] terms, String[] tags) throws IOException {
        int i = 0;
        while (ts.incrementToken()) {
            CharTermAttribute termAtt = ts.getAttribute(CharTermAttribute.class);
            String word = termAtt.toString();
            if (terms != null) {
                assertEquals(terms[i], word);
            }
            if (tags != null) {
                if (tags[i] != null) {
                    PayloadAttribute p = ts.getAttribute(PayloadAttribute.class);
                    BytesRef payload = p.getPayload();
                    //Arrays.copyOfRange(payload.bytes, payload.offset, payload.offset + payload.length);
                    byte[] data = payload.bytes;
                    assertEquals(tags[i], (data != null) ? new String(data, "UTF-8") : null);
                }
            }
            i++;
        }
        if (terms != null) {
            assertEquals(terms.length, i);
        }
    }


}
