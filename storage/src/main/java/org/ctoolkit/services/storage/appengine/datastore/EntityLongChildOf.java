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
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.OnSave;
import com.googlecode.objectify.annotation.Parent;
import org.ctoolkit.services.storage.ChildEntityOf;
import org.ctoolkit.services.storage.EntityIdentity;

import javax.annotation.Nonnull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The base objectify entity to be used in the client code with parent relationship implementation.
 * The @Id of the entity with type of <code>Long</code>. If 'Id' is not set (null value)
 * a numeric value will be generated for you using the standard App Engine allocator.
 * <p>
 * Along with the @Id and kind, the @Parent field defines the key (identity) of the entity.
 * The name of the field is irrelevant and can be changed at any time without modifying stored data.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class EntityLongChildOf<P extends EntityIdentity>
        extends EntityLongIdentity
        implements ChildEntityOf<P, Long>
{
    @Parent
    private Ref<P> parent;

    @Ignore
    private P tParent;

    @Override
    public P getParent()
    {
        if ( tParent != null )
        {
            return tParent;
        }
        else if ( parent != null )
        {
            return parent.get();
        }

        return null;
    }

    @Override
    public void setParent( @Nonnull P parent )
    {
        checkNotNull( parent );
        this.parent = Ref.create( parent );
        this.tParent = parent;
    }

    /**
     * Override if you need a customized error message thrown
     * in case of the missing parent instance checked right before the save.
     *
     * @return the error message
     */
    protected String missingParentErrorMessage()
    {
        return "The parent of " + this.getClass().getSimpleName() + " cannot be null!";
    }

    @OnSave
    private void onSave()
    {
        if ( parent == null )
        {
            throw new IllegalArgumentException( missingParentErrorMessage() );
        }
    }
}
