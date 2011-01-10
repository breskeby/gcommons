package com.goldin.gcommons.beans

import com.goldin.gcommons.BaseTest
import org.junit.Test
import static com.goldin.gcommons.Constants.*


 /**
 * {@link NetBean} tests
 */
class NetBeanTest extends BaseTest
{

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
        assert   netBean.isFtp( 'ftp://user:password@host:path' )
        assert ! netBean.isFtp( 'stp://user:password@host:path' )
        assert ! netBean.isFtp( 'scp://user:password@host:path' )
        assert   netBean.isScp( 'scp://user' )
        assert   netBean.isScp( 'scp://user:password@host:path' )
        assert ! netBean.isScp( 'ftp://user:password@host:path' )
        assert   netBean.isHttp( 'http://user' )
        assert   netBean.isHttp( 'http://user:password@host:path' )
        assert ! netBean.isHttp( 'htp://user:password@host:path' )
    }


    @Test
    void shouldMatchNetworkPattern()
    {
        assert ZYMIC_FTP ==~ NETWORK_PATTERN
        assert ZYMIC_FTP  =~ NETWORK_PATTERN
        assert "ftp://user:password@server.com:/pa"    ==~ NETWORK_PATTERN
        assert "ftp://user:password@server.com:/"       =~ NETWORK_PATTERN
        assert "http://user:password@server.com:/pat"  ==~ NETWORK_PATTERN
        assert "http://user:password@server.com:/path"  =~ NETWORK_PATTERN
        assert "scp://user:password@server.com:/path"  ==~ NETWORK_PATTERN
        assert "scp://user:password@server.com:/path"   =~ NETWORK_PATTERN
    }


    @Test
    void shouldListFtpFiles()
    {
        def htmlFiles = netBean.listFiles( ZYMIC_FTP, ['*.html'] )
        def indexFile = netBean.listFiles( ZYMIC_FTP, ['index.html'] )
        def jarFiles  = netBean.listFiles( ZYMIC_FTP, ['apache-maven-3.0.1/lib/*.jar'] )
        def txtFiles  = netBean.listFiles( ZYMIC_FTP, ['apache-maven-3.0.1/*.txt'] )

        assert 1 == htmlFiles.size()
        assert 1 == indexFile.size()
        assert indexFile[ 0 ].name == 'index.html'
        assert indexFile[ 0 ].size == 1809

        assert jarFiles.size() == 5
        assert jarFiles*.name  == [ 'apache-maven-3.0.1/lib/wagon-file-1.0-beta-7.jar',
                                    'apache-maven-3.0.1/lib/wagon-http-lightweight-1.0-beta-7.jar',
                                    'apache-maven-3.0.1/lib/wagon-http-shared-1.0-beta-7.jar',
                                    'apache-maven-3.0.1/lib/wagon-provider-api-1.0-beta-7.jar',
                                    'apache-maven-3.0.1/lib/xercesMinimal-1.9.6.2.jar' ]
        assert jarFiles*.size  == [ 11063, 14991, 25516, 53227, 39798 ]
        assert txtFiles.size() == 3
        assert txtFiles*.name  == [ 'apache-maven-3.0.1/LICENSE.txt',
                                    'apache-maven-3.0.1/NOTICE.txt',
                                    'apache-maven-3.0.1/README.txt' ]
        assert txtFiles*.size  == [ 11560, 1030, 2559 ]
    }
}