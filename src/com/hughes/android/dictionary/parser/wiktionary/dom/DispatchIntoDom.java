package com.hughes.android.dictionary.parser.wiktionary.dom;

import com.hughes.android.dictionary.parser.WikiTokenizer;
import com.hughes.util.ListUtil;
import com.hughes.util.StringUtil;

import java.util.List;
import java.util.Map;

public class DispatchIntoDom {
    
    static class Callback implements WikiTokenizer.Callback {
        
        final WikiChunk wikiChunk;
        boolean wasListItem = false;
        
        private Callback(WikiChunk chunk) {
            assert chunk != null;
            this.wikiChunk = chunk;
        }

        @Override
        public void onPlainText(String text) {
            wikiChunk.wikiElements.add(new WikiPlainText(text));
        }

        @Override
        public void onMarkup(WikiTokenizer wikiTokenizer) {
            if (wikiTokenizer.token().equals("'''")) {
                wikiChunk.wikiElements.add(WikiFormatting.BOLD);
            } else if (wikiTokenizer.token().equals("''")) {
                wikiChunk.wikiElements.add(WikiFormatting.ITALIC);
            } else {
                throw new RuntimeException();
            }

        }

        @Override
        public void onWikiLink(WikiTokenizer wikiTokenizer) {
            final WikiLink wikiLink = new WikiLink();
            wikiLink.dest = wikiTokenizer.wikiLinkDest();
            wikiLink.displayChunk = new WikiChunk();
            WikiTokenizer.dispatch(wikiTokenizer.wikiLinkText(), 
                    false, 
                    new Callback(wikiLink.displayChunk));
            wikiChunk.wikiElements.add(wikiLink);
        }

        @Override
        public void onNewline(WikiTokenizer wikiTokenizer) {
            if (!wasListItem) {
                wikiChunk.wikiElements.add(WikiFormatting.NEWLINE);
            }
            wasListItem = false;
        }

        @Override
        public void onFunction(WikiTokenizer tokenizer, String functionName,
                List<String> functionPositionArgs, Map<String, String> functionNamedArgs) {
            final WikiTemplate wikiTemplate = new WikiTemplate();
            wikiTemplate.name = functionName;
            for (final String positionArg : functionPositionArgs) {
                WikiChunk arg = new WikiChunk();
                WikiTokenizer.dispatch(positionArg, false, new Callback(arg));
                wikiTemplate.positionArgs.add(arg);
            }
            for (final Map.Entry<String, String> namedArg : functionNamedArgs.entrySet()) {
                WikiChunk arg = new WikiChunk();
                WikiTokenizer.dispatch(namedArg.getValue(), false, new Callback(arg));
                wikiTemplate.namedArgs.put(namedArg.getKey(), arg);
            }
            wikiChunk.wikiElements.add(wikiTemplate);
        }

        @Override
        public void onHeading(WikiTokenizer wikiTokenizer) {
            WikiHeading wikiHeading = new WikiHeading();
            wikiChunk.wikiElements.add(wikiHeading);
            wikiHeading.depth = wikiTokenizer.headingDepth();
            WikiTokenizer.dispatch(wikiTokenizer.headingWikiText(), 
                    false, new Callback(wikiHeading.name));
        }

        @Override
        public void onListItem(WikiTokenizer wikiTokenizer) {
            WikiList wikiList;
            if (!wikiChunk.wikiElements.isEmpty() && ListUtil.getLast(wikiChunk.wikiElements) instanceof WikiList) {
                wikiList = (WikiList) ListUtil.getLast(wikiChunk.wikiElements);
            } else {
                wikiList = new WikiList();
                wikiChunk.wikiElements.add(wikiList);
            }
            
            final WikiList.Item item = new WikiList.Item(wikiTokenizer.listItemPrefix());
            wikiList.items.add(item);
            String itemWikiText = wikiTokenizer.listItemWikiText();
            if (itemWikiText.endsWith("\n")) {
                itemWikiText = StringUtil.replaceLast(itemWikiText, "\n", ""); 
            }
            WikiTokenizer.dispatch(itemWikiText, false, new Callback(item.wikiChunk));
            wasListItem = true;
            
        }

        @Override
        public void onComment(WikiTokenizer wikiTokenizer) {
            wikiChunk.wikiElements.add(new WikiComment(wikiTokenizer.token()));
        }

        @Override
        public void onHtml(WikiTokenizer wikiTokenizer) {
            // TODO
        }
        
    }
    
    public static WikiChunk go(final String wikiText) {
        final Callback callback = new Callback(new WikiChunk());
        WikiTokenizer.dispatch(wikiText, true, callback);
        final List<WikiElement> wikiElements = callback.wikiChunk.wikiElements; 
        
        // Cleanup.
        for (int i = 0; i + 1 < wikiElements.size(); ++i) {
            if (wikiElements.get(i) instanceof WikiFormatting.Newline) {
                if (wikiElements.get(i + 1) instanceof WikiHeading) {
                    wikiElements.remove(i);
                    --i;
                }
            } else if (wikiElements.get(i) instanceof WikiHeading) {
                if (wikiElements.get(i + 1) instanceof WikiFormatting.Newline) {
                    wikiElements.remove(i + 1);
                }
            }
        }
        return callback.wikiChunk;
    }

}
