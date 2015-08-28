package org.ctoolkit.services.storage.appengine.datastore;

import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.OnLoad;
import com.googlecode.objectify.annotation.OnSave;
import org.ctoolkit.services.storage.EntityIdentity;

import java.io.Serializable;
import java.util.Date;

/**
 * The entity with common properties to all its children:
 * <ul>
 * <li><b>createdDate</b> - the date of entity creation, set only once</li>
 * <li><b>updatedDate</b> - the date of entity update</li>
 * <li><b>version</b> - </li>
 * <li><b>dbModelVersion</b> - the model update time in milliseconds, the date when the model has been first time used
 * in the code. The value is being hardcoded, evaluated by developer.</li>
 * </ul>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class BaseEntity<P extends EntityIdentity>
        implements EntityIdentity<P>, Serializable
{
    private static final long serialVersionUID = 1L;

    private Integer version;

    @Index
    private Date createdDate;

    @Index
    private Date updatedDate;

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
     * Returns the date of entity update.
     *
     * @return the date of entity update
     */
    public Date getUpdatedDate()
    {
        return updatedDate;
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
     * This method is called before every update.
     * It evaluates <code>createdDate</code>or <code>updatedDate</code>.
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
            updatedDate = new Date();
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
                ", updatedDate=" + updatedDate +
                ", createdDate=" + createdDate +
                '}';
    }
}


