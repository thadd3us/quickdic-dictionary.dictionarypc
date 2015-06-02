package com.hughes.android.dictionary.parser.wiktionary.dom;

public class WikiHeading extends WikiElement {
    
    int depth;
    final WikiChunk name = new WikiChunk();

    @Override
    public void append(StringBuilder builder) {
        builder.append("\n");
        for (int i = 0; i < depth; ++i) {
            builder.append("=");
        }
        name.append(builder);
        for (int i = 0; i < depth; ++i) {
            builder.append("=");
        }
        builder.append("\n");
    }
    

}
