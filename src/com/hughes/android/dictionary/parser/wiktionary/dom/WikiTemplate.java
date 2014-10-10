package com.hughes.android.dictionary.parser.wiktionary.dom;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WikiTemplate implements WikiElement {
    
    String name;
    final List<WikiChunk> positionArgs = new ArrayList<WikiChunk>();
    final Map<String, WikiChunk> namedArgs = new LinkedHashMap<String, WikiChunk>();
    
    
    WikiChunk expanded;

    @Override
    public void append(StringBuilder builder) {
        builder.append("{{");
        builder.append(name);
        for (final WikiChunk positionArg : positionArgs) {
            builder.append("|");
            positionArg.append(builder);
        }
        for (final Map.Entry<String, WikiChunk> entry : namedArgs.entrySet()) {
            builder.append("|");
            builder.append(entry.getKey());
            builder.append("=");
            entry.getValue().append(builder);
        }
        builder.append("}}");
    }

}
