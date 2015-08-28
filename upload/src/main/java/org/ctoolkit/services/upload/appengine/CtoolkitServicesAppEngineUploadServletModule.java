package org.ctoolkit.services.upload.appengine;

import com.google.inject.servlet.ServletModule;
import org.ctoolkit.services.storage.DataUploadHandler;

/**
 * The ctoolkit services AppEngine upload guice servlet module.
 *
 * @author <a href="mailto:aurel.medvegy@ctoolkit.org">Aurel Medvegy</a>
 */
public class CtoolkitServicesAppEngineUploadServletModule
        extends ServletModule
{
    @Override
    protected void configureServlets()
    {
        serve( DataUploadHandler.DATA_HANDLER_UPLOAD_URL ).with( DataUploadHandlerServlet.class );
    }
}
