package com.hughes.android.dictionary.parser.wiktionary.dom;

import java.io.Serializable;
import java.util.List;

public class WikiChunk implements Serializable {
    
    List<WikiElement> wikiElements;
    
    void append(StringBuilder builder) {
        for (final WikiElement wikiElement : wikiElements) {
            wikiElement.append(builder);
        }
    }


}
