package com.hughes.android.dictionary.parser.wiktionary.dom;

import com.hughes.android.dictionary.parser.WikiTokenizer;

import java.util.List;
import java.util.Map;

public class DispatchIntoDom {
    
    static class Callback implements WikiTokenizer.Callback {

        @Override
        public void onPlainText(String text) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onMarkup(WikiTokenizer wikiTokenizer) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onWikiLink(WikiTokenizer wikiTokenizer) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onNewline(WikiTokenizer wikiTokenizer) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onFunction(WikiTokenizer tokenizer, String functionName,
                List<String> functionPositionArgs, Map<String, String> functionNamedArgs) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onHeading(WikiTokenizer wikiTokenizer) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onListItem(WikiTokenizer wikiTokenizer) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onComment(WikiTokenizer wikiTokenizer) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onHtml(WikiTokenizer wikiTokenizer) {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    static WikiChunk go(final String wikiText) {
        WikiTokenizer.dispatch(wikiText, true, new Callback());
    }

}
