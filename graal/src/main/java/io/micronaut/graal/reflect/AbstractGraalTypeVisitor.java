/*
 * Copyright 2017-2018 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micronaut.graal.reflect;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.visitor.VisitorContext;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Abstract base class for Graal visitors.
 *
 * @author Iván López
 * @since 1.1.0
 */
abstract class AbstractGraalTypeVisitor {

    /**
     * System property that indicates the location of the generated reflection JSON file.
     */
    public static final String REFLECTION_JSON_FILE = "graalvm.reflection.json";

    static final String ATTR_TEST_MODE = "io.micronaut.GRAAL_TEST";
    private static Map<Class, List<Map>> testReferences = null;
    private static final String BASE_REFLECT_FILE_PATH = "src/main/graal/reflect.json";
    private static final String CLASSES_CONTEXT_KEY = "CLASSES_CONTEXT";

    private List<Map> json = null;

    /**
     * Called when visitor processing starts.
     *
     * @param visitorContext The visitor context
     */
    public void start(VisitorContext visitorContext) {
        if (json != null) {
            return;
        }

        File baseReflect = new File(BASE_REFLECT_FILE_PATH);
        if (!baseReflect.exists()) {
            json = new ArrayList<>();
        } else {
            ObjectMapper mapper = new ObjectMapper();
            try {
                CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);
                json = mapper.readValue(baseReflect, collectionType);
            } catch (IOException e) {
                json = new ArrayList<>();
            }
        }

    }

    /**
     * Called when visitor processing ends.
     *
     * @param visitorContext The visitor context
     */
    public void finish(VisitorContext visitorContext) {
        for (String className : this.getClassesFromContext(visitorContext)) {
            json.add(CollectionUtils
                    .mapOf("name", className, "allPublicMethods", true, "allDeclaredConstructors", true));
        }

        if (Boolean.getBoolean(ATTR_TEST_MODE)) {
            if (testReferences == null) {
                testReferences = new HashMap<>();
            }
            testReferences.put(getClass(), json);
            return;
        }

        String f = System.getProperty(REFLECTION_JSON_FILE);
        File file;
        if (StringUtils.isNotEmpty(f)) {
            file = new File(f);
        } else {
            File parent = new File("build");
            if (!parent.exists() || !parent.isDirectory()) {
                parent = new File("target");
            }

            if (!parent.exists() || !parent.isDirectory()) {
                visitorContext.warn("Neither 'build' nor 'target' directories exist. Graal reflection file can't be generated", null);
                file = null;
            } else {
                file = new File(parent, "reflect.json");
            }
        }

        if (file != null && !file.exists()) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            try {
                System.out.println("Writing reflect.json file to destination: " + file);
                writer.writeValue(file, json);
            } catch (IOException e) {
                System.err.println("Could not write Graal reflect.json: " + e.getMessage());
            }
        }
    }

    /**
     * Whether a class is valid to be included in the reflect.json file.
     *
     * @param type The class to check
     * @return true if the class can be included, false otherwise
     */
    protected boolean isValidType(Class<?> type) {
        return type != null && !type.isPrimitive() && type != void.class && !type.isAssignableFrom(Iterable.class);
    }

    /**
     * Get the classes from the visitor context.
     *
     * @param visitorContext The visitor context
     * @return The classes as a Set
     */
    @SuppressWarnings("unchecked")
    Set<String> getClassesFromContext(VisitorContext visitorContext) {
        return visitorContext
                .get(CLASSES_CONTEXT_KEY, ConcurrentSkipListSet.class)
                .orElse(new ConcurrentSkipListSet<String>());
    }

    /**
     * Store the classes in the visitor context.
     *
     * @param visitorContext The visitor context
     * @param classes The classes to store
     */
    void putClassesToContext(VisitorContext visitorContext, Set<String> classes) {
        visitorContext.put(CLASSES_CONTEXT_KEY, classes);
    }

    /**
     * Used for tests to store the json to be generated as a List<Map>.
     *
     * @param type The class
     * @return The internal List for the json to be generated
     */
    @Internal
    static List<Map> getOutput(Class<?> type) {
        return testReferences.get(type);
    }
}
