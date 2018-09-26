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

import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The identification of the user on behalf of whom the service account has been authenticated.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class OnBehalfOfUser
        extends User
{
    private static final long serialVersionUID = -4300320336073016157L;

    private String serviceAccount;

    private String serviceAccountId;

    OnBehalfOfUser( Builder builder )
    {
        super( checkNotNull( builder.userId, "On behalf of User ID is mandatory." ),
                checkNotNull( builder.email, "On behalf of Email is mandatory." ) );
        this.serviceAccount = checkNotNull( builder.serviceAccount, "Service Account Email is mandatory." );
        this.serviceAccountId = builder.serviceAccountId;
    }

    /**
     * Returns the email as an identification of the user on behalf of whom
     * the service account has been authenticated.
     *
     * @return the email
     */
    @Override
    public String getEmail()
    {
        return super.getEmail();
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

    /**
     * Returns the origin service account ID.
     *
     * @return the service account ID
     */
    public String getServiceAccountId()
    {
        return serviceAccountId;
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o ) return true;
        if ( !( o instanceof OnBehalfOfUser ) ) return false;
        if ( !super.equals( o ) ) return false;

        OnBehalfOfUser that = ( OnBehalfOfUser ) o;
        return Objects.equals( serviceAccountId, that.serviceAccountId );
    }

    @Override
    public int hashCode()
    {
        return Objects.hash( super.hashCode(), serviceAccountId );
    }

    @Override
    public String toString()
    {
        return String.format( "userId:%s, email:%s, serviceAccount:%s, serviceAccountId:%s",
                getId(), getEmail(), this.serviceAccount, this.serviceAccountId );
    }

    static class Builder
    {
        private String userId;

        private String email;

        private String serviceAccount;

        private String serviceAccountId;

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

        Builder serviceAccount( String serviceAccount )
        {
            this.serviceAccount = serviceAccount;
            return this;
        }

        Builder serviceAccountId( String serviceAccountId )
        {
            this.serviceAccountId = serviceAccountId;
            return this;
        }
    }
}
