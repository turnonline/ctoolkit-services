package org.ctoolkit.services.identity;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.ctoolkit.restapi.client.identity.Identity;
import org.testng.annotations.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.LOGIN_PATH;
import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.REDIRECT_PATH;
import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.SESSION_AUTH_USER_ATTRIBUTE;
import static org.ctoolkit.services.identity.IdentityToolkitCheckSessionFilter.SIGN_UP_PATH;


/**
 * Testing of {@link IdentityToolkitCheckSessionFilter}.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class IdentityToolkitCheckSessionFilterTest
{
    @Tested
    private IdentityToolkitCheckSessionFilter tested;

    @Injectable
    private IdentityResolver identityResolver;

    @SuppressWarnings( "MismatchedQueryAndUpdateOfCollection" )
    @Injectable
    private Set<IdentityLoginListener> listeners = new HashSet<>();

    @Test( expectedExceptions = IllegalArgumentException.class )
    public void initNoSessionAttribute( final @Mocked FilterConfig config )
            throws Exception
    {
        tested.init( config );
    }

    @Test
    public void fullInit( final @Mocked FilterConfig config ) throws Exception
    {
        new FilterConfigExpectations( config )
        {
            {
            }
        };

        tested.init( config );
    }

    @Test
    public void processIdentity( final @Mocked HttpServletRequest request,
                                 final @Mocked HttpServletResponse response,
                                 final @Mocked HttpSession session,
                                 final @Mocked FilterChain chain,
                                 final @Mocked FilterConfig config,
                                 final @Mocked Identity identity,
                                 final @Mocked IdentityLoginListener listener ) throws Exception
    {
        listeners.add( listener );

        new FilterConfigExpectations( config )
        {
            {
                identityResolver.resolve( ( HttpServletRequest ) any );
                result = identity;

                identity.getEmail();
                result = "identity.test@ctoolkit.org";

                session.getAttribute( SESSION_ATTR_VALUE );
                result = null;
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                listener.processIdentity( request, response, identity, FilterConfigExpectations.SESSION_ATTR_VALUE );

                response.sendRedirect( anyString );
                times = 0;

                session.invalidate();
                times = 0;

                chain.doFilter( request, response );
            }
        };
    }

    @Test
    public void sendRedirect( final @Mocked HttpServletRequest request,
                              final @Mocked HttpServletResponse response,
                              final @Mocked HttpSession session,
                              final @Mocked FilterChain chain,
                              final @Mocked FilterConfig config,
                              final @Mocked Identity identity,
                              final @Mocked IdentityLoginListener listener ) throws Exception
    {
        listeners.add( listener );

        new FilterConfigExpectations( config )
        {
            {
                identityResolver.resolve( ( HttpServletRequest ) any );
                result = identity;

                identity.getEmail();
                result = "identity.test@ctoolkit.org";

                session.getAttribute( SESSION_ATTR_VALUE );
                result = "non null value";

                request.getRequestURI();
                result = FilterConfigExpectations.LOGIN_VALUE;
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                listener.processIdentity( ( HttpServletRequest ) any, ( HttpServletResponse ) any,
                        ( Identity ) any, anyString );
                times = 0;

                session.invalidate();
                times = 0;

                response.sendRedirect( FilterConfigExpectations.REDIRECT_VALUE );
                chain.doFilter( request, response );
            }
        };
    }

    @Test
    public void invalidateSession( final @Mocked HttpServletRequest request,
                                   final @Mocked HttpServletResponse response,
                                   final @Mocked HttpSession session,
                                   final @Mocked FilterChain chain,
                                   final @Mocked FilterConfig config,
                                   final @Mocked IdentityLoginListener listener ) throws Exception
    {
        listeners.add( listener );

        new FilterConfigExpectations( config )
        {
            {
                session.getAttribute( SESSION_ATTR_VALUE );
                result = "non null value";
            }
        };

        tested.init( config );
        tested.doFilter( request, response, chain );

        new Verifications()
        {
            {
                listener.processIdentity( ( HttpServletRequest ) any, ( HttpServletResponse ) any,
                        ( Identity ) any, anyString );
                times = 0;

                response.sendRedirect( FilterConfigExpectations.REDIRECT_VALUE );
                times = 0;

                session.invalidate();
                chain.doFilter( request, response );
            }
        };
    }

    private static class FilterConfigExpectations
            extends Expectations
    {
        static String SESSION_ATTR_VALUE = "session_attribute_value";

        static String REDIRECT_VALUE = "/redirect_attribute_value";

        static String SIGN_UP_VALUE = "/sign_up_attribute_value";

        static String LOGIN_VALUE = "/login_attribute_value";

        FilterConfigExpectations( FilterConfig filterConfig )
        {
            filterConfig.getInitParameter( SESSION_AUTH_USER_ATTRIBUTE );
            result = SESSION_ATTR_VALUE;

            filterConfig.getInitParameter( REDIRECT_PATH );
            result = REDIRECT_VALUE;

            filterConfig.getInitParameter( SIGN_UP_PATH );
            result = SIGN_UP_VALUE;

            filterConfig.getInitParameter( LOGIN_PATH );
            result = LOGIN_VALUE;
        }
    }
}