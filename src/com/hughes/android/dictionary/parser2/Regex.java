package com.hughes.android.dictionary.parser2;

import java.util.regex.Pattern;

public class Regex {
    
    // Dictcc
    public static final Pattern TAB = Pattern.compile("\\t");

    // Chemnitz
    public static final Pattern DOUBLE_COLON = Pattern.compile(" :: ");
    public static final Pattern PIPE = Pattern.compile("\\|");
    
    static final Pattern SPACES = Pattern.compile("\\s+");
    
    static final Pattern BRACKETED = Pattern.compile("\\[([^]]+)\\]");
    static final Pattern PARENTHESIZED = Pattern.compile("\\(([^)]+)\\)");
    static final Pattern CURLY_BRACED = Pattern.compile("\\{([^}]+)\\}");
    
    // http://www.regular-expressions.info/unicode.html
    static final Pattern NON_CHAR_DASH = Pattern.compile("[^-'\\p{L}\\p{M}\\p{N}]+");
    public static final Pattern NON_CHAR = com.hughes.android.dictionary.engine2.Regex.NON_CHAR;

    static final Pattern TRIM_PUNC = Pattern.compile("^[^\\p{L}\\p{M}\\p{N}]+|[^\\p{L}\\p{M}\\p{N}]+$");


}
