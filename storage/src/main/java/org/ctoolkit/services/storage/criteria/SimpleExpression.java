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
 * <p>Implementation of {@link Expression} which represent simple restrictions for entity property.</p>
 * <br>
 * <p>Result query will look as follows: <code>e.name=,&gt;,&gt;=,&lt;&gt;,&lt;,&lt;=:p_0</code></p>
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class SimpleExpression
        implements Expression
{

    private String propertyName;

    private Object propertyValue;

    private String operation;

    private String parameterName;

    public SimpleExpression( String propertyName, Object propertyValue, String operation )
    {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.operation = operation;
    }

    @Override
    public String getParameterName()
    {
        return parameterName;
    }

    @Override
    public void setParameterName( String parameterName )
    {
        this.parameterName = parameterName;
    }

    @Override
    public Object getPropertyValue()
    {
        return propertyValue;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public String getOperation()
    {
        return operation;
    }

    @Override
    public <Q, E> Q build( CriteriaBuilder<Q, E> builder )
    {
        return builder.build( this );
    }
}

