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
 * Copyright 2015 ForgeRock AS.
 */

package org.forgerock.json.resource;

import org.forgerock.http.Context;
import org.forgerock.util.promise.Promise;

/**
 * Assorted utility methods useful to request handlers.
 */
class RequestHandlerUtils {

    static <T> Promise<T, ResourceException> handle(AnnotatedMethod method, Context context, Request request) {
        return method.invoke(context, request, null);
    }

    static <T> Promise<T, ResourceException> handle(AnnotatedMethod method, Context context, Request request,
            QueryResourceHandler queryResourceHandler) {
        return method.invoke(context, request, queryResourceHandler, null);
    }

    static <T> Promise<T, ResourceException> handle(AnnotatedMethod method, Context context, Request request,
            String id) {
        return method.invoke(context, request, id);
    }

    private RequestHandlerUtils() {}
}
