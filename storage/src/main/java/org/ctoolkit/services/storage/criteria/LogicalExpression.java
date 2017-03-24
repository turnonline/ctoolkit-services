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
 * <p>Implementation of {@link Expression} which represent logical restriction -
 * <code>or</code>, <code>and</code>.</p>
 * <br>
 * <p>Result query will look as follows: <code>(e.name:p_0 or e.name=:p_1)</code>, <code>(e.name:p_0 and e.name=:p_1)</code></p>
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class LogicalExpression
        implements Expression
{

    public static final String AND = "and";

    public static final String OR = "or";

    private Expression[] expressions;

    private String operation;

    public LogicalExpression( String operation, Expression... expressions )
    {
        this.expressions = expressions;
        this.operation = operation;
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

    public Expression[] getExpressions()
    {
        return expressions;
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

