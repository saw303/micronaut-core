package io.micronaut.security.utils.reactiveandsecurity

import io.micronaut.context.annotation.Requires
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule

import javax.annotation.Nullable
import java.security.Principal

@Requires(condition = ReactiveAndSecurity)
@Requires(property = 'spec.name', value = 'mockhttpserver-reactiveandsecurity')
@Controller("/user")
class EchoUserNameController {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Produces(MediaType.TEXT_PLAIN)
    @Get
    String index(@Nullable Principal principal) {
        principal != null ? principal.name : 'Anonymous'
    }
}
