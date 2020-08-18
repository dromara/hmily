/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dromara.hmily.config.loader.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

/**
 * Base class for YAML factories.
 * <p>
 * Requires SnakeYAML 1.18 or higher, as of Spring Framework 5.0.6.
 * ref: org.springframework.beans.factory.config.YamlProcessor
 *
 * @author Dave Syer
 * @author Juergen Hoeller
 * @author xiaoyu
 * @since 4.1
 */
public abstract class YamlProcessor {
    
    private ResolutionMethod resolutionMethod = ResolutionMethod.OVERRIDE;

    private Logger logger = LoggerFactory.getLogger(YamlProcessor.class);

    private List<DocumentMatcher> documentMatchers = Collections.emptyList();

    private boolean matchDefault = true;

    private InputStream[] resources = new InputStream[0];

    /**
     * A map of document matchers allowing callers to selectively use only
     * some of the documents in a YAML resource. In YAML documents are
     * separated by <code>---<code> lines, and each document is converted
     * to properties before the match is made. E.g.
     * <pre class="code">
     * environment: dev
     * url: http://dev.bar.com
     * name: Developer Setup
     * ---
     * environment: prod
     * url:http://foo.bar.com
     * name: My Cool App
     * </pre>
     * when mapped with
     * <pre class="code">
     * setDocumentMatchers(properties ->
     *     ("prod".equals(properties.getProperty("environment")) ? MatchStatus.FOUND : MatchStatus.NOT_FOUND));
     * </pre>
     * would end up as
     * <pre class="code">
     * environment=prod
     * url=http://foo.bar.com
     * name=My Cool App
     * </pre>
     */
    public void setDocumentMatchers(final DocumentMatcher... matchers) {
        this.documentMatchers = Arrays.asList(matchers);
    }

    /**
     * Flag indicating that a document for which all the
     * {@link #setDocumentMatchers(DocumentMatcher...) document matchers} abstain will
     * nevertheless match. Default is {@code true}.
     */
    public void setMatchDefault(final boolean matchDefault) {
        this.matchDefault = matchDefault;
    }

    /**
     * Method to use for resolving resources. Each resource will be converted to a Map,
     * so this property is used to decide which map entries to keep in the final output
     * from this factory. Default is {@link ResolutionMethod#OVERRIDE}.
     */
    public void setResolutionMethod(final ResolutionMethod resolutionMethod) {
        this.resolutionMethod = resolutionMethod;
    }

    /**
     * Set locations of YAML {@link InputStream resources} to be loaded.
     *
     * @see ResolutionMethod
     */
    public void setResources(final InputStream... resources) {
        this.resources = resources;
    }

    /**
     * Provide an opportunity for subclasses to process the Yaml parsed from the supplied
     * resources. Each resource is parsed in turn and the documents inside checked against
     * the {@link #setDocumentMatchers(DocumentMatcher...) matchers}. If a document
     * matches it is passed into the callback, along with its representation as Properties.
     * Depending on the {@link #setResolutionMethod(ResolutionMethod)} not all of the
     * documents will be parsed.
     *
     * @param callback a callback to delegate to once matching documents are found
     * @see #createYaml()
     */
    protected void process(final MatchCallback callback) {
        Yaml yaml = createYaml();
        for (InputStream resource : this.resources) {
            boolean found = process(callback, yaml, resource);
            if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND && found) {
                return;
            }
        }
    }

    /**
     * Create the {@link Yaml} instance to use.
     * <p>The default implementation sets the "allowDuplicateKeys" flag to {@code false},
     * enabling built-in duplicate key handling in SnakeYAML 1.18+.
     *
     * @see LoaderOptions#setAllowDuplicateKeys(boolean)
     */
    protected Yaml createYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        return new Yaml(options);
    }

    private boolean process(final MatchCallback callback, final Yaml yaml, final InputStream resource) {
        int count = 0;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Loading from YAML: " + resource);
            }
            try (Reader reader = new UnicodeReader(resource)) {
                for (Object object : yaml.loadAll(reader)) {
                    if (object != null && process(asMap(object), callback)) {
                        count++;
                        if (this.resolutionMethod == ResolutionMethod.FIRST_FOUND) {
                            break;
                        }
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Loaded " + count + " document" + (count > 1 ? "s" : "") +
                            " from YAML resource: " + resource);
                }
            }
        } catch (IOException ex) {
            handleProcessError(resource, ex);
        }
        return (count > 0);
    }

    private void handleProcessError(final InputStream resource, final IOException ex) {
        if (this.resolutionMethod != ResolutionMethod.FIRST_FOUND &&
                this.resolutionMethod != ResolutionMethod.OVERRIDE_AND_IGNORE) {
            throw new IllegalStateException(ex);
        }
        if (logger.isWarnEnabled()) {
            logger.warn("Could not load map from " + resource + ": " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(final Object object) {
        // YAML can have numbers as keys
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(object instanceof Map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                value = asMap(value);
            }
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                // It has to be a map key in this case
                result.put("[" + key.toString() + "]", value);
            }
        });
        return result;
    }

    private boolean process(final Map<String, Object> map, final MatchCallback callback) {
        Properties properties = new Properties();
        properties.putAll(getFlattenedMap(map));
        if (this.documentMatchers.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Merging document (no matchers set): " + map);
            }
            callback.process(properties, map);
            return true;
        }
        MatchStatus result = MatchStatus.ABSTAIN;
        for (DocumentMatcher matcher : this.documentMatchers) {
            MatchStatus match = matcher.matches(properties);
            result = MatchStatus.getMostSpecific(match, result);
            if (match == MatchStatus.FOUND) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Matched document with document matcher: " + properties);
                }
                callback.process(properties, map);
                return true;
            }
        }

        if (result == MatchStatus.ABSTAIN && this.matchDefault) {
            if (logger.isDebugEnabled()) {
                logger.debug("Matched document with default matcher: " + map);
            }
            callback.process(properties, map);
            return true;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Unmatched document: " + map);
        }
        return false;
    }

    /**
     * Return a flattened version of the given map, recursively following any nested Map
     * or Collection values. Entries from the resulting map retain the same order as the
     * source. When called with the Map from a {@link MatchCallback} the result will
     * contain the same values as the {@link MatchCallback} Properties.
     *
     * @param source the source map
     * @return a flattened map
     * @since 4.1.3
     */
    protected final Map<String, Object> getFlattenedMap(final Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    private void buildFlattenedMap(final Map<String, Object> result, final Map<String, Object> source, final String path) {
        source.forEach((key, value) -> {
            if (StringUtils.isNotBlank(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + '.' + key;
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                if (collection.isEmpty()) {
                    result.put(key, "");
                } else {
                    int count = 0;
                    for (Object object : collection) {
                        buildFlattenedMap(result, Collections.singletonMap(
                                "[" + (count++) + "]", object), key);
                    }
                }
            } else {
                result.put(key, (value != null ? value : ""));
            }
        });
    }


    /**
     * Callback interface used to process the YAML parsing results.
     */
    public interface MatchCallback {

        /**
         * Process the given representation of the parsing results.
         *
         * @param properties the properties to process (as a flattened
         *                   representation with indexed keys in case of a collection or map)
         * @param map        the result map (preserving the original value structure
         *                   in the YAML document)
         */
        void process(Properties properties, Map<String, Object> map);
    }


    /**
     * Strategy interface used to test if properties match.
     */
    public interface DocumentMatcher {

        /**
         * Test if the given properties match.
         *
         * @param properties the properties to test
         * @return the status of the match
         */
        MatchStatus matches(Properties properties);
    }


    /**
     * Status returned from {@link DocumentMatcher#matches(Properties)}
     */
    public enum MatchStatus {

        /**
         * A match was found.
         */
        FOUND,

        /**
         * The matcher should not be considered.
         */
        ABSTAIN;

        /**
         * Compare two {@link MatchStatus} items, returning the most specific status.
         */
        public static MatchStatus getMostSpecific(MatchStatus a, MatchStatus b) {
            return (a.ordinal() < b.ordinal() ? a : b);
        }
    }


    /**
     * Method to use for resolving resources.
     */
    public enum ResolutionMethod {

        /**
         * Replace values from earlier in the list.
         */
        OVERRIDE,

        /**
         * Replace values from earlier in the list, ignoring any failures.
         */
        OVERRIDE_AND_IGNORE,

        /**
         * Take the first resource in the list that exists and use just that.
         */
        FIRST_FOUND
    }
    
}
