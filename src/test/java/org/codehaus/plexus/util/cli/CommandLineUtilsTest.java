package org.codehaus.plexus.util.cli;

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
import java.util.Locale;
import java.util.Properties;

import org.codehaus.plexus.util.Os;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * <p>CommandLineUtilsTest class.</p>
 *
 * @author herve
 * @version $Id: $Id
 * @since 3.4.0
 */
@SuppressWarnings({"deprecation"})
class CommandLineUtilsTest {

    /**
     * <p>testQuoteArguments.</p>
     */
    @Test
    void quoteArguments() {
        Assertions.assertDoesNotThrow(() -> {
            String result = CommandLineUtils.quote("Hello");
            System.out.println(result);
            assertEquals("Hello", result);
            result = CommandLineUtils.quote("Hello World");
            System.out.println(result);
            assertEquals("\"Hello World\"", result);
            result = CommandLineUtils.quote("\"Hello World\"");
            System.out.println(result);
            assertEquals("'\"Hello World\"'", result);
        });
        try {
            CommandLineUtils.quote("\"Hello 'World''");
            fail();
        } catch (Exception ignored) {
        }
    }

    /**
     * Tests that case-insensitive environment variables are normalized to upper case.
     *
     * @throws java.lang.Exception if any.
     */
    @Test
    void getSystemEnvVarsCaseInsensitive() throws Exception {
        Properties vars = CommandLineUtils.getSystemEnvVars(false);
        for (Object o : vars.keySet()) {
            String variable = (String) o;
            assertEquals(variable.toUpperCase(Locale.ENGLISH), variable);
        }
    }

    /**
     * Tests that environment variables on Windows are normalized to upper case. Does nothing on Unix platforms.
     *
     * @throws java.lang.Exception if any.
     */
    @Test
    void getSystemEnvVarsWindows() throws Exception {
        if (!Os.isFamily(Os.FAMILY_WINDOWS)) {
            return;
        }
        Properties vars = CommandLineUtils.getSystemEnvVars();
        for (Object o : vars.keySet()) {
            String variable = (String) o;
            assertEquals(variable.toUpperCase(Locale.ENGLISH), variable);
        }
    }

    /**
     * Tests the splitting of a command line into distinct arguments.
     *
     * @throws java.lang.Exception if any.
     */
    @Test
    void translateCommandline() throws Exception {
        assertCmdLineArgs(new String[] {}, null);
        assertCmdLineArgs(new String[] {}, "");

        assertCmdLineArgs(new String[] {"foo", "bar"}, "foo bar");
        assertCmdLineArgs(new String[] {"foo", "bar"}, "   foo   bar   ");

        assertCmdLineArgs(new String[] {"foo", " double quotes ", "bar"}, "foo \" double quotes \" bar");
        assertCmdLineArgs(new String[] {"foo", " single quotes ", "bar"}, "foo ' single quotes ' bar");

        assertCmdLineArgs(new String[] {"foo", " \" ", "bar"}, "foo ' \" ' bar");
        assertCmdLineArgs(new String[] {"foo", " ' ", "bar"}, "foo \" ' \" bar");
    }

    private void assertCmdLineArgs(String[] expected, String cmdLine) throws Exception {
        String[] actual = CommandLineUtils.translateCommandline(cmdLine);
        assertNotNull(actual);
        assertEquals(expected.length, actual.length);
        assertEquals(Arrays.asList(expected), Arrays.asList(actual));
    }
}
