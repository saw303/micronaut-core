package io.micronaut.security.utils.reactiveandsecurity;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Requires(property = "spec.name", value = "reactiveandsecurity")
@Controller("/")
class GatewayController {

    MockHttpClient mockHttpClient;

    GatewayController(MockHttpClient mockHttpClient) {
        this.mockHttpClient = mockHttpClient;
    }

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get
    public Single<Map<String, String>> index(Principal principal) {
        return Single.just("foo")
                .subscribeOn(Schedulers.io())
                .flatMap(s -> mockHttpClient.userName().map(remoteUser -> {
            Map<String, String> result = new HashMap<>();
            result.put("local", principal.getName());
            result.put("remote", remoteUser);
            return result;
        }));
    }

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Get("/passingheader")
    public Single<Map<String, String>> index(Principal principal, @Header("Authorization") String authorization) {
        return mockHttpClient.userNameWithAuthorization(authorization).map(remoteUser -> {
            Map<String, String> result = new HashMap<>();
            result.put("local", principal.getName());
            result.put("remote", remoteUser);
            return result;
        });
    }

}
