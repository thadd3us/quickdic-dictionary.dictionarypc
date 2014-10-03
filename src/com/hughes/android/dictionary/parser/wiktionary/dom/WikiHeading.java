package com.hughes.android.dictionary.parser.wiktionary.dom;

public class WikiHeading implements WikiElement {
    
    int depth;
    WikiChunk name;

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
