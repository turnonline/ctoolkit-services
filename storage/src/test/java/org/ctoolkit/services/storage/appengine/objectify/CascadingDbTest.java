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

package org.ctoolkit.services.storage.appengine.objectify;

import org.ctoolkit.services.storage.appengine.ServiceTestNgCase;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Parent/Child entity group cascading saving.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CascadingDbTest
        extends ServiceTestNgCase
{
    @Test
    public void twoLevelCascading()
    {
        ParentEntity parent = new ParentEntity();
        ChildEntity child = new ChildEntity();

        parent.setChildEntity( child );
        parent.setSiblingChildEntity( new SiblingChildEntity() );
        child.setChildEntity( new Child2LevelEntity( "my-string-id" ) );

        parent.save();

        ChildEntity childEntity = parent.getChildEntity();
        SiblingChildEntity siblingChildEntity = parent.getSiblingChildEntity();
        Child2LevelEntity child2LevelEntity = childEntity.getChildEntity();

        assertNotNull( childEntity.getParent(), "Parent for child entity has not been set." );
        assertNotNull( child2LevelEntity.getParent(), "Parent for child entity has not been set." );

        // assertion check
        assertNotNull( childEntity.getId(), "Child entity with 2 level child is not persisted. ID -" );
        assertNotNull( siblingChildEntity.getId(), "Sibling child entity is not persisted. ID -" );
        assertNotNull( child2LevelEntity.getCreatedDate(), "2 level child entity is not persisted." );

        // parent entities with children are expected have to be saved 2 times, at first the entity
        // must be saved without Ref (child has no Id yet) and then again with Ref to its children
        String message = "Parent entity has been saved too many times";
        assertEquals( message, Integer.valueOf( 2 ), parent.getVersion() );

        message = "Child entity with 2 level child has been saved too many times";
        assertEquals( message, Integer.valueOf( 2 ), childEntity.getVersion() );

        // leaf entities are expected have to be saved only once
        message = "Sibling child entity has been saved too many times";
        assertEquals( message, Integer.valueOf( 1 ), siblingChildEntity.getVersion() );

        message = "2 level child entity has been saved too many times";
        assertEquals( message, Integer.valueOf( 1 ), child2LevelEntity.getVersion() );
    }

    @Test
    public void cascadingSaveWithIgnoredFields()
    {
        ParentEntity parent = new ParentEntity();
        ChildEntity child = new ChildEntity();

        parent.setChildEntity( child );
        parent.setSiblingChildEntity( new SiblingChildEntity() );
        child.setChildEntity( new Child2LevelEntity( "my-string-id" ) );

        // configured to ignore leaf entities
        parent.newCascading().ignore( "siblingChildEntity" )
                .addChild( "childEntity" )
                .ignore( "childEntity" );

        parent.save();

        ChildEntity childEntity = parent.getChildEntity();
        SiblingChildEntity siblingChildEntity = parent.getSiblingChildEntity();
        Child2LevelEntity child2LevelEntity = childEntity.getChildEntity();

        // assertion check
        assertNotNull( childEntity.getId(), "Child entity with 2 level child is not persisted. ID -" );
        assertNull( siblingChildEntity.getId(), "Sibling child entity has been saved, but cannot." );
        assertNull( child2LevelEntity.getCreatedDate(), "2 level child entity has been saved, but cannot." );

        String message = "Parent entity has been saved too many times";
        assertEquals( message, Integer.valueOf( 2 ), parent.getVersion() );

        message = "Child entity with ignored 2 level child has been saved too many times";
        assertEquals( message, Integer.valueOf( 2 ), childEntity.getVersion() );
    }
}
