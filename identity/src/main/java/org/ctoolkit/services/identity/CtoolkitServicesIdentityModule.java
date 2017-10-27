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

package org.ctoolkit.services.identity;

import com.google.appengine.api.utils.SystemProperty;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Strings;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

/**
 * The CtoolkiT Services Google Firebase based identity module.
 * The following optional properties are allowed to be configured via dependency injection:
 * <ul>
 * <li>credential.identity.credentialOn</li>
 * <li>credential.identity.fileName</li>
 * <li>credential.identity.projectId</li>
 * <li>credential.identity.databaseName</li>
 * </ul>
 * If credentialOn is true (by default is false) the fileName is mandatory and will be used to authenticate calls.
 * If projectId or databaseName is not configured, the current App Engine application Id will be used.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CtoolkitServicesIdentityModule
        extends AbstractModule
{
    private static final Logger logger = LoggerFactory.getLogger( CtoolkitServicesIdentityModule.class );

    @Override
    protected void configure()
    {
    }

    @Provides
    @Singleton
    IdentityHandler provideIdentityHandler( IdentityApiInit init )
            throws IOException
    {
        logger.info( "credential.identity.credentialOn: " + init.credentialOn );
        logger.info( "credential.identity.fileName:" + init.fileName );
        logger.info( "credential.identity.projectId: " + init.projectId );
        logger.info( "credential.identity.databaseName: " + init.databaseName );

        FirebaseOptions options;
        String databaseName = init.databaseName;
        String projectId = init.projectId;

        if ( Strings.isNullOrEmpty( databaseName ) )
        {
            databaseName = SystemProperty.applicationId.get();
        }

        if ( Strings.isNullOrEmpty( projectId ) )
        {
            projectId = SystemProperty.applicationId.get();
        }

        String databaseUrl = "https://" + databaseName + ".firebaseio.com";
        logger.info( "The final Firebase database URL: " + databaseUrl );

        if ( !init.credentialOn && SystemProperty.environment.value() == SystemProperty.Environment.Value.Production )
        {

            options = new FirebaseOptions.Builder()
                    .setCredentials( GoogleCredentials.getApplicationDefault() )
                    .setDatabaseUrl( databaseUrl )
                    .setProjectId( projectId )
                    .build();

            logger.info( "Firebase-admin built with application default credentials." );
        }
        else
        {
            if ( Strings.isNullOrEmpty( init.fileName ) )
            {
                String msg = "The property 'credential.identity.fileName' for current configuration is mandatory.";
                throw new IllegalArgumentException( msg );
            }

            URL url = CtoolkitServicesIdentityModule.class.getResource( init.fileName );
            if ( url == null )
            {
                String msg = "The file defined by property 'credential.identity.fileName' "
                        + init.fileName + " has not been found.";
                throw new IllegalArgumentException( msg );
            }
            FileInputStream serviceAccount = new FileInputStream( url.getPath() );

            options = new FirebaseOptions.Builder()
                    .setCredentials( GoogleCredentials.fromStream( serviceAccount ) )
                    .setDatabaseUrl( databaseUrl )
                    .setProjectId( projectId )
                    .build();

            logger.info( "Firebase-admin built with credentials from file." );
        }

        FirebaseApp.initializeApp( options );

        return new IdentityHandler();
    }

    static class IdentityApiInit
    {
        @Inject( optional = true )
        @Named( "credential.identity.credentialOn" )
        boolean credentialOn = false;

        @Inject( optional = true )
        @Named( "credential.identity.fileName" )
        String fileName;

        @Inject( optional = true )
        @Named( "credential.identity.projectId" )
        String projectId;

        @Inject( optional = true )
        @Named( "credential.identity.databaseName" )
        String databaseName;
    }
}
