/*
 * Copyright (c) 2017 Comvai, s.r.o. All Rights Reserved.
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

package org.ctoolkit.services.identity;

import com.google.firebase.auth.FirebaseToken;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The listener to handle notification once an identity toolkit user has logged in.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface IdentityLoginListener
{
    /**
     * Gets notification about right logged in authenticated user.
     * The implementation is responsible to put target authenticated user in to session at given session attribute.
     *
     * @param request   the current HTTP request
     * @param response  the current HTTP response
     * @param identity  the Firebase token of the logged in user
     * @param attribute the session attribute where parent filter will check authenticated user presence
     * @throws IOException
     */
    void processIdentity( @Nonnull HttpServletRequest request,
                          @Nonnull HttpServletResponse response,
                          @Nonnull FirebaseToken identity,
                          @Nonnull String attribute ) throws IOException;
}
