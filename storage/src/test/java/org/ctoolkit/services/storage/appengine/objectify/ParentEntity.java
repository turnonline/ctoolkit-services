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

package org.ctoolkit.services.storage.appengine.objectify;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.IgnoreSave;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * The parent entity for test purpose.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Entity
public class ParentEntity
        extends EntityLongIdentity
{
    private static final long serialVersionUID = 1L;

    @IgnoreSave( IfNoIdOtherwiseCascading.class )
    private Ref<ChildEntity> childEntity;

    @Ignore
    private ChildEntity tChildEntity;

    @IgnoreSave( IfNoIdOtherwiseCascading.class )
    private Ref<SiblingChildEntity> siblingChildEntity;

    @Ignore
    private SiblingChildEntity tSiblingChildEntity;

    @IgnoreSave( IfNoIdOtherwiseCascading.class )
    List<Ref<ChildEntity>> children;

    @Ignore
    private List<ChildEntity> tChildren = new ArrayList<>();

    public void add( ChildEntity entity )
    {
        tChildren.add( entity );
    }

    public void remove( ChildEntity entity )
    {
        tChildren.remove( entity );
    }

    public void clearChildren()
    {
        tChildren = null;
    }

    public List<ChildEntity> getChildren()
    {
        return fromCollectionOfRefs( children, tChildren );
    }

    ChildEntity getChildEntity()
    {
        return tChildEntity;
    }

    void setChildEntity( ChildEntity tChildEntity )
    {
        this.tChildEntity = tChildEntity;
    }

    public SiblingChildEntity getSiblingChildEntity()
    {
        return tSiblingChildEntity;
    }

    public void setSiblingChildEntity( SiblingChildEntity siblingChildEntity )
    {
        this.tSiblingChildEntity = siblingChildEntity;
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
