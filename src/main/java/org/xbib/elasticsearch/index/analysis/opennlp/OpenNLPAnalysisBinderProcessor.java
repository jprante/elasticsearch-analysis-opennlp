package org.xbib.elasticsearch.index.analysis.opennlp;

import org.elasticsearch.index.analysis.AnalysisModule;

public class OpenNLPAnalysisBinderProcessor extends AnalysisModule.AnalysisBinderProcessor {

    @Override
    public void processTokenizers(TokenizersBindings tokenizersBindings) {
        tokenizersBindings.processTokenizer("opennlp", OpenNLPTokenizerFactory.class);
    }

    @Override
    public void processTokenFilters(TokenFiltersBindings tokenFiltersBindings) {
        tokenFiltersBindings.processTokenFilter("opennlp", OpenNLPTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("strippayloads", StripPayloadsTokenFilterFactory.class);
        tokenFiltersBindings.processTokenFilter("filterpayloads", FilterPayloadsTokenFilterFactory.class);
    }
}
