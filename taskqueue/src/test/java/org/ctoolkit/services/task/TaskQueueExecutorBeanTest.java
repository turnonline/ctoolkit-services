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

import com.google.guiceberry.junit4.GuiceBerryRule;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

/**
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class TaskQueueExecutorBeanTest
        extends ServiceEnvironment
{
    @Rule
    public final GuiceBerryRule guiceBerry = new GuiceBerryRule( ServiceEnvironment.class );

    @Inject
    private TaskExecutorService executor;

    @Test
    public void testExecute() throws Exception
    {
        Task task = new FakeTask( "process", 5 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 6 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 7 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 1 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 3 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 2 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 9 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 11 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "p", 11 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 30 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 10 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 15 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 14 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 13 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 16 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 36 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 37 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 31 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 33 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 32 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 39 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 311 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 312 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 330 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 310 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 315 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 314 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 313 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 316 );
        task.setOwnerId( 1254L );
        executor.execute( task );
        task = new FakeTask( "process", 6 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 7 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 1 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 3 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 2 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 9 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process2", 11 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 12 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 30 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 10 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 15 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 14 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 13 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 16 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 36 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 37 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 31 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 33 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 32 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 39 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 311 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 312 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 330 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 310 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 315 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 314 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 313 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 316 );
        task.setOwnerId( 1254L );
        executor.execute( task );
        task = new FakeTask( "process", 6 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 7 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 1 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 3 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 2 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 9 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process3", 11 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "pro", 11 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "proc", 11 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "proces", 11 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 15 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 14 );
        task.setOwnerId( 1254L );
        executor.execute( task );

        task = new FakeTask( "process", 13 );
        task.setOwnerId( 1254L );
        executor.execute( task );

    }
}
