package org.xbib.elasticsearch.index.analysis.opennlp;

import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import org.apache.lucene.analysis.TokenStream;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.ChunkerOperation;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.NamedEntityRecognitionOperation;
import org.xbib.elasticsearch.index.analysis.opennlp.operations.PartOfSpeechOperation;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.AnalysisSettingsRequired;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.xbib.elasticsearch.plugin.analysis.OpenNLPAnalysisPlugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@AnalysisSettingsRequired
public class OpenNLPTokenFilterFactory extends AbstractTokenFilterFactory {

    private final PartOfSpeechOperation posTaggerOp;
    private final ChunkerOperation chunkerOp;
    private final List<NamedEntityRecognitionOperation> nerTaggerOps;

    @Inject
    public OpenNLPTokenFilterFactory(Index index,
                                     IndexSettingsService indexSettingsService,
                                     @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        try {
            String posTaggerModelFile = settings.get("posTaggerModel");
            if (posTaggerModelFile == null) {
                posTaggerOp = null;
            } else {
                InputStream in = getInputStream(posTaggerModelFile);
                if (in != null) {
                    POSModel posTaggerModel = new POSModel(in);
                    posTaggerOp = new PartOfSpeechOperation(posTaggerModel);
                    in.close();
                } else {
                    posTaggerOp = null;
                }
            }
            String chunkerModelFile = settings.get("chunkerModel");
            if (chunkerModelFile == null) {
                chunkerOp = null;
            } else {
                InputStream in = getInputStream(chunkerModelFile);
                if (in != null) {
                    ChunkerModel chunkerModel = new ChunkerModel(in);
                    chunkerOp = new ChunkerOperation(chunkerModel);
                    in.close();
                } else {
                    chunkerOp = null;
                }
            }
            String nerTaggerModelFiles = settings.get("nerTaggerModels");
            if (nerTaggerModelFiles == null) {
                nerTaggerOps = null;
            } else {
                nerTaggerOps = new ArrayList<>();
                for (String file : nerTaggerModelFiles.split(",")) {
                    InputStream in = getInputStream(file);
                    if (in != null) {
                        TokenNameFinderModel model = new TokenNameFinderModel(in);
                        NamedEntityRecognitionOperation op = new NamedEntityRecognitionOperation(model);
                        nerTaggerOps.add(op);
                        in.close();
                    }
                }
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        try {
            return new OpenNLPTokenFilter(tokenStream, posTaggerOp, chunkerOp, nerTaggerOps);
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
