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

package org.ctoolkit.services.storage.appengine.datastore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.cmd.Query;
import org.ctoolkit.services.storage.criteria.BetweenExpression;
import org.ctoolkit.services.storage.criteria.Bound;
import org.ctoolkit.services.storage.criteria.Criteria;
import org.ctoolkit.services.storage.criteria.CriteriaBuilder;
import org.ctoolkit.services.storage.criteria.Expression;
import org.ctoolkit.services.storage.criteria.IdInExpression;
import org.ctoolkit.services.storage.criteria.InExpression;
import org.ctoolkit.services.storage.criteria.LikeExpression;
import org.ctoolkit.services.storage.criteria.LogicalExpression;
import org.ctoolkit.services.storage.criteria.MatchMode;
import org.ctoolkit.services.storage.criteria.NameInExpression;
import org.ctoolkit.services.storage.criteria.NullExpression;
import org.ctoolkit.services.storage.criteria.Order;
import org.ctoolkit.services.storage.criteria.OrderRule;
import org.ctoolkit.services.storage.criteria.PropertyExpression;
import org.ctoolkit.services.storage.criteria.ReferenceIdExpression;
import org.ctoolkit.services.storage.criteria.ReferenceNameExpression;
import org.ctoolkit.services.storage.criteria.SimpleExpression;

import java.util.ArrayList;
import java.util.Collection;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Objectify CriteriaBuilder implementation. All expression produces a Objectify query object. Query object may
 * be chained.
 * <p>
 * It's not thread safe and it's intended to create a new instance for every query.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
class ObjectifyCriteriaBuilder<E>
        implements CriteriaBuilder<Query<E>, E>
{
    private Class<E> entityClass;

    private Query<E> query;

    ObjectifyCriteriaBuilder()
    {
    }

    @Override
    public Query<E> build( BetweenExpression expression )
    {
        StringBuilder builder = new StringBuilder();

        builder.append( expression.getPropertyName() );
        builder.append( " " );
        builder.append( expression.getLowBound() == Bound.SOFT ? ">=" : ">" );

        query = query.filter( builder.toString(), expression.getPropertyValue() );

        builder = new StringBuilder();

        builder.append( expression.getPropertyName() );
        builder.append( " " );
        builder.append( expression.getHighBound() == Bound.SOFT ? "<=" : "<" );

        return query.filter( builder.toString(), expression.getHighPropertyValue() );
    }

    @Override
    public Query<E> build( InExpression expression )
    {
        StringBuilder builder = new StringBuilder();

        builder.append( expression.getPropertyName() );
        builder.append( " " );
        builder.append( "in" );

        return query.filter( builder.toString(), expression.getPropertyValues() );
    }

    public Query<E> build( IdInExpression expression )
    {
        Collection<Key<E>> keys = new ArrayList<>();

        for ( Long id : expression.getPropertyValues() )
        {
            keys.add( Key.create( entityClass, id ) );
        }

        return query.filterKey( "in", keys );
    }

    public Query<E> build( NameInExpression expression )
    {
        Collection<Key<E>> keys = new ArrayList<>();

        for ( String id : expression.getPropertyValues() )
        {
            keys.add( Key.create( entityClass, id ) );
        }

        return query.filterKey( "in", keys );
    }

    @Override
    public Query<E> build( LikeExpression expression )
    {
        if ( expression.getMatchMode() != MatchMode.START )
        {
            throw new UnsupportedOperationException( "MatchMode " + expression.getMatchMode()
                    + " is not  supported !" );
        }
        if ( !"like".equals( expression.getOperation() ) )
        {
            throw new UnsupportedOperationException( "Expression operation " + expression.getOperation()
                    + " is not  supported !" );
        }

        // [name like 'pas%'] is just a range query:
        // objectify.query(MyEntity.class).filter("propertyName >=","value").filter("propertyName <", "value" + "\uFFFD");
        // Note that you can index [name like '%pas'] by storing the reversed string as a separate field and
        // using a range scan as above.

        StringBuilder builder = new StringBuilder();

        builder.append( expression.getPropertyName() );
        builder.append( " " );
        builder.append( ">=" );

        String value = expression.getPropertyValue().toString().replace( "%", "" );
        query = query.filter( builder.toString(), value );

        builder = new StringBuilder();

        builder.append( expression.getPropertyName() );
        builder.append( " " );
        builder.append( "<" );

        return query.filter( builder.toString(), value + "\uFFFD" );
    }

    @Override
    public Query<E> build( LogicalExpression expression )
    {
        if ( "or".equalsIgnoreCase( expression.getOperation() ) )
        {
            throw new UnsupportedOperationException( "OR logical operation is not supported !" );
        }

        for ( Expression e : expression.getExpressions() )
        {
            query = e.build( this );
        }

        return query;
    }

    @Override
    public Query<E> build( NullExpression expression )
    {
        String filterOperator;

        if ( "is null".equals( expression.getOperation() ) )
        {
            filterOperator = "=";
        }
        else
        {
            filterOperator = "!=";
        }
        StringBuilder builder = new StringBuilder();

        builder.append( expression.getPropertyName() );
        builder.append( " " );
        builder.append( filterOperator );

        return query.filter( builder.toString(), null );
    }

    @Override
    public Query<E> build( PropertyExpression expression )
    {
        throw new UnsupportedOperationException( "BigTable support only expressions: at the left hand side can only" +
                " be a property name or __key__, and the right hand side can only be a simple value." );
    }

    @Override
    public Query<E> build( SimpleExpression expression )
    {
        String filterOperator = expression.getOperation();

        if ( "<>".equals( filterOperator ) )
        {
            filterOperator = "!=";
        }

        StringBuilder builder = new StringBuilder();

        builder.append( expression.getPropertyName() );
        builder.append( " " );
        builder.append( filterOperator );

        return query.filter( builder.toString(), expression.getPropertyValue() );
    }

    @Override
    public Query<E> build( ReferenceIdExpression expression )
    {
        String condition = expression.getPropertyName() + " =";
        return query.filter( condition, Key.create( expression.getRefClass(), expression.getPropertyValue() ) );
    }

    @Override
    public Query<E> build( ReferenceNameExpression expression )
    {
        String condition = expression.getPropertyName() + " =";
        return query.filterKey( condition, Key.create( expression.getRefClass(), expression.getPropertyValue() ) );
    }

    @Override
    @SuppressWarnings( value = "unchecked" )
    public Query<E> build( Criteria<E> criteria )
    {
        entityClass = criteria.getEntityClass();
        query = ofy().load().type( criteria.getEntityClass() );

        for ( Expression c : criteria.getExpressionList() )
        {
            query = c.build( this );
        }

        // set sort order
        for ( OrderRule rule : criteria.getOrderRules() )
        {
            if ( rule.getOrder() == Order.DESC )
            {
                query = query.order( "-" + rule.getPropertyName() );
            }
            if ( rule.getOrder() == Order.ASC )
            {
                query = query.order( rule.getPropertyName() );
            }
        }

        // set first result of the query
        if ( criteria.getFirstResult() > 0 )
        {
            query = query.offset( criteria.getFirstResult() );
        }
        // set max results of the query
        if ( criteria.getMaxResults() > 0 )
        {
            query = query.limit( criteria.getMaxResults() );
        }

        return query;
    }
}
