package io.micronaut.security.utils.reactiveandsecurity

import io.micronaut.context.annotation.Requires
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Header
import io.micronaut.http.annotation.Produces
import io.micronaut.http.client.annotation.Client
import io.reactivex.Single

@Requires(property = 'spec.name', value = 'reactiveandsecurity')
@Client("mockhttpserver")
interface MockHttpClient {

    @Produces(MediaType.TEXT_PLAIN)
    @Get("/user")
    Single<String> userName()

    @Produces(MediaType.TEXT_PLAIN)
    @Get("/user")
    Single<String> userNameWithAuthorization(@Header("Authorization") String authorization)
}
