package org.ctoolkit.services.storage.appengine.datastore;

import com.googlecode.objectify.Key;
import org.ctoolkit.services.storage.EntityExecutor;
import org.ctoolkit.services.storage.EntityIdentity;
import org.ctoolkit.services.storage.criteria.Criteria;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The Objectify entity executor implementation.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
public class ObjectifyEntityExecutor
        implements EntityExecutor
{
    @Override
    @SuppressWarnings( value = "unchecked" )
    public <T> List<T> list( @Nonnull Criteria<T> criteria )
    {
        return new ObjectifyCriteriaBuilder<T>().build( criteria ).list();
    }

    @Override
    public <T> List<Long> fetchIds( @Nonnull Criteria<T> criteria )
    {
        ObjectifyCriteriaBuilder<T> builder = new ObjectifyCriteriaBuilder<>();
        com.googlecode.objectify.cmd.Query<T> build = builder.build( criteria );

        List<Long> ids = new ArrayList<>();

        for ( Key<T> key : build.keys() )
        {
            ids.add( key.getId() );
        }

        return ids;
    }

    @Override
    public <T> List<String> fetchNames( @Nonnull Criteria<T> criteria )
    {
        ObjectifyCriteriaBuilder<T> builder = new ObjectifyCriteriaBuilder<>();
        com.googlecode.objectify.cmd.Query<T> build = builder.build( criteria );

        List<String> ids = new ArrayList<>();

        for ( Key<T> key : build.keys() )
        {
            ids.add( key.getName() );
        }

        return ids;
    }

    @Override
    public void save( @Nonnull EntityIdentity entity, @Nonnull Map<String, Object> map )
    {
        Key<?> key = Key.create( entity );
        PropertyMapDbHandler dbHandler = new PropertyMapDbHandler( key, map );

        dbHandler.save();
    }

    @Override
    public Map<String, Object> load( @Nonnull EntityIdentity entity )
    {
        Key<?> key = Key.create( entity );
        PropertyMapDbHandler dbHandler = new PropertyMapDbHandler( key );

        return dbHandler.load();
    }
}
