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

package org.ctoolkit.services.datastore.objectify;

import com.googlecode.objectify.condition.PojoIf;

/**
 * The implementation of the {@link com.googlecode.objectify.condition.If} that defines a condition
 * to test whether {@link BaseEntityIdentity#createdDate} should be in indexed
 * in the datastore or not.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see IndexCreatedDate
 */
class CreatedDateIf
        extends PojoIf<BaseEntityIdentity>
{
    @Override
    public boolean matchesPojo( BaseEntityIdentity pojo )
    {
        return pojo instanceof IndexCreatedDate;
    }
}
