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

package org.ctoolkit.services.storage.appengine.datastore;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.IgnoreSave;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * The entity for test purpose with parent/child relationship.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Entity
public class ChildEntity
        extends EntityLongChildOf<ParentEntity>
{
    @IgnoreSave( IfNoIdOtherwiseCascading.class )
    private Ref<Child2LevelEntity> childEntity;

    @Ignore
    private Child2LevelEntity tChildEntity;

    Child2LevelEntity getChildEntity()
    {
        return tChildEntity;
    }

    void setChildEntity( Child2LevelEntity tChildEntity )
    {
        this.tChildEntity = tChildEntity;
    }

    @Override
    protected long getModelVersion()
    {
        return 1;
    }

    @Override
    public void save()
    {
        if ( getId() == null )
        {
            ofy().save().entity( this ).now();
        }
        ofy().save().entity( this ).now();
    }

    @Override
    public void delete()
    {
    }
}