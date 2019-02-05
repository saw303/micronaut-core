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

import io.micronaut.core.annotation.Experimental;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.HttpMethodMapping;
import io.micronaut.inject.ast.MethodElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;

import java.util.Set;

/**
 * A {@link TypeElementVisitor} that visits all the methods in {@link Controller}.
 *
 * @author Iván López
 * @since 1.1.0
 */
@Experimental
public class GraalControllerTypeVisitor extends AbstractGraalTypeVisitor implements TypeElementVisitor<Controller, HttpMethodMapping> {

    @Override
    public void visitMethod(MethodElement element, VisitorContext context) {
        Set<String> classes = getClassesFromContext(context);

        if (element.getReturnType() != null && isValidType(element.getReturnType().getClass())) {
            classes.add(element.getReturnType().getName());
        }

        putClassesToContext(context, classes);
    }
}
