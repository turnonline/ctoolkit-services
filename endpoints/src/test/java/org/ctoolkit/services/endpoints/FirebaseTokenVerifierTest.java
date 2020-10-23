/*
 * Copyright (c) 2020 Comvai, s.r.o. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.ctoolkit.services.endpoints;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.common.base.Joiner;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

/**
 * {@link FirebaseTokenVerifier} unit testing.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@SuppressWarnings( "ResultOfMethodCallIgnored" )
public class FirebaseTokenVerifierTest
{
    private static final String FAKE_TOKEN = "token-ycv234";

    @Tested
    private FirebaseTokenVerifier tested;

    @Mocked
    private GoogleIdTokenVerifier verifier;

    @Mocked
    private GoogleIdToken idToken;

    @Test
    public void verify_Valid() throws GeneralSecurityException, IOException
    {
        new Expectations( tested )
        {
            {
                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = idToken;
            }
        };

        assertThat( tested.verify( FAKE_TOKEN ) ).isNotNull();
    }

    @Test
    public void verify_TokenVerificationNull() throws GeneralSecurityException, IOException
    {
        new Expectations( tested )
        {
            {
                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = null;
            }
        };

        assertThat( tested.verify( FAKE_TOKEN ) ).isNull();
    }

    @Test
    public void verify_TokenVerificationFailure() throws GeneralSecurityException, IOException
    {
        new Expectations( tested )
        {
            {
                tested.getVerifier();
                result = verifier;

                verifier.verify( FAKE_TOKEN );
                result = new RuntimeException();
            }
        };

        assertThat( tested.verify( FAKE_TOKEN ) ).isNull();
    }

    @Test
    public void targetAudience_NotConfigured()
    {
        FirebaseTokenVerifier verifier = new FirebaseTokenVerifier();
        String audience = "my-audience";

        assertWithMessage( "Target audience (same as input)" )
                .that( verifier.targetAudience( audience ) )
                .isEqualTo( audience );
    }

    @Test
    public void targetAudience_WithAliases()
    {
        String target = "target-audience";
        String alias1 = "alias-b9";
        String alias2 = "alias-c12";

        FirebaseTokenVerifier verifier;
        verifier = new FirebaseTokenVerifier( target, Joiner.on( "," ).join( alias1, alias2 ) );

        assertWithMessage( "Target audience for alias 1" )
                .that( verifier.targetAudience( alias1 ) )
                .isEqualTo( target );

        assertWithMessage( "Target audience for alias 2" )
                .that( verifier.targetAudience( alias2 ) )
                .isEqualTo( target );

        String thirdPartyAudience = "some-business";
        assertWithMessage( "Third-party audience" )
                .that( verifier.targetAudience( thirdPartyAudience ) )
                .isNotEqualTo( target );
    }

    @Test
    public void targetAudience_WithSingleAlias()
    {
        String target = "target-audience";
        String alias = "alias-a1";

        FirebaseTokenVerifier verifier;
        verifier = new FirebaseTokenVerifier( target, alias );

        assertWithMessage( "Target audience for alias" )
                .that( verifier.targetAudience( alias ) )
                .isEqualTo( target );

        String thirdPartyAudience = "some-business";
        assertWithMessage( "Third-party audience" )
                .that( verifier.targetAudience( thirdPartyAudience ) )
                .isNotEqualTo( target );
    }

    @Test
    public void targetAudience_WithEmptyAlias()
    {
        FirebaseTokenVerifier verifier = new FirebaseTokenVerifier( "target-audience", "" );
        String audience = "customer-audience";

        assertWithMessage( "Target audience (same as input)" )
                .that( verifier.targetAudience( audience ) )
                .isEqualTo( audience );
    }
}