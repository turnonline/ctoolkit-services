/*
 * Copyright (c) 2017 Comvai, s.r.o. All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.ctoolkit.services.task;

import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Injector;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * {@link Task} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@SuppressWarnings( "ResultOfMethodCallIgnored" )
public class TaskTest
{
    @Tested
    private Task tested;

    @Injectable
    private Injector injector;

    @Injectable
    private TaskExecutor executor;

    @Test
    public void chainingNextNoOptions()
    {
        new Expectations( tested )
        {
            {
                tested.hasNext();
                result = false;
            }
        };

        final Task next = new FakeTask();
        tested.addNext( next );

        tested.run();

        assertNull( next.getPostponeFor() );
        verificationsNoOptions( next );
    }

    @Test
    public void chainingNextRemovedByFirstTask()
    {
        new Expectations( tested )
        {
            {
                tested.hasNext();
                result = false;
            }
        };

        final TaskOptions options = TaskOptions.Builder.withDefaults();
        final Task next = new FakeTask();
        tested.addNext( next );

        assertTrue( tested.clear() );
        tested.run();

        new Verifications()
        {
            {
                //noinspection ConstantConditions
                executor.schedule( ( Task ) any );
                times = 0;

                executor.schedule( ( Task ) any, options );
                times = 0;
            }
        };
    }

    @Test
    public void chainingNextWithOptions()
    {
        new Expectations( tested )
        {
            {
                tested.hasNext();
                result = false;
            }
        };

        final Task next = new FakeTask();
        final TaskOptions options = TaskOptions.Builder.withDefaults();
        tested.addNext( next ).options( options );

        tested.run();

        assertNull( next.getPostponeFor() );
        verificationsWithOptions( next, options );
    }

    @Test
    public void chainingNextWithOptionsAsArgument()
    {
        new Expectations( tested )
        {
            {
                tested.hasNext();
                result = false;
            }
        };

        final Task next = new FakeTask();
        final TaskOptions options = TaskOptions.Builder.withDefaults();
        tested.addNext( next, options );

        tested.run();

        assertNull( next.getPostponeFor() );
        verificationsWithOptions( next, options );
    }

    @Test
    public void chainingNextWithPostponeFor()
    {
        new Expectations( tested )
        {
            {
                tested.hasNext();
                result = false;
            }
        };

        final Task next = new FakeTask();
        tested.addNext( next ).postponeFor( 20 );

        tested.run();

        assertEquals( next.getPostponeFor(), Integer.valueOf( 20 ) );
        verificationsNoOptions( next );
    }

    @Test
    public void chainingNextWithPostponeForAsArgument()
    {
        new Expectations( tested )
        {
            {
                tested.hasNext();
                result = false;
            }
        };

        final Task next = new FakeTask();
        tested.addNext( next, 10 );

        tested.run();

        assertEquals( next.getPostponeFor(), Integer.valueOf( 10 ) );
        verificationsNoOptions( next );
    }

    private void verificationsWithOptions( final Task next, final TaskOptions options )
    {
        new Verifications()
        {
            {
                //noinspection ConstantConditions
                executor.schedule( ( Task ) any );
                times = 0;

                executor.schedule( next, options );
                times = 1;
            }
        };
    }

    private void verificationsNoOptions( final Task next )
    {
        new Verifications()
        {
            {
                executor.schedule( next );
                times = 1;

                //noinspection ConstantConditions
                executor.schedule( ( Task ) any, ( TaskOptions ) any );
                times = 0;
            }
        };
    }
}