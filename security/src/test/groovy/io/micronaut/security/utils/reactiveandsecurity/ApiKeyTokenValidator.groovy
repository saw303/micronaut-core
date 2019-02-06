package io.micronaut.security.utils.reactiveandsecurity

import io.micronaut.context.annotation.Requires
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.authentication.AuthenticationUserDetailsAdapter
import io.micronaut.security.authentication.UserDetails
import io.micronaut.security.token.config.TokenConfiguration
import io.micronaut.security.token.validator.TokenValidator
import io.reactivex.Flowable
import org.reactivestreams.Publisher

import javax.inject.Singleton

@Requires(condition = ReactiveAndSecurity)
@Requires(beans = TokenConfiguration)
@Singleton
class ApiKeyTokenValidator implements TokenValidator {

    private final String rolesKeyName

    ApiKeyTokenValidator(TokenConfiguration tokenConfiguration) {
        this.rolesKeyName = tokenConfiguration.getRolesName()
    }
    @Override
    Publisher<Authentication> validateToken(String token) {
        if (token == null) {
            return Flowable.empty()
        }
        return Flowable.just(new AuthenticationUserDetailsAdapter(new UserDetails("john", []), rolesKeyName))
    }
}
