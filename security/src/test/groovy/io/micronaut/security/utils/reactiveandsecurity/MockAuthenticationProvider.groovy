package io.micronaut.security.utils.reactiveandsecurity

import io.micronaut.context.annotation.Requires
import io.micronaut.security.authentication.AuthenticationProvider
import io.micronaut.security.authentication.AuthenticationRequest
import io.micronaut.security.authentication.AuthenticationResponse
import io.micronaut.security.authentication.UserDetails
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import javax.inject.Singleton

@Requires(condition = ReactiveAndSecurity)
@Requires(property = 'spec.name', value = 'reactiveandsecurity')
@Singleton
class MockAuthenticationProvider implements AuthenticationProvider {

    @Override
    Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
        UserDetails userDetails = new UserDetails(authenticationRequest.identity as String, [])
        Flowable.just(userDetails)
    }
}