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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Cron tasks servlet handler.
 * <p>
 * For correct functionality (security constraint) is being required that follows convention how to compose an URL
 * configured in App Engine queue.xml configuration file.
 * <p>
 * Contract:
 * <ul>
 * <li>/cron/whatever1/whatever2</li>
 * <li>/cron/sync/whatever1/whatever2</li>
 * </ul>
 * <ul>
 * <li>'cron' as a default and mandatory parameter that maps {@link CronServlet} as a handler of the cron tasks.</li>
 * <li>'sync' as an operator that instructs the cron task to run as a synchronous job.
 * If omitted the target task will be enqueued in to task queue and executed asynchronously.</li>
 * <li>'whatever1', 'whatever2' as an unique identifiers of the cron task.</li>
 * </ul>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
class CronServlet
        extends HttpServlet
{
    private static final long serialVersionUID = -6984813104634301746L;

    private final TaskExecutor executor;

    @Inject
    CronServlet( TaskExecutor executor )
    {
        this.executor = executor;
    }

    @Override
    @SuppressWarnings( value = "unchecked" )
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
    {
        String uri = request.getRequestURI();
        if ( uri == null )
        {
            throw new IllegalArgumentException( "Invalid URI (null)" );
        }

        if ( Strings.isNullOrEmpty( uri ) )
        {
            throw new IllegalArgumentException( "Missing cron URI" );
        }

        @SuppressWarnings( "UnstableApiUsage" )
        List<String> params = Splitter.on( "/" ).omitEmptyStrings().trimResults().splitToList( uri );
        if ( params.isEmpty() )
        {
            throw new IllegalArgumentException( "Invalid cron URI: " + uri );
        }

        if ( params.size() < 2 )
        {
            throw new IllegalArgumentException( "Invalid cron URI (missing identifier): " + uri );
        }

        String cronParam = params.get( 0 );
        if ( !"cron".equalsIgnoreCase( cronParam ) )
        {
            throw new IllegalArgumentException( "Cron URI does not start with 'cron' " + uri );
        }

        String syncParam = params.get( 1 );
        if ( "sync".equalsIgnoreCase( syncParam ) )
        {
            if ( params.size() < 3 )
            {
                throw new IllegalArgumentException( "Invalid sync cron URI (missing identifier): " + uri );
            }
            executor.syncCron( request.getRequestURI(), ( Map<String, String> ) request.getParameterMap() );
        }
        else
        {
            executor.schedule( request.getRequestURI(), ( Map<String, String> ) request.getParameterMap() );
        }
    }
}