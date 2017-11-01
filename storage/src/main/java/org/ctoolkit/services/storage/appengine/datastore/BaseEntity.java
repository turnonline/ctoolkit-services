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
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import org.ctoolkit.services.storage.EntityIdentity;

import javax.annotation.Nullable;
import java.text.Normalizer;
import java.util.Date;

/**
 * The entity with common properties to all its children:
 * <ul>
 * <li><b>createdDate</b> - the date of entity creation, set only once</li>
 * <li><b>modificationDate</b> - the date of the last modification of the entity values</li>
 * <li><b>version</b> - </li>
 * <li><b>dbModelVersion</b> - the model update time in milliseconds, the date when the model has been first time used
 * in the code. The value is being hardcoded, evaluated by developer.</li>
 * </ul>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class BaseEntity<P extends EntityIdentity>
        implements EntityIdentity<P>
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
        return "BaseEntity{" +
                "modelVersion=" + dbModelVersion +
                ", version=" + version +
                ", modificationDate=" + modificationDate +
                ", createdDate=" + createdDate +
                '}';
    }
}


