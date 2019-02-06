package io.micronaut.testutils

import io.micronaut.context.BeanContext
import io.micronaut.context.condition.Condition
import io.micronaut.context.condition.ConditionContext
import io.micronaut.core.value.PropertyResolver

abstract class PropertyEndsWithCondition implements Condition {

    static final String SPEC_NAME_PROPERTY = 'spec.name'

    protected String getPropertyName() {
        SPEC_NAME_PROPERTY
    }

    @Override
    boolean matches(ConditionContext context) {
        BeanContext beanContext = context.getBeanContext()
        if (beanContext instanceof PropertyResolver) {
            PropertyResolver propertyResolver = (PropertyResolver) beanContext
            Optional<String> specName = propertyResolver.get(getPropertyName(), String.class)
            if (specName.isPresent()) {
                return specName.get().endsWith(getSearchSequence())
            }
        }
        return false
    }

    protected abstract String getSearchSequence();

}
