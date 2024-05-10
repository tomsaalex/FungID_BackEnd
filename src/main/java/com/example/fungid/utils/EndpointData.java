package com.example.fungid.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;

import java.util.regex.Pattern;

public class EndpointData {
    private final Pattern url;
    private final HttpMethod method;

    public EndpointData(String endpoint) {
        this.url = Pattern.compile(endpoint);
        this.method = null;
    }

    public EndpointData(String endpoint, HttpMethod method) {
        this.url = Pattern.compile(endpoint);
        this.method = method;
    }

    public EndpointData(Pattern url, HttpMethod method) {
        this.url = url;
        this.method = method;
    }

    public boolean matches(HttpServletRequest request) {
        // verify that the request matches the method
        if (method != null && !method.equals(HttpMethod.valueOf(request.getMethod()))) {
            return false;
        }

        return url.matcher(request.getRequestURI()).find();
    }

}
