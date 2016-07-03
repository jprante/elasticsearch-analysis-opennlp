package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.AnalysisSettingsRequired;
import org.elasticsearch.index.settings.IndexSettingsService;

@AnalysisSettingsRequired
public class StripPayloadsTokenFilterFactory extends AbstractTokenFilterFactory {

    @Inject
    public StripPayloadsTokenFilterFactory(Index index,
                                           IndexSettingsService indexSettingsService,
                                           @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new StripPayloadsTokenFilter(tokenStream);
    }
}
