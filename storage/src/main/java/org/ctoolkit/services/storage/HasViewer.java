package org.ctoolkit.services.storage;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Declaration for an entity that's might be read by a viewer.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public interface HasViewer<T extends EntityIdentity<?>>
        extends HasOwner<T>
{
    /**
     * Returns the list of the allowed viewers.
     *
     * @return the list of the allowed viewers or {@code null} if not yet set
     */
    List<T> getViewers();

    /**
     * Checks whether specified viewer is the one allowed to read this entity.
     *
     * @param reader the viewer to be checked
     * @return {@code true} if specified reader is allowed to read this entity, otherwise {@code false}.
     */
    default boolean isViewer( @Nonnull T reader )
    {
        checkNotNull( reader, "Checked viewer can't be null" );
        List<T> viewers = getViewers();
        return viewers != null && viewers.contains( reader );
    }
}
