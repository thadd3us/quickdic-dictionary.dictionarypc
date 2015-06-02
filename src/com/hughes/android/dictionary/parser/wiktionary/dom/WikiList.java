package com.hughes.android.dictionary.parser.wiktionary.dom;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class WikiList extends WikiElement {
    
    static class Item implements Serializable {
        final String listMarker;
        final WikiChunk wikiChunk = new WikiChunk();
        
        Item(String listMarker) {
            this.listMarker = listMarker;
        }
    }
    
    final List<Item> items = new ArrayList<Item>();

    @Override
    public void append(StringBuilder builder) {
//        builder.append("\n");
        for (final Item item : items) {
            builder.append(item.listMarker).append("");
            item.wikiChunk.append(builder);
            builder.append("\n");
        }
    }

}
