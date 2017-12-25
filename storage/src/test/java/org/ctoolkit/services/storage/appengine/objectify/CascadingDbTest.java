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

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

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
        assertEquals( parent.getVersion(), Integer.valueOf( 2 ), message );

        message = "Child entity with 2 level child has been saved too many times";
        assertEquals( childEntity.getVersion(), Integer.valueOf( 2 ), message );

        // leaf entities are expected have to be saved only once
        message = "Sibling child entity has been saved too many times";
        assertEquals( siblingChildEntity.getVersion(), Integer.valueOf( 1 ), message );

        message = "2 level child entity has been saved too many times";
        assertEquals( child2LevelEntity.getVersion(), Integer.valueOf( 1 ), message );
    }

    @Test
    public void cascadingSaveWithOneToMany()
    {
        ParentEntity parent = new ParentEntity();
        ChildEntity child = new ChildEntity();
        ChildEntity secondChild = new ChildEntity();
        child.setChildEntity( new Child2LevelEntity( "my-string-id" ) );

        parent.add( child );
        parent.add( secondChild );

        // cascading save test
        parent.save();

        List<ChildEntity> children = parent.getChildren();
        assertNotNull( children );
        assertEquals( children.size(), 2, "Children list size" );
        assertNotNull( parent.children, "Persisted children references" );
        assertEquals( parent.children.size(), 2, "Persisted children list size" );

        ChildEntity firstChildEntity = children.get( 0 );
        Child2LevelEntity child2LevelEntity = firstChildEntity.getChildEntity();

        assertNotNull( firstChildEntity.getId(), "Child entity not persisted." );
        assertEquals( firstChildEntity.getParent(), parent, "Parent for child entity is not correct." );
        assertEquals( child2LevelEntity.getParent(), firstChildEntity, "Parent for child entity is not correct." );

        ChildEntity secondChildEntity = children.get( 1 );
        assertNotNull( secondChildEntity.getId(), "Child entity not persisted." );
        assertEquals( secondChildEntity.getParent(), parent, "Parent for child entity is not correct." );
        ofy().clear();

        // testing whether children records has been created
        List<ChildEntity> dbList = ofy().load().type( ChildEntity.class ).list();
        assertEquals( dbList.size(), 2, "Children list size" );

        // cascading delete test
        parent.remove( secondChild );
        parent.save();

        children = parent.getChildren();
        assertNotNull( children );

        // one child has been removed
        assertEquals( children.size(), 1, "Updated children list size" );
        assertNotNull( parent.children, "Persisted children references" );
        assertEquals( parent.children.size(), 1, "Updated persisted children list size" );

        // testing whether persisted children record has been removed from datastore too
        ofy().clear();
        dbList = ofy().load().type( ChildEntity.class ).list();
        assertEquals( dbList.size(), 1, "Children list size" );

        // testing whether simply load and immediate save will keep it same
        ParentEntity dbParentEntity = ofy().load().type( ParentEntity.class ).id( parent.getId() ).now();
        dbParentEntity.save();
        ofy().clear();

        dbParentEntity = ofy().load().type( ParentEntity.class ).id( dbParentEntity.getId() ).now();
        dbList = ofy().load().type( ChildEntity.class ).list();

        assertEquals( dbList.size(), 1, "Children list size" );

        // clearing all one to many children
        dbParentEntity.clearChildren();
        dbParentEntity.save();
        dbList = ofy().load().type( ChildEntity.class ).list();
        assertEquals( dbList.size(), 0, "Children list size" );
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
        assertEquals( parent.getVersion(), Integer.valueOf( 2 ), message );

        message = "Child entity with ignored 2 level child has been saved too many times";
        assertEquals( childEntity.getVersion(), Integer.valueOf( 2 ), message );

        // testing whether one to many entity update is being ignored
        ChildEntity oneToMany = new ChildEntity();
        parent.add( oneToMany );
        parent.save();
        ofy().clear();

        List<ChildEntity> dbList = ofy().load().type( ChildEntity.class ).list();
        assertEquals( dbList.size(), 2, "Children list size" );

        oneToMany = new ChildEntity();
        parent.add( oneToMany );
        parent.newCascading().ignore( "children" );
        parent.save();
        ofy().clear();

        parent = ofy().load().type( ParentEntity.class ).id( parent.getId() ).now();
        dbList = ofy().load().type( ChildEntity.class ).list();
        assertEquals( dbList.size(), 2, "Children list size" );

        List<ChildEntity> children = parent.getChildren();
        assertNotNull( children );
        assertEquals( children.size(), 1, "One to many  children list size" );
        assertNotNull( parent.children, "Persisted one to many children references" );
        assertEquals( parent.children.size(), 1, "Persisted one to many children list size" );
    }

    @Test
    public void cascadingSaveNullFields()
    {
        ParentEntity parent = new ParentEntity();
        parent.save();

        String message = "Parent entity has been saved too many times";
        assertEquals( parent.getVersion(), Integer.valueOf( 2 ), message );
    }
}
