package com.hughes.android.dictionary.engine2;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrieBuilderTest {

    @Test
    public void testEmpty() {
        TrieBuilder<String> trieBuilder = new TrieBuilder<String>();
        TrieBuilder.Node<String> node1 = trieBuilder.getOrCreateNode("".getBytes());
        node1.value = "my value";
        TrieBuilder.Node<String> node2 = trieBuilder.getOrCreateNode("".getBytes());
        assertEquals(node1, node2);
        assertEquals("my value", node2.value);
    }
    
    @Test
    public void testSerialize() {
        TrieBuilder<Integer> trieBuilder = new TrieBuilder<Integer>();
        List<TrieBuilder.Node<Integer>> nodes = new ArrayList<TrieBuilder.Node<Integer>>();
        
        String[] strings = new String[] {"", "abcD", "a", "ab", "a", "abcE", "abcA", "a", "abcEf"};
        
        for (String string : strings) {
            nodes.add(trieBuilder.getOrCreateNode(string.getBytes()));
        }
        
        Trie trie = trieBuilder.serialize();
        for (int i = 0; i < strings.length; ++i) {
            assertEquals(nodes.get(i).index, trie.find(strings[i].getBytes()));
        }
        assertEquals(-1, trie.find("A".getBytes()));
        assertEquals(8, trie.nodeStarts.length);
        assertEquals(7, trie.childKeyBytes.length);
    }
    
    @Test
    public void testRandom() {
        TrieBuilder<Integer> trieBuilder = new TrieBuilder<Integer>();
        List<TrieBuilder.Node<Integer>> nodes = new ArrayList<TrieBuilder.Node<Integer>>();
        
        Random random = new Random(0);
        List<byte[]> strings = new ArrayList<byte[]>();
        for (int i = 0; i < 10000; ++i) {
            int len = random.nextInt(8);
            byte[] bytes = new byte[len];
            random.nextBytes(bytes);
            strings.add(bytes);
        }
        
        for (byte[] string : strings) {
            nodes.add(trieBuilder.getOrCreateNode(string));
        }
        
        Trie trie = trieBuilder.serialize();
        for (int i = 0; i < strings.size(); ++i) {
            assertEquals(nodes.get(i).index, trie.find(strings.get(i)));
        }
        assertEquals(25963, trie.nodeStarts.length);
        assertEquals(25962, trie.childKeyBytes.length);
    }
}
