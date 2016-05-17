package org.ctoolkit.services.identity;

import com.google.common.base.Strings;
import org.ctoolkit.restapi.client.TokenVerifier;
import org.ctoolkit.restapi.client.identity.Identity;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * An helper class to wrap identity verification in to a standalone class.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
@Singleton
public class IdentityResolver
{
    /**
     * Default cookie name of the identity toolkit token
     */
    public static final String GTOKEN = "gtoken";

    private final TokenVerifier<Identity> tokenVerifier;

    @Inject
    public IdentityResolver( TokenVerifier<Identity> tokenVerifier )
    {

        this.tokenVerifier = tokenVerifier;
    }

    public Identity resolve( HttpServletRequest httpRequest )
    {
        Cookie[] cookies = httpRequest.getCookies();

        if ( cookies == null )
        {
            return null;
        }

        String token = null;

        for ( Cookie cookie : cookies )
        {
            if ( GTOKEN.equals( cookie.getName() ) )
            {
                token = cookie.getValue();
            }
        }

        if ( !Strings.isNullOrEmpty( token ) )
        {
            Identity json = tokenVerifier.verifyAndGet( token );

            if ( json.getExpiration().after( new Date() ) )
            {
                return json;
            }
        }

        return null;
    }

    /**
     * Delete identity toolkit token cookie.
     *
     * @param request  the HTTP servlet request
     * @param response the HTTP servlet response
     */
    public void delete( HttpServletRequest request, HttpServletResponse response )
    {
        Cookie[] cookies = request.getCookies();

        if ( cookies == null )
        {
            return;
        }

        for ( Cookie cookie : cookies )
        {
            if ( GTOKEN.equals( cookie.getName() ) )
            {
                //the zero value causes the cookie to be deleted
                cookie.setMaxAge( 0 );
                cookie.setValue( "" );
                cookie.setPath( "/" );

                response.addCookie( cookie );
            }
        }
    }
}
