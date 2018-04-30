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

import com.google.common.base.Strings;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.IgnoreSave;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import org.ctoolkit.services.storage.EntityIdentity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The objectify entity with common properties to all its children (unindexed):
 * <ul>
 * <li><b>createdDate</b> - the date of entity creation, set only once</li>
 * <li><b>modificationDate</b> - the date of the last modification of the entity values</li>
 * <li><b>version</b> - the number of how many times the entity has been updated</li>
 * <li><b>dbModelVersion</b> - the model update time in milliseconds, the date when the model
 * has been first time used in the code. The value is being hardcoded, evaluated by developer.</li>
 * </ul>
 * <b>Objectify Note:</b> Do not index properties with monotonically increasing values
 * (such as a NOW() timestamp). Maintaining such an index could lead to hotspots that impact
 * Cloud Datastore latency for applications with high read and write rates.
 * <p>
 * If the <b>createdDate</b>, <b>modificationDate</b> properties needs to be indexed
 * let implement your entity either {@link IndexCreatedDate}, or {@link IndexModificationDate}, or both.
 * These marker interfaces will instruct Objectify to index requested properties.
 *
 * <b>App Engine Note:</b> The Cloud Datastore API does not distinguish between creating a new entity
 * and updating an existing one. If the object's key represents an entity that already exists,
 * the put() method overwrites the existing entity.
 *
 * @param <ID_TYPE> the type of the ID of this entity, supported generic values are {@link Long} and {@link String}.
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class BaseEntityIdentity<ID_TYPE>
        implements EntityIdentity<ID_TYPE>, EntityIdentity.HasIgnored
{
    private static final long serialVersionUID = 3420341402350232214L;

    private Integer version;

    @Index( CreatedDateIf.class )
    private Date createdDate;

    @Index( ModificationDateIf.class )
    private Date modificationDate;

    /**
     * The model version persisted in datastore as a time stamp.
     */
    private Date dbModelVersion;

    @IgnoreSave
    private Ignored ignoredFields;

    public Integer getVersion()
    {
        if ( version == null )
        {
            // to be backward compatible
            version = 1;
        }

        return version;
    }

    @Override
    public String getKey()
    {
        if ( getId() == null )
        {
            return null;
        }

        return Key.create( this ).getString();
    }

    @Override
    public Set<Ignored> save( @Nullable Ignored ignored )
    {
        this.ignoredFields = ignored;
        save();

        return ignored == null ? null : ignored.children();
    }

    @Override
    public void save( @Nonnull String ignored, String... moreIgnored )
    {
        save( createIgnored().ignore( ignored, moreIgnored ) );
    }

    /**
     * Returns the objectify reference of this instance.
     *
     * @param <T> the concrete type of the entity
     * @return the objectify reference
     */
    @SuppressWarnings( "unchecked" )
    public <T extends BaseEntityIdentity> Ref<T> ref()
    {
        if ( getId() == null )
        {
            return null;
        }

        return Ref.create( ( T ) this );
    }

    /**
     * Returns the objectify entity key representation.
     *
     * @param <T> the concrete type of the entity
     * @return the objectify entity key
     */
    @SuppressWarnings( "unchecked" )
    public <T extends BaseEntityIdentity> Key<T> entityKey()
    {
        if ( getId() == null )
        {
            return null;
        }

        return Key.create( ( T ) this );
    }

    /**
     * Returns the date of entity creation.
     *
     * @return the date of entity creation
     */
    public Date getCreatedDate()
    {
        return createdDate;
    }

    /**
     * Returns the date of the last modification of the entity values.
     * If the value is equal to {@link #getCreatedDate()} the entity has not been updated yet
     * and {@link #getVersion()} returns 1.
     *
     * @return the date of the last modification
     */
    public Date getModificationDate()
    {
        return modificationDate;
    }

    /**
     * Normalize string value.
     * <b>Example:</b>
     * <p>
     * {@code Kľačany Village -> klacany village}
     *
     * @param value the string to be normalized
     * @return the normalized string or {@code null}
     */
    public String normalize( @Nullable String value )
    {
        if ( Strings.isNullOrEmpty( value ) )
        {
            return null;
        }
        String normalized = Normalizer.normalize( value.toLowerCase(), Normalizer.Form.NFD );
        return normalized.replaceAll( "[^\\p{ASCII}]", "" );
    }

    /**
     * Returns instance taken either fom the entity reference or default instance passed as a second parameter.
     *
     * @param ref             the entity reference
     * @param defaultInstance the default fallback instance if reference is null
     * @param <T>             the concrete type of the entity
     * @return the entity instance
     */
    public <T> T fromRef( @Nullable Ref<T> ref, @Nullable T defaultInstance )
    {
        if ( ref == null )
        {
            return defaultInstance;
        }
        return ref.get();
    }

    /**
     * Populates the list from the datastore references.
     *
     * @param collectionOfRefs      the collection of entity references as a source for the result
     * @param tCollectionOfEntities the empty collection to be populated, non empty will be passed back
     * @param <T>                   the concrete type of the entity
     * @return the populated list from the references or empty one
     */
    public <T> List<T> fromListOfRefs( @Nullable List<Ref<T>> collectionOfRefs,
                                       @Nullable List<T> tCollectionOfEntities )
    {
        return fromCollectionOfRefs( collectionOfRefs, tCollectionOfEntities, new ArrayList<T>() );
    }

    /**
     * Populates the set from the datastore references.
     *
     * @param collectionOfRefs      the collection of entity references as a source for the result
     * @param tCollectionOfEntities the empty collection to be populated, non empty will be passed back
     * @param <T>                   the concrete type of the entity
     * @return the populated set from the references or empty one
     */
    public <T> Set<T> fromSetOfRefs( @Nullable Set<Ref<T>> collectionOfRefs,
                                     @Nullable Set<T> tCollectionOfEntities )
    {
        return fromCollectionOfRefs( collectionOfRefs, tCollectionOfEntities, new HashSet<T>() );
    }

    /**
     * Populates the collection from the datastore references.
     *
     * @param collectionOfRefs      the collection of entity references as a source for the result
     * @param tCollectionOfEntities the empty collection to be populated, non empty will be passed back
     * @param defaultCollection     the default collection to be used if tCollectionOfEntities is {@code null}
     * @param <T>                   the concrete type of the entity
     * @return the populated collection from the references or default collection
     */
    public <T, C extends Collection<T>> C fromCollectionOfRefs( @Nullable Collection<Ref<T>> collectionOfRefs,
                                                                @Nullable C tCollectionOfEntities,
                                                                @Nonnull C defaultCollection )
    {
        checkNotNull( defaultCollection );

        if ( tCollectionOfEntities != null && !tCollectionOfEntities.isEmpty() )
        {
            return tCollectionOfEntities;
        }

        tCollectionOfEntities = tCollectionOfEntities == null ? defaultCollection : tCollectionOfEntities;

        if ( collectionOfRefs != null )
        {
            for ( Ref<T> ref : collectionOfRefs )
            {
                T entity = checkNotNull( ref.get() );
                if ( !tCollectionOfEntities.contains( entity ) )
                {
                    tCollectionOfEntities.add( entity );
                }
            }
        }
        return tCollectionOfEntities;
    }

    @Override
    public String getKind()
    {
        return this.getClass().getSimpleName();
    }

    /**
     * Checks whether instance schema needs to be migrated or not.
     *
     * @return true if instance schema needs to be migrated
     */
    public boolean migrate()
    {
        // check actual model version against persisted modelVersion
        return getModelVersion() > dbModelVersion.getTime();
    }

    /**
     * This method will be called before every update.
     * It evaluates <code>createdDate</code>or <code>modificationDate</code>.
     */
    @OnSave
    private void onSave()
    {
        if ( getModelVersion() <= 0 )
        {
            throw new IllegalArgumentException( "dbModelVersion must be specified!!" );
        }

        if ( version == null )
        {
            version = 1;
        }

        if ( createdDate == null )
        {
            dbModelVersion = new Date( getModelVersion() );
            createdDate = new Date();
            modificationDate = createdDate;
        }
        else
        {
            modificationDate = new Date();
            version++;
        }
    }

    @OnLoad
    private void onLoad()
    {
        if ( dbModelVersion == null )
        {
            // for backward compatibility, this will mark instance schema needed to be migrated
            dbModelVersion = new Date( 1L );
        }
        else if ( migrate() )
        {
            String message = getToString() + "{ getModelVersion() = " + getModelVersion() + " }";
            System.err.println( "Instance needs to be migrated: " + message );
        }
    }

    /**
     * The model update time in milliseconds, the date when the model has been first time used in the code.
     * The value is being hardcoded, evaluated by developer.
     *
     * @return the model update time in milliseconds
     */
    protected abstract long getModelVersion();

    @Override
    public String toString()
    {
        return getToString();
    }

    private String getToString()
    {
        return "BaseEntityIdentity{" +
                "modelVersion=" + dbModelVersion +
                ", version=" + version +
                ", modificationDate=" + modificationDate +
                ", createdDate=" + createdDate +
                '}';
    }

    @Override
    public Ignored createIgnored()
    {
        return new IgnoredFieldsHash();
    }

    @Override
    public Ignored newCascading()
    {
        ignoredFields = createIgnored();
        return ignoredFields;
    }

    @Override
    public Ignored cascading()
    {
        if ( ignoredFields == null )
        {
            ignoredFields = createIgnored();
        }
        return ignoredFields;
    }

    private static class IgnoredFieldsHash
            extends HashSet<String>
            implements Ignored
    {
        private static final long serialVersionUID = -2365946383963083213L;

        @SuppressWarnings( "NonSerializableFieldInSerializableClass" )
        private Set<Ignored> children;

        private String fieldName;

        IgnoredFieldsHash()
        {
            children = new HashSet<>();
        }

        IgnoredFieldsHash( String fieldName )
        {
            this();
            this.fieldName = checkNotNull( fieldName );
        }

        @Override
        public Ignored ignore( @Nonnull String fieldName, String... fieldNames )
        {
            checkNotNull( fieldName, "The entity field name to be ignored cannot be null." );
            super.add( fieldName );

            for ( String next : fieldNames )
            {
                checkNotNull( next, "Any of the field name to be ignored cannot be null." );
                super.add( next );
            }
            return this;
        }

        @Override
        public boolean isIgnored( @Nonnull String fieldName )
        {
            checkNotNull( fieldName );
            return super.contains( fieldName );
        }

        @Override
        public Ignored addChild( @Nonnull String fieldName )
        {
            IgnoredFieldsHash level = new IgnoredFieldsHash( fieldName );
            children.add( level );

            return level;
        }

        @Override
        public String getFieldName()
        {
            return fieldName;
        }

        @Override
        public Ignored search( @Nonnull String fieldName )
        {
            checkNotNull( fieldName );

            for ( Ignored next : children )
            {
                if ( fieldName.equals( next.getFieldName() ) )
                {
                    return next;
                }
            }
            return null;
        }

        @Override
        public Set<Ignored> children()
        {
            return children;
        }
    }
}


