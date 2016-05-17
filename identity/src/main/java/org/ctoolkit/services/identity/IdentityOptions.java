package org.ctoolkit.services.identity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Identity options value holder.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
public class IdentityOptions
        implements Serializable
{
    private static final long serialVersionUID = 649739600329865627L;

    private String signInSuccessUrl;

    private String signOutUrl;

    private String oobActionUrl;

    private String apiKey;

    private String siteName;

    private DisplayMode displayMode;

    private List<SignInOption> signInOptions = new ArrayList<>();

    public IdentityOptions()
    {
    }

    public String getSignInSuccessUrl()
    {
        return signInSuccessUrl;
    }

    public void setSignInSuccessUrl( String signInSuccessUrl )
    {
        this.signInSuccessUrl = signInSuccessUrl;
    }

    public String getSignOutUrl()
    {
        return signOutUrl;
    }

    public void setSignOutUrl( String signOutUrl )
    {
        this.signOutUrl = signOutUrl;
    }

    public String getOobActionUrl()
    {
        return oobActionUrl;
    }

    public void setOobActionUrl( String oobActionUrl )
    {
        this.oobActionUrl = oobActionUrl;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public void setApiKey( String apiKey )
    {
        this.apiKey = apiKey;
    }

    public String getSiteName()
    {
        return siteName;
    }

    public void setSiteName( String siteName )
    {
        this.siteName = siteName;
    }

    public DisplayMode getDisplayMode()
    {
        return displayMode;
    }

    public void setDisplayMode( DisplayMode displayMode )
    {
        this.displayMode = displayMode;
    }

    public List<SignInOption> getSignInOptions()
    {
        return signInOptions;
    }

    public String getSignInOptionsAsString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "[" );

        for ( SignInOption signInOption : getSignInOptions() )
        {
            if ( sb.length() > 1 )
            {
                sb.append( "," );
            }
            sb.append( "\"" ).append( signInOption.getType() ).append( "\"" );
        }

        sb.append( "]" );

        return sb.toString();
    }

    public enum DisplayMode
    {
        EMAIL_FIRST( "emailFirst" ),
        PROVIDER_FIRST( "providerFirst" );

        private String type;

        DisplayMode( String type )
        {
            this.type = type;
        }

        public String getType()
        {
            return type;
        }
    }

    public enum SignInOption
    {
        PASSWORD( "password" ),
        GOOGLE( "google" ),
        FACEBOOK( "facebook" ),
        YAHOO( "yahoo" ),
        PAYPAL( "paypal" ),
        AOL( "aol" ),
        MICROSOFT( "microsoft" ),
        TWITTER( "twitter" );

        private String type;

        SignInOption( String type )
        {
            this.type = type;
        }

        public String getType()
        {
            return type;
        }
    }
}