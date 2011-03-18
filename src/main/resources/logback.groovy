import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.gaffer.ConfigurationDelegate
import ch.qos.logback.classic.gaffer.GafferConfigurator
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.util.ContextUtil
import com.goldin.gcommons.util.MopHelper
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN


/**
 * http://logback.qos.ch/manual/groovy.html
 * http://logback.qos.ch/manual/layouts.html
 */

/*
appender( "FILE", FileAppender  ) {
    file   = "gcommons.log"
    append = true
    encoder( PatternLayoutEncoder ) { pattern = "[%date][%-5level] [%logger] - [%msg]%n" }
}
*/


/**
 * Patching logback - specifying CL when initializing a GroovyShell
 * http://jira.qos.ch/browse/LBCLASSIC-252
 * https://github.com/ceki/logback/blob/v_0.9.28/logback-classic/src/main/groovy/ch/qos/logback/classic/gaffer/GafferConfigurator.groovy#L45
 */

GafferConfigurator.metaClass.run = {
    String dslText->
    Binding binding  = new Binding()
    binding.setProperty( "hostname", ContextUtil.getLocalHostName())
    Script dslScript = new GroovyShell( MopHelper.class.classLoader, binding ).parse( dslText ) // <==== Patch
    dslScript.metaClass.mixin(ConfigurationDelegate)
    dslScript.setContext( context )
    dslScript.metaClass.getDeclaredOrigin = { dslScript }
    dslScript.run()
}


appender( "CONSOLE", ConsoleAppender ) {
    encoder( PatternLayoutEncoder ) { pattern = "[%date][%-5level] [%logger] - [%msg]%n" }
}

//root( WARN, [ "CONSOLE" ] ) - causes "http://evgeny-goldin.org/youtrack/issue/pl-256"
logger( "org.springframework", WARN, [ "CONSOLE" ] )
logger( "com.goldin",          INFO, [ "CONSOLE" ] )
