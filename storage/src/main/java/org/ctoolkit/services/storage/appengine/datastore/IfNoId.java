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

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.condition.InitializeIf;
import com.googlecode.objectify.condition.PojoIf;
import org.ctoolkit.services.storage.ChildEntityOf;
import org.ctoolkit.services.storage.EntityIdentity;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Ignore save of the associated field if entity is not persisted yet ({@link EntityIdentity#getId()}
 * returns {@code null}). It tells to Objectify in cooperation with {@link IgnoreSave}
 * whether to save this field into datastore or not.
 * <p>
 * This implementation expects to have a sibling field annotated with {@link Ignore}
 * and named with the following rule: the name of the field where this is placed
 * with prefix 't' and changed first character of the source field name to upper case.
 * <p>
 * If the transient entity implements {@link ChildEntityOf} the {@link ChildEntityOf#setParent(EntityIdentity)}
 * will be called in order to set filed POJO as a parent entity.
 * <p>
 * App Engine Note: The Cloud Datastore API does not distinguish between creating a new entity
 * and updating an existing one. If the object's key represents an entity that already exists,
 * the put() method overwrites the existing entity.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class IfNoId
        extends PojoIf<EntityIdentity>
        implements InitializeIf
{
    private String transientFieldName;

    private Field field;

    private String fieldName;

    @Override
    public void init( ObjectifyFactory fact, Field field )
    {
        transientFieldName = transientFieldName( field );
        this.field = field;
        this.fieldName = field.getName();
    }

    @Override
    public boolean matchesPojo( EntityIdentity pojo )
    {
        Field tField;
        try
        {
            tField = pojo.getClass().getDeclaredField( transientFieldName );
            tField.setAccessible( true );
        }
        catch ( NoSuchFieldException e )
        {
            String msg = IfNoId.class.getSimpleName()
                    + " annotation expects transient sibling field declared with the name: "
                    + transientFieldName + " of type "
                    + EntityIdentity.class.getName();

            throw new RuntimeException( msg );
        }

        try
        {
            EntityIdentity tEntity = null;
            Ref dbRef = ( Ref ) field.get( pojo );

            // if entity is already persisted the instance from the reference is preferred
            if ( dbRef != null )
            {
                Object entity = dbRef.get();
                tEntity = ( EntityIdentity ) entity;
            }

            if ( tEntity == null )
            {
                // retrieving of the transient entity instance if not persisted yet
                tEntity = ( EntityIdentity ) tField.get( pojo );
            }

            // by default this is switched off, turn it on by overriding #isCascadingOn() method
            if ( isCascadingOn() )
            {
                boolean ignoreSave = pojo instanceof BaseEntityIdentity
                        && ( ( BaseEntityIdentity ) pojo ).isIgnoredField( fieldName );

                // Whether to ignore cascading save. It's configurable per method call.
                if ( !ignoreSave
                        && pojo.getId() != null
                        && tEntity instanceof ChildEntityOf )
                {
                    EntityIdentity parent = ( ( ChildEntityOf<? extends EntityIdentity, ?> ) tEntity ).getParent();
                    if ( parent == null || isAllowedChangeParentEntity() )
                    {
                        ( ( ChildEntityOf ) tEntity ).setParent( pojo );
                    }
                    else
                    {
                        if ( !Objects.equals( pojo.getKey(), parent.getKey() ) )
                        {
                            String msg = "Why do you try to change the parent of the child entity?";
                            throw new IllegalArgumentException( msg );
                        }
                    }

                    tEntity.save();
                    if ( tEntity.getId() == null )
                    {
                        String msg = "The ID is being expected to be set."
                                + " Deferred save is not supported with this annotation.";

                        throw new RuntimeException( msg );
                    }
                }
            }

            if ( tEntity != null && tEntity.getId() != null )
            {
                Ref<EntityIdentity> ref = Ref.create( tEntity );
                field.set( pojo, ref );

                // Either new or changed value, do NOT ignore -> perform save
                return false;
            }
            // If true it already has a value, do NOT ignore -> perform save.
            // Otherwise ignore save as there are no values (db, transient) at all.
            else return dbRef == null;
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Constructs the transient field name with prefix 't' and changed first character
     * of the source field name to upper case.
     *
     * @param field the source field name
     * @return the transient field name
     */
    private String transientFieldName( Field field )
    {
        String name = field.getName();
        char c = name.charAt( 0 );
        return "t" + String.valueOf( c ).toUpperCase() + name.substring( 1 );

    }

    /**
     * If transient entity is type of {@link ChildEntityOf} and this method returns true
     * the method {@link ChildEntityOf#save()} will be called.
     *
     * @return true to turn on cascading save
     */
    protected boolean isCascadingOn()
    {
        return false;
    }

    /**
     * Returns false to throw exception if parent entity has changed.
     */
    protected boolean isAllowedChangeParentEntity()
    {
        return false;
    }
}
