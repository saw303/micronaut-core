package io.micronaut.http.server.netty.util;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.MutableHttpHeaders;

import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class MockHttpHeaders implements MutableHttpHeaders {

    private final Map<CharSequence, List<String>> headers;

    public MockHttpHeaders(Map<CharSequence, List<String>> headers) {
        this.headers = headers;
    }

    @Override
    public MutableHttpHeaders add(CharSequence header, CharSequence value) {
        headers.compute(header, (key, val) -> {
            if (val == null) {
                val = new ArrayList<>();
            }
            val.add(value.toString());
            return val;
        });
        return this;
    }

    @Override
    public MutableHttpHeaders remove(CharSequence header) {
        headers.remove(header);
        return this;
    }

    @Override
    public List<String> getAll(CharSequence name) {
        List<String> values = headers.get(name);
        if (values == null) {
            return Collections.emptyList();
        } else {
            return values;
        }
    }

    @Nullable
    @Override
    public String get(CharSequence name) {
        List<String> values = headers.get(name);
        if (values == null || values.isEmpty()) {
            return null;
        } else {
            return values.get(0);
        }
    }

    @Override
    public Set<String> names() {
        return headers.keySet().stream().map(CharSequence::toString).collect(Collectors.toSet());
    }

    @Override
    public Collection<List<String>> values() {
        return headers.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        return ConversionService.SHARED.convert(get(name), conversionContext);
    }
}
