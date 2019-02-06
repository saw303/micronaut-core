package io.micronaut.security.utils.reactiveandsecurity

import io.micronaut.testutils.PropertyEndsWithCondition

class ReactiveAndSecurity extends PropertyEndsWithCondition  {

    @Override
    protected String getSearchSequence() {
        'reactiveandsecurity'
    }
}
