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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Cron servlet handler to add scheduled task to the configured queue.
 * <p>
 * For correct functionality (security constraint) is being required to follow convention how to compose an URL
 * configured in App Engine queue.xml configuration file.
 * <p>
 * The contract: it consists from, for example: '/cron/whatever1/whatever2'
 * <ul>
 * <li>part 1: 'cron' as a default namespace as servlet mapping for the <code>CronServlet</code></li>
 * <li>part 2: 'whatever1', 'whatever2' etc it's up to you</li>
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
        executor.schedule( request.getRequestURI(), ( Map<String, String> ) request.getParameterMap() );
    }
}