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

package org.ctoolkit.services.common;

import com.google.appengine.api.datastore.Text;

/**
 * Internal helper class to convert input object to {@link String}.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
class StringConverter
        implements Converter<String>
{
    private static StringConverter INSTANCE;

    static StringConverter instance()
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new StringConverter();
        }

        return INSTANCE;
    }

    @Override
    public String convert( Object object )
    {
        if ( object == null )
        {
            return null;
        }
        else if ( object instanceof Text )
        {
            return ( ( Text ) object ).getValue();
        }

        return object.toString();
    }
}
