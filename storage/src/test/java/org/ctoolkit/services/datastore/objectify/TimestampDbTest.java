package org.ctoolkit.services.datastore.objectify;

import com.google.api.client.util.DateTime;
import com.googlecode.objectify.ObjectifyService;
import org.ctoolkit.services.datastore.BackendServiceTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * {@link Timestamp} unit testing against emulated local datastore.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
@SuppressWarnings( "FieldMayBeFinal" )
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

    }

    @BeforeMethod
    public void before()
    {
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
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isTrue();
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

        assertWithMessage( "Last modification date" )
                .that( timestamp.getLastModification() )
                .isEqualTo( originLast );
    }

    @Test
    public void isObsolete_IncomingNewerChanges()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis ) );
        timestamp.done();
        ofy().clear();

        // incoming changes are newer
        timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis + 1 ) );
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isFalse();
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

        assertWithMessage( "Last modification date" )
                .that( timestamp.getLastModification() )
                .isEqualTo( newer );
    }

    @Test
    public void isObsolete_FirstTimeIncomingChangesWithModificationDate()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis ) );
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isFalse();

        timestamp.done();
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isTrue();
    }

    @Test
    public void isObsolete_FirstTimeIncomingChangesWithModificationDateTime()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, new DateTime( dateMillis ) );
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isFalse();

        timestamp.done();
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isTrue();
    }

    @Test
    public void isObsolete_SameDateOfIncomingChangesAndLastModification()
    {
        TimestampEntity timestamp = Timestamp.of( "Order", uniqueKey, new Date( dateMillis ), TimestampEntity.class );
        timestamp.done();
        ofy().clear();

        timestamp = Timestamp.of( "Order", uniqueKey, new Date( dateMillis ), TimestampEntity.class );
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isTrue();
    }

    @Test
    public void isObsolete_SameDateTimeOfIncomingChangesAndLastModification()
    {
        TimestampEntity timestamp = Timestamp.of( "Order", uniqueKey, new DateTime( dateMillis ), TimestampEntity.class );
        timestamp.done();
        ofy().clear();

        timestamp = TimestampEntity.of( "Order", uniqueKey, new Date( dateMillis ) );
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isTrue();
    }

    @Test
    public void isObsolete_FirstTimeNullDateOfIncomingChangesConsideredAsLatestChanges()
    {
        // null date means now
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, ( Date ) null );
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isFalse();

        timestamp.done();
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isTrue();
    }

    @Test
    public void isObsolete_FirstTimeNullDateTimeOfIncomingChangesConsideredAsLatestChanges()
    {
        // null date means now
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, ( DateTime ) null );
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isFalse();

        timestamp.done();
        assertWithMessage( "Incoming obsolete changes" )
                .that( timestamp.isObsolete() )
                .isTrue();
    }

    @Test
    public void delete()
    {
        TimestampEntity timestamp = TimestampEntity.of( "Order", uniqueKey, ( DateTime ) null );
        timestamp.done();

        int count = ofy().load().type( TimestampEntity.class ).count();
        assertWithMessage( "Number of Timestamps" )
                .that( count )
                .isEqualTo( 1 );

        timestamp.delete();

        count = ofy().load().type( TimestampEntity.class ).count();
        assertWithMessage( "Number of Timestamps" )
                .that( count )
                .isEqualTo( 0 );
    }
}