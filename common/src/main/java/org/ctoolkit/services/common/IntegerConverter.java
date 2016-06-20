package org.ctoolkit.services.common;

/**
 * Internal helper class to convert input object to {@link Integer}.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
class IntegerConverter
        implements Converter<Integer>
{
    private static IntegerConverter INSTANCE;

    static IntegerConverter instance()
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new IntegerConverter();
        }

        return INSTANCE;
    }

    @Override
    public Integer convert( Object object )
    {
        if ( object == null )
        {
            return null;
        }

        try
        {
            return Integer.valueOf( object.toString() );
        }
        catch ( NumberFormatException e )
        {
            return null;
        }
    }
}
