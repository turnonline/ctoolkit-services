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

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Injector;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * {@link TaskQueueExecutorBean} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class TaskQueueExecutorBeanTest
{
    @Tested
    private TaskQueueExecutorBean tested;

    @Injectable
    private Injector injector;

    @Injectable
    private ModulesService modulesService;

    @Test( expectedExceptions = NullPointerException.class )
    public void batchScheduleEmpty()
    {
        final Task[] tasks = new Task[0];

        tested.schedule( "my-queue", tasks );
    }

    @Test( expectedExceptions = NullPointerException.class )
    public void batchScheduleNullArray()
    {
        final Task[] tasks = new Task[2];
        tasks[0] = null;
        tasks[1] = null;

        tested.schedule( "my-queue", tasks );
    }

    @Test
    public void scheduleNoOptions( @Mocked final Queue queue )
    {
        final Task task = new FakeTask();

        new ModulesServiceExpectations( tested, queue, task );

        tested.schedule( task );

        new Verifications()
        {
            {
                TaskOptions options;
                queue.add( options = withCapture() );

                Map<String, List<String>> headers = options.getHeaders();
                assertTrue( headers.size() > 0, "There are no headers!" );

                List<String> host = headers.get( "Host" );
                assertEquals( host.size(), 1, "Host header is" );

                String hostname = host.get( 0 );
                assertEquals( hostname, "complete-hostname" );
            }
        };
    }

    @Test
    public void scheduleFulOptions( @Mocked final Queue queue )
    {
        final Integer postponeFor = 300;
        final Long id = 123987L;

        final Task task = new FakeTask( "my-prefix", "my-queue" );
        task.postponeFor( postponeFor );
        task.setEntityId( id );

        new ModulesServiceExpectations( tested, queue, task );

        tested.schedule( task );

        new Verifications()
        {
            {
                TaskOptions options;
                queue.add( options = withCapture() );

                Map<String, List<String>> headers = options.getHeaders();
                assertTrue( headers.size() > 0, "There are no headers!" );

                List<String> host = headers.get( "Host" );
                assertEquals( host.size(), 1, "Host header is" );

                String taskName = options.getTaskName();
                assertEquals( taskName, "my-prefix_" + id, "Task name" );

                Long eta = options.getEtaMillis();
                Long diff = ( eta - System.currentTimeMillis() ) / 1000;
                assertTrue( diff <= postponeFor, "Countdown" );
            }
        };
    }

    final class ModulesServiceExpectations
            extends Expectations
    {
        ModulesServiceExpectations( TaskQueueExecutorBean partiallyMocked,
                                    Queue queue,
                                    Task task )
        {
            super( partiallyMocked );

            modulesService.getVersionHostname( anyString, anyString );
            result = "complete-hostname";

            tested.getQueue( task );
            result = queue;
        }
    }
}