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

/**
 * The entity identity with declared parent relationship.
 *
 * @param <P>       the type of the parent entity
 * @param <ID_TYPE> the type of the ID of the child entity
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface ChildEntityOf<P extends EntityIdentity, ID_TYPE>
        extends EntityIdentity<ID_TYPE>
{
    /**
     * Returns the parent instance of this child entity.
     *
     * @return the parent entity
     */
    P getParent();

    /**
     * Sets the parent instance for this child entity.
     *
     * @param parent the parent entity instance to be set
     */
    void setParent( P parent );
}
