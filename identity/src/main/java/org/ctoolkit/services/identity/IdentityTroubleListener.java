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

/**
 * The listener to receive reset password notification or change email notification.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface IdentityTroubleListener
{
    /**
     * Gets notification about request to reset password.
     *
     * @param email     the email of the user wants to reset password
     * @param resetLink the confirmation link to handle reset password
     */
    void resetPassword( String email, String resetLink );

    /**
     * Gets notification about request to change email.
     *
     * @param localId  the localId of the user wants to change password
     * @param email    the old email to be changed
     * @param newEmail the new email
     */
    void changeEmail( String localId, String email, String newEmail );
}
