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
    static final String HASHER_NAME = "PubSub";

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
        assertThat( entity.hashCodeSnapshot() ).isTrue();
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
        assertThat( entity.hashCodeSnapshot() ).isTrue();

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
        assertThat( hashCodeEntity.getHashCode( HASHER_NAME ) ).isNull();

        // entity with initialized PropertiesHashCode already saved, but recalculated hashcode snapshot not yet
        assertThat( entity.isPropsHashCodeChanged() ).isTrue();
        assertThat( entity.isPropsHashCodeChanged( HASHER_NAME ) ).isTrue();

        // calculates and persists
        assertThat( entity.hashCodeSnapshot() ).isTrue();
        assertThat( entity.isPropsHashCodeChanged() ).isFalse();

        hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();

        String hashCode = hashCodeEntity.getHashCode();
        assertThat( hashCode ).isNotNull();
        assertThat( hashCode ).isEqualTo( "5fea62f1155fa63f3872a03fe9af20f14cc3d2966f914a74d845b55e7388261d" );

        // the second standalone (independent) hash code
        assertThat( entity.hashCodeSnapshot( HASHER_NAME ) ).isTrue();
        assertThat( entity.isPropsHashCodeChanged( HASHER_NAME ) ).isFalse();

        hashCode = hashCodeEntity.getHashCode( HASHER_NAME );
        assertThat( hashCode ).isNotNull();
        assertThat( hashCode ).isEqualTo( "b7cd687f79c11015872c7e4dc1e8caa2becee2c132da78867e9ccec2d94d07b3" );
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
        assertThat( hashCodeEntity.getHashCode( HASHER_NAME ) ).isNull();

        // change value of the shared property, calculate and persist
        entity.setXyz( "abc_efg" );
        assertThat( entity.hashCodeSnapshot() ).isTrue();
        assertThat( entity.hashCodeSnapshot( HASHER_NAME ) ).isTrue();

        hashCodeEntity = entity.getPropsHashCode();
        assertThat( hashCodeEntity ).isNotNull();

        String hashCode = hashCodeEntity.getHashCode();
        assertThat( hashCode ).isNotNull();
        assertThat( hashCode ).isEqualTo( "1c55b869961672d5befbd58a01a732172bad50b0781e890b89a5984279791590" );

        // the second standalone (independent) hash code
        hashCode = hashCodeEntity.getHashCode( HASHER_NAME );
        assertThat( hashCode ).isNotNull();
        assertThat( hashCode ).isEqualTo( "873b0bf8809ad748928f4beb562b663c151a2958be30bc3d73ff560c75e53b50" );
    }
}