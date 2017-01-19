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
 * <p>Implementation of {@link Expression} which represent restrictions of value with low and high bound.
 * It can by used to determine if for example date is in specified date interval.</p>
 * <br>
 * <p>Low and high bound of expression can be set as follows:</p>
 * <table border="1">
 * <tr><th>Low bound</th><th>High bound</th><th>Result</th></tr>
 * <tr><td>{@link Bound#SOFT}</td><td>{@link Bound#SOFT}</td><td>e.date&gt;=:p_0 and e.date&lt;=:p_0</td></tr>
 * <tr><td>{@link Bound#HARD}</td><td>{@link Bound#SOFT}</td><td>e.date&gt;:p_0 and e.date&lt;=:p_0</td></tr>
 * <tr><td>{@link Bound#SOFT}</td><td>{@link Bound#HARD}</td><td>e.date&gt;=:p_0 and e.date&lt;:p_0</td></tr>
 * <tr><td>{@link Bound#HARD}</td><td>{@link Bound#HARD}</td><td>e.date&gt;:p_0 and e.date&lt;:p_0</td></tr>
 * </table>
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class BetweenExpression
        implements Expression
{

    private String propertyName;

    private Object lowPropertyValue;

    private Object highPropertyValue;

    private Bound lowBound;

    private Bound highBound;

    private String parameterName;

    private String highParameterName;

    public BetweenExpression( String propertyName, Object propertyValue, Object highPropertyValue, Bound lowBound, Bound highBound )
    {
        this.propertyName = propertyName;
        this.lowPropertyValue = propertyValue;
        this.highPropertyValue = highPropertyValue;
        this.lowBound = lowBound;
        this.highBound = highBound;
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

    public String getHighParameterName()
    {
        return highParameterName;
    }

    public void setHighParameterName( String highParameterName )
    {
        this.highParameterName = highParameterName;
    }

    @Override
    public Object getPropertyValue()
    {
        return lowPropertyValue;
    }

    public Object getHighPropertyValue()
    {
        return highPropertyValue;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public Bound getLowBound()
    {
        return lowBound;
    }

    public Bound getHighBound()
    {
        return highBound;
    }

    @Override
    public <Q, E> Q build( CriteriaBuilder<Q, E> builder )
    {
        return builder.build( this );
    }
}
