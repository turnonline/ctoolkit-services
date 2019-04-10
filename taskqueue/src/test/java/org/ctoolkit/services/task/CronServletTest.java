/*
 * Copyright (c) 2019 Comvai, s.r.o. All Rights Reserved.
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

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * {@link CronServlet} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CronServletTest
{
    @Tested
    private CronServlet tested;

    @Injectable
    private TaskExecutor executor;

    @Mocked
    private HttpServletRequest request;

    @Mocked
    private HttpServletResponse response;

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void doGet_RequestURINull()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = null;
            }
        };
        tested.doGet( request, response );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void doGet_RequestURIEmptyString()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = "";
            }
        };
        tested.doGet( request, response );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void doGet_RequestURISlashOnly()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = "/";
            }
        };
        tested.doGet( request, response );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void doGet_RequestURIMissingCron()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = "/whatever1/whatever2";
            }
        };
        tested.doGet( request, response );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void doGet_RequestURIMissingIdentifier()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = "/cron/";
            }
        };
        tested.doGet( request, response );
    }

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void doGet_RequestURIInvalidSync()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = "/cron/sync/";
            }
        };
        tested.doGet( request, response );
    }

    @Test
    public void doGet_RequestURICronSlashIdentifier()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = "/cron/whatever1";
            }
        };
        tested.doGet( request, response );

        new Verifications()
        {
            {
                //noinspection unchecked
                executor.schedule( "/cron/whatever1", ( Map ) any );
            }
        };
    }

    @Test
    public void doGet_RequestURICronSlash2xIdentifier()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = "/cron/whatever1/whatever2";
            }
        };
        tested.doGet( request, response );

        new Verifications()
        {
            {
                //noinspection unchecked
                executor.schedule( "/cron/whatever1/whatever2", ( Map ) any );
            }
        };
    }

    @Test
    public void doGet_RequestURISyncCronSlashIdentifier()
    {
        new Expectations()
        {
            {
                request.getRequestURI();
                result = "/cron/sync/whatever1/whatever2";
            }
        };
        tested.doGet( request, response );

        new Verifications()
        {
            {
                //noinspection unchecked
                executor.syncCron( "/cron/sync/whatever1/whatever2", ( Map ) any );
            }
        };
    }
}