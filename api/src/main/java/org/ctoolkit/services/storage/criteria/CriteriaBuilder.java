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
 * Criteria builder interface in order to build implementation specific criteria and its expressions.
 *
 * @param <Q> a typed Query
 * @param <E> a persisted entity type
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface CriteriaBuilder<Q, E>
{
    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( BetweenExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( InExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( IdInExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( NameInExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( LikeExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( LogicalExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( NullExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( PropertyExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( SimpleExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( ReferenceIdExpression expression );

    /**
     * Returns the implementation specific expression.
     *
     * @param expression the criteria expression instance
     * @return the implementation specific expression
     */
    Q build( ReferenceNameExpression expression );

    /**
     * Returns the implementation specific criteria.
     *
     * @param criteria the criteria instance
     * @return the implementation specific criteria
     */
    Q build( Criteria<E> criteria );
}
