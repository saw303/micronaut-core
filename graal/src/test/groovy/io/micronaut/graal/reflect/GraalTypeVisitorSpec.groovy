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

class GraalTypeVisitorSpec extends AbstractTypeElementSpec {

    void 'foo'() {
        given:
        buildBeanDefinition('test.MyBean', '''
package test;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.reactivex.Single;

import java.util.Arrays;
import java.util.List;

@Controller("/")
class HelloController {

    @Get("/hello/{name}")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHi(String name) {
        return "Hello " + name;
    }

    @Get("/hello/msg")
    public Message msg() {
        return new Message("Hello world!");
    }

    @Get("/hello/list")
    public List<String> list() {
        return Arrays.asList("Hello", "World");
    }

    @Get("/hello/list-pojo")
    public List<Message> listPojo() {
        return Arrays.asList(
                new Message("Hello"),
                new Message("World")
        );
    }

    @Get("/hello/msg-single")
    public Single<Message> msgSingle() {
        return Single.just(new Message("Hello world!"));
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
        List<Map> reflectJson = GraalTypeVisitor.json

        then:
        reflectJson != null
        // Fails here because "visitMethod" is not called
        reflectJson.name.any { it == 'test.Message'}
        reflectJson.name.any { it == 'java.util.List'}
        reflectJson.find { it.name == 'test.Message'}.allDeclaredConstructors == true
        reflectJson.find { it.name == 'test.Message'}.allPublicMethods == true
    }
}
