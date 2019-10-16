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
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A verified user will be instantiated once authentication successfully has passed.
 * <p>
 * It represents an user that belongs solely to one audience (tenant). User with the same email address
 * might be part of two or more audiences and those users are operated within different Google Cloud Project IDs
 * that might belong to different Organization.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 * @see <a href="http://openid.net/specs/openid-connect-basic-1_0-27.html#id_token">ID Token</a>
 */
public class AudienceUser
        extends User
{
    private static final long serialVersionUID = 5538973246473903961L;

    private String token;

    private String audience;

    private String serviceAccount;

    /**
     * These values are mandatory:
     * <ul>
     *     <li>{@link Builder#userId(String)}</li>
     *     <li>{@link Builder#email(String)}</li>
     *     <li>{@link Builder#audience(String)}</li>
     * </ul>
     */
    private AudienceUser( Builder builder )
    {
        super( checkNotNull( builder.userId, "User ID is mandatory" ),
                checkNotNull( builder.email, "Email is mandatory" ) );
        this.audience = checkNotNull( builder.audience, "Audience is mandatory" );
        this.token = builder.token;
        this.serviceAccount = builder.serviceAccount;
    }

    /**
     * The current and verified JWT token.
     *
     * @return the JWT token.
     */
    public final String getToken()
    {
        return token;
    }

    /**
     * The audience that this ID Token is intended for.
     *
     * @return the audience
     */
    public final String getAudience()
    {
        return audience;
    }

    /**
     * Returns the origin service account email.
     *
     * @return the service account email
     */
    public String getServiceAccount()
    {
        return serviceAccount;
    }

    @Override
    public final boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( !( o instanceof AudienceUser ) ) return false;
        if ( !super.equals( o ) ) return false;

        AudienceUser that = ( AudienceUser ) o;
        return Objects.equals( audience, that.audience );
    }

    @Override
    public final int hashCode()
    {
        return Objects.hash( super.hashCode(), audience );
    }

    @Override
    public String toString()
    {
        MoreObjects.ToStringHelper string = MoreObjects.toStringHelper( "User" );
        string.add( "email", this.getEmail() )
                .add( "userId", this.getId() )
                .add( "audience", audience );

        if ( !Strings.isNullOrEmpty( serviceAccount ) )
        {
            string.add( "serviceAccount", serviceAccount );
        }

        return string.toString();
    }

    public static class Builder
    {
        private String userId;

        private String email;

        private String token;

        private String audience;

        private String serviceAccount;

        public Builder userId( @Nonnull String userId )
        {
            this.userId = userId;
            return this;
        }

        public Builder email( @Nonnull String email )
        {
            this.email = email;
            return this;
        }

        public Builder token( @Nullable String token )
        {
            this.token = token;
            return this;
        }

        public Builder audience( @Nonnull String audience )
        {
            this.audience = audience;
            return this;
        }

        public Builder audiences( @Nullable Set<String> audiences )
        {
            if ( audiences != null && !audiences.isEmpty() )
            {
                this.audience = audiences.iterator().next();
            }
            return this;
        }

        public Builder serviceAccount( @Nullable String serviceAccount )
        {
            this.serviceAccount = serviceAccount;
            return this;
        }

        public AudienceUser build()
        {
            return new AudienceUser( this );
        }
    }
}
