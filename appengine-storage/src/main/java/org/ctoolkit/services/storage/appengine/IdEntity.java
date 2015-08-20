package org.ctoolkit.services.storage.appengine;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import org.ctoolkit.services.storage.EntityIdentity;

/**
 * The objectified id entity (not GWT compatible). The @Id as 'id', type of <code>Long</code>. If 'id' is not set
 * (null value), will be set automatically by datastore engine.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class IdEntity<P extends EntityIdentity>
        extends GwtIdEntity<P>
{
    private static final long serialVersionUID = 1L;

    /**
     * Returns the unique string identification unique across all entities.
     *
     * @return the unique string identification
     */
    public String getKey()
    {
        if ( super.id == null )
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
    <T extends IdEntity> Ref<T> ref()
    {
        if ( super.id == null )
        {
            return null;
        }

        return Ref.create( ( T ) this );
    }

    @Override
    public String getKind()
    {
        return this.getClass().getSimpleName();
    }
}
