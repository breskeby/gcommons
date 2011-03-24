package com.goldin.gcommons

import com.goldin.gcommons.util.MopHelper
import org.slf4j.LoggerFactory
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.support.ClassPathXmlApplicationContext
import com.goldin.gcommons.beans.*

 /**
 * "GCommons" entry points
 */
class GCommons
{
    private static final String CONTEXT_KEY         = GCommons.class.name + "_CONTEXT"
    private static final String BEANS_KEY           = GCommons.class.name + "_BEANS"
    private static final String SPRING_CONFIG_NAME  = 'spring-context.xml'
    private static final String LOGBACK_CONFIG_NAME = 'logback.groovy'

    private static Map<Class<? extends BaseBean>, ? extends BaseBean> BEANS_MAP
    private static ConfigurableApplicationContext                     CONTEXT


    /**
     * Gets a context instance, initializing it if necessary.
     *
     * @param refresh    whether context instance needs to be refreshed (reinitialized)
     * @param contextMap Map to store the context instance, optional
     * @return           context instance
     */
    private static ConfigurableApplicationContext getContext( boolean refresh, Map contextMap )
    {
        ConfigurableApplicationContext context =
            (( refresh            ) ? null    :
             ( contextMap == null ) ? CONTEXT :
                                      ( ConfigurableApplicationContext ) contextMap[ CONTEXT_KEY ] )

        /**
         * Initialization is not protected with "synchronized": if executed concurrently the context will be initialized
         * more than once, not a big deal.
         */
        if ( context == null )
        {
            def logbackConfig = GCommons.classLoader.getResource( LOGBACK_CONFIG_NAME )
            def springConfig  = GCommons.classLoader.getResource( SPRING_CONFIG_NAME  )

            assert logbackConfig, "Failed to load [$LOGBACK_CONFIG_NAME] resource"
            assert springConfig,  "Failed to load [$SPRING_CONFIG_NAME] resource"

            java.util.logging.Logger.getLogger( 'org.springframework' ).level = java.util.logging.Level.WARNING

            long t                       = System.currentTimeMillis()
            context                      = new ClassPathXmlApplicationContext( SPRING_CONFIG_NAME )
            MopHelper helper             = context.getBean( MopHelper.class )

            Object.metaClass.splitWith   = { Object[] args                       -> helper.splitWith( delegate, args ) }
            File.metaClass.recurse       = { Map configs = [:], Closure callback -> helper.recurse(( File ) delegate, configs, callback ) }
            File.metaClass.directorySize = { helper.directorySize(( File ) delegate ) }

            if ( contextMap == null )
            {
                CONTEXT   = context
                BEANS_MAP = [:]
            }
            else
            {
                contextMap[ CONTEXT_KEY ] = context
                contextMap[ BEANS_KEY   ] = [:]
            }

            LoggerFactory.getLogger( GCommons.class ).info(
                "GCommons context initialized using [$logbackConfig] and [$springConfig]: " +
                "[$context.beanDefinitionCount] beans - $context.beanDefinitionNames (${ System.currentTimeMillis() - t } ms)" )
        }

        assert context
        assert (( CONTEXT   != null ) || ( contextMap[ CONTEXT_KEY ] != null ))
        assert (( BEANS_MAP != null ) || ( contextMap[ BEANS_KEY   ] != null ))
        context
    }


    /**
     * Retrieves bean instance for the class specified.
     *
     * @param beanClass  bean class, extends {@link BaseBean}
     * @param refresh    whether a new instance should be retrieved from Spring context
     * @param contextMap Map to store context and beans cache, optional
     *
     * @return bean instance for the class specified
     */
    private static <T extends BaseBean> T getBean( Class<T> beanClass, boolean refresh, Map contextMap )
    {
        ConfigurableApplicationContext                     context  = getContext( false, contextMap )
        Map<Class<? extends BaseBean>, ? extends BaseBean> beansMap = ( contextMap == null ) ? BEANS_MAP : ( Map ) contextMap[ BEANS_KEY ]

        assert ( beansMap != null ) && ( beanClass != null ) && BaseBean.class.isAssignableFrom( beanClass )

        if ( refresh )
        {
            context.refresh()
            beansMap.clear()
        }

        beansMap[ beanClass ] = beansMap.containsKey( beanClass ) ? beansMap[ beanClass ] :
                                                                    context.getBean( beanClass )
    }


    static ConfigurableApplicationContext context   ( boolean refresh = false, Map contextMap = null ) { getContext( refresh, contextMap ) }
    static ConstantsBean                  constants ( boolean refresh = false, Map contextMap = null ) { getBean( ConstantsBean,  refresh, contextMap ) }
    static VerifyBean                     verify    ( boolean refresh = false, Map contextMap = null ) { getBean( VerifyBean,     refresh, contextMap ) }
    static GeneralBean                    general   ( boolean refresh = false, Map contextMap = null ) { getBean( GeneralBean,    refresh, contextMap ) }
    static FileBean                       file      ( boolean refresh = false, Map contextMap = null ) { getBean( FileBean,       refresh, contextMap ) }
    static IOBean                         io        ( boolean refresh = false, Map contextMap = null ) { getBean( IOBean,         refresh, contextMap ) }
    static NetBean                        net       ( boolean refresh = false, Map contextMap = null ) { getBean( NetBean,        refresh, contextMap ) }
    static GroovyBean                     groovy    ( boolean refresh = false, Map contextMap = null ) { getBean( GroovyBean,     refresh, contextMap ) }
    static AlgorithmsBean                 alg       ( boolean refresh = false, Map contextMap = null ) { getBean( AlgorithmsBean, refresh, contextMap ) }
}
