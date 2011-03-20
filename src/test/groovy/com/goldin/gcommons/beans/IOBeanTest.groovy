package com.goldin.gcommons.beans

import com.goldin.gcommons.BaseTest
import org.junit.Test

/**
 * {@link IOBean} tests
 */
class IOBeanTest extends BaseTest
{
    @Test
    void testResource()
    {
        assert   ioBean.resource( 'emptyTestResource.txt' )
        assert   ioBean.resource( '/emptyTestResource.txt' )
        assert ! ioBean.resource( 'emptyTestResource.txt' ).available()
        assert   ioBean.resource( 'testResource.txt' )
        assert   ioBean.resource( 'testResource.txt' ).available()
        assert   ioBean.resource( '/testResource.txt' )
        assert   ioBean.resource( '/testResource.txt' ).available()
        assert   ioBean.resource( 'gradle-0.9.jar' )
        assert   ioBean.resource( 'gradle-0.9.jar' ).available()
        assert   ioBean.resource( '/gradle-0.9.jar' )
        assert   ioBean.resource( '/gradle-0.9.jar' ).available()
    }

    @Test
    void testResourceText()
    {
        assert '' ==  ioBean.resourceText( 'emptyTestResource.txt' )
        assert '' !=  ioBean.resourceText( 'testResource.txt' )
        assert "1-2-3-4:5-10:100<code>''''''</code>" ==  ioBean.resourceText( 'testResource.txt' )
    }
}
