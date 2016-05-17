package org.ctoolkit.services.identity;

import com.google.common.base.Strings;
import org.ctoolkit.restapi.client.identity.Identity;

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
 * The filter to check application's session user with a logged in user.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
@Singleton
public class IdentityToolkitCheckSessionFilter
        implements Filter
{
    public static String SESSION_AUTH_USER_ATTRIBUTE = "_identity_filter_SESSION_AUTH_USER_ATTRIBUTE";

    public static String REDIRECT_PATH = "_identity_filter_REDIRECT_TO_IF_LOGGED_IN";

    public static String SIGN_UP_PATH = "_identity_filter_SIGN_UP_PATH";

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
