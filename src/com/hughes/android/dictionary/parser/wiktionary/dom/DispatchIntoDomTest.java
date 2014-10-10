package com.hughes.android.dictionary.parser.wiktionary.dom;

import static org.junit.Assert.*;

import org.junit.Test;

public class DispatchIntoDomTest {

    @Test
    public void test1() {
        final String wikiText =
                "Hi" + "\n" +
                "Hello =thad| you're <!-- not --> '''pretty''' cool '''''over''''' there." + "\n" +
                "hi <!--" + "\n" +
                "multi-line" + "\n" +
                "# comment -->" + "\n" +
                "" + "\n" +
                "asdf\n" +
                "{{template_not_in_list}}" + "\n" +
                "# {{template_in_list}}" + "\n" +
                "[[wikitext]]:[[wikitext]]" + "\n" +  // don't want this to trigger a list
                ": but this is a list!" + "\n" +
                ": and this is the second item" + "\n" +
                "*:* and so is this :::" + "\n" +
                "here's [[some blah|some]] wikitext." + "\n" +
                "here's a {{template|[[asdf]|[asdf asdf asdf]]|this has an = sign|blah=2|blah2=3|blah3=3,blah4=4}} and some more text." + "\n" +
                "== Header 2 ==" + "\n" +
                "{{some-func|blah={{nested-func|n2}}|blah2=asdf}}" + "\n" +
                "=== {{header-template}} ===" + "\n";
        
        WikiChunk wikiChunk = DispatchIntoDom.go(wikiText);
        final StringBuilder builder = new StringBuilder();
        wikiChunk.append(builder);
        assertEquals(wikiText, builder.toString());
    }

}
