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

import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.condition.InitializeIf;
import com.googlecode.objectify.condition.PojoIf;
import org.ctoolkit.services.storage.ChildEntityOf;
import org.ctoolkit.services.storage.EntityIdentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * {@link IgnoreSave} of the associated field (reference to another entity) if that entity is not persisted yet;
 * {@link EntityIdentity#getId()} returns {@code null}. Additionally in case of the use {@link IfNoIdOtherwiseCascading}
 * the Objectify will be instructed in cooperation with {@link IgnoreSave} annotation whether to save transient field
 * into datastore or not.
 * <p>
 * This implementation expects to have a sibling field next to this field (a transient entity)
 * annotated with {@link Ignore} and named with the following rule: the name of the field  with prefix 't'
 * and changed the first character of the source field name to capital letter.
 * <p>
 * <b>For example</b>, entity extends {@link BaseEntityIdentity}.
 * <pre>
 * {@code
 * // reference to the entity, a relationship
 * @literal @IgnoreSave( {IfNoId.class} )
 *  private Ref<BillingAddress> billingAddress;
 *
 *  // transient entity
 * @literal @Ignore
 *  private BillingAddress tBillingAddress;
 *
 *  public BillingAddress getBillingAddress()
 *  {
 *      return BaseEntityIdentity#fromRef( billingAddress, tBillingAddress );
 *  }
 *
 * @literal @Override
 *  public void save()
 *  {
 *      if ( getId() == null )
 *      {
 *          // first, parent entity needs to saved itself without references (children has no IDs yet)
 *          ofy().save().entity( this ).now();
 *      }
 *
 *      // ... here save and manage relationships yourself (for better control) or use IfNoIdOtherwiseCascading
 *
 *      // now parent entity will be updated with children references
 *      ofy().save().entity( this ).now();
 *  }
 * }
 * </pre>
 * The referenced entity BillingAddress in this example is expected to be type of {@link EntityIdentity}.
 * <p>
 * <b>One to many cascading save support</b>
 * <pre>
 * {@code
 *
 *  // if you provide your concrete type of collection (for example ArrayList), type will be preserved
 * @literal @IgnoreSave( IfNoIdOtherwiseCascading.class )
 *  private List<Ref<Ingredient>> ingredients = new ArrayList<>();
 *
 *  // transient collection of entities
 *  // Note: the null collection of entities is an explicit mark to delete all datastore references
 * @literal @Ignore
 *  private List<Ingredient> tIngredients = new ArrayList<>();
 *
 *  public List<Ingredient> getIngredients()
 *  {
 *      return fromCollectionOfRefs( ingredients, tIngredients );
 *  }
 * }
 * </pre>
 * <b>Note:</b> if an entity has been removed from the collection comparing to the persisted one,
 * the {@link EntityIdentity#delete()} will be called at missing instance. Make sure equals and hashCode
 * methods are properly implemented.
 *
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 * @see IfNoIdOtherwiseCascading
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
            String msg = getClass().getSimpleName()
                    + " annotation expects transient sibling field declared with the name: "
                    + transientFieldName + " of type "
                    + EntityIdentity.class.getName();

            throw new RuntimeException( msg );
        }

        try
        {
            Object fieldObject = field.get( pojo );
            boolean ignoreSave;

            if ( Collection.class.isAssignableFrom( tField.getType() ) )
            {
                @SuppressWarnings( "unchecked" )
                Collection<Ref<EntityIdentity>> collectionOfRefs = ( Collection<Ref<EntityIdentity>> ) fieldObject;
                ignoreSave = populateField( pojo, tField, collectionOfRefs );
            }
            else
            {
                ignoreSave = populateField( pojo, tField, ( Ref ) fieldObject );
            }

            return ignoreSave;
        }
        catch ( IllegalAccessException e )
        {
            throw new RuntimeException( e );
        }
    }

    private boolean populateField( @Nonnull EntityIdentity pojo,
                                   @Nonnull Field tField,
                                   @Nullable Ref dbRef )
            throws IllegalAccessException
    {
        EntityIdentity tEntity = null;

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

        // by default cascading is switched off, turn it on by overriding #isCascadingOn() method
        if ( isCascadingOn() && tEntity != null )
        {
            cascadingSave( pojo, tEntity );
        }

        if ( tEntity != null && tEntity.getId() != null )
        {
            Ref<EntityIdentity> ref = Ref.create( tEntity );
            field.set( pojo, ref );

            // Either new or changed value, do NOT ignore -> perform save
            return false;
        }
        // If false it already has a value, do NOT ignore -> perform save.
        // Otherwise ignore save as there are no values (db, transient) at all.
        else return dbRef == null;
    }

    private boolean populateField( @Nonnull EntityIdentity pojo,
                                   @Nonnull Field tField,
                                   @Nullable Collection<Ref<EntityIdentity>> dbCollectionOfRefs )
            throws IllegalAccessException
    {
        // retrieving the collection of transient entities, it represents the most current state
        @SuppressWarnings( "unchecked" )
        Collection<EntityIdentity> tCollectionOfEntities = ( Collection<EntityIdentity> ) tField.get( pojo );

        // by default cascading is switched off, turn it on by overriding #isCascadingOn() method
        if ( isCascadingOn() && tCollectionOfEntities != null )
        {
            for ( EntityIdentity tEntity : tCollectionOfEntities )
            {
                if ( cascadingSave( pojo, tEntity ) )
                {
                    // cascading save operation has been configured to be ignored
                    break;
                }
            }
        }

        if ( dbCollectionOfRefs == null
                && tCollectionOfEntities != null
                && !tCollectionOfEntities.isEmpty() )
        {
            dbCollectionOfRefs = newCollection( tCollectionOfEntities );
        }

        if ( dbCollectionOfRefs != null )
        {
            boolean allowDeletion;
            if ( tCollectionOfEntities == null )
            {
                allowDeletion = true;
                tCollectionOfEntities = new ArrayList<>();
            }
            else
            {
                allowDeletion = !tCollectionOfEntities.isEmpty();
            }

            // Some of the item could be removed in meantime, thus first remove the items
            // not presented in the current list. Ref collection represents datastore state.
            Iterator<Ref<EntityIdentity>> iterator = dbCollectionOfRefs.iterator();
            Ref<EntityIdentity> originItem;

            while ( allowDeletion && iterator.hasNext() )
            {
                originItem = iterator.next();
                EntityIdentity originEntity = originItem.get();
                if ( !tCollectionOfEntities.contains( originEntity ) )
                {
                    iterator.remove();
                    if ( isCascadingOn() && originEntity != null )
                    {
                        originEntity.delete();
                    }
                }
            }

            Ref<EntityIdentity> tEntityRef;
            for ( EntityIdentity next : tCollectionOfEntities )
            {
                if ( next.getId() == null )
                {
                    // not saved yet, configured to be ignored
                    continue;
                }

                tEntityRef = Ref.create( next );
                if ( !dbCollectionOfRefs.contains( tEntityRef ) )
                {
                    dbCollectionOfRefs.add( tEntityRef );
                }
            }

            field.set( pojo, dbCollectionOfRefs );
            // Either new or changed value, do NOT ignore -> perform save
            return false;
        }

        // there are no values (db, transient) at all.
        return true;
    }

    private Collection<Ref<EntityIdentity>> newCollection( Collection type )
    {
        if ( type instanceof List )
        {
            return new ArrayList<>();
        }
        else if ( type instanceof Set )
        {
            return new HashSet<>();
        }
        else
        {
            throw new IllegalArgumentException( "Unsupported collection type: " + type.getClass() );
        }
    }

    private boolean cascadingSave( @Nonnull EntityIdentity pojo, @Nonnull EntityIdentity tEntity )
    {
        EntityIdentity.HasIgnored hasIgnored = null;
        if ( pojo instanceof EntityIdentity.HasIgnored )
        {
            hasIgnored = ( EntityIdentity.HasIgnored ) pojo;
        }

        boolean ignoreSave = hasIgnored != null
                && hasIgnored.cascading().isIgnored( fieldName );

        // Whether to ignore cascading save. It's configurable per method call.
        if ( !ignoreSave && pojo.getId() != null )
        {
            if ( tEntity instanceof ChildEntityOf )
            {
                @SuppressWarnings( "unchecked" )
                ChildEntityOf<? super EntityIdentity, ?> tChildEntity = ( ChildEntityOf ) tEntity;
                tChildEntity.setParent( pojo );
            }

            if ( hasIgnored == null )
            {
                tEntity.save();
            }
            else
            {
                EntityIdentity.Ignored ignored = hasIgnored.cascading().search( fieldName );
                tEntity.save( ignored );
            }
            if ( tEntity.getId() == null )
            {
                String msg = "The ID is being expected to be set."
                        + " Deferred save is not supported with this annotation.";

                throw new RuntimeException( msg );
            }
        }
        return ignoreSave;
    }

    /**
     * Constructs the transient field name with prefix 't' and changed first character
     * of the source field name to capital letter.
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
     * The boolean indicating whether transient entity should be evaluated for cascading save.
     * If all conditions are met the {@link EntityIdentity#save()} will be called.
     *
     * @return true to turn on cascading save
     */
    protected boolean isCascadingOn()
    {
        return false;
    }
}
