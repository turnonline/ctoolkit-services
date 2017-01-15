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
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class ReferenceIdExpression
        implements Expression
{
    private String propertyName;

    private Long id;

    private Class refClass;

    public ReferenceIdExpression( String propertyName, Class refClass, Long id )
    {
        this.propertyName = propertyName;
        this.id = id;
        this.refClass = refClass;
    }

    @Override
    public String getParameterName()
    {
        return null;
    }

    @Override
    public void setParameterName( String parameterName )
    {

    }

    @Override
    public Long getPropertyValue()
    {
        return id;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public Class getRefClass()
    {
        return refClass;
    }

    @Override
    public <Q, E> Q build( CriteriaBuilder<Q, E> builder )
    {
        return builder.build( this );
    }
}
