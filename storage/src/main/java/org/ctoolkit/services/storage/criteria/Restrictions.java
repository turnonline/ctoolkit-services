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
     * Equal restriction: <i>e.name=:p_0</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @return {@link SimpleExpression}
     */
    public static Expression eq( String propertyName, Object propertyValue )
    {
        return new SimpleExpression( propertyName, propertyValue, "=" );
    }

    /**
     * Not equal restriction: <i>e.name<>:p_0</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @return {@link SimpleExpression}
     */
    public static Expression ne( String propertyName, Object propertyValue )
    {
        return new SimpleExpression( propertyName, propertyValue, "<>" );
    }

    /**
     * Greater then restriction: <i>e.name>:p_0</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @return {@link SimpleExpression}
     */
    public static Expression gt( String propertyName, Object propertyValue )
    {
        return new SimpleExpression( propertyName, propertyValue, ">" );
    }

    /**
     * Greater then or equal restriction: <i>e.name>=:p_0</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @return {@link SimpleExpression}
     */
    public static Expression ge( String propertyName, Object propertyValue )
    {
        return new SimpleExpression( propertyName, propertyValue, ">=" );
    }

    /**
     * Less then restriction: <i>e.name>=:p_0</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @return {@link SimpleExpression}
     */
    public static Expression lt( String propertyName, Object propertyValue )
    {
        return new SimpleExpression( propertyName, propertyValue, "<" );
    }

    /**
     * Less then or equal restriction: <i>e.name>=:p_0</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @return {@link SimpleExpression}
     */
    public static Expression le( String propertyName, Object propertyValue )
    {
        return new SimpleExpression( propertyName, propertyValue, "<=" );
    }

    /**
     * Is null restriction: <i>e.name is null</i>
     *
     * @param propertyName name of property
     * @return {@link NullExpression}
     */
    public static Expression isNull( String propertyName )
    {
        return new NullExpression( propertyName, "is null" );
    }

    /**
     * Is not null restriction: <i>e.name is not null</i>
     *
     * @param propertyName name of property
     * @return {@link NullExpression}
     */
    public static Expression isNotNull( String propertyName )
    {
        return new NullExpression( propertyName, "is not null" );
    }

    /**
     * In restriction: <i>e.name in (:p_0)</i>
     *
     * @param propertyName   name of property
     * @param propertyValues array of property values
     * @return {@link InExpression}
     */
    public static Expression in( String propertyName, Object[] propertyValues )
    {
        return new InExpression( propertyName, propertyValues, "in" );
    }

    /**
     * In restriction: <i>e.name in (:p_0)</i>
     *
     * @param propertyName   name of property
     * @param propertyValues collection of property values
     * @return {@link InExpression}
     */
    public static Expression in( String propertyName, Collection propertyValues )
    {
        return new InExpression( propertyName, propertyValues.toArray(), "in" );
    }

    /**
     * ID In restriction: <i>e.id in (:p_0)</i>. Wouldn't you rather do a batch load-by-key?
     *
     * @param idPropertyName name of the property
     * @param ids            array of ids Long (id)
     * @return {@link IdInExpression}
     */
    public static Expression idIn( String idPropertyName, Long[] ids )
    {
        return new IdInExpression( idPropertyName, ids );
    }

    /**
     * ID In restriction: <i>e.name in (:p_0)</i>. Wouldn't you rather do a batch load-by-key?
     *
     * @param idPropertyName name of the property
     * @param ids            array of ids as String (name)
     * @return {@link IdInExpression}
     */
    public static Expression idIn( String idPropertyName, String[] ids )
    {
        return new NameInExpression( idPropertyName, ids );
    }

    /**
     * Not in restriction: <i>e.name not in (:p_0)</i>
     *
     * @param propertyName   name of property
     * @param propertyValues array of property values
     * @return {@link InExpression}
     */
    public static Expression notIn( String propertyName, Object[] propertyValues )
    {
        return new InExpression( propertyName, propertyValues, "not in" );
    }

    /**
     * Not in restriction: <i>e.name not in (:p_0)</i>
     *
     * @param propertyName   name of property
     * @param propertyValues collection of property values
     * @return {@link InExpression}
     * @deprecated no direct support, https://developers.google.com/appengine/docs/java/datastore/queries
     */
    @Deprecated
    public static Expression notIn( String propertyName, Collection propertyValues )
    {
        return new InExpression( propertyName, propertyValues.toArray(), "not in" );
    }

    /**
     * Between restriction: <i>e.name >=:p_0 and e.name <=:p_0</i>
     *
     * @param propertyName      name of property
     * @param lowPropertyValue  low bound property value
     * @param highPropertyValue high bound  property value
     * @param lowBound          low comparison type
     * @param highBound         high comparison type
     * @return {@link BetweenExpression}
     */
    public static Expression between( String propertyName, Object lowPropertyValue, Object highPropertyValue, Bound lowBound, Bound highBound )
    {
        return new BetweenExpression( propertyName, lowPropertyValue, highPropertyValue, lowBound, highBound );
    }

    /**
     * Logical expression <i>or</i>
     *
     * @param expressions array of expressions
     * @return {@link LogicalExpression}
     */
    public static Expression or( Expression... expressions )
    {
        return new LogicalExpression( LogicalExpression.OR, expressions );
    }

    /**
     * Logical expression <i>and</i>
     *
     * @param expressions array of expressions
     * @return {@link LogicalExpression}
     */
    public static Expression and( Expression... expressions )
    {
        return new LogicalExpression( LogicalExpression.AND, expressions );
    }

    /**
     * Equal restriction for entity properties: <i>e.name=e.otherName</i>
     *
     * @param propertyName      name of property
     * @param otherPropertyName other property name
     * @return {@link PropertyExpression}
     */
    public static Expression eqProperty( String propertyName, String otherPropertyName )
    {
        return new PropertyExpression( propertyName, otherPropertyName, "=" );
    }

    /**
     * Not equal restriction for entity properties: <i>e.name<>e.otherName</i>
     *
     * @param propertyName      name of property
     * @param otherPropertyName other property name
     * @return {@link PropertyExpression}
     */
    public static Expression neProperty( String propertyName, String otherPropertyName )
    {
        return new PropertyExpression( propertyName, otherPropertyName, "<>" );
    }

    /**
     * Greater then restriction for entity properties: <i>e.name>e.otherName</i>
     *
     * @param propertyName      name of property
     * @param otherPropertyName other property name
     * @return {@link PropertyExpression}
     */
    public static Expression gtProperty( String propertyName, String otherPropertyName )
    {
        return new PropertyExpression( propertyName, otherPropertyName, ">" );
    }

    /**
     * Greater then or equal restriction for entity properties: <i>e.name>=e.otherName</i>
     *
     * @param propertyName      name of property
     * @param otherPropertyName other property name
     * @return {@link PropertyExpression}
     */
    public static Expression geProperty( String propertyName, String otherPropertyName )
    {
        return new PropertyExpression( propertyName, otherPropertyName, ">=" );
    }

    /**
     * Lower then restriction for entity properties: <i>e.name<e.otherName</i>
     *
     * @param propertyName      name of property
     * @param otherPropertyName other property name
     * @return {@link PropertyExpression}
     */
    public static Expression ltProperty( String propertyName, String otherPropertyName )
    {
        return new PropertyExpression( propertyName, otherPropertyName, "<" );
    }

    /**
     * Lower then or equal restriction for entity properties: <i>e.name<=e.otherName</i>
     *
     * @param propertyName      name of property
     * @param otherPropertyName other property name
     * @return {@link PropertyExpression}
     */
    public static Expression leProperty( String propertyName, String otherPropertyName )
    {
        return new PropertyExpression( propertyName, otherPropertyName, "<=" );
    }

    /**
     * Like restriction: <i>e.name like :p_0</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @param matchMode     match mode(EXACT, START, END, ANYWHERE)
     * @return {@link LikeExpression}
     */
    public static Expression like( String propertyName, String propertyValue, MatchMode matchMode )
    {
        return new LikeExpression( propertyName, propertyValue, matchMode, "like", false );
    }

    /**
     * Ignore case like restriction: <i>lower(e.name) like lower(:p_0)</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @param matchMode     match mode(EXACT, START, END, ANYWHERE)
     * @return {@link LikeExpression}
     */
    public static Expression ilike( String propertyName, String propertyValue, MatchMode matchMode )
    {
        return new LikeExpression( propertyName, propertyValue, matchMode, "like", true );
    }

    /**
     * Not like restriction: <i>e.name not like :p_0</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @param matchMode     match mode(EXACT, START, END, ANYWHERE)
     * @return {@link LikeExpression}
     */
    public static Expression notLike( String propertyName, String propertyValue, MatchMode matchMode )
    {
        return new LikeExpression( propertyName, propertyValue, matchMode, "not like", false );
    }

    /**
     * Ignore case not like restriction: <i>lower(e.name) not like lower(:p_0)</i>
     *
     * @param propertyName  name of property
     * @param propertyValue property value
     * @param matchMode     match mode(EXACT, START, END, ANYWHERE)
     * @return {@link LikeExpression}
     */
    public static Expression iNotLike( String propertyName, String propertyValue, MatchMode matchMode )
    {
        return new LikeExpression( propertyName, propertyValue, matchMode, "not like", true );
    }

    /**
     * Reference restriction.
     *
     * @param propertyName the property name
     * @param refClass     the type of the referenced class
     * @param id           the referenced class identification
     * @return {@link ReferenceIdExpression}
     */
    public static Expression idRef( String propertyName, Class refClass, Long id )
    {
        return new ReferenceIdExpression( propertyName, refClass, id );
    }

    /**
     * Reference restriction.
     *
     * @param propertyName the property name
     * @param refClass     the type of the referenced class
     * @param name         the referenced class identification
     * @return {@link ReferenceIdExpression}
     */
    public static Expression idRef( String propertyName, Class refClass, String name )
    {
        return new ReferenceNameExpression( propertyName, refClass, name );
    }
}
