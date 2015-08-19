/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2012-2015 ForgeRock AS.
 */

package org.forgerock.json.resource.http;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.forgerock.http.context.AbstractContext;
import org.forgerock.http.context.ClientInfoContext;
import org.forgerock.http.Context;
import org.forgerock.json.JsonValue;
import org.forgerock.json.resource.ClientContext;
import org.forgerock.util.Factory;
import org.forgerock.util.LazyMap;

/**
 * A {@link Context} containing information relating to the originating HTTP
 * Servlet request.
 */
public final class HttpContext extends AbstractContext implements ClientContext {

    // TODO: security parameters such as user name, etc?
    /**
     * Attribute in the serialized JSON form that holds the request headers.
     * @see #HttpContext(JsonValue, ClassLoader)
     */
    public static final String ATTR_HEADERS = "headers";

    /**
     * Attribute in the serialized JSON form that holds the query and/or form parameters.
     * @see #HttpContext(JsonValue, ClassLoader)
     */
    public static final String ATTR_PARAMETERS = "parameters";

    /**
     * Attribute in the serialised JSON form that holds the HTTP method of the request.
     * @see #HttpContext(JsonValue, ClassLoader)
     */
    public static final String ATTR_METHOD = "method";

    /**
     * Attribute in the serialised JSON form that holds the full URI of the request, excluding anything beyond the
     * path component (i.e., no query parameters).
     * @see #HttpContext(JsonValue, ClassLoader)
     */
    public static final String ATTR_PATH = "path";

    /**
     * Attribute in the serialised JSON form that holds the remote client IP address.
     * @see #HttpContext(JsonValue, ClassLoader)
     */
    public static final String ATTR_REMOTE_ADDRESS = "remoteAddress";

    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> parameters;

    /**
     * {@inheritDoc}
     */
    public boolean isExternal() {
        return true;
    }

    HttpContext(Context parent, final org.forgerock.http.protocol.Request req) {
        super(parent, "http");
        data.put(ATTR_METHOD, HttpUtils.getMethod(req));
        data.put(ATTR_PATH, getRequestPath(req));
        data.put(ATTR_REMOTE_ADDRESS, getRemoteAddress(parent));
        this.headers = Collections.unmodifiableMap(new LazyMap<>(
                new Factory<Map<String, List<String>>>() {
                    @Override
                    public Map<String, List<String>> newInstance() {
                        Map<String, List<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                        Set<Map.Entry<String, List<String>>> headers = req.getHeaders().entrySet();
                        for (Map.Entry<String, List<String>> header : headers) {
                            String name = header.getKey();
                            List<String> values = header.getValue();
                            result.put(name, values);
                        }
                        return result;
                    }
                }));
        data.put(ATTR_HEADERS, headers);
        this.parameters = Collections.unmodifiableMap(new LazyMap<>(
                new Factory<Map<String, List<String>>>() {
                    @Override
                    public Map<String, List<String>> newInstance() {
                        Map<String, List<String>> result = new LinkedHashMap<>();
                        Set<Map.Entry<String, List<String>>> parameters = req.getForm().entrySet();
                        for (Map.Entry<String, List<String>> parameter : parameters) {
                            String name = parameter.getKey();
                            List<String> values = parameter.getValue();
                            result.put(name, values);
                        }
                        return result;
                    }
                }));
        data.put(ATTR_PARAMETERS, parameters);
    }

    /**
     * Restore from JSON representation.
     *
     * @param savedContext
     *            The JSON representation from which this context's attributes
     *            should be parsed. Must be a JSON Object that contains {@link #ATTR_HEADERS} and
     *            {@link #ATTR_PARAMETERS} attributes.
     * @param classLoader
     *            The ClassLoader which can properly resolve the persisted class-name.
     */
    public HttpContext(final JsonValue savedContext, final ClassLoader classLoader) {
        super(savedContext, classLoader);
        this.headers = data.get(ATTR_HEADERS).required().asMapOfList(String.class);
        this.parameters = data.get(ATTR_PARAMETERS).required().asMapOfList(String.class);
    }

    private String getRequestPath(org.forgerock.http.protocol.Request req) {
        return new StringBuilder()
                .append(req.getUri().getScheme())
                .append("://")
                .append(req.getUri().getRawAuthority())
                .append(req.getUri().getRawPath()).toString();
    }

    private String getRemoteAddress(Context context) {
        if (context.containsContext(ClientInfoContext.class)) {
            return context.asContext(ClientInfoContext.class).getRemoteAddress();
        } else {
            return null;
        }
    }

    /**
     * Returns an unmodifiable list containing the values of the named HTTP
     * request header.
     *
     * @param name
     *            The name of the HTTP request header.
     * @return An unmodifiable list containing the values of the named HTTP
     *         request header, which may be empty if the header is not present
     *         in the request.
     */
    public List<String> getHeader(String name) {
        List<String> header = headers.get(name);
        return Collections.unmodifiableList(header != null ? header : Collections.<String> emptyList());
    }

    /**
     * Returns the first value of the named HTTP request header.
     *
     * @param name
     *            The name of the HTTP request header.
     * @return The first value of the named HTTP request header, or {@code null}
     *         if the header is not present in the request.
     */
    public String getHeaderAsString(String name) {
        List<String> header = getHeader(name);
        return header.isEmpty() ? null : header.get(0);
    }

    /**
     * Returns an unmodifiable map of the HTTP request headers.
     *
     * @return An unmodifiable map of the HTTP request headers.
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Returns the effective HTTP method, taking into account presence of the
     * {@code X-HTTP-Method-Override} header.
     *
     * @return The effective HTTP method, taking into account presence of the
     *         {@code X-HTTP-Method-Override} header.
     */
    public String getMethod() {
        return data.get(ATTR_METHOD).asString();
    }

    /**
     * Returns the address of the client making the request. This may be an IPV4 or
     * an IPv6 address depending on server configuration. Note that the address returned
     * may also be the address of a proxy.
     * No guarantees of whether the returned address is reachable are made.
     *
     * @return The address of the client or proxy making the request.
     */
    public String getRemoteAddress() {
        return data.get(ATTR_REMOTE_ADDRESS).asString();
    }

    /**
     * Returns an unmodifiable list containing the values of the named HTTP
     * request parameter.
     *
     * @param name
     *            The name of the HTTP request parameter.
     * @return An unmodifiable list containing the values of the named HTTP
     *         request parameter, which may be empty if the parameter is not
     *         present in the request.
     */
    public List<String> getParameter(String name) {
        final List<String> parameter = parameters.get(name);
        return Collections.unmodifiableList(parameter != null ? parameter : Collections.<String> emptyList());
    }

    /**
     * Returns the first value of the named HTTP request parameter.
     *
     * @param name
     *            The name of the HTTP request parameter.
     * @return The first value of the named HTTP request parameter, or
     *         {@code null} if the parameter is not present in the request.
     */
    public String getParameterAsString(String name) {
        final List<String> parameter = getParameter(name);
        return parameter.isEmpty() ? null : parameter.get(0);
    }

    /**
     * Returns an unmodifiable map of the HTTP request parameters.
     *
     * @return An unmodifiable map of the HTTP request parameters.
     */
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    /**
     * Returns the HTTP request path.
     *
     * @return The HTTP request path.
     */
    public String getPath() {
        return data.get(ATTR_PATH).asString();
    }
}
