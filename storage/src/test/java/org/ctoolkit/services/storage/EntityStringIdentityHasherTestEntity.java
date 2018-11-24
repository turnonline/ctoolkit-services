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

package org.ctoolkit.services.storage;

import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.googlecode.objectify.annotation.Entity;
import org.ctoolkit.services.storage.appengine.objectify.EntityStringIdentityHasher;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Concrete {@link EntityStringIdentityHasher} entity impl for testing and demonstration purpose.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Entity
public class EntityStringIdentityHasherTestEntity
        extends EntityStringIdentityHasher
{
    private static final long serialVersionUID = -79109897537646409L;

    private String xyz;

    @Override
    public String calcPropsHashCode()
    {
        Funnel<EntityStringIdentityHasherTestEntity> funnel;
        funnel = ( Funnel<EntityStringIdentityHasherTestEntity> ) ( item, into ) -> {
            if ( item.getXyz() != null )
            {
                into.putString( item.getXyz(), Charsets.UTF_8 );
            }
        };

        Hasher hasher = Hashing.sha256().newHasher();
        hasher = hasher.putObject( this, funnel );

        return hasher.hash().toString();
    }

    @Override
    protected PropertiesHashCode newPropertiesHashCode()
    {
        return new MyHashCode();
    }

    public String getXyz()
    {
        return xyz;
    }

    public void setXyz( String xyz )
    {
        this.xyz = xyz;
    }

    @Override
    protected long getModelVersion()
    {
        //21.10.2017 08:00:00 GMT+0200
        return 1508565600000L;
    }

    @Override
    public void save()
    {
        ofy().transact( () -> ofy().save().entity( this ).now() );
    }

    @Override
    public void delete()
    {
        ofy().transact( () -> ofy().delete().entity( this ).now() );
    }

    /**
     * Annotated implementation of the {@link PropertiesHashCode}, for testing and demonstration purpose only.
     */
    @Entity( name = "Entity_String_HashCode" )
    public static class MyHashCode
            extends PropertiesHashCode
    {
        private static final long serialVersionUID = -9126064441435013244L;
    }
}
