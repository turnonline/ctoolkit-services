/*
 * Copyright (c) 2019 Comvai, s.r.o. All Rights Reserved.
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

import javax.annotation.Nullable;

/**
 * Helps to manage conversion between general storage naming
 * and the one specific to the Google Storage.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public interface GoogleStorageAwareGeneralMapping
{
    String GS_PREFIX = "/gs/";

    /**
     * Converts the google storage name to the name that is more general,
     * excluding the {@link #GS_PREFIX} if present. If not, only passed through.
     *
     * @param gs the storage name either with prefix or not
     * @return the general storage name
     */
    default String general( @Nullable String gs )
    {
        String storageName = gs;
        if ( storageName != null && storageName.startsWith( GS_PREFIX ) )
        {
            storageName = storageName.substring( 4 );
        }

        return storageName;
    }

    /**
     * Enriches the specified storage name with {@link #GS_PREFIX}.
     * If already specified then only passed through.
     *
     * @param general the storage name either with prefix or not
     * @return the Google Storage specific name
     */
    default String gsAware( @Nullable String general )
    {
        String storageName = general;

        if ( general != null && !general.startsWith( GS_PREFIX ) )
        {
            if ( general.startsWith( "/" ) )
            {
                storageName = general.substring( 1 );
            }
            else
            {
                storageName = general;
            }
            storageName = GS_PREFIX + storageName;
        }

        return storageName;
    }
}
