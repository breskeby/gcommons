import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import static ch.qos.logback.classic.Level.*

/**
 * http://logback.qos.ch/manual/groovy.html
 * http://logback.qos.ch/manual/layouts.html
 */

appender( 'CONSOLE', ConsoleAppender ) { encoder( PatternLayoutEncoder ) { pattern = '[%date][%-5level] [%logger] - [%msg]%n' }}
logger( 'org.springframework', WARN, [ 'CONSOLE' ] )
logger( 'com.goldin',          INFO, [ 'CONSOLE' ] )
