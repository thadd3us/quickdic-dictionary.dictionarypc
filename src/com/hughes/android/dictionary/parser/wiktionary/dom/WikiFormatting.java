package com.hughes.android.dictionary.parser.wiktionary.dom;

public class WikiFormatting {
    
    static class Bold implements WikiElement {
        @Override
        public void append(StringBuilder builder) {
            builder.append("'''");
        }
    };
    static final Bold BOLD = new Bold();
    
    static class Italic implements WikiElement {
        @Override
        public void append(StringBuilder builder) {
            builder.append("''");
        }
    }
    static final Italic ITALIC = new Italic();

    static class Newline implements WikiElement {
        @Override
        public void append(StringBuilder builder) {
            builder.append("\n");
        }
    }
    static final Newline NEWLINE = new Newline();
    
    enum HtmlStyleType {
        MATH,
        SUBSCRIPT,
        SUPERSCRIPT,
        PRE,
        REF,
    }
    static class HtmlStyle {
        HtmlStyleType htmlStyleType;
        WikiChunk wikiChunk;
    };

}
