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

import org.ctoolkit.services.storage.EntityIdentity;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <p>Criteria class is helper class for creating criteria.</p>
 * <p><b>Usage:</b></p>
 * <pre>
 * Criteria&#60;Entity&#62; criteria = Criteria.of(Entity.class);
 * criteria.equal("id", 1);
 * </pre>
 *
 * @param <T> the concrete type of the entity
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class Criteria<T>
{
    private int limit = -1;

    private int offset = -1;

    private Class<T> entity;

    private List<Expression> expressionList = new ArrayList<>();

    private List<OrderRule> orderRules = new ArrayList<>();

    private Criteria( @Nonnull Class<T> entity )
    {
        this.entity = checkNotNull( entity );
    }

    /**
     * Creates criteria instance for requested entity type.
     *
     * @param entity entity on which criteria will be applied
     * @param <T>    the concrete type of the entity
     * @return new instance of {@link Criteria}
     */
    public static <T> Criteria<T> of( @Nonnull Class<T> entity )
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
    public Criteria<T> addCriteria( @Nonnull Expression expression )
    {
        expressionList.add( expression );
        return this;
    }

    /**
     * The result for this criteria definition will be ordered by ascending.
     *
     * @param property the name of the property on which order will be applied
     * @return this criteria instance
     */
    public Criteria<T> ascending( @Nonnull String property )
    {
        addOrderRule( property, Order.ASC );
        return this;
    }

    /**
     * The result for this criteria definition will be ordered by descending.
     *
     * @param property the name of the property on which order will be applied
     * @return this criteria instance
     */
    public Criteria<T> descending( @Nonnull String property )
    {
        addOrderRule( property, Order.DESC );
        return this;
    }

    /**
     * Configures this criteria to filter query result by referenced entity.
     *
     * @param property the name of the property that holds the referenced entity key
     * @param type     the type of the referenced class used to filter results
     * @param id       the referenced entity identification
     * @return this criteria instance
     */
    public Criteria<T> reference( @Nonnull String property, @Nonnull Class type, @Nonnull Long id )
    {
        addCriteria( new ReferenceIdExpression( property, type, id ) );
        return this;
    }

    /**
     * Configures this criteria to filter query result by referenced entity.
     *
     * @param property the name of the property that holds the referenced entity key
     * @param type     the type of the referenced class used to filter results
     * @param name     the referenced entity identification
     * @return this criteria instance
     */
    public Criteria<T> reference( @Nonnull String property, @Nonnull Class type, @Nonnull String name )
    {
        addCriteria( new ReferenceNameExpression( property, type, name ) );
        return this;
    }

    /**
     * Configures this criteria to filter query result by referenced entity.
     *
     * @param property the name of the property that holds the referenced entity key
     * @param entity   the referenced entity instance as a type used to filter results
     * @return this criteria instance
     */
    public Criteria<T> reference( @Nonnull String property, @Nonnull EntityIdentity entity )
    {
        Object identification = entity.getId();
        if ( identification instanceof String )
        {
            addCriteria( new ReferenceNameExpression( property, entity.getClass(), ( String ) identification ) );
        }
        else
        {
            addCriteria( new ReferenceIdExpression( property, entity.getClass(), ( Long ) identification ) );
        }

        return this;
    }

    /**
     * Equal restriction: {@code <i>e.name=:p_0</i>}
     *
     * @param property the name of the property
     * @param value    the property value
     * @return this criteria instance
     */
    public Criteria<T> equal( @Nonnull String property, @Nonnull Object value )
    {
        addCriteria( new SimpleExpression( property, value, "=" ) );
        return this;
    }

    /**
     * Not equal restriction: {@code <i>e.name<>:p_0</i>}
     *
     * @param property the name of the property
     * @param value    the property value
     * @return this criteria instance
     */
    public Criteria<T> notEqual( @Nonnull String property, @Nonnull Object value )
    {
        addCriteria( new SimpleExpression( property, value, "<>" ) );
        return this;
    }

    /**
     * Greater then restriction: {@code <i>e.name>:p_0</i>}
     *
     * @param property the name of the property
     * @param value    the property value
     * @return this criteria instance
     */
    public Criteria<T> gt( @Nonnull String property, @Nonnull Object value )
    {
        addCriteria( new SimpleExpression( property, value, ">" ) );
        return this;
    }

    /**
     * Greater then or equal restriction: {@code <i>e.name>=:p_0</i>}
     *
     * @param property the name of the property
     * @param value    the property value
     * @return this criteria instance
     */
    public Criteria<T> ge( @Nonnull String property, @Nonnull Object value )
    {
        addCriteria( new SimpleExpression( property, value, ">=" ) );
        return this;
    }

    /**
     * Less then restriction: {@code <i>e.name>=:p_0</i>}
     *
     * @param property the name of the property
     * @param value    the property value
     * @return this criteria instance
     */
    public Criteria<T> lt( @Nonnull String property, @Nonnull Object value )
    {
        addCriteria( new SimpleExpression( property, value, "<" ) );
        return this;
    }

    /**
     * Less then or equal restriction: {@code <i>e.name>=:p_0</i>}
     *
     * @param property the name of the property
     * @param value    the property value
     * @return this criteria instance
     */
    public Criteria<T> le( @Nonnull String property, @Nonnull Object value )
    {
        addCriteria( new SimpleExpression( property, value, "<=" ) );
        return this;
    }

    /**
     * Is null restriction: {@code <i>e.name is null</i>}
     *
     * @param property the name of the property
     * @return this criteria instance
     */
    public Criteria<T> isNull( @Nonnull String property )
    {
        addCriteria( new NullExpression( property, "is null" ) );
        return this;
    }

    /**
     * Is not null restriction: {@code <i>e.name is not null</i>}
     *
     * @param property the name of the property
     * @return this criteria instance
     */
    public Criteria<T> isNotNull( @Nonnull String property )
    {
        addCriteria( new NullExpression( property, "is not null" ) );
        return this;
    }

    /**
     * In restriction: {@code <i>e.name in (:p_0)</i>}
     *
     * @param property the name of the property
     * @param values   the array of property values
     * @return this criteria instance
     */
    public Criteria<T> in( @Nonnull String property, @Nonnull Object[] values )
    {
        addCriteria( new InExpression( property, values, "in" ) );
        return this;
    }

    /**
     * In restriction: {@code <i>e.name in (:p_0)</i>}
     *
     * @param property the name of the property
     * @param values   the collection of property values
     * @return this criteria instance
     */
    public Criteria<T> in( @Nonnull String property, @Nonnull Collection values )
    {
        addCriteria( new InExpression( property, values.toArray(), "in" ) );
        return this;
    }

    /**
     * ID In restriction: {@code <i>e.id in (:p_0)</i>}.
     *
     * @param property the name of the property
     * @param ids      the array of ids Long (id)
     * @return this criteria instance
     */
    public Criteria<T> idIn( @Nonnull String property, @Nonnull Long[] ids )
    {
        addCriteria( new IdInExpression( property, ids ) );
        return this;
    }

    /**
     * ID In restriction: {@code <i>e.name in (:p_0)</i>}.
     *
     * @param property the name of the property
     * @param ids      the array of ids as String (name)
     * @return this criteria instance
     */
    public Criteria<T> idIn( @Nonnull String property, @Nonnull String[] ids )
    {
        addCriteria( new NameInExpression( property, ids ) );
        return this;
    }

    /**
     * Between restriction: {@code <i>e.name >=:p_0 and e.name <=:p_0</i>}
     *
     * @param property  the name of the property
     * @param lowValue  the low bound property value
     * @param highValue the high bound  property value
     * @param lowBound  the low comparison type
     * @param highBound the high comparison type
     * @return this criteria instance
     */
    public Criteria<T> between( @Nonnull String property,
                                @Nonnull Object lowValue,
                                @Nonnull Object highValue,
                                @Nonnull Bound lowBound,
                                @Nonnull Bound highBound )
    {
        addCriteria( new BetweenExpression( property, lowValue, highValue, lowBound, highBound ) );
        return this;
    }

    /**
     * Logical expression {@code <i>or</i>}
     *
     * @param expressions the array of expressions
     * @return this criteria instance
     */
    public Criteria<T> or( @Nonnull Expression... expressions )
    {
        addCriteria( new LogicalExpression( LogicalExpression.OR, expressions ) );
        return this;
    }

    /**
     * Logical expression {@code <i>and</i>}
     *
     * @param expressions the array of expressions
     * @return this criteria instance
     */
    public Criteria<T> and( @Nonnull Expression... expressions )
    {
        addCriteria( new LogicalExpression( LogicalExpression.AND, expressions ) );
        return this;
    }

    /**
     * Equal restriction for entity properties: {@code <i>e.name=e.otherName</i>}
     *
     * @param property      the name of the property
     * @param otherProperty the other property name
     * @return this criteria instance
     */
    public Criteria<T> eqProperty( @Nonnull String property, @Nonnull String otherProperty )
    {
        addCriteria( new PropertyExpression( property, otherProperty, "=" ) );
        return this;
    }

    /**
     * Not equal restriction for entity properties: {@code <i>e.name<>e.otherName</i>}
     *
     * @param property      the name of the property
     * @param otherProperty the other property name
     * @return this criteria instance
     */
    public Criteria<T> neProperty( @Nonnull String property, @Nonnull String otherProperty )
    {
        addCriteria( new PropertyExpression( property, otherProperty, "<>" ) );
        return this;
    }

    /**
     * Greater then restriction for entity properties: {@code <i>e.name>e.otherName</i>}
     *
     * @param property      the name of the property
     * @param otherProperty the other property name
     * @return this criteria instance
     */
    public Criteria<T> gtProperty( @Nonnull String property, @Nonnull String otherProperty )
    {
        addCriteria( new PropertyExpression( property, otherProperty, ">" ) );
        return this;
    }

    /**
     * Greater then or equal restriction for entity properties: {@code <i>e.name>=e.otherName</i>}
     *
     * @param property      the name of the property
     * @param otherProperty the other property name
     * @return this criteria instance
     */
    public Criteria<T> geProperty( @Nonnull String property, @Nonnull String otherProperty )
    {
        addCriteria( new PropertyExpression( property, otherProperty, ">=" ) );
        return this;
    }

    /**
     * Lower then restriction for entity properties: {@code <i>e.name<e.otherName</i>}
     *
     * @param property      the name of the property
     * @param otherProperty the other property name
     * @return this criteria instance
     */
    public Criteria<T> ltProperty( @Nonnull String property, @Nonnull String otherProperty )
    {
        addCriteria( new PropertyExpression( property, otherProperty, "<" ) );
        return this;
    }

    /**
     * Lower then or equal restriction for entity properties: {@code <i>e.name<=e.otherName</i>}
     *
     * @param property      the name of the property
     * @param otherProperty the other property name
     * @return this criteria instance
     */
    public Criteria<T> leProperty( @Nonnull String property, @Nonnull String otherProperty )
    {
        addCriteria( new PropertyExpression( property, otherProperty, "<=" ) );
        return this;
    }

    /**
     * Like restriction: {@code <i>e.name like :p_0</i>}
     *
     * @param property  the name of the property
     * @param value     the property value
     * @param matchMode match mode(EXACT, START, END, ANYWHERE)
     * @return this criteria instance
     */
    public Criteria<T> like( @Nonnull String property, @Nonnull String value, @Nonnull MatchMode matchMode )
    {
        addCriteria( new LikeExpression( property, value, matchMode, "like", false ) );
        return this;
    }

    /**
     * Ignore case like restriction: {@code <i>lower(e.name) like lower(:p_0)</i>}
     *
     * @param property  the name of the property
     * @param value     the property value
     * @param matchMode match mode(EXACT, START, END, ANYWHERE)
     * @return this criteria instance
     */
    public Criteria<T> ilike( @Nonnull String property, @Nonnull String value, @Nonnull MatchMode matchMode )
    {
        addCriteria( new LikeExpression( property, value, matchMode, "like", true ) );
        return this;
    }

    /**
     * Not like restriction: {@code <i>e.name not like :p_0</i>}
     *
     * @param property  the name of the property
     * @param value     the property value
     * @param matchMode match mode(EXACT, START, END, ANYWHERE)
     * @return this criteria instance
     */
    public Criteria<T> notLike( @Nonnull String property, @Nonnull String value, @Nonnull MatchMode matchMode )
    {
        addCriteria( new LikeExpression( property, value, matchMode, "not like", false ) );
        return this;
    }

    /**
     * Ignore case not like restriction: {@code <i>lower(e.name) not like lower(:p_0)</i>}
     *
     * @param property  the name of the property
     * @param value     the property value
     * @param matchMode match mode(EXACT, START, END, ANYWHERE)
     * @return this criteria instance
     */
    public Criteria<T> iNotLike( @Nonnull String property, @Nonnull String value, @Nonnull MatchMode matchMode )
    {
        addCriteria( new LikeExpression( property, value, matchMode, "not like", true ) );
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
    public int getLimit()
    {
        return limit;
    }

    /**
     * Sets the maximum number of results to retrieve.
     *
     * @param limit the max number of results to retrieve
     */
    public Criteria<T> limit( int limit )
    {
        this.limit = limit;
        return this;
    }

    /**
     * Returns the position of the first result to retrieve.
     *
     * @return the position of the first result to retrieve.
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * Sets the position of the first result to retrieve.
     *
     * @param offset first result of query
     */
    public Criteria<T> offset( int offset )
    {
        this.offset = offset;
        return this;
    }

    /**
     * <p>Add order rule for query. Order rule will be applied as follows:</p>
     * <p><code>order by e.propertyName asc[desc]</code></p>
     *
     * @param propertyName name of property on which order will be applied
     * @param order        type of order asc[desc]
     * @return this
     */
    public Criteria<T> addOrderRule( @Nonnull String propertyName, @Nonnull Order order )
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

