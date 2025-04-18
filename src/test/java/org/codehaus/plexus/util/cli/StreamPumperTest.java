/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2003, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 500
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>StreamPumperTest class.</p>
 *
 * @author <a href="mailto:pj@thoughtworks.com">Paul Julius</a>
 * @version $Id: $Id
 * @since 3.4.0
 */
class StreamPumperTest {
    private final String lineSeparator = System.lineSeparator();

    /**
     * <p>testPumping.</p>
     */
    @Test
    void pumping() {
        String line1 = "line1";
        String line2 = "line2";
        String lines = line1 + "\n" + line2;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(lines.getBytes());

        TestConsumer consumer = new TestConsumer();
        StreamPumper pumper = new StreamPumper(inputStream, consumer);
        new Thread(pumper).run();

        // Check the consumer to see if it got both lines.
        assertTrue(consumer.wasLineConsumed(line1, 1000));
        assertTrue(consumer.wasLineConsumed(line2, 1000));
    }

    /**
     * <p>testPumpingWithPrintWriter.</p>
     */
    @Test
    void pumpingWithPrintWriter() {
        String inputString = "This a test string";
        ByteArrayInputStream bais = new ByteArrayInputStream(inputString.getBytes());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        StreamPumper pumper = new StreamPumper(bais, pw);
        pumper.run();
        pumper.flush();
        System.out.println("aaa" + sw);
        assertEquals("This a test string" + lineSeparator, sw.toString());
        pumper.close();
    }

    /**
     * <p>testPumperReadsInputStreamUntilEndEvenIfConsumerFails.</p>
     */
    @Test
    void pumperReadsInputStreamUntilEndEvenIfConsumerFails() {
        // the number of bytes generated should surely exceed the read buffer used by the pumper
        GeneratorInputStream gis = new GeneratorInputStream(1024 * 1024 * 4);
        StreamPumper pumper = new StreamPumper(gis, new FailingConsumer());
        pumper.run();
        assertEquals(gis.size, gis.read, "input stream was not fully consumed, producer deadlocks");
        assertTrue(gis.closed);
        assertNotNull(pumper.getException());
    }

    static class GeneratorInputStream extends InputStream {

        final int size;

        int read = 0;

        boolean closed = false;

        public GeneratorInputStream(int size) {
            this.size = size;
        }

        public int read() throws IOException {
            if (read < size) {
                read++;
                return '\n';
            } else {
                return -1;
            }
        }

        public void close() throws IOException {
            closed = true;
        }
    }

    static class FailingConsumer implements StreamConsumer {

        public void consumeLine(String line) {
            throw new NullPointerException("too bad, the consumer is badly implemented...");
        }
    }

    /**
     * Used by the test to track whether a line actually got consumed or not.
     */
    static class TestConsumer implements StreamConsumer {

        private final List<String> lines = new ArrayList<>();

        /**
         * Checks to see if this consumer consumed a particular line. This method will wait up to timeout number of
         * milliseconds for the line to get consumed.
         *
         * @param testLine Line to test for.
         * @param timeout Number of milliseconds to wait for the line.
         * @return true if the line gets consumed, else false.
         */
        public boolean wasLineConsumed(String testLine, long timeout) {

            long start = System.currentTimeMillis();
            long trialTime = 0;

            do {
                if (lines.contains(testLine)) {
                    return true;
                }

                // Sleep a bit.
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // ignoring...
                }

                // How long have been waiting for the line?
                trialTime = System.currentTimeMillis() - start;

            } while (trialTime < timeout);

            // If we got here, then the line wasn't consumed within the timeout
            return false;
        }

        public void consumeLine(String line) {
            lines.add(line);
        }
    }

    /**
     * <p>testEnabled.</p>
     */
    @Test
    void enabled() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("AB\nCE\nEF".getBytes());
        TestConsumer streamConsumer = new TestConsumer();
        StreamPumper streamPumper = new StreamPumper(byteArrayInputStream, streamConsumer);
        streamPumper.run();
        assertEquals(3, streamConsumer.lines.size());
    }

    /**
     * <p>testDisabled.</p>
     */
    @Test
    void disabled() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("AB\nCE\nEF".getBytes());
        TestConsumer streamConsumer = new TestConsumer();
        StreamPumper streamPumper = new StreamPumper(byteArrayInputStream, streamConsumer);
        streamPumper.disable();
        streamPumper.run();
        assertEquals(0, streamConsumer.lines.size());
    }
}
