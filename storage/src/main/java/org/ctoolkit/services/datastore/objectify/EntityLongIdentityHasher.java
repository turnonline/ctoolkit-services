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

package org.ctoolkit.services.datastore.objectify;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.OnSave;
import org.ctoolkit.services.storage.PropertiesHashCode;
import org.ctoolkit.services.storage.PropertiesHasher;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * The base objectify entity to be used in the client code that implements
 * {@link PropertiesHasher} with basic implementation in order to simplify client code.
 * <p>
 * Once used, the client is being required to implement only {@link PropertiesHasher#calcPropsHashCode()}
 * and {@link EntityLongIdentityHasher#newPropertiesHashCode()}.
 * <p>
 * It keeps the reference to the client specified implementation of {@link PropertiesHashCode}
 * in property {@link #hashCode}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class EntityLongIdentityHasher
        extends EntityLongIdentity
        implements PropertiesHasher
{
    private static final long serialVersionUID = 7444984756468293559L;

    private Key<PropertiesHashCode> hashCode;

    /**
     * Check whether client selected properties are ready to be calculated.
     * By default it returns {@code true}, override for customized behavior.
     * <p>
     * If this returns {@code false}, call to the {@link PropertiesHasher#hashCodeSnapshot()}
     * will not persist anything and method itself will return {@code false} indicating call has been ignored.
     *
     * @return true if ready to calculate
     */
    public boolean isPropertiesReady()
    {
        return true;
    }

    @Override
    public final PropertiesHashCode getPropsHashCode()
    {
        if ( !isPropertiesReady() )
        {
            return null;
        }
        return hashCode == null ? null : ofy().load().key( hashCode ).now();
    }

    @OnSave
    private void onSave()
    {
        if ( hashCode == null )
        {
            PropertiesHashCode hashCode = newPropertiesHashCode();
            hashCode.save();
            this.hashCode = Key.create( hashCode );
        }
    }

    /**
     * Returns new instance of the {@link PropertiesHashCode}
     *
     * @return the new instance of the properties hashcode
     */
    protected abstract PropertiesHashCode newPropertiesHashCode();
}
