/*
 * Copyright (c) 2018 Comvai, s.r.o. All Rights Reserved.
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

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.OnSave;
import org.ctoolkit.services.storage.PropertiesHashCode;
import org.ctoolkit.services.storage.PropertiesHasher;

/**
 * The base objectify entity to be used in the client code that implements
 * {@link PropertiesHasher} with basic implementation in order to simplify client code.
 * <p>
 * Once used, the client is being required to implement only {@link PropertiesHasher#calcPropsHashCode()}
 * and {@link EntityStringIdentityHasher#newPropertiesHashCode()}.
 * <p>
 * It keeps the reference to the client specified implementation of {@link PropertiesHashCode}
 * in property {@link #hashCode}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class EntityStringIdentityHasher
        extends EntityStringIdentity
        implements PropertiesHasher
{
    private Ref<PropertiesHashCode> hashCode;

    @Override
    public final PropertiesHashCode getPropsHashCode()
    {
        return hashCode == null ? null : hashCode.get();
    }

    @OnSave
    private void onSave()
    {
        if ( hashCode == null )
        {
            PropertiesHashCode hashCode = newPropertiesHashCode();
            hashCode.save();
            this.hashCode = Ref.create( hashCode );
        }
    }

    /**
     * Returns new instance of the {@link PropertiesHashCode}
     *
     * @return the new instance of the properties hashcode
     */
    protected abstract PropertiesHashCode newPropertiesHashCode();
}
