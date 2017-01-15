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
 * <p>Implementation of {@link Expression} which represent <code>in</code>
 * restriction.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class NameInExpression
        implements Expression
{

    private String propertyName;

    private String[] propertyValues;

    public NameInExpression( String propertyName, String[] propertyValues )
    {
        this.propertyName = propertyName;
        this.propertyValues = propertyValues;
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
    public Object getPropertyValue()
    {
        return null;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public String[] getPropertyValues()
    {
        return propertyValues;
    }

    @Override
    public <Q, E> Q build( CriteriaBuilder<Q, E> builder )
    {
        return builder.build( this );
    }
}


