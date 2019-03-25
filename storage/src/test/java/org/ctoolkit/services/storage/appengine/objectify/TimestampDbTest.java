package org.ctoolkit.services.storage.appengine.objectify;

import com.google.api.client.util.DateTime;
import com.googlecode.objectify.ObjectifyService;
import org.ctoolkit.services.storage.appengine.BackendServiceTestCase;
import org.ctoolkit.services.storage.appengine.TimestampEntity;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * {@link Timestamp} unit testing against emulated local datastore.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public class TimestampDbTest
        extends BackendServiceTestCase
{
    private List<String> uniqueKey;

    // Jul 14 2017 02:38:35
    private long dateMillis = 1499999915000L;

    {
        uniqueKey = new ArrayList<>();
        uniqueKey.add( "7392991330598912" );
        uniqueKey.add( "1112282472694100" );
        uniqueKey.add( "5369461238768899" );

        ObjectifyService.register( TimestampEntity.class );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void getName_EmptyUniqueKey()
    {
        TimestampEntity.of( "Order", new ArrayList<>(), new Date() );
    }

    @Test
    public void getName()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new Date() );
        timestamp.done();

        assertThat( timestamp.getName() ).isEqualTo( "Order::7392991330598912::1112282472694100::5369461238768899" );
    }

    @Test
    public void isObsolete_IncomingOlderChanges()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis ) );
        timestamp.done();
        ofy().clear();

        // incoming changes are older
        timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis - 1 ) );
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isTrue();
    }

    @Test
    public void getLastModification_AfterSaveIncomingOlderChanges()
    {
        Date originLast = new Date( dateMillis );
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, originLast );
        timestamp.done();
        ofy().clear();

        // incoming changes are older
        timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis - 1 ) );
        timestamp.done();

        assertThat( timestamp.getLastModification() ).named( "Last modification date" ).isEqualTo( originLast );
    }

    @Test
    public void isObsolete_IncomingNewerChanges()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis ) );
        timestamp.done();
        ofy().clear();

        // incoming changes are newer
        timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis + 1 ) );
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isFalse();
    }

    @Test
    public void getLastModification_AfterSaveIncomingNewerChanges()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis ) );
        timestamp.done();
        ofy().clear();

        // incoming changes are newer
        Date newer = new Date( dateMillis + 1 );
        timestamp = TimestampEntity.of( "Order", uniqueKey, newer );
        timestamp.done();

        assertThat( timestamp.getLastModification() ).named( "Last modification date" ).isEqualTo( newer );
    }

    @Test
    public void isObsolete_FirstTimeIncomingChangesWithModificationDate()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis ) );
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isFalse();

        timestamp.done();
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isTrue();
    }

    @Test
    public void isObsolete_FirstTimeIncomingChangesWithModificationDateTime()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new DateTime( dateMillis ) );
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isFalse();

        timestamp.done();
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isTrue();
    }

    @Test
    public void isObsolete_SameDateOfIncomingChangesAndLastModification()
    {
        TimestampEntity timestamp = Timestamp.of( "Order", uniqueKey, new Date( dateMillis ), TimestampEntity.class );
        timestamp.done();
        ofy().clear();

        timestamp = Timestamp.of( "Order", uniqueKey, new Date( dateMillis ), TimestampEntity.class );
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isTrue();
    }

    @Test
    public void isObsolete_SameDateTimeOfIncomingChangesAndLastModification()
    {
        TimestampEntity timestamp = Timestamp.of( "Order", uniqueKey, new DateTime( dateMillis ), TimestampEntity.class );
        timestamp.done();
        ofy().clear();

        timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis ) );
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isTrue();
    }

    @Test
    public void isObsolete_FirstTimeNullDateOfIncomingChangesConsideredAsLatestChanges()
    {
        // null date means now
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, ( Date ) null );
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isFalse();

        timestamp.done();
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isTrue();
    }

    @Test
    public void isObsolete_FirstTimeNullDateTimeOfIncomingChangesConsideredAsLatestChanges()
    {
        // null date means now
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, ( DateTime ) null );
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isFalse();

        timestamp.done();
        assertThat( timestamp.isObsolete() ).named( "Incoming obsolete changes" ).isTrue();
    }

    @Test
    public void delete()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, ( DateTime ) null );
        timestamp.done();

        int count = ofy().load().type( TimestampEntity.class ).count();
        assertThat( count ).named( "Number of Timestamps" ).isEqualTo( 1 );

        timestamp.delete();

        count = ofy().load().type( TimestampEntity.class ).count();
        assertThat( count ).named( "Number of Timestamps" ).isEqualTo( 0 );
    }
}