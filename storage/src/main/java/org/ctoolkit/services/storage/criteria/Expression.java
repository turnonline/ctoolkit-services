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

package org.ctoolkit.services.storage.criteria;

/**
 * Interface for query language expression representation
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public interface Expression
{
    /**
     * Returns parameter name or <code>null</code> if implementation does not require parameters
     *
     * @return the parameter name
     */
    String getParameterName();

    /**
     * Sets parameter name or <code>null</code> if implementation does not require parameters
     *
     * @param parameterName the parameter name to be set
     */
    void setParameterName( String parameterName );

    /**
     * Return property value or <code>null</code> if implementation does not require properties
     *
     * @return the property value
     */
    Object getPropertyValue();

    /**
     * Build implementation specific expression object.
     *
     * @param builder the expression builder
     * @param <Q> the typed Query
     * @param <E> the entity type
     * @return the implementation specific expression object
     */
    <Q, E> Q build( CriteriaBuilder<Q, E> builder );

}
