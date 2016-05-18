package org.ctoolkit.services.identity;

import com.google.common.base.Strings;
import org.ctoolkit.restapi.client.identity.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * The application's session filter to manage authenticated identity toolkit user instance.
 * If there is an instance that's not presented in the session (filter config value value of the
 * {@link #SESSION_AUTH_USER_ATTRIBUTE}) filter will call
 * {@link IdentityLoginListener#processIdentity(HttpServletRequest, HttpServletResponse, Identity, String)}
 * to manage its presence (client's implementation responsibility).
 * If session has a non null user instance presented but no current authenticated identity user instance,
 * session will be invalidated.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
public class IdentityToolkitCheckSessionFilter
        implements Filter
{
    private static final Logger logger = LoggerFactory.getLogger( IdentityToolkitCheckSessionFilter.class );

    /**
     * The filter config attribute to configure key where authenticated identity user will be placed in session
     */
    public static String SESSION_AUTH_USER_ATTRIBUTE = "_identity_filter_SESSION_AUTH_USER_ATTRIBUTE";

    /**
     * The filter config attribute to configure a redirect path, for example "/my-account".
     * Once configured, logged in user, filter matching at these paths LOGIN_PATH or SIGN_UP_PATH,
     * will be redirected to this.
     */
    public static String REDIRECT_PATH = "_identity_filter_REDIRECT_TO_IF_LOGGED_IN";

    /**
     * The filter config attribute to configure a sign up path, for example "/sign-up"
     */
    public static String SIGN_UP_PATH = "_identity_filter_SIGN_UP_PATH";

    /**
     * The filter config attribute to configure a login path, for example "/login"
     */
    public static String LOGIN_PATH = "_identity_filter_LOGIN_PATH";

    private final IdentityResolver identityResolver;

    private final Set<IdentityLoginListener> listeners;

    private String sessionAttribute;

    private String loggedInRedirect;

    private String signUpPath;

    private String loginPath;

    @Inject
    public IdentityToolkitCheckSessionFilter( IdentityResolver identityResolver, Set<IdentityLoginListener> listeners )
    {
        this.identityResolver = identityResolver;
        this.listeners = listeners;
    }

    @Override
    public void init( FilterConfig filterConfig )
            throws ServletException
    {
        this.sessionAttribute = filterConfig.getInitParameter( SESSION_AUTH_USER_ATTRIBUTE );
        this.loggedInRedirect = filterConfig.getInitParameter( REDIRECT_PATH );
        this.signUpPath = filterConfig.getInitParameter( SIGN_UP_PATH );
        this.loginPath = filterConfig.getInitParameter( LOGIN_PATH );

        if ( Strings.isNullOrEmpty( this.sessionAttribute ) )
        {
            throw new IllegalArgumentException( "Session attribute must be configured. " );
        }
        if ( Strings.isNullOrEmpty( this.loggedInRedirect ) )
        {
            this.loggedInRedirect = "";
            logger.warn( "No REDIRECT_PATH has been configured!" );
        }
        if ( Strings.isNullOrEmpty( this.signUpPath ) )
        {
            this.signUpPath = "";
            logger.warn( "No SIGN_UP_PATH has been configured!" );
        }
        if ( Strings.isNullOrEmpty( this.loginPath ) )
        {
            this.loginPath = "";
            logger.warn( "No LOGIN_PATH has been configured!" );
        }
    }

    @Override
    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
            throws IOException, ServletException
    {
        HttpServletRequest httpRequest = ( HttpServletRequest ) request;
        HttpServletResponse httpResponse = ( HttpServletResponse ) response;

        Identity identity = identityResolver.resolve( httpRequest );
        String signedEmail = identity != null ? identity.getEmail() : null;

        if ( signedEmail != null )
        {
            // the user is logged in google but not in application yet
            if ( httpRequest.getSession().getAttribute( sessionAttribute ) == null )
            {
                for ( IdentityLoginListener listener : listeners )
                {
                    listener.processIdentity( httpRequest, httpResponse, identity, sessionAttribute );
                }
            }
            else
            {
                if ( !Strings.isNullOrEmpty( loggedInRedirect ) &&
                        ( httpRequest.getRequestURI().startsWith( signUpPath )
                                || httpRequest.getRequestURI().startsWith( loginPath ) ) )
                {
                    // if user is being logged in redirect him for these pages to my account
                    httpResponse.sendRedirect( loggedInRedirect );
                }
            }
        }
        else
        {
            // user is logged out in google but not in application -> invalidate session
            if ( httpRequest.getSession().getAttribute( sessionAttribute ) != null )
            {
                httpRequest.getSession().invalidate();
            }
        }

        chain.doFilter( request, response );
    }

    @Override
    public void destroy()
    {
    }
}
