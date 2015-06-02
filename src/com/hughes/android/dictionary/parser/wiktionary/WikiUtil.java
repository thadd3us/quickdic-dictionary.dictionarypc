package com.hughes.android.dictionary.parser.wiktionary;

import com.hughes.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class WikiUtil {
    
    static final Logger LOG = Logger.getLogger("WikiUtil");

    
    public static String instantiateAllTemplateArgs(String templateText, 
            List<String> functionPositionArgs,
            Map<String, String> functionNamedArgs) {
        int argStart = 0;
        while ((argStart = templateText.indexOf("{{{", argStart)) != -1) {
            argStart += 3;
            final int argEnd = StringUtil.nestedIndexOf(templateText, argStart, "{{{", "}}}");
            if (argEnd == -1) {
                LOG.warning("Unterminated {{{: " + templateText);
                continue;
            }
            
            int nameEnd = argStart;
            boolean hasPipe = false;
            for (; nameEnd < templateText.length(); ++nameEnd) {
                if (templateText.charAt(nameEnd) == '|') {
                    hasPipe = true;
                    break;
                }
                if (templateText.charAt(nameEnd) == '}') {
                    break;
                }
            }
            
            final String name = templateText.substring(argStart, nameEnd);
            String value = null;
            if (StringUtil.isDigits(name)) {
                int index = Integer.valueOf(name) - 1;
                if (index < functionPositionArgs.size()) {
                    value = functionPositionArgs.get(index);
                }
            } else {
                value = functionNamedArgs.get(name); 
            }
            if (value == null) {
                if (hasPipe) {
                    value = templateText.substring(nameEnd + 1, argEnd);
                } else {
                    value = "";
                }
            }
            templateText = templateText.substring(0, argStart - 3) + value + templateText.substring(argEnd + 3);
        }
        return templateText;
    }


}
