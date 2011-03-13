package com.goldin.gcommons.beans

import com.goldin.gcommons.BaseTest
import org.junit.Test

 /**
 * Tests {@link AlgorithmsBean}
 */
class AlgorithmsTest extends BaseTest
{

    static def TEST_ARRAYS = [
        [[],              []],
        [[1],             [1]],
        [[0],             [0]],
        [[-1],            [-1]],
        [[1, 2],          [1, 2]],
        [[2, 1],          [1, 2]],
        [[2, 2],          [2, 2]],
        [[2, 2, 0],       [0, 2, 2]],
        [[2, 2, 1],       [1, 2, 2]],
        [[1, 2, 1],       [1, 1, 2]],
        [[1, 2, 3, 4, 5], [1, 2, 3, 4, 5]],
        [[1, 2, 3, 5, 4], [1, 2, 3, 4, 5]],
        [[5, 4, 3, 2, 1], [1, 2, 3, 4, 5]],
        [[5, 6, 8, 2, 1], [1, 2, 5, 6, 8]],
        [[5, 5, 5, 5, 5], [5, 5, 5, 5, 5]],
        [[5, 6, 8, 2, 1, -1, -3, -12345, 0, 0],
         [-12345, -3, -1, 0, 0, 1, 2, 5, 6, 8]],
        [[10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2, -3, -4, -5],
         [-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]],
        [[1, 3, 6, 43, 12, 78, 123, -98, -23, 0, 0, 2, 4, 5, 78, 93 ],
         [-98, -23, 0, 0, 1, 2, 3, 4, 5, 6, 12, 43, 78, 78, 93, 123]]
    ]


    /**
     * Retrieves a number of arrays with random content.
     * @param randomSize whether arrays returned should be of random size as well
     *
     * @return number of arrays with random content
     */
    static List<int[]> randomArrays( boolean randomSize = true )
    {
        def random = new Random( new Random( System.currentTimeMillis()).nextLong())
        def size   = System.getProperty( 'slowTests' ) ?  9999 : 999
        def list   = []

        3.times {
            def arraySize = ( randomSize ? random.nextInt( size ) + 11 : size )
            def array     = new int[ arraySize ]
            for ( j in ( 0 ..< array.length )){ array[ j ] = random.nextInt() }
            list << array
        }

        list
    }

    
    void applySort( SortOption option )
    {
        println "Testing --== [$option] ==-- sorting method"

        for ( arraysList in TEST_ARRAYS )
        {
            int[] input    = new ArrayList( arraysList[ 0 ] ) as int[] // Making a copy for println() below
            int[] expected = arraysList[ 1 ] as int[]
            int[] output   = algBean.sort( arraysList[ 0 ] as int[], option )

            assert output == expected, "$output != $expected"
            println "$input => $output"
        }

        def arrays = randomArrays( false )
        print "Testing sort of random arrays of size [${ arrays.first().size()}]: "

        for ( int[] randomArray in arrays )
        {
            long t = System.currentTimeMillis()
            algBean.sort( randomArray, option )
            print "[${ System.currentTimeMillis() - t }] ms, "
        }

        println "Ok"

        print "Testing sort of random arrays of random size: "
        for ( int[] randomArray in randomArrays())
        {
            long t = System.currentTimeMillis()
            algBean.sort( randomArray, option )
            print "[$randomArray.length] - [${ System.currentTimeMillis() - t }] ms, "
        }

        println "Ok"
    }


    @SuppressWarnings( 'JUnitTestMethodWithoutAssert' )
    @Test
    void selectionSort () { applySort( SortOption.Selection ) }

    
    @SuppressWarnings( 'JUnitTestMethodWithoutAssert' )
    @Test
    void insertionSort () { applySort( SortOption.Insertion ) }


    @SuppressWarnings( 'JUnitTestMethodWithoutAssert' )
    @Test
    void mergeSort () { applySort( SortOption.Merge ) }


    @SuppressWarnings( 'JUnitTestMethodWithoutAssert' )
    @Test
    void quickSort () { applySort( SortOption.Quick ) }


    @Test
    void binarySearch()
    {
        for ( arraysList in TEST_ARRAYS )
        {
            int[] inputArray  = arraysList[ 0 ] as int[]
            int[] sortedArray = arraysList[ 1 ] as int[]

            for ( int j in inputArray )
            {
                assert j == sortedArray[ algBean.binarySearch( sortedArray, j ) ]
            }
        }

        def randomArrays = randomArrays()
        print "Testing binary search in random arrays: "
        
        def checkArray = {
            int[] array, int j ->

            int index = algBean.binarySearch( array, j )
            if ( index < 0 ) { assert ( ! array.any{ it == j } ) }
            else             { assert j == array[ index ]        }
        }

        for ( int[] sortedArray in randomArrays.collect{ algBean.sort( it ) })
        {
            long t = System.currentTimeMillis()
            
            for ( int j in sortedArray )
            {
                assert j == sortedArray[ algBean.binarySearch( sortedArray, j ) ]
                checkArray( sortedArray, j )
                
                if ( j < Integer.MAX_VALUE ) { checkArray( sortedArray, j + 1 ) }
                if ( j > Integer.MIN_VALUE ) { checkArray( sortedArray, j - 1 ) }
            }
            
            print "[$sortedArray.length] - [${ System.currentTimeMillis() - t }] ms, "
        }

        /**
         * "Find an Item in a Sorted Array with Shifted Elements"
         * http://www.technicalinterviewquestions.net/2009/02/sorted-array-shifted-elements-search.html
         */
        assert 0 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 2 )
        assert 0 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 2, 6  )
        assert 0 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 2, -6 )
        assert 1 == algBean.binarySearch( [ 11, 2, 3, 4, 8, 10 ] as int[], 2, 1  )
        assert 1 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 3 )
        assert 3 == algBean.binarySearch( [ 10, 11, 2, 3, 4, 8 ] as int[], 3, 2  )
        assert 2 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 4 )
        assert 1 == algBean.binarySearch( [ 3, 4, 8, 10, 11, 2 ] as int[], 4, -1 )
        assert 0 == algBean.binarySearch( [ 4, 8, 10, 11, 2, 3 ] as int[], 4, -2 )
        assert 3 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 8 )
        assert 4 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 10 )
        assert 5 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 11 )
        assert 5 == algBean.binarySearch( [ 2, 3, 4, 8, 10, 11 ] as int[], 11, 6 )
        assert 4 == algBean.binarySearch( [ 3, 4, 8, 10, 11, 2 ] as int[], 11, 5 )

        println "Ok"
    }


    @Test
    void maxRange()
    {
        def maxRange = { List<Integer> list -> algBean.maxRange( list as int[] ) }

        assert [ -1, -1 ] == maxRange( [] )
        assert [  0,  0 ] == maxRange( [  1 ] )
        assert [ -1, -1 ] == maxRange( [ -1 ] )
        assert [  0,  1 ] == maxRange( [ 1, 2 ] )
        assert [  0,  1 ] == maxRange( [ 1, 2, -3 ] )
        assert [  0,  1 ] == maxRange( [ 1, 2, -3, 2 ] )
        assert [  3,  3 ] == maxRange( [ 1, 2, -3, 4 ] )
        assert [  0,  3 ] == maxRange( [ 2, 2, -3, 4 ] )
        assert [  0,  1 ] == maxRange( [ 2, 5, -10, 4 ] )
        assert [  3,  3 ] == maxRange( [ 2, 5, -10, 10 ] )
        assert [  3,  5 ] == maxRange( [ 2, 5, -10, 10, 20, 30 ] )
        assert [  5,  5 ] == maxRange( [ 2, 5, -10, 10, -20, 30 ] )
        assert [  7,  7 ] == maxRange( [ -1, 1, 2, -3, 4, 5, -10,  100 ] )
        assert [  7,  7 ] == maxRange( [ -1, 1, 2, -4, 4, 5, -10,  100 ] )
        assert [  4,  7 ] == maxRange( [ -1, 1, 2, -4, 4, 205, -101, 100 ] )
        assert [  4, 12 ] == maxRange( [ -1, 1, 2, -4, 6, 5, -10, 100, 20, -50, 51, -50, 49, -100, -200, 0, 3, -5, 6 ] )
    }


    @Test
    void convert()
    {
        def check = {
            long number, int base, String result ->

            assert result == algBean.convert( number, base )
            println "[$number] = [$result], base [$base]"
        }

        check( 0,           2, '0'     )
        check( 1,           2, '1'     )
        check( 2,           2, '10'    )
        check( 3,           2, '11'    )
        check( 5,           2, '101'   )
        check( 12345,      10, '12345' )
        check( 12345,       2, '11000000111001' )
        check( 12345,      16, '3039'   )
        check( 678910,     10, '678910' )
        check( 678910,      2, '10100101101111111110' )
        check( 678910,      8, '2455776'  )
        check( 678910,     16, 'A5BFE'    )
        check( 3405691582, 16, 'CAFEBABE' )
        check( 3735928559, 16, 'DEADBEEF' )
        check( 2881150637, 16, 'ABBADEAD' )
    }
}
