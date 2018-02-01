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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Criteria class is helper class for creating criteria.</p>
 * <p><b>Usage:</b></p>
 * <pre>
 * Criteria&#60;Entity&#62; crit = Criteria.create(Entity.class);
 * crit.add(Restrictions.eq("id", 1);
 * </pre>
 *
 * @param <T> the concrete type of the entity
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class Criteria<T>
{
    private int maxResults = -1;

    private int firstResult = -1;

    private Class<T> entity;

    private List<Expression> expressionList = new ArrayList<>();

    private List<OrderRule> orderRules = new ArrayList<>();

    private Criteria( Class<T> entity )
    {
        this.entity = entity;
    }

    /**
     * Construct criteria object.
     *
     * @param entity entity on which criteria will be applied
     * @param <T>    the concrete type of the entity
     * @return new instance of {@link Criteria}
     */
    public static <T> Criteria<T> create( Class<T> entity )
    {
        return new Criteria<>( entity );
    }

    public Class<T> getEntityClass()
    {
        return entity;
    }

    /**
     * Add criteria expression to criteria
     *
     * @param expression implementation of {@link Expression}
     * @return this
     */
    public Criteria addCriteria( Expression expression )
    {
        expressionList.add( expression );
        return this;
    }

    /**
     * Returns the list of <code>Expression</code>.
     *
     * @return the list of <code>Expression</code>.
     */
    public List<Expression> getExpressionList()
    {
        return expressionList;
    }

    /**
     * Returns the maximum number of results to retrieve.
     *
     * @return the maximum number of results to retrieve.
     */
    public int getMaxResults()
    {
        return maxResults;
    }

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param maxResults max results of query
     */
    public void setMaxResults( int maxResults )
    {
        this.maxResults = maxResults;
    }

    /**
     * Returns the position of the first result to retrieve.
     *
     * @return the position of the first result to retrieve.
     */
    public int getFirstResult()
    {
        return firstResult;
    }

    /**
     * Sets the position of the first result to retrieve.
     *
     * @param firstResult first result of query
     */
    public void setFirstResult( int firstResult )
    {
        this.firstResult = firstResult;
    }

    /**
     * <p>Add order rule for query. Order rule will be applied as follows:</p>
     * <p><code>order by e.propertyName asc[desc]</code></p>
     *
     * @param propertyName name of property on which order will be applied
     * @param order        type of order asc[desc]
     * @return this
     */
    public Criteria addOrderRule( String propertyName, Order order )
    {
        orderRules.add( new OrderRule( propertyName, order ) );
        return this;
    }

    /**
     * Returns the list of <code>OrderRule</code>.
     *
     * @return the list of <code>OrderRule</code>.
     */
    public List<OrderRule> getOrderRules()
    {
        return orderRules;
    }
}

