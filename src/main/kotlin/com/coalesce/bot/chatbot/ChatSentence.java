package com.coalesce.bot.chatbot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatSentence implements Cloneable {
    /**
     * List of words.
     */
    private List<Object> words;
    /**
     * Quick search construct to have O(ln) lookup times.
     */
    private Set<Object> contains;

    /**
     * Starts to build a sentence with a single word as anchor
     */
    public ChatSentence(ChatWord anchor) {
        if (anchor == null) {
            throw new IllegalArgumentException("Anchor must not be null");
        }
        words = new ArrayList<>();
        contains = new HashSet<>();
        words.add(anchor);
        contains.add(anchor);
    }

    /**
     * Starts a sentence using an existing ChatSentence. Also used for
     * cloning.
     */
    public ChatSentence(ChatSentence src) {
        words = new ArrayList<>();
        contains = new HashSet<>();
        appendSentence(src);
    }

    /**
     * Adds a word to a sentence
     */
    public ChatSentence addWord(ChatWord word) {
        if (word == null) {
            throw new IllegalArgumentException("Can't add null word");
        }
        words.add(word);
        contains.add(word);
        return this;
    }

    /**
     * Adds a character to a sentence.
     */
    public ChatSentence addCharacter(Character punc) {
        if (punc == null) {
            throw new IllegalArgumentException("Can't add null punctuation");
        }
        words.add(punc);
        contains.add(punc);
        return this;
    }

    /**
     * Replace a sentence with some other sentence.
     * Useful to preserve references.
     */
    public ChatSentence replaceSentence(ChatSentence src) {
        words.clear();
        contains.clear();
        appendSentence(src);
        return this;
    }

    public ChatSentence appendSentence(ChatSentence src) {
        words.addAll(src.getWords());
        contains.addAll(src.getWords());
        return this;
    }

    /**
     * Get last word of the sentence.
     */
    public ChatWord getLastWord() {
        for (int i=words.size()-1; i>=0; i--) {
            if (words.get(i) instanceof ChatWord) {
                return (ChatWord) words.get(i);
            }
        }
        throw new IllegalStateException("No ChatWords found!");
    }

    /**
     * Checks if the sentence has a word
     */
    public boolean hasWord(ChatWord word) {
        return contains.contains(word);
    }

    /**
     * Counts the number of words in a sentence.
     */
    public int countWords() {
        int cnt = 0;
        for (Object o : words) {
            if (o instanceof ChatWord) {
                cnt++;
            }
        }
        return cnt;
    }

    /**
     * Gets all the words of the sentence
     */
    private List<Object> getWords() {
        return words;
    }

    /**
     * Returns the sentence as a string.
     */
    @Override
    public String toString() {
        //TODO make something that actually works
        /*StringBuffer sb = new StringBuffer();
        for (Object o : words) {
            if (o instanceof ChatWord) {
                ChatWord cw = (ChatWord) o;
                sb.append(" ");
                sb.append( cw.getWord() );
            } else {
                sb.append(o);
            }
        }
        return sb.toString().trim();*/
        return "";
    }

    /**
     * Clones this sentence.
     */
    @Override
    public Object clone() {
        return new ChatSentence(this);
    }
}
