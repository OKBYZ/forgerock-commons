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
 * information: "Portions Copyright [year] [name of copyright owner]".
 *
 * Copyright 2014-2015 ForgeRock AS.
 */

package org.forgerock.http.protocol;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Locale;

import org.forgerock.util.i18n.PreferredLocales;
import org.testng.annotations.Test;

@SuppressWarnings("javadoc")
public class RequestTest {

    @Test
    public void testMethodChaining() {
        Request request = new Request().setVersion("123").setMethod("GET");
        assertThat(request.getVersion()).isEqualTo("123");
        assertThat(request.getMethod()).isEqualTo("GET");
    }

    @Test
    public void testAcceptLanguageHeader() {
        // Given
        Request request = new Request().setPreferredLocales(new PreferredLocales(asList(Locale.forLanguageTag("en"))));

        // When
        String languageHeader = request.getHeaders().getFirst("Accept-Language");

        // Then
        assertThat(languageHeader).isEqualTo("en;q=1");
    }

}
