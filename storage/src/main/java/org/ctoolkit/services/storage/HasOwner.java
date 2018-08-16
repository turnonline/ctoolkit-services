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

package org.ctoolkit.services.storage;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Declaration for an entity that's being owned by another entity.
 * It's NOT a parent/child relationship, but represents an ownership from the business point of view.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface HasOwner<T extends EntityIdentity>
{
    /**
     * Returns the owner of the entity that implements this interface.
     *
     * @return the owner of this entity or {@code null} if not yet set
     */
    T getOwner();

    /**
     * Sets the owner for this entity.
     *
     * @param owner the entity to be set as an owner
     */
    void setOwner( @Nonnull T owner );

    /**
     * Checks whether specified entity is the same entity as declared owner.
     *
     * @param checked the entity to be checked
     * @return {@code true} if specified entity is the same as declared owner, otherwise {@code false}.
     */
    default boolean checkOwner( @Nonnull T checked )
    {
        checkNotNull( checked );
        return getOwner() != null && checked.equals( getOwner() );
    }
}
