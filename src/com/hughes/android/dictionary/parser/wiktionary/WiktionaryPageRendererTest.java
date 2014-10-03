package com.hughes.android.dictionary.parser.wiktionary;

import com.hughes.android.dictionary.engine.WiktionarySplitter.Article;
import com.hughes.android.dictionary.parser.wiktionary.WiktionaryLangs.WiktionaryDescriptor;
import com.hughes.android.dictionary.parser.wiktionary.WiktionaryPageRenderer.Section;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class WiktionaryPageRendererTest {
    
    public static final String WIKISPLIT = "data/inputs/wikiSplit/";


    @Test
    public void test() throws IOException {
        WiktionaryDescriptor descriptor = WiktionaryLangs.wikiCodeToWiktionaryDescriptor.get("EN");
        WiktionaryPageRenderer renderer = new WiktionaryPageRenderer(descriptor, new File(WIKISPLIT + "en"));
        
        final Map<String, Article> enENPages = new LinkedHashMap<String, Article>();
        WiktionaryPageRenderer.loadAndIndexPages(new File(WIKISPLIT + "en/EN.data"), 100, enENPages);
        
        for (final Article article : enENPages.values()) {
            System.out.println(article.title);
            Section section = renderer.renderPage(article.title, article.text);
            System.out.println(section.toStringBuilder(new StringBuilder()).toString());
        }
           
    }

}
