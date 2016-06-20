package org.ctoolkit.services.common;

/**
 * Internal helper class to convert input object to {@link Double}.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
class DoubleConverter
        implements Converter<Double>
{
    private static DoubleConverter INSTANCE;

    static DoubleConverter instance()
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new DoubleConverter();
        }

        return INSTANCE;
    }

    @Override
    public Double convert( Object object )
    {
        if ( object == null )
        {
            return null;
        }

        try
        {
            return Double.valueOf( object.toString() );
        }
        catch ( NumberFormatException e )
        {
            return null;
        }
    }
}
