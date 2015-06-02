package com.hughes.android.dictionary.parser.wiktionary.dom;

public class WikiPlainText extends WikiElement {
    
    String text;

    public WikiPlainText(String text) {
        this.text = text;
    }

    @Override
    public void append(StringBuilder builder) {
        builder.append(text);
    }

}
