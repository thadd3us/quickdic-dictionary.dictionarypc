package com.hughes.android.dictionary.parser.wiktionary.dom;

public class WikiFormatting {
    
    class Bold implements WikiElement {

        @Override
        public void append(StringBuilder builder) {
            builder.append("'''");
        }
        
    };
    
    class Italic implements WikiElement {

        @Override
        public void append(StringBuilder builder) {
            builder.append("''");
        }

    }

}
