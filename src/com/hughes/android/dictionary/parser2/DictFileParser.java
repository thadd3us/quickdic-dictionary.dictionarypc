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

package com.hughes.android.dictionary.parser2;

import com.hughes.android.dictionary.engine2.DictionaryBuilder;
import com.hughes.android.dictionary.engine2.EntryRefType;
import com.hughes.android.dictionary.engine2.EntrySource;
import com.hughes.android.dictionary.engine2.IndexBuilder;
import com.hughes.android.dictionary.engine2.Language;
import com.hughes.android.dictionary.engine2.PairEntry;
import com.hughes.android.dictionary.engine2.PairEntry.Pair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DictFileParser {

    static final Logger logger = Logger.getLogger(DictFileParser.class.getName());

    final Charset charset;
    final boolean flipCols;

    final Pattern fieldSplit;
    final Pattern subfieldSplit;

    final IndexBuilder[] langIndexBuilders;

    EntrySource entrySource;

    // final Set<String> alreadyDone = new HashSet<String>();

    public DictFileParser(final Charset charset, boolean flipCols,
            final Pattern fieldSplit, final Pattern subfieldSplit,
            final DictionaryBuilder dictBuilder, final IndexBuilder[] langIndexBuilders,
            final IndexBuilder bothIndexBuilder) {
        this.charset = charset;
        this.flipCols = flipCols;
        this.fieldSplit = fieldSplit;
        this.subfieldSplit = subfieldSplit;
        this.langIndexBuilders = langIndexBuilders;
    }

    public void parse(final File file, final EntrySource entrySouce, final int pageLimit)
            throws IOException {
        this.entrySource = entrySouce;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
                file), charset));
        String line;
        int count = 0;
        while ((line = reader.readLine()) != null) {
            if (pageLimit >= 0 && count >= pageLimit) {
                return;
            }
            if (count % 10000 == 0) {
                logger.info("count=" + count + ", line=" + line);
            }
            parseLine(line);
            ++count;
        }
    }

    private void parseLine(final String line) {
        if (line.startsWith("#") || line.length() == 0) {
            logger.info("Skipping comment line: " + line);
            return;
        }
        final String[] fields = fieldSplit.split(line);
        // dictcc now has a part of speech field as field #3.
        if (fields.length < 2 || fields.length > 3) {
            logger.warning("Malformed line: " + line);
            return;
        }

        if (flipCols) {
            final String temp = fields[0];
            fields[0] = fields[1];
            fields[1] = temp;
        }

        final String[][] subfields = new String[2][];
        if (subfieldSplit != null) {
            subfields[0] = subfieldSplit.split(fields[0]);
            subfields[1] = subfieldSplit.split(fields[1]);
            if (subfields[0].length != subfields[1].length) {
                logger.warning("Number of subfields doesn't match: " + line);
                return;
            }
        } else {
            subfields[0] = new String[] {
                    fields[0]
            };
            subfields[1] = new String[] {
                    fields[1]
            };
        }

        final PairEntry pairEntry = new PairEntry(entrySource);
        for (int i = 0; i < subfields[0].length; ++i) {
            subfields[0][i] = subfields[0][i].trim();
            subfields[1][i] = subfields[1][i].trim();
            if (subfields[0][i].length() == 0 && subfields[1][i].length() == 0) {
                logger.warning("Empty pair: " + line);
                continue;
            }
            if (subfields[0][i].length() == 0) {
                subfields[0][i] = "__";
            }
            if (subfields[1][i].length() == 0) {
                subfields[1][i] = "__";
            }
            pairEntry.pairs.add(new Pair(subfields[0][i], subfields[1][i]));
        }

        for (int l = 0; l < 2; ++l) {
            final IndexBuilder indexBuilder = langIndexBuilders[l];
            final Language language = indexBuilder.index.language;
            IndexBuilder.TokensInEntry tokensInEntry = new IndexBuilder.TokensInEntry();

            for (int j = 0; j < subfields[l].length; ++j) {
                String subfield = language.normalizeText(subfields[l][j]);
                if (language == Language.de) {
                    subfield = indexField_DE(subfield, j, tokensInEntry);
                } else if (language == Language.en) {
                    subfield = indexField_EN(subfield, j, tokensInEntry);
                }
                indexFieldGeneric(language, subfield, j, subfields[l].length, tokensInEntry);
            }
        }
    }

    static void indexFieldGeneric(final Language language, String field,
            final int subfieldIdx, final int numSubFields,
            IndexBuilder.TokensInEntry tokensInEntry) {
        // remove bracketed and parenthesized stuff.

        Matcher matcher;
        while ((matcher = Regex.BRACKETED.matcher(field)).find()) {
            for (final String token : language.tokenizeText(matcher.group(1))) {
                tokensInEntry.addReference(token, EntryRefType.PARENTHESIZED);
            }
            field = matcher.replaceFirst(" ");
        }

        while ((matcher = Regex.PARENTHESIZED.matcher(field)).find()) {
            for (final String token : language.tokenizeText(matcher.group(1))) {
                tokensInEntry.addReference(token, EntryRefType.BRACKETED);
            }
            field = matcher.replaceFirst(" ");
        }

        List<String> tokens = language.tokenizeText(field);

        final EntryRefType entryRefType;
        if (numSubFields == 1) {
            assert subfieldIdx == 0;
            if (tokens.size() == 1) {
                entryRefType = EntryRefType.ONE_WORD;
            } else if (tokens.size() == 2) {
                entryRefType = EntryRefType.TWO_WORDS;
            } else if (tokens.size() == 3) {
                entryRefType = EntryRefType.THREE_WORDS;
            } else if (tokens.size() == 4) {
                entryRefType = EntryRefType.FOUR_WORDS;
            } else {
                entryRefType = EntryRefType.FIVE_OR_MORE_WORDS;
            }
        } else {
            assert numSubFields > 1;
            if (subfieldIdx == 0) {
                if (tokens.size() == 1) {
                    entryRefType = EntryRefType.MULTIROW_HEAD_ONE_WORD;
                } else {
                    entryRefType = EntryRefType.MULTIROW_HEAD_MANY_WORDS;
                }
            } else {
                assert subfieldIdx > 0;
                if (tokens.size() == 1) {
                    entryRefType = EntryRefType.MULTIROW_TAIL_ONE_WORD;
                } else {
                    entryRefType = EntryRefType.MULTIROW_TAIL_MANY_WORDS;
                }
            }
        }

        for (String token : tokens) {
            tokensInEntry.addReference(token, entryRefType);
        }
    }

    private String indexField_DE(String field,
            final int subfieldIdx, IndexBuilder.TokensInEntry tokensInEntry) {
        field = Regex.CURLY_BRACED.matcher(field).replaceAll(" ");
        return field;
    }

    private String indexField_EN(String field,
            final int subfieldIdx, IndexBuilder.TokensInEntry tokensInEntry) {
        if (field.startsWith("to ")) {
            // Don't index leading "to "s.
            field = field.substring(3);
        }
        return field;
    }

}
