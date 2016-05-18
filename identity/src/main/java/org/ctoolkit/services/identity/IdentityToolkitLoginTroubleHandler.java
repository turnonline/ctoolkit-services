package org.ctoolkit.services.identity;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.identitytoolkit.GitkitClient;
import com.google.identitytoolkit.GitkitServerException;
import org.ctoolkit.restapi.client.identity.IdentityToolkitClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import static com.google.identitytoolkit.GitkitClient.OobAction.RESET_PASSWORD;

/**
 * The servlet to handle 'reset password' action requested by identity toolkit.
 * In case of successful oob URL retrieval it sends request to backend REST API to send reset password email.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
public class IdentityToolkitLoginTroubleHandler
        extends HttpServlet
{
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger( IdentityToolkitLoginTroubleHandler.class );

    @SuppressWarnings( "NonSerializableFieldInSerializableClass" )
    private final IdentityToolkitClient client;

    private final Set<IdentityTroubleListener> listeners;

    private String loginPath;

    @Inject
    public IdentityToolkitLoginTroubleHandler( IdentityToolkitClient client, Set<IdentityTroubleListener> listeners )
    {
        this.client = client;
        this.listeners = listeners;
    }

    @Override
    public void init( ServletConfig config ) throws ServletException
    {
        super.init( config );
        this.loginPath = config.getInitParameter( IdentityToolkitCheckSessionFilter.LOGIN_PATH );

        if ( Strings.isNullOrEmpty( this.loginPath ) )
        {
            throw new IllegalArgumentException( "Identity filter LOGIN_PATH attribute must be configured!" );
        }
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException
    {
        try ( PrintWriter writer = response.getWriter() )
        {
            IdentityToolkitClient.OobResponse oobResponse = client.getOobResponse( request, loginPath );

            String email = oobResponse.getEmail();
            GitkitClient.OobAction action = oobResponse.getOobAction();

            if ( action != null )
            {
                switch ( action )
                {

                    case RESET_PASSWORD:
                        Optional<String> oobUrl = oobResponse.getOobUrl();
                        if ( oobUrl.isPresent() )
                        {
                            String resetLink = oobUrl.get();
                            for ( IdentityTroubleListener listener : listeners )
                            {
                                listener.resetPassword( email, resetLink );
                            }
                        }
                        else
                        {
                            logger.warn( "The OobUrl is not present, email: " + email + " action: " + RESET_PASSWORD );
                        }
                        break;
                    case CHANGE_EMAIL:
                    {
                        for ( IdentityTroubleListener listener : listeners )
                        {
                            listener.changeEmail( null, email, oobResponse.getNewEmail() );
                        }
                    }
                    break;
                }
            }
            else
            {
                logger.warn( "The OobAction is null, email: " + email );
            }

            writer.append( oobResponse.getResponseBody() );
        }
        catch ( GitkitServerException e )
        {
            logger.error( "An error has occurred while calling identity toolkit", e );
            response.setStatus( HttpStatusCodes.STATUS_CODE_SERVER_ERROR );
        }
    }
}
