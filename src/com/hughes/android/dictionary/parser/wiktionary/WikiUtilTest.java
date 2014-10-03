
package com.hughes.android.dictionary.parser.wiktionary;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WikiUtilTest {

    @Test
    public void test() {
        final Map<String, String> namedArgs = new LinkedHashMap<String, String>();
        final List<String> positionArgs = new ArrayList<String>();
        assertEquals("asdf", WikiUtil.instantiateAllTemplateArgs("asdf", positionArgs, namedArgs));

        positionArgs.add("123");
        assertEquals("a123f",
                WikiUtil.instantiateAllTemplateArgs("a{{{1}}}f", positionArgs, namedArgs));
        assertEquals("a123f123d",
                WikiUtil.instantiateAllTemplateArgs("a{{{1}}}f{{{1}}}d", positionArgs, namedArgs));
        assertEquals("a123fd",
                WikiUtil.instantiateAllTemplateArgs("a{{{1}}}f{{{2}}}d", positionArgs, namedArgs));
        assertEquals("a123fXYZd",
                WikiUtil.instantiateAllTemplateArgs("a{{{1}}}f{{{2|XYZ}}}d", positionArgs, namedArgs));
        assertEquals("a123fXYZd",
                WikiUtil.instantiateAllTemplateArgs("a{{{1}}}f{{{gMoney|XYZ}}}d", positionArgs, namedArgs));
        
        namedArgs.put("gMoney", "$100");
        assertEquals("a123f$100d",
                WikiUtil.instantiateAllTemplateArgs("a{{{1}}}f{{{gMoney|XYZ}}}d", positionArgs, namedArgs));
    }

}
