package com.hughes.android.dictionary.engine2;

import com.hughes.android.dictionary.engine2.Trie;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TrieBuilder<T> {

    static class Node<T> {
        final Map<Byte, Node<T>> children = new TreeMap<Byte, Node<T>>();
        T value;
        int index = -1;
        
        void serialize(TrieBuilder<T> trieBuilder) {
            assert trieBuilder.nodeLengths.size() == trieBuilder.nodeStarts.size();
            assert trieBuilder.childKeyBytes.size() == trieBuilder.childNextNodeIndices.size();
            index = trieBuilder.nodeLengths.size();
            final int start = trieBuilder.childKeyBytes.size();
            trieBuilder.nodeStarts.add(start);
            assert children.size() < 127;
            trieBuilder.nodeLengths.add((short) children.size());
            
            for (int i = 0; i < children.size(); ++i) {
                trieBuilder.childKeyBytes.add(null);
                trieBuilder.childNextNodeIndices.add(null);
            }
            
            int pos = start;
            for (final Map.Entry<Byte, Node<T>> entry : children.entrySet()) {
                entry.getValue().serialize(trieBuilder);
                trieBuilder.childKeyBytes.set(pos, entry.getKey());
                assert entry.getValue().index != -1;
                trieBuilder.childNextNodeIndices.set(pos, entry.getValue().index);
                ++pos;
            }
        }
    }
    

    final Node<T> root = new Node<T>();
    
    List<Integer> nodeStarts = new ArrayList<Integer>(); 
    List<Short> nodeLengths = new ArrayList<Short>(); 
    List<Byte> childKeyBytes = new ArrayList<Byte>(); 
    List<Integer> childNextNodeIndices = new ArrayList<Integer>(); 

    Node<T> getOrCreateNode(byte[] bytes) {
        Node<T> node = root;
        for (int i = 0; i < bytes.length; ++i) {
            Node<T> nextNode = node.children.get(bytes[i]);
            if (nextNode == null) {
                nextNode = new Node<T>();
                node.children.put(bytes[i], nextNode);
            }
            node = nextNode;
        }
        return node;
    }
    
    Trie serialize() {
        nodeStarts = new ArrayList<Integer>(); 
        nodeLengths = new ArrayList<Short>(); 
        childKeyBytes = new ArrayList<Byte>(); 
        childNextNodeIndices = new ArrayList<Integer>(); 
        
        root.serialize(this);
        assert nodeLengths.size() == nodeStarts.size();
        assert childKeyBytes.size() == childNextNodeIndices.size();
        
        Trie result = new Trie();
        result.nodeStarts = new int[nodeStarts.size()];
        result.nodeLengths = new short[nodeLengths.size()];
        result.childKeyBytes = new byte[childKeyBytes.size()];
        result.childNextNodeIndices = new int[childNextNodeIndices.size()];
        for (int i = 0; i < result.nodeStarts.length; ++i) {
            result.nodeStarts[i] = nodeStarts.get(i);
            result.nodeLengths[i] = nodeLengths.get(i);
        }
        for (int i = 0; i < result.childKeyBytes.length; ++i) {
            result.childKeyBytes[i] = childKeyBytes.get(i);
            result.childNextNodeIndices[i] = childNextNodeIndices.get(i);
        }
        return result;
    }
}
