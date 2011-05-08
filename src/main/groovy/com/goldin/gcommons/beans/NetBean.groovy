package com.goldin.gcommons.beans

import groovy.util.slurpersupport.GPathResult
import java.util.regex.Matcher
import net.sf.json.JSONObject
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import org.apache.http.HttpResponse
import groovyx.net.http.*
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static com.goldin.gcommons.GCommons.*


/**
 * Network-related helper methods.
 */
class NetBean extends BaseBean
{
    boolean isHttp ( String ... s ) { s && s.every{ it && it.toLowerCase().startsWith( 'http://' ) }}
    boolean isScp  ( String ... s ) { s && s.every{ it && it.toLowerCase().startsWith( 'scp://'  ) }}
    boolean isFtp  ( String ... s ) { s && s.every{ it && it.toLowerCase().startsWith( 'ftp://'  ) }}
    boolean isNet  ( String ... s ) { s && s.every{ isHttp( it ) || isScp( it ) || isFtp( it )     }}


    /**
     * Parses network path in the following format:
     * {@code "(http|scp|ftp)://user:password@server:/path/to/file"}
     *
     * @param path network path to parse
     * @return map with following entries: "protocol", "username", "password", "host", "directory"
     */
    Map<String, String> parseNetworkPath( String path )
    {
        assert isNet( verify().notNullOrEmpty( path ))
        Matcher matcher = ( path =~ constants().NETWORK_PATTERN )

        assert ( matcher.find() && ( matcher.groupCount() == 5 )), \
               "Unable to parse [$path] as network path: it should be in format [<protocol>://<user>:<password>@<host>:<path>]. " +
               "Regex pattern is [${ constants().NETWORK_PATTERN }]"

        def ( String protocol, String username, String password, String host, String directory ) =
            matcher[ 0 ][ 1 .. 5 ].collect{ verify().notNullOrEmpty( it ) }

        [
            protocol  : protocol,
            username  : username,
            password  : password,
            host      : host,
            directory : directory.replace( '\\', '/' )
        ]
    }


    /**
     * Initializes and connects an {@link FTPClient} using remote path specified of form:
     * {@code ftp://<user>:<password>@<host>:<path>}
     *
     * @param remotePath remote path to establish ftp connection to: {@code ftp://<user>:<password>@<host>:<path>}
     * @return client instance initialized and connected to FTP server specified
     */
    FTPClient ftpClient( String remotePath )
    {
        Map       data   = parseNetworkPath( remotePath )
        FTPClient client = new FTPClient()

        getLog( this ).info( "Connecting to FTP server [$data.host:$data.directory] as [$data.username] .." )

        try
        {
            client.connect( data.host )
            int reply = client.getReplyCode()
            assert FTPReply.isPositiveCompletion( reply ),          "Failed to connect to FTP server [$data.host], reply code is [$reply]"
            assert client.login( data.username, data.password ),    "Failed to connect to FTP server [$data.host] as [$data.username]"
            assert client.changeWorkingDirectory( data.directory ), "Failed to change FTP server [$data.host] directory to [$data.directory]"
            client.setFileType( FTP.BINARY_FILE_TYPE )
            client.enterLocalPassiveMode()
        }
        catch ( Throwable t )
        {
            client.logout()
            client.disconnect()
            throw new RuntimeException( "Failed to connect to FTP server [$remotePath]: $t", t )
        }

        getLog( this ).info( "Connected to FTP server [$data.host:$data.directory] as [$data.username]. " +
                             "Remote system is [$client.systemName], status is [$client.status]" )
        client
    }


    /**
     * Initializes and connects an {@link FTPClient} using remote path specified of form:
     * {@code ftp://<user>:<password>@<host>:<path>}. When connected, invokes the closure specified, passing
     * it {@link FTPClient} instance connected, and disconnects the client.
     *
     * @param remotePath remote path to establish ftp connection to: {@code ftp://<user>:<password>@<host>:<path>}
     * @param resultType closure expected result type,
     *                   if <code>null</code> - result type check is not performed
     * @param c closure to invoke and pass {@link FTPClient} instance
     * @return closure invocation result
     */
    public <T> T ftpClient( String remotePath, Class<T> resultType, Closure c )
    {
        verify().notNullOrEmpty( remotePath )
        verify().notNull( c, resultType )

        FTPClient client = null

        try
        {
            client = ftpClient( remotePath )
            return general().tryIt( 1, resultType ){ c( client ) }
        }
        finally
        {
            if ( client )
            {
                client.logout()
                client.disconnect()
            }
        }
    }


    /**
     * Lists files on the FTP server specified.
     *
     * @param remotePath      remote path to establish ftp connection to: <code>"ftp://<user>:<password>@<host>:<path>"</code>
     * @param globPatterns    glob patterns of files to list: <code>"*.*"</code> or "<code>*.zip"</code>
     * @param excludes        exclude patterns of files to exclude, empty by default
     * @param tries           number of attempts, <code>5</code> by default
     * @param listDirectories whether directories should be returned in result, <code>false</code> by default
     *
     * @return FTP files listed by remote FTP server using glob patterns specified
     */
    List<GFTPFile> listFiles( String       remotePath,
                              List<String> globPatterns    = [ '*' ],
                              List<String> excludes        = null,
                              int          tries           = 10,
                              boolean      listDirectories = false )
    {
        verify().notNullOrEmpty( remotePath )
        assert tries > 0

        /**
         * Trying "tries" times to list files
         */
        general().tryIt( tries, List,
        {   /**
             * Getting a list of files for remote path
             */
            ftpClient( remotePath, List )
            {
                FTPClient client ->

                List<GFTPFile> result = []

                getLog( this ).info( "Listing $globPatterns${ excludes ? '/' + excludes : '' } files .." )

                for ( String globPattern in globPatterns*.trim().collect{ verify().notNullOrEmpty( it ) } )
                {
                    List<GFTPFile> gfiles = client.listFiles( globPattern ).
                                            findAll { it != null }.
                                            findAll { FTPFile  file -> (( file.name != '.' ) && ( file.name != '..' )) }.
                                            collect { FTPFile  file -> new GFTPFile( file, remotePath, globPattern ) }.
                                            findAll { GFTPFile file -> listDirectories ? true /* all entries */ : ( ! file.isDirectory()) /* files */ }.
                                            findAll { GFTPFile file -> ( ! excludes.any{ String exclude -> general().match( file.name, exclude ) ||
                                                                                                           exclude.endsWith( file.name ) } ) }

                    getLog( this ).info( "[$globPattern] - [${ gfiles.size() }] file${ general().s( gfiles.size() ) }" )
                    if ( getLog( this ).isDebugEnabled()) { getLog( this ).debug( "\n" + general().stars( gfiles*.path ))}

                    result.addAll( gfiles )
                }

                getLog( this ).info( "[${ result.size() }] file${ general().s( result.size()) }" )
                result
            }
        })
    }


    void http( Map config, Closure callback )
    {
        /**
         * Copying configuration map.
         * Every time a value is read it is removed from the copy and we verify that it is empty when all values
         * are read - to prevent user errors when wrong keys are specified.
         */
        Map<String, String> configCopy       = new HashMap( config )
        def                 c                = { String key                         -> configCopy.remove( key ) }
        def                 readBoolean      = { Object value, boolean defaultValue -> ( value == null ) ?
                                                                                            defaultValue :
                                                                                            verify().isInstance( value, Boolean )
        }
        def responseText                     = {
            HttpResponse response -> verify().notNull( new InputStreamReader( response.entity.content,
                                                                            ParserRegistry.getCharset( response )).text.trim())
        }

        String              charset          = c( 'charset' )     ?: 'UTF-8'
        String              contentType      = c( 'contentType' ) ?: "text/xml; charset=$charset"
        String              path             = c( 'path' )        ?: '/'
        boolean             passObject       = readBoolean( c( 'object'           ), false ) // false by default
        boolean             failOnError      = readBoolean( c( 'failOnError'      ), true  ) // true  by default
        boolean             verbose          = readBoolean( c( 'verbose'          ), true  ) // true  by default
        boolean             checkContentType = readBoolean( c( 'checkContentType' ), true  ) // true  by default
        String              resource         = c( 'resource' )
        ContentType         type             = JSON.contentTypeStrings.grep( contentType ) ? JSON : XML
        String              postData         = resource ? io().resourceText( resource ) : c( 'data' ) // Allowed to be null
        Map                 extraHeaders     = ( Map         ) c( 'headers' )
        Method              method           = ( Method      ) c( 'method'  ) ?: ( postData ? POST : GET )
        HTTPBuilder         service          = ( HTTPBuilder ) c( 'service' )

        if ( ! service )
        {
            String host = c( 'host' )
            int    port = ( c( 'port' ) ?: System.getProperty( 'defaultPort' ) ?: 80 ) as int
            service     = new HTTPBuilder( "http://$host${( port == 80 ) ? '' : ':' + port }" )
        }

        verify().isInstance( service, HTTPBuilder )
        verify().isInstance( method,  Method      )
        verify().isInstance( type,    ContentType )

        assert configCopy.isEmpty(), "Config keys left unread: ${ configCopy.keySet()}"

        /**
         * http://groovy.codehaus.org/HTTP+Builder
         * http://groovy.codehaus.org/modules/http-builder/
         * http://groovy.codehaus.org/modules/http-builder/apidocs/groovyx/net/http/HTTPBuilder.RequestConfigDelegate.html
         */

        long t                  = System.currentTimeMillis()
        service.encoderRegistry = new EncoderRegistry( charset: charset )
        service.request( method, type ) {
            request ->

            uri.path                  = verify().notNullOrEmpty( path )
            headers[ 'Content-Type' ] = verify().notNullOrEmpty( contentType )

            if ( postData )     { send( type, postData )         }
            if ( extraHeaders ) { headers.putAll( extraHeaders ) }

            def headersString = headers.collect{ String key, String value ->  "'$key'".padRight( 20 ) + " : '$value'" }.sort()
            def logMessage    = """
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Sending HTTP request
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Data        : [${ resource ?: postData ?: 'None' }]${ resource ? ' - [' + io().resource( resource ) + ']' : '' }
URL         : [${ service.uri }${ path.startsWith( '/' ) ? '' : '/' }${ path }]
Method      : [${ method }]
Type        : [${ type }]
Charset     : [${ charset }]
Pass Object : [${ passObject }]
Headers     : ${ general().stars( headersString, '', 'Headers     : '.size())}
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"""

            if ( verbose ) { println logMessage }


            def responseHandler = {
                HttpResponse response, Object responseObject ->

                def statusCode = response.statusLine.statusCode
                assert ( statusCode > 0 ), "Unknown status code [$statusCode] in response"
                if ( checkContentType ){ assert ParserRegistry.getContentType( response ), "Missing \"Content-Type\" in response" }

                if ( verbose ) { println "Done, status code [$statusCode], " +
                                         "${ checkContentType ? 'content type [' + ParserRegistry.getContentType( response ) + '], ' : '' }" +
                                         "[${ System.currentTimeMillis() - t }] ms" }

                if ( failOnError ) { assert 200 == statusCode, "Response status code is [$statusCode]" }
                GResponse gresponse = new GResponse( response : response )

                if ( passObject )
                {
                    gresponse.object = verify().notNull( responseObject )
                }
                else
                {
                    assert     responseObject == null // We don't have it, see "response.success" assignment below
                    gresponse.object = responseText( response )
                }

                callback( gresponse.validate( verbose ))
            }

            /**
             * "HttpBuilder: is it possible to read both response object (like XML or JSON) and String response?"
             * http://groovy.329449.n5.nabble.com/HttpBuilder-is-it-possible-to-read-both-response-object-like-XML-or-JSON-and-String-response-td3406887.html
             */
            response.success = ( passObject ? { HttpResponse response, Object responseObject -> responseHandler( response, responseObject ) } :
                                              { HttpResponse response                        -> responseHandler( response, null           ) } )

            response.failure = {
                HttpResponse response ->

                def statusCode = response.statusLine.statusCode
                if ( verbose     ) { System.err.println( "Done (error!), status code [$statusCode], [${ System.currentTimeMillis() - t }] ms" )}
                if ( failOnError ) { assert false, ( logMessage + "\nHTTP request failed and returned [$statusCode] :\n[${ responseText( response ) }]" )}
                else               { responseHandler( response, null )}
            }
        }
    }
}


/**
 * {@link FTPFile} extension providing file's full {@code "ftp://user:pass@server:/path"} and remote path {@code "/path"}.
 */
class GFTPFile
{
    @Delegate FTPFile file

    String  fullPath
    String  path
    boolean directory

    GFTPFile ( FTPFile file, String remotePath, String globPattern )
    {
        assert ( file != null ) && file.name && remotePath, "File [$file], name [$file.name], path [$remotePath] - should be defined"

        this.file       = file
        def patternPath = globPattern.replace( '\\', '/' ).replaceFirst( /^\//, '' ).replaceAll( /\/?[^\/]+$/, '' ) // "/aaaa/bbbb/*.zip" => "aaaa/bbbb"
        def filePath    = "${ file.name.startsWith( patternPath ) ? '' : patternPath + '/' }$file.name"             // "aaaa/bbbb/file.zip"
        this.fullPath   = "$remotePath/$filePath".replace( '\\', '/' ).replaceAll( /(?<!ftp:)\/+/, '/' )            // "ftp://user:pass@server:/path/aaaa/bbbb/file.zip"
        this.path       = this.fullPath.replaceAll( /.+:/, '' )                                                     // "/path/aaaa/bbbb/file.zip"
        this.directory  = file.rawListing.startsWith( 'd' )
    }


    @Override
    public String toString() { "[${ this.rawListing }][${ this.path }]" }
}


/**
 * Wraps {@link NetBean#http(Map, Closure)} response passed to the callback.
 */
class GResponse extends BaseBean
{
    HttpResponse response
    Object       object

    GResponse validate( boolean verbose = true )
    {
        assert ( response != null ), "Response is null"
        assert ( object   != null ), "Response object is null"

        if ( verbose ) { println "Response \"object\" is [${ object.getClass().name }]" }
        this
    }

    String      getContent() { verify().isInstance( object, String      ) }
    JSONObject  getJson()    { verify().isInstance( object, JSONObject  ) }
    GPathResult getXml()     { verify().isInstance( object, GPathResult ) }
}
