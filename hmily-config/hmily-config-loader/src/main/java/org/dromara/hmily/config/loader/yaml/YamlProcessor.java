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

package org.dromara.hmily.config.loader.yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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
 * The type Yaml processor.
 *
 * @author xiaoyu
 */
public abstract class YamlProcessor {
    
    private ResolutionMethod resolutionMethod = ResolutionMethod.OVERRIDE;

    private Logger logger = LoggerFactory.getLogger(YamlProcessor.class);

    private List<DocumentMatcher> documentMatchers = Collections.emptyList();
    
    private InputStream[] resources = new InputStream[0];
    
    /**
     * Sets resources.
     *
     * @param resources the resources
     */
    public void setResources(final InputStream... resources) {
        this.resources = resources;
    }
    
    /**
     * Process.
     *
     * @param callback the callback
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
                    logger.debug("Loaded " + count + " document" + (count > 1 ? "s" : "") + " from YAML resource: " + resource);
                }
            }
        } catch (IOException ex) {
            handleProcessError(resource, ex);
        }
        return count > 0;
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
        if (result == MatchStatus.ABSTAIN) {
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
    
    protected Yaml createYaml() {
        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        return new Yaml(options);
    }
    
    private void handleProcessError(final InputStream resource, final IOException ex) {
        if (this.resolutionMethod != ResolutionMethod.FIRST_FOUND && this.resolutionMethod != ResolutionMethod.OVERRIDE_AND_IGNORE) {
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
    
    /**
     * Gets flattened map.
     *
     * @param source the source
     * @return the flattened map
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
                result.put(key, value != null ? value : "");
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
         * @param properties the properties to process (as a flattened                   representation with indexed keys in case of a collection or map)
         * @param map        the result map (preserving the original value structure                   in the YAML document)
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
     * Status returned from {@link DocumentMatcher#matches(Properties)}.
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
         *
         * @param a the a
         * @param b the b
         * @return the most specific
         */
        public static MatchStatus getMostSpecific(final MatchStatus a, final MatchStatus b) {
            return a.ordinal() < b.ordinal() ? a : b;
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
