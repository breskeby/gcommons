package com.goldin.gcommons.beans

import com.goldin.gcommons.Constants
import java.util.regex.Matcher
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply

 /**
 * Network-related helper methods.
 */
class NetBean extends BaseBean
{

    /**
     * Other beans, set by Spring
     */
    GeneralBean general
    VerifyBean  verify


    private boolean isHttp ( String s ) { s.toLowerCase().startsWith( 'http://' ) }
    private boolean isScp  ( String s ) { s.toLowerCase().startsWith( 'scp://'  ) }
    private boolean isFtp  ( String s ) { s.toLowerCase().startsWith( 'ftp://'  ) }
    private boolean isNet  ( String s ) { isHttp( s ) || isScp( s ) || isFtp( s ) }


    /**
     * Parses network path in the following format:
     * "(http|scp|ftp)://user:password@server:/path/to/file"
     *
     *
     * @param path network path to parse
     * @return map with following entries: "protocol", "username", "password", "host", "directory"
     */
    private Map<String, String> parseNetworkPath( String path )
    {
        assert isNet( verify.notNullOrEmpty( path ))
        Matcher matcher = ( path =~ Constants.NETWORK_PATTERN )

        assert ( matcher.find() && ( matcher.groupCount() == 5 )), \
               "Unable to parse [$path] as network path: it should be in format [<protocol>://<user>:<password>@<server>:<path>]. " +
               "Regex pattern is [${ Constants.NETWORK_PATTERN }]"

        def ( String protocol, String username, String password, String host, String directory ) =
            matcher[ 0 ][ 1 .. 5 ].collect{ verify.notNullOrEmpty( it ) }

        [
            protocol  : protocol,
            username  : username,
            password  : password,
            host      : host,
            directory : directory.replace( '\\', '/' )
        ]
    }


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

        getLog( this ).info( "Connected to FTP server [$data.host:$data.directory] as [$data.username]. Remote system is [${ client.getSystemName()}]" )
        client
    }


    public <T> T ftpClient( String remotePath, Class<T> resultType, Closure c )
    {
        verify.notNullOrEmpty( remotePath )
        verify.notNull( c, resultType )

        FTPClient client

        try
        {
            client       = ftpClient( remotePath )
            Object value = c.call( client )
            assert ( value != null ),              "Result returned is null, should be of type [$resultType]"
            assert resultType.isInstance( value ), "Result returned [$value] is of type [${ value.class }], should be of type [$resultType]"
            return (( T ) value )
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


    List<FTPFile> listFiles( String remotePath, String includePatterns, int tries = 5 )
    {
        List<String> includes = includePatterns.split( /\s*,\s*/ )*.trim().collect { verify.notNullOrEmpty( it ) }

        general.tryIt( tries, List.class,
        {
            ftpClient( remotePath, List.class )
            {
                FTPClient client ->

                List<FTPFile> result = []

                getLog( this ).info( "Listing $includes files .." )

                includes.each {
                    String includePattern ->
                    FTPFile[] files = client.listFiles( includePattern )
                    if ( getLog( this ).isDebugEnabled())
                    {
                        getLog( this ).debug( "[$includePattern] - ${ files*.name.join( '\n' ) }" )
                    }
                    result.addAll( files )
                }

                getLog( this ).info( "[${ result.size() }] file${ general.s( result.size()) }: ${ result*.name }" )
                result
            }
        })
    }
}
