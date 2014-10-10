package com.hughes.android.dictionary.parser.wiktionary.dom;

public class WikiComment implements WikiElement {
    
    final String text;
    
    WikiComment(String text) {
        this.text = text;
    }

    @Override
    public void append(StringBuilder builder) {
        builder.append(text);
    }

}
