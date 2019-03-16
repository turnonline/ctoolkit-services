package org.ctoolkit.services.storage.appengine.objectify;

import com.google.api.client.util.DateTime;
import com.google.common.base.Joiner;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Entity that keeps latest timestamp of a resource processing identified by unique key.
 * <p>
 * <strong>Example:</strong>
 * <pre>
 * Timestamp timestamp = Timestamp.of( uniqueKey, order.getModificationDate() );
 * if ( timestamp.isObsolete() )
 * {
 *     // act based on the result
 * }
 *
 * // once you are done, call end() (best within transaction)
 * timestamp.end();
 *
 * // Timestamp with specified concrete entity type
 * TimestampEntity timestamp = Timestamp.of( uniqueKey, order.getModificationDate(), TimestampEntity.class );
 *
 * ...
 *
 * // Feel free to implement own static convenient helper methods, for example:
 *
 * &#64;Entity
 * public class TimestampEntity
 *         extends Timestamp
 * {
 *     TimestampEntity()
 *     {
 *         // non private no arg constructor for Objectify
 *     }
 *
 *     public TimestampEntity( &#64;Nonnull List<String> uniqueKey, &#64;Nonnull Date last )
 *     {
 *         super( uniqueKey, last );
 *     }
 *
 *     public static TimestampEntity of( &#64;Nonnull List<String> uniqueKey, &#64;Nullable DateTime last )
 *     {
 *         return of( uniqueKey, last, TimestampEntity.class );
 *     }
 *
 *     public static TimestampEntity of( &#64;Nonnull List<String> uniqueKey, &#64;Nullable Date last )
 *     {
 *         return of( uniqueKey, last, TimestampEntity.class );
 *     }
 * }
 *
 * </pre>
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public abstract class Timestamp
{
    private static final String KEY_SEPARATOR = "::";

    @Id
    private String name;

    private Date lastModification;

    @Ignore
    private Date incoming;

    @SuppressWarnings( "unused" )
    Timestamp()
    {
    }

    /**
     * Constructor.
     * <p>
     * Sets the last modification date of the original resource.
     * It will be used to distinguish whether an incoming changes are obsolete or not.
     *
     * @param uniqueKey the resource unique key as a list of IDs
     * @param last      the last modification date of incoming resource
     */
    public Timestamp( @Nonnull List<String> uniqueKey, @Nonnull Date last )
    {
        this.name = uniqueKey( uniqueKey );
        this.lastModification = checkNotNull( last );
    }

    /**
     * Get timestamp for specified unique key.
     *
     * @param uniqueKey the resource unique key as a list of IDs
     * @param last      the last modification date of incoming resource, {@code null} for now
     * @return the timestamp
     */
    public static Timestamp of( @Nonnull List<String> uniqueKey, @Nullable DateTime last )
    {
        return of( uniqueKey, last == null ? new Date() : new Date( last.getValue() ), Timestamp.class );
    }

    /**
     * Get timestamp for specified unique key.
     *
     * @param uniqueKey the resource unique key as a list of IDs
     * @param last      the last modification date of incoming resource, {@code null} for now
     * @param target    the entity target type, a type that will be stored in datastore
     * @return the timestamp
     */
    public static <T extends Timestamp> T of( @Nonnull List<String> uniqueKey,
                                              @Nullable DateTime last,
                                              @Nonnull Class<T> target )
    {
        return of( uniqueKey, last == null ? new Date() : new Date( last.getValue() ), target );
    }

    /**
     * Get timestamp for specified unique key.
     *
     * @param uniqueKey the resource unique key as a list of IDs
     * @param last      the last modification date of incoming resource, {@code null} for now
     * @return the timestamp
     */
    public static Timestamp of( @Nonnull List<String> uniqueKey, @Nullable Date last )
    {
        return of( uniqueKey, last, Timestamp.class );
    }

    /**
     * Get timestamp for specified unique key.
     *
     * @param uniqueKey the resource unique key as a list of IDs
     * @param last      the last modification date of incoming resource, {@code null} for now
     * @param target    the entity target type, a type that will be stored in datastore
     * @return the timestamp
     */
    public static <T extends Timestamp> T of( @Nonnull List<String> uniqueKey,
                                              @Nullable Date last,
                                              @Nonnull Class<T> target )
    {
        checkNotNull( target, "Target entity type is mandatory" );

        String key = uniqueKey( uniqueKey );
        T timestamp = ofy().load().type( target ).id( key ).now();

        last = last == null ? new Date() : last;
        if ( timestamp == null )
        {
            try
            {
                // -1 only used while it's not saved
                Date ld = new Date( last.getTime() - 1 );
                timestamp = target.getConstructor( List.class, Date.class ).newInstance( uniqueKey, ld );
            }
            catch ( NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e )
            {
                throw new IllegalArgumentException( e );
            }
        }

        timestamp.setIncoming( last );
        return timestamp;
    }

    private static String uniqueKey( @Nonnull List<String> uniqueKey )
    {
        if ( uniqueKey.isEmpty() )
        {
            throw new IllegalArgumentException( "Timestamp identification cannot be empty" );
        }
        return Joiner.on( KEY_SEPARATOR ).join( uniqueKey );
    }

    /**
     * Returns the entity identification.
     *
     * @return the entity identification
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the boolean indication whether modification date represents an obsolete changes.
     * Same incoming date as {@link #lastModification} is considered as obsolete changes.
     *
     * @return true if incoming changes are obsolete
     */
    public boolean isObsolete()
    {
        checkNotNull( incoming, "Modification date of incoming resource cannot be null" );
        return lastModification.equals( incoming ) || lastModification.after( incoming );
    }

    /**
     * Returns the last modification date as a timestamp of the latest date of a resource modification.
     *
     * @return the last modification date
     */
    public Date getLastModification()
    {
        return lastModification;
    }

    void setIncoming( Date incoming )
    {
        this.incoming = incoming;
    }

    /**
     * It saves the instance in to datastore.
     * <p>
     * If the modification date of incoming resource represents a newer date
     * (time is being also considered in to comparison) against stored one,
     * this incoming resource date will be set as a last modification date.
     */
    public void end()
    {
        if ( lastModification.before( incoming ) )
        {
            lastModification = incoming;
        }

        ofy().transact( () -> ofy().save().entity( this ).now() );
    }

    public void delete()
    {
        ofy().transact( () -> ofy().delete().entity( this ).now() );
    }

}
