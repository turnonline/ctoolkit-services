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

package org.ctoolkit.services.storage;

import mockit.Tested;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.ctoolkit.services.storage.GoogleStorageAwareGeneralMapping.GS_PREFIX;

/**
 * {@link GoogleStorageAwareGeneralMapping} unit testing.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public class GoogleStorageAwareGeneralMappingTest
{
    private static final String GENERAL_STORAGE_NAME = "test-bucket.appspot.com/3445273846/uploads/something.jpeg";

    @Tested
    private MockedAwareGeneralMapping tested;

    @Test
    public void general_WithGsPrefix()
    {
        String general = tested.general( GS_PREFIX + GENERAL_STORAGE_NAME );
        assertThat( general ).isEqualTo( GENERAL_STORAGE_NAME );
    }

    @Test
    public void general_OnlyPassThrough()
    {
        String general = tested.general( GENERAL_STORAGE_NAME );
        assertThat( general ).isEqualTo( GENERAL_STORAGE_NAME );
    }

    @Test
    public void gsAware_WithGsPrefix()
    {
        String gs = tested.gsAware( GENERAL_STORAGE_NAME );
        assertThat( gs ).isEqualTo( GS_PREFIX + GENERAL_STORAGE_NAME );
    }

    @Test
    public void gsAware_WithLeadingSlash()
    {
        String gs = tested.gsAware( "/" + GENERAL_STORAGE_NAME );
        assertThat( gs ).isEqualTo( GS_PREFIX + GENERAL_STORAGE_NAME );
    }

    @Test
    public void gsAware_OnlyPassThrough()
    {
        String inclPrefix = GS_PREFIX + GENERAL_STORAGE_NAME;
        String gs = tested.gsAware( inclPrefix );
        assertThat( gs ).isEqualTo( inclPrefix );
    }
}