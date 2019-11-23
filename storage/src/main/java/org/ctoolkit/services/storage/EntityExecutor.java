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

package org.ctoolkit.services.storage;

import org.ctoolkit.services.storage.criteria.Criteria;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * The entity executor.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface EntityExecutor
{
    /**
     * Executes a query based on the given criteria and retrieves a list of objects.
     *
     * @param criteria a criteria holder {@link Criteria}
     * @param <T>      the concrete type of the entity
     * @return list of objects that meets the criteria
     */
    <T> List<T> list( @Nonnull Criteria<T> criteria );

    /**
     * Executes a query based on the given criteria and retrieves the first entity in the result list if any.
     * If the result does not match any criteria returns <code>null</code>
     *
     * @param criteria a criteria holder {@link Criteria}
     * @param <T>      the concrete type of the entity
     * @return the first entity in the result list
     */
    <T> T first( @Nonnull Criteria<T> criteria );

    /**
     * Count the total number of values in the result.  <em>limit</em> and <em>offset</em> are obeyed.
     * This is somewhat faster than fetching, but the time still grows with the number of results.
     * The datastore actually walks through the result set and counts for you.
     * <p>
     * WARNING:  Each counted entity is billed as a "datastore minor operation".  Even though these
     * are free, they may take significant time because they require an index walk.
     * <p>
     *
     * @param criteria a criteria holder {@link Criteria}
     * @param <T>      the type of the entity to be counted
     * @return the total number of values in the result
     */
    <T> int count( @Nonnull Criteria<T> criteria );

    /**
     * Executes a keys-only query based on given criteria and get the result as a list of entity names.
     * This is more efficient than fetching the actual full result set.
     *
     * @param criteria a criteria holder {@link Criteria}
     * @param <T>      the type of the entity to be queried
     * @return the list of long IDs (entity identification)
     */
    <T> List<Long> fetchIds( @Nonnull Criteria<T> criteria );

    /**
     * Executes a keys-only query based on given criteria and get the result as a list of entity names.
     * This is more efficient than fetching the actual full result set.
     *
     * @param criteria a criteria holder {@link Criteria}
     * @param <T>      the type of the entity to be queried
     * @return the list of string names (entity identification)
     */
    <T> List<String> fetchNames( @Nonnull Criteria<T> criteria );
}
