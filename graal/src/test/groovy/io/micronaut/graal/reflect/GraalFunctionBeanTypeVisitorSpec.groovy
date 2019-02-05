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

package io.micronaut.graal.reflect

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec

class GraalFunctionBeanTypeVisitorSpec extends AbstractTypeElementSpec {

    def setup() {
        System.setProperty(AbstractGraalTypeVisitor.ATTR_TEST_MODE, "true")
    }

    def cleanup() {
        System.setProperty(AbstractGraalTypeVisitor.ATTR_TEST_MODE, "")
    }

    void 'test the return types for @FunctionBeans are added to reflect.json'() {
        given:
        buildBeanDefinition('test.MyBean', '''
package test;

import io.micronaut.function.FunctionBean;

import java.util.function.Function;

@FunctionBean("greeting")
class GreetingFunction implements Function<String, Message> {

    @Override
    public Message apply(String name) {
        return new Message("Hello " + name);
    }
}

class Message {

    private String text;

    public Message() {
    }

    public Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

@javax.inject.Singleton
class MyBean {}
''')

        when:
        List<Map> reflectJson = AbstractGraalTypeVisitor.getOutput(GraalControllerTypeVisitor)

        then:
        reflectJson != null
        reflectJson.name.any { it == 'test.Message'}
        reflectJson.find { it.name == 'test.Message'}.allDeclaredConstructors == true
        reflectJson.find { it.name == 'test.Message'}.allPublicMethods == true
    }
}
