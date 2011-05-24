package com.goldin.gcommons.beans

import com.goldin.gcommons.GCommons
import java.lang.reflect.Array
import org.springframework.util.AntPathMatcher
import org.apache.commons.exec.*
import static com.goldin.gcommons.GCommons.*


/**
 * General usage methods
 */
class GeneralBean extends BaseBean
{
    /**
     * {@link org.springframework.util.PathMatcher#match(String, String)} wrapper
     * @param path    path to match
     * @param pattern pattern to use, prepended with {@link org.springframework.util.AntPathMatcher#DEFAULT_PATH_SEPARATOR}
     *                                if path start with {@link org.springframework.util.AntPathMatcher#DEFAULT_PATH_SEPARATOR}
     *
     * @return true if path specified matches the pattern,
     *         false otherwise
     */
    boolean match ( String path, String pattern )
    {
        verify().notNullOrEmpty( path, pattern )

        ( path, pattern ) = [ path, pattern ]*.replaceAll( /\\+/, AntPathMatcher.DEFAULT_PATH_SEPARATOR )

        if ( path.startsWith( AntPathMatcher.DEFAULT_PATH_SEPARATOR ) != pattern.startsWith( AntPathMatcher.DEFAULT_PATH_SEPARATOR ))
        {   // Otherwise, false is returned
            pattern = "${ AntPathMatcher.DEFAULT_PATH_SEPARATOR }${ pattern }"
        }

        new AntPathMatcher().match( pattern, path )
    }


    /**
     * Retrieves first non-null object.
     * @param objects objects to check
     * @return first non-null object
     */
    public <T> T choose ( T ... objects )
    {
        def      result = ( T ) objects.find { it != null }
        assert ( result != null ), "All objects specified are null"
        result
    }


    /**
     * Attempts to execute a closure specified and return its result.
     *
     * @param nTries     number of time execution will be attempted
     * @param resultType expected type of result to be returned by closure,
     *                   if <code>null</code> - result type check is not performed
     * @param c          closure to invoke
     * @return closure execution result
     * @throws RuntimeException if execution fails nTries times
     */
    public <T> T tryIt( int nTries, Class<T> resultType, Closure c )
    {
        assert ( nTries > 0 )
        verify().notNull( c )

        def tries = 0

        while( true )
        {
            try
            {
                Object value = c()
                assert ( resultType == null ) || ( value != null ), \
                       "Result returned is null, should be of type [$resultType]"
                assert ( resultType == null ) || resultType.isInstance( value ), \
                       "Result returned [$value] is of type [${ value.class }], should be of type [$resultType]"
                return (( T ) value )
            }
            catch ( Throwable t )
            {
                assert tries < nTries
                if (( ++tries ) == nTries )
                {
                    throw new RuntimeException( "Failed to perform action after [$tries] attempt${s( tries )}: $t", t )
                }
            }
        }
    }


    /**
     * Returns '' if number specified is 1, 's' otherwise. Used for combining plural sentences in log messages.
     * @param n number to check
     * @return '' if number specified is 1, 's' otherwise
     */
    String s( Number n ) { ( n == 1 ) ? '' : 's' }


    /**
     * {@code "Object.metaClass.splitWith"} wrapper - splits object to "pieces" uses method specified.
     *
     * @param o          object to split
     * @param methodName name of the method to use, the method should accept a Closure argument
     * @return           list of objects returned by iterating method
     */
    public <T> List<T> splitWith( Object o, String methodName, Class<T> type = Object ) { o.splitWith( methodName, type ) }


    /**
     * Retrieves an array combined from values provided:
     * <ul>
     * <li> If first parameter specified is not <code>null</code> - it is returned as is
     * <li> If second parameter specified is not <code>null</code> - it is returned as a single-element array
     * <li> Otherwise, empty array is returned
     * </ul>
     *
     * @param array    first option to check
     * @param instance single instance to return as a single-element array if <code>array</code> is <code>null</code>
     * @param type     element's type (required for creating an array)
     * @param <T>      element's type
     *
     * @return         see above
     */
    public <T> T[] array( T[] array, T instance, Class<T> type )
    {
        if ( array != null )
        {
            array
        }
        else if ( instance != null )
        {
            T[] newArray = (( T[] ) Array.newInstance( type, 1 ))
            newArray[ 0 ] = instance
            newArray
        }
        else
        {
            (( T[] ) Array.newInstance( type, 0 ))
        }
    }


    /**
     * Determines if current OS is Windows according to "os.name" system property
     * @return true  if current OS is Windows,
     *         false otherwise
     */
    boolean isWindows()
    {
        System.getProperty( 'os.name' ).toLowerCase().contains( 'windows' )
    }


    /**
     * Executes the command specified.
     *
     * @param command     command to execute
     * @param option      strategy for executing the command, ExecOption.CommonsExec by default
     * @param stdout      OutputStream to send command's stdout to, System.out by default
     * @param stderr      OutputStream to send command's stderr to, System.err by default
     * @param timeoutMs   command's timeout in ms, 5 min by default.
     *                    Returns immediately if zero value is specified, blocks and waits for process
     *                    to terminate if negative value is specified, blocks and waits amount of
     *                    milliseconds specified if positive value is specified.
     * @param directory   process working directory
     * @param environment environment to pass to process started
     *
     * @return           command exit value or -1 if negative or zero timeout was specified
     */
    int execute ( String       command,
                  ExecOption   option      = ExecOption.CommonsExec,
                  OutputStream stdout      = System.out,
                  OutputStream stderr      = System.err,
                  long         timeoutMs   = ( 5 * GCommons.constants().MILLIS_IN_MINUTE ) /* 5 min */,
                  File         directory   = new File( GCommons.constants().USER_DIR ),
                  Map          environment = new HashMap( System.getenv()))
    {
        GCommons.verify().notNullOrEmpty( command )
        GCommons.verify().directory( directory )

        switch ( option )
        {
            case ExecOption.CommonsExec:

                Executor executor = new DefaultExecutor()
                executor.with {
                    streamHandler    = new PumpStreamHandler( stdout, stderr )
                    workingDirectory = directory
                    watchdog         = ( timeoutMs == 0 ) ? null :
                                       ( timeoutMs  < 0 ) ? new ExecuteWatchdog( ExecuteWatchdog.INFINITE_TIMEOUT ) :
                                                            new ExecuteWatchdog( timeoutMs )
                }

                return executor.execute( CommandLine.parse( command ), environment )

            case ExecOption.Runtime:

                return handleProcess( command.execute(), stdout, stderr, timeoutMs )

            case ExecOption.ProcessBuilder:

                ProcessBuilder builder = new ProcessBuilder( command ).directory( directory )
                builder.environment() << environment
                return handleProcess( builder.start(), stdout, stderr, timeoutMs )

            default:
                assert false : "Unknown option [$option]. Known options are ${ ExecOption.values() }"
        }
    }


    /**
     * Handles the process specified.
     *
     * @param p         process to handle
     * @param stdout    standard output to send process output stream
     * @param stderr    error output to send process error stream
     * @param timeoutMs process execution timeout in milliseconds,
     *                  execution is asynchronous if timeout is zero
     * @return          process exit code
     */
    private int handleProcess( Process p, OutputStream stdout, OutputStream stderr, long timeoutMs )
    {
        p.consumeProcessOutputStream( stdout )
        p.consumeProcessErrorStream ( stderr )

        int exitValue = -1

        if ( timeoutMs != 0 )
        {
            ( timeoutMs < 0 ) ? p.waitFor() : p.waitForOrKill( timeoutMs )
            exitValue = p.exitValue()
        }

        exitValue
    }


    /**
     * Creates a decorated multi-line <code>Collection</code> representation where each element is prepended
     * with a prefix and optional space.
     *
     * @param          c <code>Collection</code> to iterate
     * @param prefix   decoration prefix
     * @param padSize  elements spacing, starting from the second element
     * @param crlf     line separator to use, system's 'line.separator' by default
     * @return         multi-line <code>Collection</code> representation where each element is prepended with a prefix,
     *                 empty <code>String</code> if collection is empty
     */
    String stars ( Collection c, String prefix = '* ', int padSize = 0, String crlf = constants().CRLF )
    {
        ( c ? "$prefix[${ c.join( "]$crlf${ ' ' * padSize }$prefix[") }]" : '' )
    }
}


/**
 * Strategy for executing the command, see {@link GeneralBean#execute}
 */
public enum ExecOption
{
    /**
     * Apache Commons Exec {@link Executor} is used
     */
    CommonsExec,

    /**
     * {@link Runtime#getRuntime()} is used
     */
    Runtime,


    /**
     * {@link ProcessBuilder} is used
     */
    ProcessBuilder
}
