package org.xbib.elasticsearch.index.analysis.opennlp;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.AnalysisSettingsRequired;
import org.elasticsearch.index.settings.IndexSettingsService;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@AnalysisSettingsRequired
public class FilterPayloadsTokenFilterFactory extends AbstractTokenFilterFactory {

    private final byte[][] payloads;

    private final boolean keep;

    @Inject
    public FilterPayloadsTokenFilterFactory(Index index,
                                            IndexSettingsService indexSettingsService,
                                            @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.indexSettings(), name, settings);
        this.keep = settings.getAsBoolean("keep", false);
        String[] payloadStrings = settings.getAsArray("payloads");
        List<byte[]> list = new ArrayList<>();
        for (String payloadString : payloadStrings) {
            list.add(payloadString.getBytes(StandardCharsets.UTF_8));
        }
        payloads = list.toArray(new byte[0][0]);
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new FilterPayloadsFilter(tokenStream, payloads, keep);
    }
}
