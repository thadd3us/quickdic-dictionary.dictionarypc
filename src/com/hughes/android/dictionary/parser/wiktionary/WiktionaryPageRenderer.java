
package com.hughes.android.dictionary.parser.wiktionary;

import com.hughes.android.dictionary.engine.WiktionarySplitter;
import com.hughes.android.dictionary.engine.WiktionarySplitter.Article;
import com.hughes.android.dictionary.parser.WikiTokenizer;
import com.hughes.android.dictionary.parser.WikiTokenizer.Callback;
import com.hughes.util.FileUtil;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiktionaryPageRenderer {
    
    static final Logger LOG = Logger.getLogger("WiktionaryPageRenderer");

    static class Section {
        final Section parent;
        int headingDepth;
        final StringBuilder html = new StringBuilder();
        final List<Section> subsections = new ArrayList<Section>();

        private Section(Section parent, int headingDepth) {
            this.parent = parent;
            this.headingDepth = headingDepth;
        }

        StringBuilder toStringBuilder(final StringBuilder builder) {
            for (int i = 0; i < headingDepth; ++i) {
                builder.append("\t");
            }
            builder.append(html).append("\n");
            for (final Section subsection : subsections) {
                subsection.toStringBuilder(builder);
            }
            return builder;
        }
    }

    static void loadAndIndexPages(final File file, int pageLimit, final Map<String, Article> output)
            throws IOException {
        assert file.exists() && file.canRead();
        final DataInputStream dis = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)));
        try {
            int pageCount = 0;
            while (true) {
                if (pageLimit >= 0 && pageCount >= pageLimit) {
                    return;
                }

                Article article = new WiktionarySplitter.Article(dis);
                if (output.containsKey(article.title)) {
//                    LOG.info("Duplicate title: " + article.title + 
//                            ", \n\nthisOne:" + article.text + "\n\nlastOne:" + output.get(article.title).text);
                    // This can happen when an article has multiple usable sections.
                    
                }
                output.put(article.title, article);
                ++pageCount;
            }
        } catch (EOFException e) {
            return;
        } finally {
            LOG.info("loaded pages: " + output.size());
            dis.close();
        }
    }


    interface CustomTemplateHander {
    }
    final Map<String, CustomTemplateHander> customTemplateHander = 
            new LinkedHashMap<String, CustomTemplateHander>();

    final Map<String, Article> templates = new LinkedHashMap<String, Article>();
    final Map<String, Article> modules = new LinkedHashMap<String, Article>();
    
    LuaEnvironment luaEnvironment = new LuaEnvironment();
    
    public WiktionaryPageRenderer(final WiktionaryLangs.WiktionaryDescriptor descriptor,
            final File dir) throws IOException {
        loadAndIndexPages(new File(dir, "Templates.data"), -1, templates);
        loadAndIndexPages(new File(dir, "Modules.data"), -1, modules);
        
        LOG.info("Loading MediaWiki stub framework.");
        final String mw = FileUtil.readToString(new File("src/com/hughes/android/dictionary/parser/wiktionary/mw.lua"));
        luaEnvironment.globals.load(mw, "mw.lua").call();
        
        for (final Article module : modules.values()) {
            luaEnvironment.preloadModule(
                    "Module:" + module.title, module.text);
        }
        LOG.info("Preloaded all modules.");
        LuaValue m = luaEnvironment.globals.load("s = ''; for k,v in pairs(package.loaded) do s = s .. k .. ' '; end return s;", "myscript").call();
        LOG.info(m.tojstring());
        
        m = luaEnvironment.globals.load("s = ''; for k,v in pairs(package.preload) do s = s .. k .. ' '; end return s;", "myscript").call();
        LOG.info(m.tojstring());

        //        LuaValue m = luaEnvironment.globals.load("require(\"a\")", "myscript").call();
//        LOG.info(modules.get("languages/data2").text);
        m = luaEnvironment.globals.load("return require('Module:languages/alldata')['en'].names[1]", "myscript").call();
        LOG.info("English name: " + m.toString());

        m = luaEnvironment.globals.load("return require('Module:languages/alldata')['da'].names[1]", "myscript").call();
        LOG.info("Danish name: " + m.toString());
        
        customTemplateHander.put("wikipedia", null);
    }

    Section current;

    WikiTokenizer.Callback callback = new Callback() {

        @Override
        public void onHeading(WikiTokenizer wikiTokenizer) {
            final int depth = wikiTokenizer.headingDepth();
            while (current.headingDepth >= depth) {
                current = current.parent;
            }
            final Section section = new Section(current, depth);
            current.subsections.add(section);
            current = section;

            // TODO: We have to render this text, too, but it shouldn't have
            // lists or sections in it.
            current.html.append(String.format("<h%d>", depth));
            final String headingText = wikiTokenizer.headingWikiText();
            WikiTokenizer.dispatch(headingText, false, this);
            current.html.append(String.format("</h%d>", depth));
        }

        @Override
        public void onWikiLink(WikiTokenizer wikiTokenizer) {
            current.html.append(wikiTokenizer.token());
        }

        @Override
        public void onPlainText(String text) {
            current.html.append(text);
        }

        @Override
        public void onNewline(WikiTokenizer wikiTokenizer) {
        }

        boolean boldOn = false;
        boolean italicOn = false;

        @Override
        public void onMarkup(WikiTokenizer wikiTokenizer) {
            if ("'''".equals(wikiTokenizer.token())) {
                if (!boldOn) {
                    current.html.append("<b>");
                } else {
                    current.html.append("</b>");
                }
                boldOn = !boldOn;
            } else if ("''".equals(wikiTokenizer.token())) {
                if (!italicOn) {
                    current.html.append("<em>");
                } else {
                    current.html.append("</em>");
                }
                italicOn = !italicOn;
            } else {
                assert false: wikiTokenizer.token();
            }
        }
        
        @Override
        public void onListItem(WikiTokenizer wikiTokenizer) {
        }

        @Override
        public void onHtml(WikiTokenizer wikiTokenizer) {
            current.html.append(wikiTokenizer.token());
        }

        @Override
        public void onFunction(WikiTokenizer wikiTokenizer, String functionName,
                List<String> functionPositionArgs, Map<String, String> functionNamedArgs) {
            if (functionName.startsWith("#invoke:")) {
                invokeModule(functionName.substring("#invoke:".length()), 
                        functionPositionArgs, functionNamedArgs);
            }
            
            if (customTemplateHander.containsKey(functionName)) {
                return;
            }
            
            // This could possibly create new sections, so we can't deal with it
            // here.
            final Article template = templates.get(functionName);
            LOG.info("Processing template: " + wikiTokenizer.token());
            if (template != null) {
                String instantiatedTemplate = WikiUtil.instantiateAllTemplateArgs(template.text, functionPositionArgs, functionNamedArgs);
                LOG.info("Instantiating template " + functionName + ": " + instantiatedTemplate);
                WikiTokenizer.dispatch(instantiatedTemplate, false, callback);
            } else {
                current.html.append(wikiTokenizer.token());
            }
        }

        @Override
        public void onComment(WikiTokenizer wikiTokenizer) {
            // Do nothing.
        }
    };
    
    private void invokeModule(
            String moduleName, List<String> functionPositionArgs,
            Map<String, String> functionNamedArgs) {
        Article module = modules.get(moduleName);
        if (module == null) {
            LOG.warning("Missing module: " + moduleName);
            return;
        }
        LOG.info("Invoking module: " + module.title + ", " + module.text);
        LuaValue m = luaEnvironment.globals.load(String.format("return require(\"Module:%s\")", module.title)).call();
        
        LuaTable args = new LuaTable();
        for (int i = 0; i < functionPositionArgs.size(); ++i) {
            args.set(i + 1, functionPositionArgs.get(i));
        }
        for (final Map.Entry<String, String> namedArg : functionNamedArgs.entrySet()) {
            args.set(namedArg.getKey(), namedArg.getValue());
        }
        
        luaEnvironment.frame.set("args", args);
        LOG.info(luaEnvironment.frame.tojstring());
        
        try {
          LuaValue result = m.get(functionPositionArgs.get(0)).call(luaEnvironment.frame);
        } catch (LuaError luaError) {
            LOG.log(Level.SEVERE, luaError.getMessage());
            final Pattern pattern = Pattern.compile("Module:([^:]+):([0-9]+).*");
            final Matcher matcher = pattern.matcher(luaError.getMessage()); 
            if (matcher.matches()) {
                final String file = matcher.group(1);
                final int line = Integer.valueOf(matcher.group(2));
                final Article article = modules.get(file);
                final String[] lines = article.text.split("\n");
                // Lua is 1-based indexing.
                LOG.log(Level.SEVERE, "Error on line: " + lines[line - 1]);
            }
            assert false;
        }
        assert false;
    }


    public Section renderPage(
            final String title, final String wikiText) {
        final Section top = new Section(null, 0);
        current = top;
        render(title, wikiText);
        return top;
    }

    private void render(String title, String wikiText) {
        WikiTokenizer.dispatch(wikiText, true, callback);
    }

}
