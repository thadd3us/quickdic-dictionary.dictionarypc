package com.hughes.android.dictionary.parser.wiktionary.dom;

import java.io.Serializable;

public interface WikiElement extends Serializable {
    
    void append(StringBuilder builder);

}
