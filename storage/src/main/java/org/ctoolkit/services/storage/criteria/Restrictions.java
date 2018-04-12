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

import java.util.Collection;

/**
 * Restrictions holder.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class Restrictions
{
    private Restrictions()
    {
        // no default constructor
    }

    /**
     * Equal restriction: {@code <i>e.name=:p_0</i>}
     *
     * @param property the property name
     * @param value    the property value
     * @return {@link SimpleExpression}
     */
    public static Expression eq( String property, Object value )
    {
        return new SimpleExpression( property, value, "=" );
    }

    /**
     * Not equal restriction: {@code <i>e.name<>:p_0</i>}
     *
     * @param property the property name
     * @param value    the property value
     * @return {@link SimpleExpression}
     */
    public static Expression ne( String property, Object value )
    {
        return new SimpleExpression( property, value, "<>" );
    }

    /**
     * Greater then restriction: {@code <i>e.name>:p_0</i>}
     *
     * @param property the property name
     * @param value    the property value
     * @return {@link SimpleExpression}
     */
    public static Expression gt( String property, Object value )
    {
        return new SimpleExpression( property, value, ">" );
    }

    /**
     * Greater then or equal restriction: {@code <i>e.name>=:p_0</i>}
     *
     * @param property the property name
     * @param value    the property value
     * @return {@link SimpleExpression}
     */
    public static Expression ge( String property, Object value )
    {
        return new SimpleExpression( property, value, ">=" );
    }

    /**
     * Less then restriction: {@code <i>e.name>=:p_0</i>}
     *
     * @param property the property name
     * @param value    the property value
     * @return {@link SimpleExpression}
     */
    public static Expression lt( String property, Object value )
    {
        return new SimpleExpression( property, value, "<" );
    }

    /**
     * Less then or equal restriction: {@code <i>e.name>=:p_0</i>}
     *
     * @param property the property name
     * @param value    the property value
     * @return {@link SimpleExpression}
     */
    public static Expression le( String property, Object value )
    {
        return new SimpleExpression( property, value, "<=" );
    }

    /**
     * Is null restriction: {@code <i>e.name is null</i>}
     *
     * @param property the property name
     * @return {@link NullExpression}
     */
    public static Expression isNull( String property )
    {
        return new NullExpression( property, "is null" );
    }

    /**
     * Is not null restriction: {@code <i>e.name is not null</i>}
     *
     * @param property the property name
     * @return {@link NullExpression}
     */
    public static Expression isNotNull( String property )
    {
        return new NullExpression( property, "is not null" );
    }

    /**
     * In restriction: {@code <i>e.name in (:p_0)</i>}
     *
     * @param property the property name
     * @param values   the array of property values
     * @return {@link InExpression}
     */
    public static Expression in( String property, Object[] values )
    {
        return new InExpression( property, values, "in" );
    }

    /**
     * In restriction: {@code <i>e.name in (:p_0)</i>}
     *
     * @param property the property name
     * @param values   the collection of property values
     * @return {@link InExpression}
     */
    public static Expression in( String property, Collection values )
    {
        return new InExpression( property, values.toArray(), "in" );
    }

    /**
     * ID In restriction: {@code <i>e.id in (:p_0)</i>}. Wouldn't you rather do a batch load-by-key?
     *
     * @param property the name of the ID property
     * @param ids      the array of ids Long (id)
     * @return {@link IdInExpression}
     */
    public static Expression idIn( String property, Long[] ids )
    {
        return new IdInExpression( property, ids );
    }

    /**
     * ID In restriction: {@code <i>e.name in (:p_0)</i>}. Wouldn't you rather do a batch load-by-key?
     *
     * @param property the name of the ID property
     * @param ids      the array of ids as String (name)
     * @return {@link IdInExpression}
     */
    public static Expression idIn( String property, String[] ids )
    {
        return new NameInExpression( property, ids );
    }

    /**
     * Not in restriction: {@code <i>e.name not in (:p_0)</i>}
     *
     * @param property the property name
     * @param values   the array of property values
     * @return {@link InExpression}
     */
    public static Expression notIn( String property, Object[] values )
    {
        return new InExpression( property, values, "not in" );
    }

    /**
     * Not in restriction: {@code <i>e.name not in (:p_0)</i>}
     *
     * @param property the property name
     * @param values   the collection of property values
     * @return {@link InExpression}
     * @deprecated no direct support, https://developers.google.com/appengine/docs/java/datastore/queries
     */
    @Deprecated
    public static Expression notIn( String property, Collection values )
    {
        return new InExpression( property, values.toArray(), "not in" );
    }

    /**
     * Between restriction: {@code <i>e.name >=:p_0 and e.name <=:p_0</i>}
     *
     * @param property  the property name
     * @param lowValue  the low bound property value
     * @param highValue the high bound  property value
     * @param lowBound  the low comparison type
     * @param highBound the high comparison type
     * @return {@link BetweenExpression}
     */
    public static Expression between( String property,
                                      Object lowValue,
                                      Object highValue,
                                      Bound lowBound,
                                      Bound highBound )
    {
        return new BetweenExpression( property, lowValue, highValue, lowBound, highBound );
    }

    /**
     * Logical expression {@code <i>or</i>}
     *
     * @param expressions the array of expressions
     * @return {@link LogicalExpression}
     */
    public static Expression or( Expression... expressions )
    {
        return new LogicalExpression( LogicalExpression.OR, expressions );
    }

    /**
     * Logical expression {@code <i>and</i>}
     *
     * @param expressions the array of expressions
     * @return {@link LogicalExpression}
     */
    public static Expression and( Expression... expressions )
    {
        return new LogicalExpression( LogicalExpression.AND, expressions );
    }

    /**
     * Equal restriction for entity properties: {@code <i>e.name=e.otherName</i>}
     *
     * @param property      the property name
     * @param otherProperty the other property name
     * @return {@link PropertyExpression}
     */
    public static Expression eqProperty( String property, String otherProperty )
    {
        return new PropertyExpression( property, otherProperty, "=" );
    }

    /**
     * Not equal restriction for entity properties: {@code <i>e.name<>e.otherName</i>}
     *
     * @param property      the property name
     * @param otherProperty the other property name
     * @return {@link PropertyExpression}
     */
    public static Expression neProperty( String property, String otherProperty )
    {
        return new PropertyExpression( property, otherProperty, "<>" );
    }

    /**
     * Greater then restriction for entity properties: {@code <i>e.name>e.otherName</i>}
     *
     * @param propertyName  the property name
     * @param otherProperty the other property name
     * @return {@link PropertyExpression}
     */
    public static Expression gtProperty( String propertyName, String otherProperty )
    {
        return new PropertyExpression( propertyName, otherProperty, ">" );
    }

    /**
     * Greater then or equal restriction for entity properties: {@code <i>e.name>=e.otherName</i>}
     *
     * @param property      the property name
     * @param otherProperty the other property name
     * @return {@link PropertyExpression}
     */
    public static Expression geProperty( String property, String otherProperty )
    {
        return new PropertyExpression( property, otherProperty, ">=" );
    }

    /**
     * Lower then restriction for entity properties: {@code <i>e.name<e.otherName</i>}
     *
     * @param property      the property name
     * @param otherProperty the other property name
     * @return {@link PropertyExpression}
     */
    public static Expression ltProperty( String property, String otherProperty )
    {
        return new PropertyExpression( property, otherProperty, "<" );
    }

    /**
     * Lower then or equal restriction for entity properties: {@code <i>e.name<=e.otherName</i>}
     *
     * @param property      the property name
     * @param otherProperty the other property name
     * @return {@link PropertyExpression}
     */
    public static Expression leProperty( String property, String otherProperty )
    {
        return new PropertyExpression( property, otherProperty, "<=" );
    }

    /**
     * Like restriction: {@code <i>e.name like :p_0</i>}
     *
     * @param property  the property name
     * @param value     the property value
     * @param matchMode match mode(EXACT, START, END, ANYWHERE)
     * @return {@link LikeExpression}
     */
    public static Expression like( String property, String value, MatchMode matchMode )
    {
        return new LikeExpression( property, value, matchMode, "like", false );
    }

    /**
     * Ignore case like restriction: {@code <i>lower(e.name) like lower(:p_0)</i>}
     *
     * @param property  the property name
     * @param value     the property value
     * @param matchMode match mode(EXACT, START, END, ANYWHERE)
     * @return {@link LikeExpression}
     */
    public static Expression ilike( String property, String value, MatchMode matchMode )
    {
        return new LikeExpression( property, value, matchMode, "like", true );
    }

    /**
     * Not like restriction: {@code <i>e.name not like :p_0</i>}
     *
     * @param property  the property name
     * @param value     the property value
     * @param matchMode match mode(EXACT, START, END, ANYWHERE)
     * @return {@link LikeExpression}
     */
    public static Expression notLike( String property, String value, MatchMode matchMode )
    {
        return new LikeExpression( property, value, matchMode, "not like", false );
    }

    /**
     * Ignore case not like restriction: {@code <i>lower(e.name) not like lower(:p_0)</i>}
     *
     * @param property  the property name
     * @param value     the property value
     * @param matchMode match mode(EXACT, START, END, ANYWHERE)
     * @return {@link LikeExpression}
     */
    public static Expression iNotLike( String property, String value, MatchMode matchMode )
    {
        return new LikeExpression( property, value, matchMode, "not like", true );
    }

    /**
     * Reference restriction.
     *
     * @param property the property name
     * @param refClass the type of the referenced class
     * @param id       the referenced class identification
     * @return {@link ReferenceIdExpression}
     */
    public static Expression idRef( String property, Class refClass, Long id )
    {
        return new ReferenceIdExpression( property, refClass, id );
    }

    /**
     * Reference restriction.
     *
     * @param property the property name
     * @param refClass the type of the referenced class
     * @param name     the referenced class identification
     * @return {@link ReferenceIdExpression}
     */
    public static Expression idRef( String property, Class refClass, String name )
    {
        return new ReferenceNameExpression( property, refClass, name );
    }
}
