package com.hughes.android.dictionary.parser.wiktionary;

import com.hughes.android.dictionary.engine.EntrySource;
import com.hughes.android.dictionary.engine2.TrieBuilder;
import com.hughes.android.dictionary.parser.Parser;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class ParsedWiktionaryParser implements Parser {

    @Override
    public void parse(File file, EntrySource entrySource, int pageLimit) throws IOException {
        // TODO Auto-generated method stub
        
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
      TrieBuilder<Integer> trieBuilder = new TrieBuilder<Integer>();
      
      final DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(
              args[0])));
      


    }

}
