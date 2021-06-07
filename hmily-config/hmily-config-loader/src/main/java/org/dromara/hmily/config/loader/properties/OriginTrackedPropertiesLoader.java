/*
 * Copyright 2017-2021 Dromara.org
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

package org.dromara.hmily.config.loader.properties;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The type Origin tracked properties loader.
 *
 * @author xiaoyu
 */
public class OriginTrackedPropertiesLoader {

    private final InputStream resource;
    
    /**
     * Instantiates a new Origin tracked properties loader.
     *
     * @param resource the resource
     */
    OriginTrackedPropertiesLoader(final InputStream resource) {
        this.resource = resource;
    }
    
    
    /**
     * Load map.
     *
     * @return the map
     * @throws IOException the io exception
     */
    public Map<String, Object> load() throws IOException {
        return load(true);
    }
    
    /**
     * Load map.
     *
     * @param expandLists the expand lists
     * @return the map
     * @throws IOException the io exception
     */
    public Map<String, Object> load(final boolean expandLists) throws IOException {
        try (CharacterReader reader = new CharacterReader(this.resource)) {
            Map<String, Object> result = new LinkedHashMap<>();
            StringBuilder buffer = new StringBuilder();
            while (reader.read()) {
                String key = loadKey(buffer, reader).trim();
                if (expandLists && key.endsWith("[]")) {
                    key = key.substring(0, key.length() - 2);
                    int index = 0;
                    do {
                        Object value = loadValue(buffer, reader, true);
                        put(result, key + "[" + (index++) + "]", value);
                        if (!reader.isEndOfLine()) {
                            reader.read();
                        }
                    }
                    while (!reader.isEndOfLine());
                } else {
                    Object value = loadValue(buffer, reader, false);
                    put(result, key, value);
                }
            }
            return result;
        }
    }

    private void put(final Map<String, Object> result, final String key, final Object value) {
        if (!key.isEmpty()) {
            result.put(key, value);
        }
    }

    private String loadKey(final StringBuilder buffer, final CharacterReader reader) throws IOException {
        buffer.setLength(0);
        boolean previousWhitespace = false;
        while (!reader.isEndOfLine()) {
            if (reader.isPropertyDelimiter()) {
                reader.read();
                return buffer.toString();
            }
            if (!reader.isWhiteSpace() && previousWhitespace) {
                return buffer.toString();
            }
            previousWhitespace = reader.isWhiteSpace();
            buffer.append(reader.getCharacter());
            reader.read();
        }
        return buffer.toString();
    }

    private Object loadValue(final StringBuilder buffer, final CharacterReader reader, final boolean splitLists) throws IOException {
        buffer.setLength(0);
        while (reader.isWhiteSpace() && !reader.isEndOfLine()) {
            reader.read();
        }
        while (!reader.isEndOfLine() && !(splitLists && reader.isListDelimiter())) {
            buffer.append(reader.getCharacter());
            reader.read();
        }
        return buffer.toString().trim();
    }

    /**
     * Reads characters from the source resource, taking care of skipping comments,
     * handling multi-line values and tracking {@code '\'} escapes.
     */
    private static class CharacterReader implements Closeable {

        private static final String[] ESCAPES = {"trnf", "\t\r\n\f"};

        private final LineNumberReader reader;

        private int columnNumber = -1;

        private boolean escaped;

        private int character;
    
        /**
         * Instantiates a new Character reader.
         *
         * @param resource the resource
         */
        CharacterReader(final InputStream resource) {
            this.reader = new LineNumberReader(new InputStreamReader(resource, StandardCharsets.ISO_8859_1));
        }

        @Override
        public void close() throws IOException {
            this.reader.close();
        }
    
        /**
         * Read boolean.
         *
         * @return the boolean
         * @throws IOException the io exception
         */
        public boolean read() throws IOException {
            return read(false);
        }
    
        /**
         * Read boolean.
         *
         * @param wrappedLine the wrapped line
         * @return the boolean
         * @throws IOException the io exception
         */
        public boolean read(final boolean wrappedLine) throws IOException {
            this.escaped = false;
            this.character = this.reader.read();
            this.columnNumber++;
            if (this.columnNumber == 0) {
                skipLeadingWhitespace();
                if (!wrappedLine) {
                    skipComment();
                }
            }
            if (this.character == '\\') {
                this.escaped = true;
                readEscaped();
            } else if (this.character == '\n') {
                this.columnNumber = -1;
            }
            return !isEndOfFile();
        }

        private void skipLeadingWhitespace() throws IOException {
            while (isWhiteSpace()) {
                this.character = this.reader.read();
                this.columnNumber++;
            }
        }

        private void skipComment() throws IOException {
            if (this.character == '#' || this.character == '!') {
                while (this.character != '\n' && this.character != -1) {
                    this.character = this.reader.read();
                }
                this.columnNumber = -1;
                read();
            }
        }

        private void readEscaped() throws IOException {
            this.character = this.reader.read();
            int escapeIndex = ESCAPES[0].indexOf(this.character);
            if (escapeIndex != -1) {
                this.character = ESCAPES[1].charAt(escapeIndex);
            } else if (this.character == '\n') {
                this.columnNumber = -1;
                read(true);
            } else if (this.character == 'u') {
                readUnicode();
            }
        }

        private void readUnicode() throws IOException {
            this.character = 0;
            for (int i = 0; i < 4; i++) {
                int digit = this.reader.read();
                if (digit >= '0' && digit <= '9') {
                    this.character = (this.character << 4) + digit - '0';
                } else if (digit >= 'a' && digit <= 'f') {
                    this.character = (this.character << 4) + digit - 'a' + 10;
                } else if (digit >= 'A' && digit <= 'F') {
                    this.character = (this.character << 4) + digit - 'A' + 10;
                } else {
                    throw new IllegalStateException("Malformed \\uxxxx encoding.");
                }
            }
        }
    
        /**
         * Is white space boolean.
         *
         * @return the boolean
         */
        public boolean isWhiteSpace() {
            return !this.escaped && (this.character == ' ' || this.character == '\t'
                    || this.character == '\f');
        }
    
        /**
         * Is end of file boolean.
         *
         * @return the boolean
         */
        public boolean isEndOfFile() {
            return this.character == -1;
        }
    
        /**
         * Is end of line boolean.
         *
         * @return the boolean
         */
        public boolean isEndOfLine() {
            return this.character == -1 || (!this.escaped && this.character == '\n');
        }
    
        
        /**
         * Is list delimiter boolean.
         *
         * @return the boolean
         */
        public boolean isListDelimiter() {
            return !this.escaped && this.character == ',';
        }
    
        /**
         * Is property delimiter boolean.
         *
         * @return the boolean
         */
        public boolean isPropertyDelimiter() {
            return !this.escaped && (this.character == '=' || this.character == ':');
        }
    
        /**
         * Gets character.
         *
         * @return the character
         */
        public char getCharacter() {
            return (char) this.character;
        }
    }

}
