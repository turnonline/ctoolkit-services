package org.ctoolkit.services.storage.appengine.datastore;

import com.googlecode.objectify.annotation.Entity;
import org.ctoolkit.services.storage.EntityIdentity;

/**
 * The fake entity for test purpose.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Entity
public class FakeEntity
        extends IdEntity
{
    @Override
    public void setParent( EntityIdentity parent )
    {

    }

    @Override
    protected long getModelVersion()
    {
        return 1;
    }
}
