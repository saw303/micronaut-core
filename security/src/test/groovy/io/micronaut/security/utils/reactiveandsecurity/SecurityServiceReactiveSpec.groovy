package io.micronaut.security.utils.reactiveandsecurity

import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.socket.SocketUtils
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.token.propagation.TokenPropagationHttpClientFilter
import spock.lang.Specification

class SecurityServiceReactiveSpec extends Specification {

    def "test authentication is resolved even with a reactive flow"() {
        setup: 'create a mock http server'
        int mockHttpServerPort = SocketUtils.findAvailableTcpPort()
        String mockHttpServerUrl = "http://localhost:${mockHttpServerPort}"
        EmbeddedServer mockHtptServer = ApplicationContext.run(EmbeddedServer, [
                'micronaut.security.enabled': true,
                'spec.name': 'mockhttpserver-reactiveandsecurity',
                'micronaut.server.port': mockHttpServerPort
        ])

        expect: 'verify the expected bean are in the mock HttpServer context'
        mockHtptServer.applicationContext.containsBean(EchoUserNameController)
        !mockHtptServer.applicationContext.containsBean(GatewayController)
        !mockHtptServer.applicationContext.containsBean(MockHttpClient)
        mockHtptServer.applicationContext.containsBean(ApiKeyTokenReader)
        mockHtptServer.applicationContext.containsBean(ApiKeyTokenValidator)
        HttpClient mockHttpClient = HttpClient.create(mockHtptServer.URL)

        when: 'if we do a request to the mock http server endpoint with an Authorization header'
        HttpResponse<String> rsp = mockHttpClient.toBlocking().exchange(HttpRequest.GET("/user").header(HttpHeaders.AUTHORIZATION, 'XXXX'), String)

        then: 'user is authenticated in the mock http server and we get a username back'
        rsp.status == HttpStatus.OK
        rsp.body() == 'john'

        when: 'create a gateway server'
        EmbeddedServer server = ApplicationContext.run(EmbeddedServer, [
                'micronaut.security.enabled': true,
                'spec.name': 'reactiveandsecurity',
                'micronaut.http.services.mockhttpserver.url': mockHttpServerUrl,
                'micronaut.security.token.writer.header.enabled': true,
                'micronaut.security.token.writer.header.headerName': 'Authorization',
                'micronaut.security.token.writer.header.prefix': '',
                'micronaut.security.token.propagation.enabled': true,
                'micronaut.security.token.propagation.service-id-regex': "mockhttpserver",
        ])

        then: 'verify the beans expected are registered in the application context'
        server.applicationContext.containsBean(GatewayController)
        server.applicationContext.containsBean(MockHttpClient)
        !server.applicationContext.containsBean(EchoUserNameController)
        server.applicationContext.containsBean(ApiKeyTokenReader)
        server.applicationContext.containsBean(ApiKeyTokenValidator)
        server.applicationContext.containsBean(TokenPropagationHttpClientFilter)

        when: 'if we do a request to the gateway server to an endpoint which passes manually the authorization header to the mock server'
        HttpClient httpClient = HttpClient.create(server.URL)
        HttpRequest request = HttpRequest.GET("/passingheader").header(HttpHeaders.AUTHORIZATION, 'XXXX')
        HttpResponse<Map> response = httpClient.toBlocking().exchange(request, Map)

        then: 'user is authenticated in both the gateway and mock httpserver'
        response.status == HttpStatus.OK
        response.body() == [local: 'john', remote: 'john']

        when: 'if we do a request to the gateway server without authenticating'
        request = HttpRequest.GET("/anonymous")
        response = httpClient.toBlocking().exchange(request, Map)

        then: 'user is not authenticated in the gateway or in the mock httpserver'
        response.status == HttpStatus.OK
        response.body() == [local: 'Anonymous', remote: 'Anonymous']

        when: 'if we do a request to the gateway server and rely on propagation filter'
        request = HttpRequest.GET("/").header(HttpHeaders.AUTHORIZATION, 'XXXX')
        response = httpClient.toBlocking().exchange(request, Map)

        then: 'user is authenticated in both the gateway and mock httpserver'
        response.status == HttpStatus.OK
        response.body() == [local: 'john', remote: 'john']

        cleanup: 'close clients and servers'
        mockHtptServer.close()

        and:
        mockHttpClient.close()

        and:
        httpClient.close()

        and:
        server.close()
    }
}
