package io.micronaut.security

import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class EnableSecuritySpec extends Specification {

    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run(['micronaut.security.enabled': true])

    def "read a boolean property"() {
        expect:
        applicationContext.getProperty('micronaut.security.enabled', Boolean).get() == true
    }
}
