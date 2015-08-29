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

package com.hughes.android.dictionary.engine2;

import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.hughes.android.dictionary.engine.Index.IndexEntry;
import com.hughes.android.dictionary.parser.DictFileParser;

public class IndexBuilder {

    final DictionaryBuilder dictionaryBuilder;
    public final Index index;
    final Set<String> stoplist;
    
    static class TrieEntry {
        Map<String, TokenData> tokenToTokenData = new TreeMap<String, IndexBuilder.TokenData>();
    }
    final TrieBuilder<TrieEntry> trieBuilder = new TrieBuilder<TrieEntry>();
    
    public static class TokenData {
        final Map<AbstractEntry, EntryRefType> entryToBestRefType = new LinkedHashMap<AbstractEntry, EntryRefType>();
        public boolean hasMainEntry = false;

        TokenData(final String token) {
            assert token.equals(token.trim());
            assert token.length() > 0;
        }
    }

    public static class TokensInEntry {
        Map<String, EntryRefType> tokenToRefType = new LinkedHashMap<String, EntryRefType>();

        public void addReference(String token, EntryRefType entryRefType) {
            EntryRefType currentEntryRefType = tokenToRefType.get(token);
            if (currentEntryRefType == null) {
                tokenToRefType.put(token, entryRefType);
            } else if (entryRefType.ordinal() < currentEntryRefType.ordinal()) {
                assert !currentEntryRefType.mainWord || entryRefType.mainWord;
                tokenToRefType.put(token, entryRefType);
            }
        }
    }

    IndexBuilder(final DictionaryBuilder dictionaryBuilder, final String name,
            final Language language,
            final Set<String> stoplist) {
        this.dictionaryBuilder = dictionaryBuilder;
        this.index = new Index(dictionaryBuilder.dictionary, language, name);
        this.stoplist = stoplist;
    }
    
    public void addEntryRef(final String token, final AbstractEntry entry, final EntryRefType entryRefType) {
        final String normalized = index.language.normalizeText(token);
        TrieBuilder.Node<TrieEntry> trieEntry = trieBuilder.getOrCreateNode(Index.toUTF8(normalized));
        trieEntry.value
    }

    public void build() {
        final Set<IndexedEntry> tokenIndexedEntries = new HashSet<IndexedEntry>();
        final List<RowBase> rows = index.rows;
        index.mainTokenCount = 0;
        for (final TokenData tokenData : tokenToData.values()) {
            tokenIndexedEntries.clear();
            final int indexIndex = index.sortedIndexEntries.size();
            final int startRow = rows.size();

            TokenRow tokenRow = null;
            if (!tokenData.htmlEntries.isEmpty()) {
                tokenRow = new TokenRow(indexIndex, rows.size(), index, tokenData.hasMainEntry);
                rows.add(tokenRow);
            }

            // System.out.println("Added TokenRow: " + rows.get(rows.size() -
            // 1));

            int numRows = 0; // off by one--doesn't count the token row!
            // System.out.println("TOKEN: " + tokenData.token);
            for (final Map.Entry<EntryRefType, List<IndexedEntry>> typeToIndexedEntries : tokenData.typeToEntries
                    .entrySet()) {
                for (final IndexedEntry indexedEntry : typeToIndexedEntries.getValue()) {
                    if (!indexedEntry.isValid) {
                        continue;
                    }

                    if (tokenRow == null) {
                        tokenRow = new TokenRow(indexIndex, rows.size(), index,
                                tokenData.hasMainEntry);
                        rows.add(tokenRow);
                    }

                    if (indexedEntry.entry.index() == -1) {
                        indexedEntry.entry.addToDictionary(dictionaryBuilder.dictionary);
                        assert indexedEntry.entry.index() >= 0;
                    }
                    if (tokenIndexedEntries.add(indexedEntry)
                            && !tokenData.htmlEntries.contains(indexedEntry.entry)) {
                        rows.add(indexedEntry.entry.CreateRow(rows.size(), index));
                        ++indexedEntry.entry.entrySource.numEntries;
                        ++numRows;

                        // System.out.print("  " + typeToEntry.getKey() + ": ");
                        // rows.get(rows.size() - 1).print(System.out);
                        // System.out.println();
                    }
                }
            }

            if (tokenRow != null) {
                if (tokenRow.hasMainEntry) {
                    index.mainTokenCount++;
                }

                final Index.IndexEntry indexEntry = new Index.IndexEntry(index, tokenData.token,
                        index
                                .normalizer().transliterate(tokenData.token), startRow, numRows);
                indexEntry.htmlEntries.addAll(tokenData.htmlEntries);
                index.sortedIndexEntries.add(indexEntry);
            }
        }

        final List<IndexEntry> entriesSortedByNumRows = new ArrayList<IndexEntry>(
                index.sortedIndexEntries);
        Collections.sort(entriesSortedByNumRows, new Comparator<IndexEntry>() {
            @Override
            public int compare(IndexEntry object1, IndexEntry object2) {
                return object2.numRows - object1.numRows;
            }
        });
        System.out.println("Most common tokens:");
        for (int i = 0; i < 50 && i < entriesSortedByNumRows.size(); ++i) {
            System.out.println("  " + entriesSortedByNumRows.get(i));
        }
    }

    public TokenData getOrCreateTokenData(final String token) {
        TokenData tokenData = tokenToData.get(token);
        if (tokenData == null) {
            tokenData = new TokenData(token);
            tokenToData.put(token, tokenData);
        }
        return tokenData;
    }

    private List<IndexedEntry> getOrCreateEntries(final String token,
            final EntryRefType entryTypeName) {
        final TokenData tokenData = getOrCreateTokenData(token);
        List<IndexedEntry> entries = tokenData.typeToEntries.get(entryTypeName);
        if (entryTypeName.mainWord) {
            tokenData.hasMainEntry = true;
        }
        if (entries == null) {
            entries = new ArrayList<IndexedEntry>();
            tokenData.typeToEntries.put(entryTypeName, entries);
        }
        return entries;
    }

    public void addEntryWithTokens(final IndexedEntry indexedEntry, final Set<String> tokens,
            final EntryRefType entryTypeName) {
        if (indexedEntry == null) {
            System.out.println("asdfasdf");
        }
        assert indexedEntry != null;
        for (final String token : tokens) {
            if (entryTypeName.overridesStopList || !stoplist.contains(token)) {
                getOrCreateEntries(token, entryTypeName).add(indexedEntry);
            }
        }
    }

    public void addEntryWithString(final IndexedEntry indexedEntry, final String untokenizedString,
            final EntryRefType entryTypeName) {
        final Set<String> tokens = DictFileParser.tokenize(untokenizedString,
                DictFileParser.NON_CHAR);
        addEntryWithTokens(indexedEntry, tokens,
                tokens.size() == 1 ? entryTypeName.singleWordInstance : entryTypeName);
    }

    public void addEntryWithStringNoSingle(final IndexedEntry indexedEntry,
            final String untokenizedString,
            final EntryRefType entryTypeName) {
        final Set<String> tokens = DictFileParser.tokenize(untokenizedString,
                DictFileParser.NON_CHAR);
        addEntryWithTokens(indexedEntry, tokens, entryTypeName);
    }
}
