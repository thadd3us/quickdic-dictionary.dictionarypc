
package com.hughes.android.dictionary.parser.wiktionary;

import com.hughes.android.dictionary.parser.wiktionary.ParsedWiktionary.PageData;
import com.hughes.android.dictionary.parser.wiktionary.ParsedWiktionary.SectionData;
import com.hughes.android.dictionary.parser.wiktionary.ParsedWiktionary.TranslationForPage;
import com.hughes.android.dictionary.parser.wiktionary.ParsedWiktionary.TranslationPOS;
import com.hughes.android.dictionary.parser.wiktionary.ParsedWiktionary.TranslationSense;
import com.hughes.android.dictionary.parser.wiktionary.WiktionaryLangs.WiktionaryDescriptor;
import com.hughes.util.StringUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ScrapeProcessor {

    final ParsedWiktionary wiktionary;
    final WiktionaryDescriptor wiktionaryDescriptor;

    ScrapeProcessor(final String wikiLangCode, final String outputDir) {
        wiktionary = new ParsedWiktionary(wikiLangCode, outputDir);
        wiktionaryDescriptor = WiktionaryLangs.wikiCodeToWiktionaryDescriptor.get(wikiLangCode);
        assert wiktionaryDescriptor != null;
    }

    void Process(final String filename) throws ParserConfigurationException, SAXException,
            IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        final DataInputStream in = new DataInputStream(new GZIPInputStream(new FileInputStream(
                filename)));
        int pageCount = 0, recordCount = -1;
        while (true) {
            ++recordCount;
            final int pageid;
            try {
                pageid = in.readInt();
            } catch (EOFException e) {
                break;
            }

            final int byte_length = in.readInt();
            final byte[] bytes = new byte[byte_length];
            in.readFully(bytes);
            
            // Done reading
            if (recordCount < -1) {
                continue;
            }

            Document doc = db.parse(new ByteArrayInputStream(bytes));
            Node firstChild = doc.getDocumentElement().getFirstChild();
            if (firstChild.getNodeName() != "parse") {
                continue;
            }
            int revid = Integer.valueOf(firstChild.getAttributes().getNamedItem("revid")
                    .getNodeValue());
            String displayTitle = firstChild.getAttributes().getNamedItem("displaytitle")
                    .getNodeValue();
            if (false && !displayTitle.equals("approximate")) {
                continue;
            }
            Node text = firstChild.getFirstChild();
            if (text.getNodeName() != "text") {
                throw new RuntimeException(text.getNodeName());
            }
            System.out.printf("****************** %d, %s, pageCount=%d, recordCount=%d\n", revid,
                    displayTitle, pageCount, recordCount);
            if (displayTitle.startsWith("User talk:") ||
                    displayTitle.startsWith("Appendix:") ||
                    displayTitle.startsWith("Category:") ||
                    displayTitle.startsWith("Index:") ||
                    displayTitle.startsWith("MediaWiki:") ||
                    displayTitle.startsWith("Module:") ||
                    displayTitle.startsWith("Template:") ||
                    displayTitle.startsWith("User:") ||
                    displayTitle.startsWith("Wiktionary:")) {
                continue;
            }

            String html = text.getTextContent();
            handleHtml(pageid, revid, displayTitle, html);
            ++pageCount;
            if (pageCount % 5000 == 0) {
                wiktionary.flush();
            }
        }
        in.close();

        for (String sectionName : wiktionary.sectionNameToHtmlSize.keySet()) {
            System.out.println(sectionName + ": "
                    + wiktionary.sectionNameToHtmlSize.get(sectionName));
        }

        wiktionary.close();
        // FileUtil.writeObject(translations, args[1]);
    }

    private void handleHtml(int pageid, int revid, String displayTitle, String html)
            throws ParserConfigurationException, SAXException, IOException {
        org.jsoup.nodes.Document doc = Jsoup.parse(html);
        // System.out.println(doc);
        parseEnWiktionary(displayTitle, doc);
    }

    static class ParseContext {
        String pageName;
        String languageName;
        String lastPosName;
        String lastHeadline;
    }

    static interface ElementVisitor {
        void visit(Element element);
    }

    static void traverseElements(Element element, ElementVisitor elementVisitor) {
        elementVisitor.visit(element);
        for (final Element child : element.children()) {
            traverseElements(child, elementVisitor);
        }
    }

    static String GetHeadingName(Element element) {
        Elements headlines = element.select("span.mw-headline");
        if (headlines.size() == 0)
            throw new RuntimeException(element.html());
        if (headlines.size() != 1)
            throw new RuntimeException(element.html());
        return headlines.get(0).text();
    }

    class EnLanguageVisitor implements ElementVisitor {
        final String displayName;
        SectionData lastSectionData = new SectionData();
        SectionData lastPosSection = null;
        int translationCount = 0;

        public EnLanguageVisitor(String displayName) {
            lastSectionData.headerDepth = 2;
            this.displayName = displayName;
        }

        @Override
        public void visit(Element element) {
            int headerDepth = Section.GetHeaderDepth(element);
            if (headerDepth != -1) {
                while (headerDepth <= lastSectionData.headerDepth) {
                    if (lastSectionData.parent == null) {
                        System.err.println("<h2> inside languageDiv: " + displayName);
                        return;
                    }
                    lastSectionData = lastSectionData.parent;
                }
                SectionData sectionData = new SectionData();
                sectionData.name = GetHeadingName(element);
                sectionData.headerDepth = headerDepth;
                sectionData.parent = lastSectionData;
                lastSectionData.subsections.add(sectionData);
                lastSectionData = sectionData;

                if (sectionData.name.equals("Translations")) {
                    return;
                }
                int siblingIndex = element.siblingIndex() + 1;
                StringBuilder builder = new StringBuilder();
                while (siblingIndex < element.siblingElements().size() &&
                        Section.GetHeaderDepth(element.siblingElements().get(siblingIndex)) == -1) {
                    // System.out.println("Sibling: " + sibling);
                    builder.append(element.siblingElements().get(siblingIndex).html());
                    ++siblingIndex;
                }
                lastSectionData.html = builder.toString();
            } else if (element.attr("class").contains("headword")) {
                lastSectionData.posHeadline = element.parent().html();
                lastPosSection = lastSectionData;
            } else if (element.classNames().contains("translations")) {
                if (!"Translations".equals(lastSectionData.name)) {
                    System.err.println("Translations in wrong place: ");
                }
                // "quarter to" fails here:
                // assert lastPosSection != null;

                final String dataGloss = element.attr("data-gloss");

                Elements listItems = element.select("li");
                for (final Element listItem : listItems) {
                    String html = listItem.html();
                    final int colonPos = html.indexOf(":");
                    if (colonPos == -1) {
                        continue;
                    }
                    final String langName = html.substring(0, colonPos);
                    final String translationHtml = html.substring(colonPos + 1);
                    final String isoCode = wiktionaryDescriptor.headerNameToIsoCode.get(langName);
                    if (isoCode == null) {
                        continue;
                    }

                    TranslationForPage translationData = wiktionary.getTranslations(isoCode,
                            displayName);
                    TranslationPOS translationPOS;
                    if (lastPosSection != null) {
                        translationPOS = translationData.getTranslationPOS(lastPosSection.name,
                                lastPosSection.posHeadline);
                    } else {
                        translationPOS = translationData.getTranslationPOS("", "");
                    }

                    TranslationSense translationSense = new TranslationSense();
                    translationSense.sense = dataGloss;
                    translationSense.translationHtml = translationHtml;

                    if (translationPOS.translationSenses.get(translationCount) != null) {
                        // "that", Dutch.
                        translationPOS.translationSenses.get(translationCount).translationHtml += "\n<!-- WIKTIONARY_ERROR: duplicate language -->\n"
                                + translationHtml;
                    } else {
                        translationPOS.translationSenses.put(translationCount, translationSense);
                    }
                }
                ++translationCount;
            }
        }
    }

    private void parseEnWiktionary(String displayTitle, org.jsoup.nodes.Document doc) {
        // System.out.println(doc.toString());

        final Elements topHeadings = doc.select("h2");
        for (final Element topHeading : topHeadings) {
            final String languageName = GetHeadingName(topHeading);
            if (StringUtil.isNullOrEmpty(languageName)) {
                continue;
            }
            final String isoCode = wiktionaryDescriptor.headerNameToIsoCode.get(languageName);
            if (isoCode == null) {
                continue;
            }
            ParsedWiktionary.LanguageOutput languageData = wiktionary.getLanguageOutput(isoCode);

            // TODO: Should we process all siblings?
            final Element languageDiv = topHeading.nextElementSibling();
            // System.out.println(languageDiv);
            EnLanguageVisitor visitor = new EnLanguageVisitor(displayTitle);
            traverseElements(languageDiv, visitor);
            // System.out.println(visitor.lastSectionData.getStructure(new
            // StringBuilder()).toString());
            if (visitor.lastPosSection == null) {
                // System.out.println("Null pos: " + languageDiv);
                continue;
            }
            SectionData sectionData = visitor.lastSectionData.getRoot();
            languageData.writePageData(new PageData(displayTitle, sectionData));
            for (Map.Entry<String, TranslationForPage> entry : wiktionary.isoCodeToCurrentTranslations
                    .entrySet()) {
                wiktionary.getLanguageOutput(entry.getKey()).writeTranslation(entry.getValue());
            }
            wiktionary.isoCodeToCurrentTranslations.clear();
        }

    }

    public static void main(String[] args) throws Exception {
        new ScrapeProcessor(args[0], args[1]).Process(args[2]);
    }

}
