// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.hughes.android.dictionary.engine;

import com.hughes.android.dictionary.parser.wiktionary.WiktionaryLangs;
import com.hughes.android.dictionary.parser.wiktionary.WiktionaryLangs.WiktionaryDescriptor;

import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

public class WiktionarySplitter extends org.xml.sax.helpers.DefaultHandler {
    
    public static class Article {
        
        public final String title;
        public final String heading;
        public final String text;
        
        private Article(String title, String text) {
            this.title = title;
            this.heading = "";
            this.text = text;
        }

        private Article(String title, String heading, String text) {
            this.title = title;
            this.heading = heading;
            this.text = text;
        }
        
        void write(DataOutputStream out) throws IOException {
            out.writeUTF(title);
            out.writeUTF(heading);
            final byte[] textBytes = text.getBytes("UTF8");
            out.writeInt(textBytes.length);
            out.write(textBytes);
        }

        public Article(DataInputStream dis) throws IOException {
            title = dis.readUTF();
            heading = dis.readUTF();
            final int bytesLength = dis.readInt();
            final byte[] bytes = new byte[bytesLength];
            dis.readFully(bytes);
            text = new String(bytes, "UTF8");
        }

    }

    public static void main(final String[] args) throws Exception {
        System.out.println("Hello.");
        for (final String code : WiktionaryLangs.wikiCodeToWiktionaryDescriptor.keySet()) {
            System.err.println("code=" + code);
            // if (!code.equals("fr")) {continue;}
            final WiktionaryDescriptor wiktionaryDescriptor = WiktionaryLangs.wikiCodeToWiktionaryDescriptor.get(code);
            final WiktionarySplitter splitter = new WiktionarySplitter(wiktionaryDescriptor);
            final String outputDir = String.format("data/inputs/wikiSplit/%s", code);
            splitter.selectors = new ArrayList<WiktionarySplitter.Selector>();
            for (final Map.Entry<String, String> entry : wiktionaryDescriptor.isoCodeToLocalNameRegex.entrySet()) {
                new File(outputDir).mkdirs();
                splitter.selectors.add(new Selector(String.format("%s/%s.data", outputDir, entry.getKey()), entry
                        .getValue()));
            }
            splitter.templateSelector = new Selector(String.format("%s/%s.data", outputDir, "Templates"), "");
            splitter.moduleSelector = new Selector(String.format("%s/%s.data", outputDir, "Modules"), "");
            splitter.moduleDir = new File(outputDir, "Modules");

            final String filePath = String.format("data/inputs/%swiktionary-pages-articles.xml", code);
            splitter.parseAndSplit(new File(filePath));
        }
    }
    
    static class Selector {
        final String outFilename;
        final Pattern pattern;

        final DataOutputStream out;

        public Selector(final String filename, final String pattern) throws IOException {
            this.outFilename = filename;
            this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            out = new DataOutputStream(new BufferedOutputStream(
                    new FileOutputStream(outFilename)));
        }
    }

    // The matches the whole line, otherwise regexes don't work well on French:
    // {{=uk=}}
    static final Pattern headingStart = Pattern.compile("^(=+)[^=].*$", Pattern.MULTILINE);

    final WiktionaryDescriptor wiktionaryDescriptor;
    List<Selector> selectors = null;
    Selector templateSelector = null;
    Selector moduleSelector = null;
    File moduleDir = null;

    StringBuilder titleBuilder;
    StringBuilder textBuilder;
    StringBuilder currentBuilder = null;

    private WiktionarySplitter(WiktionaryDescriptor wiktionaryDescriptor) {
        this.wiktionaryDescriptor = wiktionaryDescriptor;
    }

    private void parseAndSplit(final File inputFile) throws Exception {
        final SAXParser parser = SAXParserFactoryImpl.newInstance().newSAXParser();

        // Do it.
        try {
            parser.parse(inputFile, this);
        } catch (Exception e) {
            System.err.println("Exception during parse, lastPageTitle=" + lastPageTitle
                    + ", titleBuilder=" + titleBuilder.toString());
            throw e;
        }

        // Shutdown.
        for (final Selector selector : selectors) {
            selector.out.close();
        }
        templateSelector.out.close();
    }

    String lastPageTitle = null;
    int pageCount = 0;

    private void endPage() {
        final String title = titleBuilder.toString();
        lastPageTitle = title;
        if (++pageCount % 10000 == 0) {
            System.out.println("endPage: " + title + ", count=" + pageCount);
        }
        if (title.startsWith("Wiktionary:") ||
                title.startsWith("Appendix:") ||
                title.startsWith("Help:") ||
                title.startsWith("Index:") ||
                title.startsWith("MediaWiki:") ||
                title.startsWith("Citations:") ||
                title.startsWith("Concordance:") ||
                title.startsWith("Glossary:") ||
                title.startsWith("Rhymes:") ||
                title.startsWith("Category:") ||
                title.startsWith("Wikisaurus:") ||
                title.startsWith("Unsupported titles/") ||
                title.startsWith("Transwiki:") ||
                title.startsWith("File:") ||
                title.startsWith("Thread:") ||
                title.startsWith("Summary:") ||
                // DE
                title.startsWith("Datei:") ||
                title.startsWith("Verzeichnis:") ||
                title.startsWith("Thesaurus:") ||
                title.startsWith("Kategorie:") ||
                title.startsWith("Hilfe:") ||
                title.startsWith("Reim:") ||
                // FR:
                title.startsWith("Annexe:") ||
                title.startsWith("Catégori:") ||
                title.startsWith("Thésaurus:") ||
                title.startsWith("Projet:") ||
                title.startsWith("Aide:") ||
                title.startsWith("Fichier:") ||
                title.startsWith("Wiktionnaire:") ||
                title.startsWith("Catégorie:") ||
                title.startsWith("Portail:") ||
                title.startsWith("utiliusateur:") ||
                title.startsWith("Kategorio:") ||
                
                // IT
                title.startsWith("Wikizionario:") ||
                title.startsWith("Appendice:") ||
                title.startsWith("Categoria:") ||
                title.startsWith("Aiuto:") ||
                title.startsWith("Portail:") ||
                // sentinel
                false) {
            return;
        }

        String text = textBuilder.toString();

        if (wiktionaryDescriptor.templatePrefix != null && 
                title.startsWith(wiktionaryDescriptor.templatePrefix)) {
            try {
                new Article(title.replaceFirst(wiktionaryDescriptor.templatePrefix, ""), 
                            text).write(templateSelector.out);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }
        if (wiktionaryDescriptor.modulePrefix != null &&
                title.startsWith(wiktionaryDescriptor.modulePrefix)) {
            try {
//                FileUtil.writeObject(o, file);
                new Article(title.replaceFirst(wiktionaryDescriptor.modulePrefix, ""), 
                            text).write(moduleSelector.out);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        if (title.contains(":")) {
            if (!title.startsWith("Sign gloss:")) {
                System.err.println("title with colon: " + title);
            }
        }

        while (text.length() > 0) {
            // Find start.
            final Matcher startMatcher = headingStart.matcher(text);
            if (!startMatcher.find()) {
                return;
            }
            text = text.substring(startMatcher.end());

            final String heading = startMatcher.group();
            for (final Selector selector : selectors) {
                if (selector.pattern.matcher(heading).find()) {

                    // Find end.
                    final int depth = startMatcher.group(1).length();
                    final Pattern endPattern = Pattern.compile(
                            String.format("^={1,%d}[^=].*$", depth), Pattern.MULTILINE);

                    final Matcher endMatcher = endPattern.matcher(text);
                    final int end;
                    if (endMatcher.find()) {
                        end = endMatcher.start();
                    } else {
                        end = text.length();
                    }

                    final String sectionText = text.substring(0, end);

                    try {
                        final Article section = new Article(title, heading, sectionText);
                        section.write(selector.out);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    text = text.substring(end);
                }
            }
        }

    }

    // -----------------------------------------------------------------------


    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) {
        currentBuilder = null;
        if ("page".equals(qName)) {
            titleBuilder = new StringBuilder();

            // Start with "\n" to better match certain strings.
            textBuilder = new StringBuilder("\n");
        } else if ("title".equals(qName)) {
            currentBuilder = titleBuilder;
        } else if ("text".equals(qName)) {
            currentBuilder = textBuilder;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentBuilder != null) {
            currentBuilder.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        currentBuilder = null;
        if ("page".equals(qName)) {
            endPage();
        }
    }

    public void parse(final File file) throws ParserConfigurationException,
            SAXException, IOException {
        final SAXParser parser = SAXParserFactoryImpl.newInstance().newSAXParser();
        parser.parse(file, this);
    }

}
