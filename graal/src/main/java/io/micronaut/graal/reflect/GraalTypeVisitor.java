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
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.HttpMethodMapping;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * A {@link TypeElementVisitor} that builds the GraalVM reflect.json file from Micronaut controllers at compile time.
 *
 * @author Iván López
 * @since 1.1.0
 */
@Experimental
public class GraalTypeVisitor implements TypeElementVisitor<Controller, HttpMethodMapping> {

    /**
     * System property that indicates the location of the generated reflection JSON file.
     */
    public static final String REFLECTION_JSON_FILE = "graalvm.reflection.json";

    private static final String BASE_REFLECT_FILE_PATH = "src/main/graal/reflect.json";
    private final Set<String> classes = new ConcurrentSkipListSet<>();
    static List<Map> json;

    @Override
    public void visitMethod(MethodElement element, VisitorContext context) {
        if (element.getReturnType() != null && isValidType(element.getReturnType().getClass())) {
            classes.add(element.getReturnType().getName());
        }
    }

    @Override
    public void start(VisitorContext visitorContext) {
        File baseReflect = new File(BASE_REFLECT_FILE_PATH);
        if (!baseReflect.exists()) {
            // TODO: Compilation error?
            throw new RuntimeException("File " + BASE_REFLECT_FILE_PATH + " doesn't exist");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            CollectionType collectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);
            json = mapper.readValue(baseReflect, collectionType);
        } catch (IOException e) {
            // TODO: Compilation error??
            throw new RuntimeException("File " + BASE_REFLECT_FILE_PATH + " is not a valid json file");
        }
    }

    @Override
    public void finish(VisitorContext visitorContext) {
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
                // TODO: Compilation error?
                throw new RuntimeException("Neither 'build' nor 'target' directories exist. Graal reflection file can't be generated");
            } else {
                file = new File(parent, "reflect.json");
            }
        }

        if (!file.exists()) {
            for (String className : classes) {
                json.add(CollectionUtils
                        .mapOf("name", className, "allPublicMethods", true, "allDeclaredConstructors", true));
            }

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

    private boolean isValidType(Class<?> type) {
        return type != null && !type.isPrimitive() && type != void.class;
    }
}
