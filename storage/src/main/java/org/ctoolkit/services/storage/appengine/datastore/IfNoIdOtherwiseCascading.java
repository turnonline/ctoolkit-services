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

import com.googlecode.objectify.condition.If;
import org.ctoolkit.services.storage.ChildEntityOf;
import org.ctoolkit.services.storage.EntityIdentity;

/**
 * The {@link If} implementation where cascading save is supported. Once condition is met
 * either the {@link EntityIdentity#save()} or {@link EntityIdentity#save(EntityIdentity.Ignored)}
 * will be called. Deferred save is not supported.
 * <p>
 * Including support of the {@link EntityIdentity.HasIgnored} for more fine-grained control
 * over which entities (relationships) are going to be saved.
 * <p>
 * <b>For example</b>, the parent entity extends {@link BaseEntityIdentity}.
 * <pre>
 * {@code
 *  ParentEntity parent = new ParentEntity();
 *  parent.newCascading().ignore( "siblingChildEntity" )
 *          .addChild( "childEntity" )
 *          .ignore( "childEntity" );
 *
 *  parent.save();
 * }
 * </pre>
 * If the referenced entity implements {@link ChildEntityOf} the {@link ChildEntityOf#setParent(EntityIdentity)}
 * will be called in order to set enclosing POJO (object that holds the associated field) as a parent entity.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class IfNoIdOtherwiseCascading
        extends IfNoId
{
    @Override
    protected boolean isCascadingOn()
    {
        return true;
    }
}
