package org.ctoolkit.services.storage;

/**
 * The entity identity abstraction.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public interface EntityIdentity<P extends EntityIdentity>
{
    /**
     * Returns the unique string identification unique across all entities of all kinds and parents.
     *
     * @return the unique string identification
     */
    String getKey();

    /**
     * Returns the name (type of <code>String</code>) identification
     * unique only for entities with the same kind and parent.
     *
     * @return the name associated with this entity, or null if has an id
     */
    String getName();

    /**
     * Returns the id (type of <code>Long</code>) identification unique only for entities with the same kind and parent.
     *
     * @return the id associated with this entity, or null if has a name
     */
    Long getId();

    /**
     * Returns the datastore kind of this entity.
     *
     * @return the datastore kind associated with this entity
     */
    String getKind();

    /**
     * Returns the version of this entity instance. Incremented on save.
     *
     * @return the version of this entity instance
     */
    Integer getVersion();

    /**
     * Returns the parent identification of this entity instance, or null if there is no parent.
     *
     * @return the parent identification
     */
    P getParent();

    /**
     * Sets the parent identification of this entity instance, or null if there is no parent.
     *
     * @param parent the parent instance to be set
     */
    void setParent( P parent );
}
