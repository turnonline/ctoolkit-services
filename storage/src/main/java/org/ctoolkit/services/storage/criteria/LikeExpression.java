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
 * Implementation of {@link Expression} which represent <code>like</code> restriction.
 * If <code>ignoreCase</code> is set to true than <code>ilike</code> will be applied.
 * <p>
 * Match modes can be set as follows:
 * <ul>
 * <li>{@link MatchMode#EXACT} : %propertyValue%</li>
 * <li>{@link MatchMode#START} : %propertyValue%</li>
 * <li>{@link MatchMode#END} : %propertyValue</li>
 * <li>{@link MatchMode#ANYWHERE} : %propertyValue%</li>
 * </ul>
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class LikeExpression
        implements Expression
{

    private String propertyName;

    private String propertyValue;

    private MatchMode matchMode;

    private boolean ignoreCase;

    private String parameterName;

    private String operation;

    public LikeExpression( String propertyName, String propertyValue, MatchMode matchMode, String operation, boolean ignoreCase )
    {
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.matchMode = matchMode;
        this.ignoreCase = ignoreCase;
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
        Object value = matchMode.getPropertyValue( propertyValue );
        if ( ignoreCase )
        {
            value = value.toString().toLowerCase();
        }

        return value;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public MatchMode getMatchMode()
    {
        return matchMode;
    }

    public boolean isIgnoreCase()
    {
        return ignoreCase;
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

