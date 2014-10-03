package com.hughes.android.dictionary.parser.wiktionary.dom;

import java.util.List;
import java.util.Map;

public class WikiTemplate implements WikiElement {
    
    String name;
    List<WikiChunk> positionArgs;
    Map<String, WikiChunk> namedArgs;
    
    
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
