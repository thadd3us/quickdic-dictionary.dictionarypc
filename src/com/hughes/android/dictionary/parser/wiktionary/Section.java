package com.hughes.android.dictionary.parser.wiktionary;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class Section {
    int depth;
    
    Elements headerLine;
    List<Element> elements = new ArrayList<Element>();
    List<Section> subsections = new ArrayList<Section>();
    
    static int GetHeaderDepth(Element header) {
        final String tagName = header.tagName();
        if (tagName.equals("h1")) {
            return 1;
        }
        if (tagName.equals("h2")) {
            return 2;
        }
        if (tagName.equals("h3")) {
            return 3;
        }
        if (tagName.equals("h4")) {
            return 4;
        }
        if (tagName.equals("h5")) {
            return 5;
        }
        if (tagName.equals("h6")) {
            return 6;
        }
        if (tagName.equals("h7")) {
            return 7;
        }
        return -1;
    }
    
    static Element GetContainingHeader(Element target, int maxDepthAllowed) {
        do {
            Element sibling = target;
            while ((sibling = sibling.previousElementSibling()) != null) {
                final int depth = GetHeaderDepth(sibling);
                if (depth != -1 && depth <= maxDepthAllowed) {
                    return sibling;
                }
            }
        } while ((target = target.parent()) != null);
        return null;
    }
    
    static Elements GetAllContainingHeaders(Element target) {
        Elements result = new Elements();
        int maxDepthAllowed = 7;
        while ((target = GetContainingHeader(target, maxDepthAllowed)) != null) {
            maxDepthAllowed = GetHeaderDepth(target) - 1;
            result.add(target);
        }
        return result;
    }

    public static Element findStrong(Element sibling) {
        while (sibling != null && sibling.tagName().equals("p")) {
            if (sibling.select("strong").size() > 0) {
                return sibling;
            }
            sibling = sibling.nextElementSibling();
        }
        return null;
    }
    
    
}
