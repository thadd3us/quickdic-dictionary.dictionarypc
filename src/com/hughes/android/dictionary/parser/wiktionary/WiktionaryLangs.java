// Copyright 2012 Google Inc. All Rights Reserved.
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

package com.hughes.android.dictionary.parser.wiktionary;

import com.hughes.android.dictionary.engine.Language;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class WiktionaryLangs {
  
  public static final Map<String,String> isoCodeToEnWikiRegex = new LinkedHashMap<String,String>();
  static {
    isoCodeToEnWikiRegex.put("AF", "Afrikaans");
    isoCodeToEnWikiRegex.put("SQ", "Albanian");
    isoCodeToEnWikiRegex.put("AR", "Arabic");
    isoCodeToEnWikiRegex.put("HY", "Armenian");
    isoCodeToEnWikiRegex.put("BE", "Belarusian");
    isoCodeToEnWikiRegex.put("BN", "Bengali");
    isoCodeToEnWikiRegex.put("BG", "Bulgarian");
    isoCodeToEnWikiRegex.put("CA", "Catalan");
    isoCodeToEnWikiRegex.put("SH", "Serbo-Croatian");
    isoCodeToEnWikiRegex.put("HR", "Croatian");
    isoCodeToEnWikiRegex.put("CS", "Czech");
    isoCodeToEnWikiRegex.put("ZH", "Chinese");
    isoCodeToEnWikiRegex.put("cmn", "Mandarin");
    isoCodeToEnWikiRegex.put("yue", "Cantonese");
    isoCodeToEnWikiRegex.put("DA", "Danish");
    isoCodeToEnWikiRegex.put("NL", "Dutch");
    isoCodeToEnWikiRegex.put("EN", "English");
    isoCodeToEnWikiRegex.put("EO", "Esperanto");
    isoCodeToEnWikiRegex.put("ET", "Estonian");
    isoCodeToEnWikiRegex.put("FI", "Finnish");
    isoCodeToEnWikiRegex.put("FR", "French");
    isoCodeToEnWikiRegex.put("DE", "German");
    isoCodeToEnWikiRegex.put("EL", "Greek");
    isoCodeToEnWikiRegex.put("grc", "Ancient Greek");
    isoCodeToEnWikiRegex.put("haw", "Hawaiian");
    isoCodeToEnWikiRegex.put("HE", "Hebrew");
    isoCodeToEnWikiRegex.put("HI", "Hindi");
    isoCodeToEnWikiRegex.put("HU", "Hungarian");
    isoCodeToEnWikiRegex.put("IS", "Icelandic");
    isoCodeToEnWikiRegex.put("ID", "Indonesian");
    isoCodeToEnWikiRegex.put("GA", "Irish");
    isoCodeToEnWikiRegex.put("GD", "Gaelic");
    isoCodeToEnWikiRegex.put("GV", "Manx");
    isoCodeToEnWikiRegex.put("IT", "Italian");
    isoCodeToEnWikiRegex.put("LA", "Latin");
    isoCodeToEnWikiRegex.put("LV", "Latvian");
    isoCodeToEnWikiRegex.put("LT", "Lithuanian");
    isoCodeToEnWikiRegex.put("JA", "Japanese");
    isoCodeToEnWikiRegex.put("KO", "Korean");
    isoCodeToEnWikiRegex.put("KU", "Kurdish");
    isoCodeToEnWikiRegex.put("LO", "Lao");
    isoCodeToEnWikiRegex.put("MS", "Malay$");
    isoCodeToEnWikiRegex.put("ML", "Malayalam");
    isoCodeToEnWikiRegex.put("MI", "Maori");
    isoCodeToEnWikiRegex.put("MN", "Mongolian");
    isoCodeToEnWikiRegex.put("NE", "Nepali");
    isoCodeToEnWikiRegex.put("NO", "Norwegian");
    isoCodeToEnWikiRegex.put("FA", "Persian");
    isoCodeToEnWikiRegex.put("PL", "Polish");
    isoCodeToEnWikiRegex.put("PT", "Portuguese");
    isoCodeToEnWikiRegex.put("PA", "Punjabi");
    isoCodeToEnWikiRegex.put("RO", "Romanian");
    isoCodeToEnWikiRegex.put("RU", "Russian");
    isoCodeToEnWikiRegex.put("SA", "Sanskrit");
    isoCodeToEnWikiRegex.put("SK", "Slovak");
    isoCodeToEnWikiRegex.put("SL", "Slovene|Slovenian");
    isoCodeToEnWikiRegex.put("SO", "Somali");
    isoCodeToEnWikiRegex.put("ES", "Spanish");
    isoCodeToEnWikiRegex.put("SW", "Swahili");
    isoCodeToEnWikiRegex.put("SV", "Swedish");
    isoCodeToEnWikiRegex.put("TL", "Tagalog");
    isoCodeToEnWikiRegex.put("TG", "Tajik");
    isoCodeToEnWikiRegex.put("TA", "Tamil");
    isoCodeToEnWikiRegex.put("TH", "Thai");
    isoCodeToEnWikiRegex.put("BO", "Tibetan");
    isoCodeToEnWikiRegex.put("TR", "Turkish");
    isoCodeToEnWikiRegex.put("UK", "Ukrainian");
    isoCodeToEnWikiRegex.put("UR", "Urdu");
    isoCodeToEnWikiRegex.put("VI", "Vietnamese");
    isoCodeToEnWikiRegex.put("CI", "Welsh");
    isoCodeToEnWikiRegex.put("YI", "Yiddish");
    isoCodeToEnWikiRegex.put("ZU", "Zulu");
    isoCodeToEnWikiRegex.put("AZ", "Azeri");
    isoCodeToEnWikiRegex.put("EU", "Basque");
    isoCodeToEnWikiRegex.put("BR", "Breton");
    isoCodeToEnWikiRegex.put("MR", "Marathi");
    isoCodeToEnWikiRegex.put("FO", "Faroese");
    isoCodeToEnWikiRegex.put("GL", "Galician");
    isoCodeToEnWikiRegex.put("KA", "Georgian");
    isoCodeToEnWikiRegex.put("HT", "Haitian Creole");
    isoCodeToEnWikiRegex.put("LB", "Luxembourgish");
    isoCodeToEnWikiRegex.put("MK", "Macedonian");
    isoCodeToEnWikiRegex.put("GV", "Manx");
    isoCodeToEnWikiRegex.put("SD", "Sindhi");
    
    // No longer exists in EN:
    // isoCodeToEnWikiName.put("BS", "Bosnian");
    // isoCodeToEnWikiName.put("SR", "Serbian");
    
    // Font doesn't work:
    //isoCodeToEnWikiName.put("MY", "Burmese");

    {
        Set<String> missing = new LinkedHashSet<String>(isoCodeToEnWikiRegex.keySet());
        missing.removeAll(Language.isoCodeToResources.keySet());
        //System.out.println(missing);
    }
    assert Language.isoCodeToResources.keySet().containsAll(isoCodeToEnWikiRegex.keySet());
  }
  
    public static class WiktionaryDescriptor {
        public final List<String> articleTitlesToIgnore = new ArrayList<String>();
        public final String templatePrefix;
        public final String modulePrefix;
        public final Map<String, String> isoCodeToLocalNameRegex = new LinkedHashMap<String, String>();

        private WiktionaryDescriptor(String templatePrefix, String modulePrefix) {
            this.templatePrefix = templatePrefix;
            this.modulePrefix = modulePrefix;
        }
    }

  public static final Map<String, WiktionaryDescriptor> wikiCodeToWiktionaryDescriptor = new LinkedHashMap<String, WiktionaryDescriptor>();
  static {
    // en
      WiktionaryDescriptor wiktionaryDescriptor;
      wiktionaryDescriptor = new WiktionaryDescriptor("Template:", "Module:");
      wikiCodeToWiktionaryDescriptor.put("en", wiktionaryDescriptor);
      wiktionaryDescriptor.isoCodeToLocalNameRegex.putAll(isoCodeToEnWikiRegex); 
    
    
    // egrep -o '\{\{Wortart[^}]+\}\}' dewiktionary-pages-articles.xml | cut -d \| -f3 | sort | uniq -c | sort -nr
    wiktionaryDescriptor = new WiktionaryDescriptor("Vorlage:", "Modul:");
    wikiCodeToWiktionaryDescriptor.put("de", wiktionaryDescriptor);
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("DE", "Deutsch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("EN", "Englisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("IT", "Italienisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("PL", "Polnisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("FR", "Französisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("EO", "Esperanto");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("CA", "Katalanisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("LA", "Lateinisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("CS", "Tschechisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("HU", "Ungarisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("SV", "Schwedisch");
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("ES", "Spanisch");

    // egrep -o '\{\{=[a-zA-Z]+=\}\}' frwiktionary-pages-articles.xml | sort | uniq -c | sort -nr
    wiktionaryDescriptor = new WiktionaryDescriptor("Modèle:", "Module:");
    wikiCodeToWiktionaryDescriptor.put("fr", wiktionaryDescriptor);
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("FR", Pattern.quote("{{langue|fr}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("RU", Pattern.quote("{{langue|ru}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("BG", Pattern.quote("{{langue|bg}}"));  // Bulgarian
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("EN", Pattern.quote("{{langue|en}}"));
    //wiktionaryDescriptor.isoCodeToLocalNameRegex.put("", Pattern.quote("{{langue|sl}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("LA", Pattern.quote("{{langue|la}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("IT", Pattern.quote("{{langue|it}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("EO", Pattern.quote("{{langue|eo}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("CS", Pattern.quote("{{langue|cs}}"));  // Czech
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("NL", Pattern.quote("{{langue|nl}}"));  // Dutch
    //wiktionaryDescriptor.isoCodeToLocalNameRegex.put("", Pattern.quote("{{langue|mg}}"));
    //wiktionaryDescriptor.isoCodeToLocalNameRegex.put("", Pattern.quote("{{langue|hsb}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("ZH", Pattern.quote("{{langue|zh}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("cmn", Pattern.quote("{{langue|cmn}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("yue", Pattern.quote("{{langue|yue}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("JA", Pattern.quote("{{langue|ja}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("DE", Pattern.quote("{{langue|de}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("IS", Pattern.quote("{{langue|is}}"));  // Icelandic
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("ES", Pattern.quote("{{langue|es}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("UK", Pattern.quote("{{langue|uk}}"));

    // egrep -o '= *\{\{-[a-z]+-\}\} *=' itwiktionary-pages-articles.xml | sort | uniq -c | sort -n
    wiktionaryDescriptor = new WiktionaryDescriptor("Template:", "Modulo:");
    wikiCodeToWiktionaryDescriptor.put("it", wiktionaryDescriptor);
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("IT", "\\{\\{-(it|scn|nap|cal|lmo)-\\}\\}");  // scn, nap, cal, lmo
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("EN", Pattern.quote("{{-en-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("FR", Pattern.quote("{{-fr-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("DE", Pattern.quote("{{-de-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("ES", Pattern.quote("{{-es-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("JA", Pattern.quote("{{-ja-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("PL", Pattern.quote("{{-pl-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("NL", Pattern.quote("{{-nl-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("LV", Pattern.quote("{{-lv-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("LA", Pattern.quote("{{-la-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("HU", Pattern.quote("{{-hu-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("EL", Pattern.quote("{{-grc-}}"));
    wiktionaryDescriptor.isoCodeToLocalNameRegex.put("SV", Pattern.quote("{{-sv-}}"));

  }
  public static String getEnglishName(String langCode) {
      String name = isoCodeToEnWikiRegex.get(langCode);
      if (name == null) {
          name = isoCodeToEnWikiRegex.get(langCode.toUpperCase());
      }
      if (name == null) {
          return null;
      }
      if (name.indexOf('|') != -1) {
          return name.substring(0, name.indexOf('|'));
      }
      if (name.indexOf('$') != -1) {
          return name.substring(0, name.indexOf('$'));
      }
      return name;  // can be null.
  }
  
}
