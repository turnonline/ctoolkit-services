package org.ctoolkit.services.storage.appengine.datastore;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import org.ctoolkit.services.storage.EntityIdentity;

/**
 * The objectified name entity (not GWT compatible). The @Id as 'name', type of <code>String</code>.
 * The 'name' must be set manually, it's application responsibility to make it unique.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Entity
public abstract class NameEntity<P extends EntityIdentity>
        extends BaseEntity<P>
{
    private static final long serialVersionUID = 1L;

    @Id
    protected String name;

    /**
     * Constructs a new instance.
     */
    public NameEntity()
    {
    }

    /**
     * Constructs a new instance with given NAME as instance identification.
     *
     * @param name the instance NAME to be set
     */
    public NameEntity( String name )
    {
        this.name = name;
    }

    @Override
    public Long getId()
    {
        return null;
    }

    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Manually sets the NAME of this entity instance.
     *
     * @param name the instance NAME to be set
     */
    protected void setName( String name )
    {
        this.name = name;
    }

    @Override
    public String getKey()
    {
        if ( this.name == null )
        {
            return null;
        }

        return Key.create( this ).getString();
    }

    @Override
    public String getKind()
    {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns the objectify reference of this instance.
     *
     * @return the objectify reference
     */
    @SuppressWarnings( "unchecked" )
    <T extends NameEntity> Ref<T> ref()
    {
        if ( this.name == null )
        {
            return null;
        }

        return Ref.create( ( T ) this );
    }

    @Override
    public String toString()
    {
        return "{name=" + name + "} " + super.toString();
    }
}