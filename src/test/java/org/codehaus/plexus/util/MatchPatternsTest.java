package org.codehaus.plexus.util;

/*
 * Copyright The Codehaus Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>MatchPatternsTest class.</p>
 *
 * @author herve
 * @version $Id: $Id
 * @since 3.4.0
 */
class MatchPatternsTest {
    /**
     * <p>testGetSource</p>
     */
    @Test
    void getSources() {
        List<String> expected = Arrays.asList("ABC**", "some/ABC*", "[ABC].*");
        MatchPatterns from = MatchPatterns.from("ABC**", "%ant[some/ABC*]", "%regex[[ABC].*]");
        List<String> actual = from.getSources();
        assertEquals(expected, actual);
    }

    /**
     * <p>testMatches.</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Test
    void matches() throws Exception {
        MatchPatterns from = MatchPatterns.from("ABC**", "CDE**");
        assertTrue(from.matches("ABCDE", true));
        assertTrue(from.matches("CDEF", true));
        assertFalse(from.matches("XYZ", true));
    }
}
