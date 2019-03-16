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

package org.ctoolkit.services.storage.appengine.objectify;

import com.google.api.client.util.DateTime;
import com.googlecode.objectify.annotation.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 * Extended {@link Timestamp} for testing purpose.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@Entity
public class TimestampEntity
        extends Timestamp
{
    TimestampEntity()
    {
    }

    public TimestampEntity( @Nonnull List<String> uniqueKey,
                            @Nonnull Date last )
    {
        super( uniqueKey, last );
    }

    public static TimestampEntity of( @Nonnull List<String> uniqueKey, @Nullable DateTime last )
    {
        return of( uniqueKey, last, TimestampEntity.class );
    }

    public static TimestampEntity of( @Nonnull List<String> uniqueKey, @Nullable Date last )
    {
        return of( uniqueKey, last, TimestampEntity.class );
    }
}
