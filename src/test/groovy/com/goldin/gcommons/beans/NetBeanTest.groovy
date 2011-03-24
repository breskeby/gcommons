package com.goldin.gcommons.beans

import com.goldin.gcommons.BaseTest
import org.apache.commons.net.ftp.FTPFile
import org.junit.Test
import org.junit.Ignore

/**
 * {@link NetBean} tests
 */
class NetBeanTest extends BaseTest
{
    private static final String  ZYMIC_FTP = 'ftp://evgenyg_zxq:sdaed432e23@evgenyg.zxq.net:/'

    @Test
    void shouldParseNetworkPath()
    {
        def map = netBean.parseNetworkPath( 'ftp://someUser:somePassword@someServer:/somePath' )
        assert [ 'ftp', 'someUser', 'somePassword', 'someServer', '/somePath' ] ==
               [ map.protocol, map.username, map.password, map.host, map.directory ]

        map = netBean.parseNetworkPath( 'scp://another.user:strange@passw@rd@aaa.server.com:/' )
        assert [ 'scp', 'another.user', 'strange@passw@rd', 'aaa.server.com', '/' ] ==
               [ map.protocol, map.username, map.password, map.host, map.directory ]

        map = netBean.parseNetworkPath( 'http://another.-weir.d.user:even-more.!strange@passw@rd@address.server.com:path' )
        assert [ 'http', 'another.-weir.d.user', 'even-more.!strange@passw@rd', 'address.server.com', 'path' ] ==
               [ map.protocol, map.username, map.password, map.host, map.directory ]
    }


    @Test
    void shouldRecognizeNetworkPath()
    {
        assert   netBean.isFtp( 'ftp://user' )
        assert   netBean.isFtp( 'ftp://user',  'ftp://user' )
        assert   netBean.isFtp( 'ftp://user',  'ftp://user', 'ftp://user' )
        assert ! netBean.isFtp( 'ftp://user',  'ftp://user', 'ftp://user', 'ftp1://user' )
        assert ! netBean.isFtp( 'ftp2://user', 'ftp://user', 'ftp://user', 'ftp://user' )
        assert ! netBean.isFtp( 'scp://user',  'ftp://user', 'ftp://user', 'ftp://user' )
        assert ! netBean.isFtp( ' ftp://user', 'ftp://user', 'ftp://user' )
        assert   netBean.isFtp( 'ftp://user:password@host:path' )
        assert ! netBean.isFtp( 'stp://user:password@host:path' )
        assert ! netBean.isFtp( 'scp://user:password@host:path' )

        assert   netBean.isScp( 'scp://user' )
        assert   netBean.isScp( 'scp://user', 'scp://user' )
        assert   netBean.isScp( 'scp://user', 'scp://user', 'scp://user' )
        assert ! netBean.isScp( ' scp://user', 'scp://user', 'scp://user' )
        assert ! netBean.isScp( 'http://user', 'scp://user', 'scp://user' )
        assert ! netBean.isScp( 'scp://user', 'scp ://user' )
        assert   netBean.isScp( 'scp://user:password@host:path' )
        assert ! netBean.isScp( 'ftp://user:password@host:path' )

        assert   netBean.isHttp( 'http://user' )
        assert   netBean.isHttp( 'http://user', 'http://user' )
        assert   netBean.isHttp( 'http://user', 'http://user', 'http://user' )
        assert ! netBean.isHttp( 'http ://user', 'http://user', 'http://user' )
        assert ! netBean.isHttp( 'http://user', 'scp://user', 'http://user' )
        assert ! netBean.isHttp( 'http://user', 'http://user', 'ftp://user' )
        assert   netBean.isHttp( 'http://user:password@host:path' )
        assert ! netBean.isHttp( 'htp://user:password@host:path' )

        assert   netBean.isNet( 'http://user:password@host:path' )
        assert   netBean.isNet( 'scp://user', 'ftp://user' )
        assert   netBean.isNet( 'scp://user' )
        assert   netBean.isNet( 'ftp://user' )
        assert   netBean.isNet( 'http://user:password@host:path', 'scp://user', 'ftp://user' )
        assert ! netBean.isNet( 'htp://user:password@host:path', 'scp://user', 'ftp://user' )
        assert ! netBean.isNet( 'http://user:password@host:path', 'scp ://user', 'ftp://user' )
        assert ! netBean.isNet( 'http://user:password@host:path', 'scp://user', 'fttp://user' )

        assert ! netBean.isFtp()
        assert ! netBean.isFtp( null )
        assert ! netBean.isScp()
        assert ! netBean.isScp( null )
        assert ! netBean.isHttp()
        assert ! netBean.isHttp( null )
        assert ! netBean.isNet()
        assert ! netBean.isNet( null )
    }


    @Test
    void shouldMatchNetworkPattern()
    {
        assert ZYMIC_FTP ==~ constantsBean.NETWORK_PATTERN
        assert ZYMIC_FTP  =~ constantsBean.NETWORK_PATTERN
        assert "ftp://user:password@server.com:/pa"    ==~ constantsBean.NETWORK_PATTERN
        assert "ftp://user:password@server.com:/"       =~ constantsBean.NETWORK_PATTERN
        assert "http://user:password@server.com:/pat"  ==~ constantsBean.NETWORK_PATTERN
        assert "http://user:password@server.com:/path"  =~ constantsBean.NETWORK_PATTERN
        assert "scp://user:password@server.com:/path"  ==~ constantsBean.NETWORK_PATTERN
        assert "scp://user:password@server.com:/path"   =~ constantsBean.NETWORK_PATTERN
    }


    @Ignore
    void shouldListFtpFiles()
    {
        def htmlFiles = netBean.listFiles( ZYMIC_FTP, ['*.html'] )
        def indexFile = netBean.listFiles( ZYMIC_FTP, ['index.html'] )
        def jarFiles  = netBean.listFiles( ZYMIC_FTP, ['apache-maven-3.0.1/lib/*.jar'] )
        def txtFiles  = netBean.listFiles( ZYMIC_FTP, ['apache-maven-3.0.1/*.txt'] )

        assert 1 == htmlFiles.size()
        assert 1 == indexFile.size()
        assert indexFile[ 0 ].name == 'index.html'
        assert indexFile[ 0 ].fullPath.endsWith( ':/index.html' )
        assert indexFile[ 0 ].fullPath == ZYMIC_FTP + 'index.html'
        assert indexFile[ 0 ].path == '/index.html'
        assert indexFile[ 0 ].size == 1809

        assert jarFiles.size() == 5
        assert jarFiles*.name  == [ 'apache-maven-3.0.1/lib/wagon-file-1.0-beta-7.jar',
                                    'apache-maven-3.0.1/lib/wagon-http-lightweight-1.0-beta-7.jar',
                                    'apache-maven-3.0.1/lib/wagon-http-shared-1.0-beta-7.jar',
                                    'apache-maven-3.0.1/lib/wagon-provider-api-1.0-beta-7.jar',
                                    'apache-maven-3.0.1/lib/xercesMinimal-1.9.6.2.jar' ]
        assert jarFiles*.fullPath.every { it.startsWith( 'ftp://' ) && it.endsWith( '.jar' ) }

        assert jarFiles[ -2 ].fullPath.endsWith( ':/apache-maven-3.0.1/lib/wagon-provider-api-1.0-beta-7.jar' )
        assert jarFiles[ -2 ].fullPath == ZYMIC_FTP + 'apache-maven-3.0.1/lib/wagon-provider-api-1.0-beta-7.jar'

        assert jarFiles[ -1 ].fullPath.endsWith( ':/apache-maven-3.0.1/lib/xercesMinimal-1.9.6.2.jar' )
        assert jarFiles[ -1 ].fullPath == ZYMIC_FTP + 'apache-maven-3.0.1/lib/xercesMinimal-1.9.6.2.jar'

        assert jarFiles[ -2 ].path == '/apache-maven-3.0.1/lib/wagon-provider-api-1.0-beta-7.jar'
        assert jarFiles[ -1 ].path == '/apache-maven-3.0.1/lib/xercesMinimal-1.9.6.2.jar'
        assert jarFiles*.size  == [ 11063, 14991, 25516, 53227, 39798 ]

        assert txtFiles.size() == 3
        assert txtFiles*.name  == [ 'apache-maven-3.0.1/LICENSE.txt',
                                    'apache-maven-3.0.1/NOTICE.txt',
                                    'apache-maven-3.0.1/README.txt' ]
        assert txtFiles*.fullPath.every { it.startsWith( 'ftp://' ) && it.endsWith( '.txt' ) }

        assert txtFiles[ -2 ].fullPath.endsWith( ':/apache-maven-3.0.1/NOTICE.txt' )
        assert txtFiles[ -2 ].fullPath == ZYMIC_FTP + 'apache-maven-3.0.1/NOTICE.txt'

        assert txtFiles[ -1 ].fullPath.endsWith( ':/apache-maven-3.0.1/README.txt' )
        assert txtFiles[ -1 ].fullPath == ZYMIC_FTP + 'apache-maven-3.0.1/README.txt'

        assert txtFiles[ -2 ].path == '/apache-maven-3.0.1/NOTICE.txt'
        assert txtFiles[ -1 ].path == '/apache-maven-3.0.1/README.txt'
        assert txtFiles*.size  == [ 11560, 1030, 2559 ]
    }


    @Test
    void shouldListFtpFilesWithMultiplePatterns()
    {
        def files = netBean.listFiles( ZYMIC_FTP, [ '*.html', '*.zip', '*.jar', '*.xml' ] )
        assert files.size() == 4
        assert [ 'index.html', 'net.jar', 'net.zip', 'pom.xml' ].every { String filename -> files.any { GFTPFile file -> file.name.endsWith( filename ) }}
    }


    @Test
    void shouldListFtpFilesWithExcludes()
    {
        def fileNames = [ 'wagon-file-1.0-beta-7.jar', 'wagon-provider-api-1.0-beta-7.jar', 'xercesMinimal-1.9.6.2.jar' ]
        def files1    = netBean.listFiles( ZYMIC_FTP, ['apache-maven-3.0.1/lib/*.jar'],      [ '**/wagon-http*' ] )
        def files2    = netBean.listFiles( ZYMIC_FTP, [ '**/lib/*.jar'], [ 'apache-maven-3.0.1/lib/wagon-http-lightweight-1.0-beta-7.jar',
                                                                           'apache-maven-3.0.1/lib/wagon-http-shared-1.0-beta-7.jar' ] )
        def files3    = netBean.listFiles( ZYMIC_FTP + 'apache-maven-3.0.1', [ 'lib/*.jar'], [ 'lib/wagon-http-*.jar' ] )
        def files4    = netBean.listFiles( ZYMIC_FTP + 'apache-maven-3.0.1', [ '**/*.jar'],  [ 'boot/**', '**/lib/wagon-h*.jar' ] )

        assert [ files1, files2, files3, files4 ].every {
            List<FTPFile> files ->
            ( files.size() == fileNames.size()) &&
            ( fileNames.each{    String fileName -> files*.name.any{ it.endsWith( fileName ) }} ) &&
            ( files*.name.every{ String fileName -> fileNames.any{ fileName.endsWith( it )   }} )
        }
    }


    @Test
    void shouldListFtpDirectories()
    {
        def    files = netBean.listFiles( ZYMIC_FTP, [ '*' ], [], 5, true )
        assert files.size() == 11
        assert files.findAll { it.isDirectory() }.size() == 4
        assert [ 'bin', 'boot', 'conf', 'lib' ].every{ String dirName -> files.any{ it.name.endsWith( dirName ) }}

        files = netBean.listFiles( ZYMIC_FTP, [ 'apache-maven-3.0.1/*' ], [], 5, true )
        assert files.size() == 16
        assert files.findAll { it.isDirectory() }.size() == 0 // "apache-maven-3.0.1/*" pattern doesn't list directories any more
        assert files.findAll { it.isDirectory() }.every { it.path.contains( '/apache-maven-3.0.1/' ) }

        files = netBean.listFiles( ZYMIC_FTP, [ '*' ] )
        assert files.size() == 7
    }


    @Test
    void testHttpGet()
    {
        /**
         * http://groovy.codehaus.org/modules/http-builder/doc/get.html
         */

        netBean.http( [ host : 'google.com' ], {})
    }

}
