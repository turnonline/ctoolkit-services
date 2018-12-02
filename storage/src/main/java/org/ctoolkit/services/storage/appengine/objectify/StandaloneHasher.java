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

package org.ctoolkit.services.storage.appengine.objectify;

import org.ctoolkit.services.storage.PropertiesHashCode;
import org.ctoolkit.services.storage.PropertiesHasher;

import java.util.Map;

/**
 * Use this as entity reference if you need more {@link PropertiesHasher} implementations
 * within single entity.
 * <p>
 * <strong>Example of the implementation:</strong>
 * <pre>
 *  &#64;Entity
 *  public class MytEntity
 *          extends EntityLongIdentity
 *  {
 *      private Ref&#60;AnotherHasher&#62; anotherHashCode;
 *
 *      // as many as you need
 *      private Ref&#60;ThirdHasher&#62; thirdHashCode;
 *
 *      public AnotherHasher getAnotherHashCode()
 *      {
 *          return anotherHashCode == null ? null : anotherHashCode.get();
 *      }
 *
 *      &#64;OnSave
 *      private void onSave()
 *      {
 *          if ( anotherHashCode == null )
 *          {
 *              AnotherHasher hashCode = new AnotherHasher();
 *              hashCode.save();
 *              this.anotherHashCode = Ref.create( hashCode );
 *          }
 *      }
 *
 *      &#64;Entity( name = "Another_HashCode" )
 *      public class AnotherHasher
 *              extends StandaloneHasher
 *      {
 *          &#64;Override
 *          protected Map&#60;String, Object&#62; propertiesMap()
 *          {
 *              Map&#60;String, Object&#62; properties = new HashMap&#60;&#62;();
 *              properties.put( "something", "Something else" );
 *              // custom list of properties
 *
 *              return properties;
 *          }
 *      }
 *  }
 * </pre>
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public abstract class StandaloneHasher
        extends PropertiesHashCode
        implements PropertiesHasher
{
    private static final long serialVersionUID = 9092008615655537629L;

    @Override
    public String calcPropsHashCode()
    {
        return calcPropsHashCode( propertiesMap() );
    }

    @Override
    public PropertiesHashCode getPropsHashCode()
    {
        return this;
    }

    protected abstract Map<String, Object> propertiesMap();
}
