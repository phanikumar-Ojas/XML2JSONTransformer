package com.ebsco.platform.shared.cmsimport.rs.config;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;

public class HTMLCharacterEscapes extends CharacterEscapes {
    private final int[] asciiEscapes;

    public HTMLCharacterEscapes() {
        int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
        esc['<'] = CharacterEscapes.ESCAPE_NONE;
        esc['>'] = CharacterEscapes.ESCAPE_NONE;
        esc['&'] = CharacterEscapes.ESCAPE_NONE;
        esc['\''] = CharacterEscapes.ESCAPE_NONE;
        esc['\"'] = CharacterEscapes.ESCAPE_STANDARD;
        asciiEscapes = esc;
    }

    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }

    @Override
    public SerializableString getEscapeSequence(int ch) {
        return null;
    }
}