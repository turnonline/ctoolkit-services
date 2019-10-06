/*
 * Copyright (c) 2018 Comvai, s.r.o. All Rights Reserved.
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

import com.google.api.server.spi.auth.common.User;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The Firebase verified user created by {@link FirebaseJwtAuthenticator}
 * once authentication has been successfully passed.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see <a href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id_token">ID Token</a>
 */
public class VerifiedUser
        extends User
{
    private static final long serialVersionUID = -5338777667946885418L;

    private String token;

    private String audience;

    VerifiedUser( Builder builder )
    {
        super( builder.userId, checkNotNull( builder.email, "Email is mandatory." ) );
        this.token = builder.token;
        this.audience = builder.audience;
    }

    /**
     * The current and verified JWT token.
     *
     * @return the JWT token.
     */
    public String getToken()
    {
        return token;
    }

    /**
     * The audience that this ID Token is intended for.
     *
     * @return the audience
     */
    public String getAudience()
    {
        return audience;
    }

    @Override
    public String toString()
    {
        return "{" +
                "userId='" + this.getId() + '\'' +
                ", email='" + this.getEmail() + '\'' +
                ", audience='" + audience + '\'' +
                '}';
    }

    static class Builder
    {
        private String userId;

        private String email;

        private String token;

        private String audience;

        Builder userId( String userId )
        {
            this.userId = userId;
            return this;
        }

        Builder email( String email )
        {
            this.email = email;
            return this;
        }

        Builder token( String token )
        {
            this.token = token;
            return this;
        }

        Builder audience( String audience )
        {
            this.audience = audience;
            return this;
        }
    }
}
