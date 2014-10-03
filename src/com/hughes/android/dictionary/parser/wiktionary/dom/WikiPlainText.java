package com.hughes.android.dictionary.parser.wiktionary.dom;

public class WikiPlainText implements WikiElement {
    
    String text;

    @Override
    public void append(StringBuilder builder) {
        builder.append(text);
    }

}
