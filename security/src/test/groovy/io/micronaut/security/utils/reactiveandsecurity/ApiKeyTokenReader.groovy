package io.micronaut.security.utils.reactiveandsecurity

import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.security.token.reader.HttpHeaderTokenReader
import javax.inject.Singleton

@Requires(condition = ReactiveAndSecurity)
@Singleton
class ApiKeyTokenReader extends HttpHeaderTokenReader {
    @Override
    protected String getPrefix() {
         null
    }

    @Override
    protected String getHeaderName() {
        HttpHeaders.AUTHORIZATION
    }
}
