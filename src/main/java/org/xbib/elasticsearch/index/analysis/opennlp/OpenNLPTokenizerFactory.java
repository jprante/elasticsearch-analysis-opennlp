package org.xbib.elasticsearch.index.analysis.opennlp;

import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.lucene.analysis.Tokenizer;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.SentenceDetectorOperation;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.TokenizeOperation;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import org.elasticsearch.index.analysis.AnalysisSettingsRequired;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.xbib.elasticsearch.plugin.analysis.OpenNLPAnalysisPlugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

@AnalysisSettingsRequired
public class OpenNLPTokenizerFactory extends AbstractTokenizerFactory {

    private final SentenceDetectorOperation sentenceOp;
    private final TokenizeOperation tokenizerOp;

    @Inject
    public OpenNLPTokenizerFactory(Index index, IndexSettingsService indexSettingsService,
                                   @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        try {
            String sentenceModelFile = settings.get("sentenceModel");
            if (sentenceModelFile != null) {
                InputStream in = getInputStream(sentenceModelFile);
                if (in != null) {
                    SentenceModel sentenceModel = new SentenceModel(in);
                    sentenceOp = new SentenceDetectorOperation(sentenceModel);
                    in.close();
                } else {
                    sentenceOp = null;
                }
            } else {
                sentenceOp = null;
            }
            String tokenizerModelFile = settings.get("tokenizerModel");
            if (tokenizerModelFile != null) {
                InputStream in = getInputStream(tokenizerModelFile);
                if (in != null) {
                    TokenizerModel tokenizerModel = new TokenizerModel(in);
                    tokenizerOp = new TokenizeOperation(tokenizerModel);
                    in.close();
                } else {
                    tokenizerOp = null;
                }
            } else {
                tokenizerOp = null;
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    @Override
    public Tokenizer create() {
        try {
            return new OpenNLPTokenizer(sentenceOp, tokenizerOp);
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    private InputStream getInputStream(String setting) {
        URL url;
        try {
            url = new URL(setting);
            return url.openStream();
        } catch (Exception e1) {
            try {
                url = new URL("file://" + setting);
                return url.openStream();
            } catch (Exception e2) {
                try {
                    return OpenNLPAnalysisPlugin.class.getClassLoader().getResource(setting).openStream();
                } catch (Exception e3) {
                    try {
                        return new FileInputStream(setting);
                    } catch (Exception e4) {
                        return null;
                    }
                }
            }
        }
    }

}
