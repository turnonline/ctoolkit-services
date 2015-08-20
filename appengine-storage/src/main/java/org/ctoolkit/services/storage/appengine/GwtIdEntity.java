package org.ctoolkit.services.storage.appengine;

import com.googlecode.objectify.annotation.Id;
import org.ctoolkit.services.storage.EntityIdentity;

/**
 * The GWT compatible id entity. The @Id as 'id', type of <code>Long</code>. If 'id' is not set (null value),
 * will be set automatically by datastore engine.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class GwtIdEntity<P extends EntityIdentity>
        extends BaseEntity<P>
{
    private static final long serialVersionUID = 1L;

    @Id
    protected Long id;

    @Override
    public String getKey()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public Long getId()
    {
        return id;
    }

    /**
     * Manually sets the ID of this entity instance.
     *
     * @param id the instance ID to be set
     */
    protected void setId( Long id )
    {
        this.id = id;
    }

    @Override
    public String getKind()
    {
        return this.getClass().getSimpleName();
    }

    @Override
    public P getParent()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return "{id=" + id + "} " + super.toString();
    }
}
