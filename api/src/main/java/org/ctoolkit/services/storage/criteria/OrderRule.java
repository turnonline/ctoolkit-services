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

package org.ctoolkit.services.storage.criteria;

/**
 * Represent order rule for query. <code>Property name</code> is name of property for query sorting and <code>order</code> is type of
 * ordering - ascending/descening.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class OrderRule
{
    private String propertyName;

    private Order order;

    public OrderRule( String propertyName, Order order )
    {
        this.propertyName = propertyName;
        this.order = order;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public Order getOrder()
    {
        return order;
    }
}
