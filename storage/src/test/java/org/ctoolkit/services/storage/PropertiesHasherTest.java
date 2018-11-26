/*
 * Copyright (c) 2018 Comvai, s.r.o. All Rights Reserved.
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

import org.ctoolkit.services.storage.appengine.BackendServiceTestCase;
import org.testng.annotations.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * {@link PropertiesHasher} unit testing.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class PropertiesHasherTest
        extends BackendServiceTestCase
{
    @Test
    public void getHashCode_LongId_SavedButNotInitializedYet()
    {
        EntityLongIdentityHasherTestEntity entity = new EntityLongIdentityHasherTestEntity();
        entity.save();

        PropertiesHashCode hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();
        assertThat( hashCodeEntity.getHashCode() ).isNull();
    }

    @Test
    public void isPropsHashCodeChanged_LongId_InitializedAndCalculated()
    {
        EntityLongIdentityHasherTestEntity entity = new EntityLongIdentityHasherTestEntity();
        entity.save();

        PropertiesHashCode hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();
        assertThat( hashCodeEntity.getHashCode() ).isNull();

        // entity with initialized PropertiesHashCode already saved, but recalculated hashcode snapshot not yet
        assertThat( entity.isPropsHashCodeChanged() ).isTrue();

        // calculates and persists
        assertThat( entity.snapshotOfPropsHashCode() ).isTrue();
        assertThat( entity.isPropsHashCodeChanged() ).isFalse();

        hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();

        String hashCode = hashCodeEntity.getHashCode();
        assertThat( hashCode ).isNotNull();
        assertThat( hashCode ).isEqualTo( "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855" );
    }

    @Test
    public void putPropsHashCode_LongId_CalculatedThenChanged()
    {
        EntityLongIdentityHasherTestEntity entity = new EntityLongIdentityHasherTestEntity();
        entity.save();

        PropertiesHashCode hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();
        assertThat( hashCodeEntity.getHashCode() ).isNull();

        // change value, calculate and persist
        entity.setAbc( "abc_efg" );
        assertThat( entity.snapshotOfPropsHashCode() ).isTrue();

        hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();

        String hashCode = hashCodeEntity.getHashCode();
        assertThat( hashCode ).isNotNull();
        assertThat( hashCode ).isEqualTo( "c742891325ca2a5199d2b1c6846a4be59ad8a1b24c48288ce331aec103997a1a" );
    }

    @Test
    public void getHashCode_StringId_SavedButNotInitializedYet()
    {
        EntityStringIdentityHasherTestEntity entity = new EntityStringIdentityHasherTestEntity();
        entity.setId( "my-id-123" );
        entity.save();

        PropertiesHashCode hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();
        assertThat( hashCodeEntity.getHashCode() ).isNull();
    }

    @Test
    public void isPropsHashCodeChanged_StringId_InitializedAndCalculated()
    {
        EntityStringIdentityHasherTestEntity entity = new EntityStringIdentityHasherTestEntity();
        entity.setId( "my-id-123" );
        entity.save();

        PropertiesHashCode hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();
        assertThat( hashCodeEntity.getHashCode() ).isNull();

        // entity with initialized PropertiesHashCode already saved, but recalculated hashcode snapshot not yet
        assertThat( entity.isPropsHashCodeChanged() ).isTrue();

        // calculates and persists
        assertThat( entity.snapshotOfPropsHashCode() ).isTrue();
        assertThat( entity.isPropsHashCodeChanged() ).isFalse();

        hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();

        String hashCode = hashCodeEntity.getHashCode();
        assertThat( hashCode ).isNotNull();
        assertThat( hashCode ).isEqualTo( "5fea62f1155fa63f3872a03fe9af20f14cc3d2966f914a74d845b55e7388261d" );
    }

    @Test
    public void putPropsHashCode_StringId_CalculatedThenChanged()
    {
        EntityStringIdentityHasherTestEntity entity = new EntityStringIdentityHasherTestEntity();
        entity.setId( "my-id-123" );
        entity.save();

        PropertiesHashCode hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();
        assertThat( hashCodeEntity.getHashCode() ).isNull();

        // change value, calculate and persist
        entity.setXyz( "abc_efg" );
        assertThat( entity.snapshotOfPropsHashCode() ).isTrue();

        hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();

        String hashCode = hashCodeEntity.getHashCode();
        assertThat( hashCode ).isNotNull();
        assertThat( hashCode ).isEqualTo( "1c55b869961672d5befbd58a01a732172bad50b0781e890b89a5984279791590" );
    }
}