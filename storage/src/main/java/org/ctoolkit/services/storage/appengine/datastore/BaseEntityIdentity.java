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

import com.google.common.base.Strings;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import org.ctoolkit.services.storage.EntityIdentity;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.Date;

/**
 * The objectify entity with common properties to all its children:
 * <ul>
 * <li><b>createdDate</b> - Indexed; the date of entity creation, set only once</li>
 * <li><b>modificationDate</b> - Indexed; the date of the last modification of the entity values</li>
 * <li><b>version</b> - the number of how many times the entity has been updated</li>
 * <li><b>dbModelVersion</b> - Indexed; the model update time in milliseconds, the date when the model
 * has been first time used in the code. The value is being hardcoded, evaluated by developer.</li>
 * </ul>
 *
 * @param <ID_TYPE> the type of the ID of this entity, supported generic values are {@link Long} and {@link String}.
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class BaseEntityIdentity<ID_TYPE>
        implements EntityIdentity<ID_TYPE>
{
    private Integer version;

    @Index
    private Date createdDate;

    @Index
    private Date modificationDate;

    /**
     * The model version persisted in datastore as a time stamp.
     */
    @Index
    private Date dbModelVersion;

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

    /**
     * Returns the objectify reference of this instance.
     *
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
     *
     * @return the date of the last modification
     */
    public Date getModificationDate()
    {
        return modificationDate;
    }

    /**
     * Normalize string value.
     * <b>Example:</b><code> Kľačany Village</code> -> <code>klacany village</code>
     *
     * @param value the string to be normalized
     * @return the normalized string or empty string for null input value
     */
    public String normalize( @Nullable String value )
    {
        if ( Strings.isNullOrEmpty( value ) )
        {
            return "";
        }
        String normalized = Normalizer.normalize( value.toLowerCase(), Normalizer.Form.NFD );
        return normalized.replaceAll( "[^\\p{ASCII}]", "" );
    }

    /**
     * Returns instance taken either fom the entity reference or default instance passed as a second parameter.
     *
     * @param ref             the entity reference
     * @param defaultInstance the default fallback instance if reference is null
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
}


