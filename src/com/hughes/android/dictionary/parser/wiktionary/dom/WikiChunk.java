package com.hughes.android.dictionary.parser.wiktionary.dom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WikiChunk implements Serializable {
    
    final List<WikiElement> wikiElements = new ArrayList<WikiElement>();
    
    void append(StringBuilder builder) {
        for (final WikiElement wikiElement : wikiElements) {
            wikiElement.append(builder);
        }
    }


}
