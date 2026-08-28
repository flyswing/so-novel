package com.pcdd.sonovel.dsl.core;

import com.pcdd.sonovel.core.HtmlExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DSLParserTest {

    @Test
    @DisplayName("parse(null) 不抛出空指针")
    void parseNull() {
        assertDoesNotThrow(() -> DSLParser.parse(null));
        assertNotNull(DSLParser.parse(null));
    }

    @Test
    @DisplayName("executeDsl(null, input) 原样返回 input")
    void executeDslNullDsl() {
        assertEquals("hello", HtmlExtractor.executeDsl(null, "hello"));
        assertNull(HtmlExtractor.executeDsl(null, null));
    }

}
