package org.ctoolkit.services.common;

/**
 * Internal helper interface.
 *
 * @author <a href="mailto:jozef.pohorelec@ctoolkit.org">Jozef Pohorelec</a>
 */
interface Converter<T>
{
    T convert( Object object );
}
