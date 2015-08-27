
package com.hughes.android.dictionary.parser.wiktionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

public class ParsedWiktionary {
    final String wikiCode;
    final String outputDir;
    
    final Map<String, Integer> sectionNameToHtmlSize = new TreeMap<String, Integer>();
    
    ParsedWiktionary(String wikiCode, final String outputDir) {
        this.wikiCode = wikiCode;
        this.outputDir = outputDir;
    }
    
    void flush() {
        for (LanguageOutput languageOutput : isoCodeToLangugeOutput.values()) {
            languageOutput.flush();
        }
    }
    void close() {
        for (LanguageOutput languageOutput : isoCodeToLangugeOutput.values()) {
            languageOutput.close();
        }
    }

    class LanguageOutput implements Serializable {
        private static final long serialVersionUID = -4722068055759370115L;
        
        final String isoCode;
        private final ObjectOutputStream pageDataOos;
        private final ObjectOutputStream translationOos;
        
        LanguageOutput(String isoCode, final String outputDir) {
            this.isoCode = isoCode;
            try {
                pageDataOos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(new File(
                        outputDir, isoCode + ".PageData.serialized.gz")), false));
                translationOos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(new File(
                        outputDir, isoCode + ".Translations.serialized.gz")), false));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        void writePageData(PageData pageData) {
            pageData.sectionData.accumulateSizes(sectionNameToHtmlSize);
            try {
                pageDataOos.writeObject(pageData);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        void writeTranslation(TranslationForPage translation) {
            try {
                translationOos.writeObject(translation);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        void flush() {
            try {
                translationOos.flush();
                pageDataOos.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        void close() {
            try {
                translationOos.close();
                pageDataOos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    Map<String, LanguageOutput> isoCodeToLangugeOutput = new TreeMap<String, LanguageOutput>();

    public LanguageOutput getLanguageOutput(String isoCode) {
        LanguageOutput result = isoCodeToLangugeOutput.get(isoCode);
        if (result == null) {
            result = new LanguageOutput(isoCode, outputDir);
            isoCodeToLangugeOutput.put(isoCode, result);
        }
        return result;
    }

    final Map<String, TranslationForPage> isoCodeToCurrentTranslations = new TreeMap<String, TranslationForPage>();
    public TranslationForPage getTranslations(String isoCode, final String pageName) {
        TranslationForPage result = isoCodeToCurrentTranslations.get(isoCode);
        if (result == null) {
            result = new TranslationForPage(pageName);
            isoCodeToCurrentTranslations.put(isoCode, result);
        }
        return result;
    }

    // -------------------------------------------------------------------

    static class TranslationForPage implements Serializable {
        private static final long serialVersionUID = 2449023708581474623L;
        
        final String pageName;

        public TranslationForPage(String pageName) {
            this.pageName = pageName;
        }

        final Map<String, TranslationPOS> translationPoses = new TreeMap<String, TranslationPOS>();

        TranslationPOS getTranslationPOS(String posName, String headLine) {
            final String key = posName + ": " + headLine;
            TranslationPOS result = translationPoses.get(key);
            if (result == null) {
                result = new TranslationPOS(posName, headLine);
                translationPoses.put(key, result);
            }
            return result;
        }

        @Override
        public String toString() {
            return pageName + " " + translationPoses;
        }

    }

    static class TranslationPOS implements Serializable {
        private static final long serialVersionUID = -5750417937854444856L;

        final String posName;
        final String headLine;

        TranslationPOS(String posName, String headLine) {
            this.posName = posName;
            this.headLine = headLine;
        }

        final Map<Integer, TranslationSense> translationSenses = new TreeMap<Integer, TranslationSense>();

        @Override
        public String toString() {
            return translationSenses.values().toString();
        }
    }

    static class TranslationSense implements Serializable {
        private static final long serialVersionUID = 3681155921596797704L;

        String sense;
        String translationHtml;

        @Override
        public String toString() {
            return sense + ": " + translationHtml;
        }
    }
    
    // -------------------------------------------------------------------
    
    static class PageData implements Serializable {
        private static final long serialVersionUID = 5205597197460190675L;

        final String pageName;
        SectionData sectionData;
        
        PageData(String pageName, SectionData sectionData) {
            this.pageName = pageName;
            this.sectionData = sectionData;
        }
    }

    static class SectionData implements Serializable {
        private static final long serialVersionUID = 8964319368442388553L;

        String name;
        
        String posHeadline = null;
        String html = "";
        int headerDepth;
        SectionData parent = null;
        
        final List<SectionData> subsections = new ArrayList<SectionData>();

        StringBuilder getStructure(StringBuilder builder) {
            for (int i = 0; i < headerDepth; ++i) {
                builder.append("  ");
            }
            builder.append(name).append(" ").append(posHeadline);
            builder.append(" ");
            builder.append(html.replaceAll("\n", ""));
            builder.append("\n");
            for (final SectionData subsection : subsections) {
                subsection.getStructure(builder);
            }
            return builder;
        }
        
        SectionData getRoot() {
            SectionData result = this;
            while (result.parent != null) {
                result = result.parent;
            }
            return result;
        }

        public void removeSection(String name) {
            for (int i = 0; i < subsections.size(); ++i) {
                if (subsections.get(i).name.equals(name)) {
                    subsections.remove(i);
                    --i;
                } else {
                    subsections.get(i).removeSection(name);
                }
            }
        }
        
        public void accumulateSizes(final Map<String, Integer> nameToSize) {
            final String key = name == null ? "(null)" : name;
            if (!nameToSize.containsKey(key)) {
                nameToSize.put(key, 0);
            }
            nameToSize.put(key, nameToSize.get(key) + html.length());
            
            for (SectionData sectionData : subsections) {
                sectionData.accumulateSizes(nameToSize);
            }
        }
    }

}
