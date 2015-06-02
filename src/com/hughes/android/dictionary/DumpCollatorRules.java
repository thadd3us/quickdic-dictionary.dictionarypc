package com.hughes.android.dictionary;

import com.hughes.android.dictionary.engine.Language;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Locale;

public class DumpCollatorRules {

    public static void main(String[] args) {
        for (String key : Language.registry.keySet()) {
            Language l = Language.registry.get(key);
            Collator collator = Collator.getInstance(l.locale);
            if (collator instanceof RuleBasedCollator) {
                RuleBasedCollator rbc = (RuleBasedCollator) collator;
                System.out.println(key + ":" + rbc.getRules());
            } else {
                System.out.println(key + ":NOT RBC");
            }
        }
        for (Locale locale : Locale.getAvailableLocales()) {
            Collator collator = Collator.getInstance(locale);
            if (collator instanceof RuleBasedCollator) {
                RuleBasedCollator rbc = (RuleBasedCollator) collator;
                System.out.println(locale.getISO3Language() + ":" + rbc.getRules());
            } else {
                System.out.println(locale.getISO3Language() + ":NOT RBC");
            }
        }
        // TODO Auto-generated method stub

    }

}
