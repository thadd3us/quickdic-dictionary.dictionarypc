package com.hughes.android.dictionary.parser.wiktionary.dom;

public class WikiLink implements WikiElement {
    
    String dest;
    WikiChunk displayChunk;
    
    @Override
    public void append(StringBuilder builder) {
        if (dest == null) {
            builder.append("[[");
            displayChunk.append(builder);
            builder.append("]]");
        } else {
            builder.append("[[").append(dest).append("|");
            displayChunk.append(builder);
            builder.append("]]");
        }
    }

}
