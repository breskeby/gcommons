package com.goldin.gcommons.beans

import org.codehaus.groovy.reflection.ReflectionUtils

/**
 * I/O-related utilities
 */
class IOBean extends BaseBean
{
    long copy ( InputStream input, OutputStream output, long bytesExpected = -1 )
    {
        byte[] buffer         = new byte[ 2 * 1024 ]
        long   totalBytesRead = 0

        for ( int bytesRead = 0; (( bytesRead = input.read( buffer )) != -1 ); totalBytesRead += bytesRead )
        {
            output.write( buffer, 0, bytesRead )
        }

        close( input, output )

        if ( bytesExpected > -1 )
        {
            assert ( totalBytesRead == bytesExpected ), "[$bytesExpected] bytes should be read but [$totalBytesRead] bytes were read"
        }

        totalBytesRead
    }


    Closeable close ( Closeable ... closeables )
    {
        for ( c in closeables )
        {
            try { if ( c != null ) { c.close() }}
            catch ( IOException ignored ) {}
        }

        first( closeables )
    }

    /**
     * Retrieves resource specified using calling class ClassLoader.
     *
     * @param resource resource path
     * @return input stream to the resource specified
     */
    InputStream resource( String resource )
    {
        assert resource
        resource   = ( resource.startsWith( '/' ) ? resource : '/' + resource )
        assert resource.startsWith( '/' )

        def cl     = ReflectionUtils.getCallingClass( 0 )
        def stream = cl.getResourceAsStream( resource )

        if ( ! stream )
        {
            def urls = ( cl.classLoader instanceof URLClassLoader ) ?
                (( URLClassLoader ) cl.classLoader).URLs :
                []
            assert false, "Failed to load resource [$resource] using ClassLoader of class [$cl.name]:\n${ stars( urls as List ) }"
        }

        stream
    }


    /**
     * Retrieves text of the resource specified using calling class ClassLoader.
     *
     * @param resourceName resource path
     * @param charset resource charset, <code>"UTF-8"</code> by default
     * @return
     */
    String resourceText( String resourceName, String charset = 'UTF-8' ) { resource( resourceName ).getText( charset ) }
}
