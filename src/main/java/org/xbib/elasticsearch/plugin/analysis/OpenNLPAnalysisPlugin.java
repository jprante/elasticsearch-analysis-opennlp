package org.xbib.elasticsearch.plugin.analysis;

import org.elasticsearch.index.analysis.AnalysisModule;
import org.xbib.elasticsearch.index.analysis.opennlp.OpenNLPAnalysisBinderProcessor;
import org.elasticsearch.plugins.Plugin;

public class OpenNLPAnalysisPlugin extends Plugin {

    @Override
    public String name() {
        return "analysis-opennlp";
    }

    @Override
    public String description() {
        return "OpenNLP analysis";
    }

    public void onModule(AnalysisModule module) {
        module.addProcessor(new OpenNLPAnalysisBinderProcessor());
    }
}

