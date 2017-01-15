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

import java.util.List;

/**
 * The criteria executor.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
interface CriteriaExecutor
{
    /**
     * Executes a query based on the given criteria and retrieves a list of objects.
     *
     * @param criteria a criteria holder {@link Criteria}
     * @return list of objects that meets the criteria
     */
    <T> List<T> list( Criteria<T> criteria );

    /**
     * Executes a keys-only query based on given criteria and get the result as a list of entity names.
     * This is more efficient than fetching the actual full result set.
     *
     * @param criteria a criteria holder {@link Criteria}
     * @param <T>      the type of the entity to be queried
     * @return the list of long IDs (entity identification)
     */
    <T> List<Long> fetchIds( Criteria<T> criteria );

    /**
     * Executes a keys-only query based on given criteria and get the result as a list of entity names.
     * This is more efficient than fetching the actual full result set.
     *
     * @param criteria a criteria holder {@link Criteria}
     * @param <T>      the type of the entity to be queried
     * @return the list of string names (entity identification)
     */
    <T> List<String> fetchNames( Criteria<T> criteria );
}
