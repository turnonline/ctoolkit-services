package org.ctoolkit.services.common;

/**
 * Internal helper class to convert input object to {@link String}.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
class StringConverter
        implements Converter<String>
{
    private static StringConverter INSTANCE;

    static StringConverter instance()
    {
        if ( INSTANCE == null )
        {
            INSTANCE = new StringConverter();
        }

        return INSTANCE;
    }

    @Override
    public String convert( Object object )
    {
        if ( object == null )
        {
            return null;
        }

        return object.toString();
    }
}
