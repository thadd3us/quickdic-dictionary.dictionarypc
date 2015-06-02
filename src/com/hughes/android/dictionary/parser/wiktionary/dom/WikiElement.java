package com.hughes.android.dictionary.parser.wiktionary.dom;

import java.io.Serializable;

public abstract class WikiElement implements Serializable {
    
    abstract void append(StringBuilder builder);
    
}
