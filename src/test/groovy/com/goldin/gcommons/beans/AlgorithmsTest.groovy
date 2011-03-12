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

        def randomArrays = randomArrays( false )
        print "Testing sort of random arrays of size [${ randomArrays.first().size()}]: "

        for ( int[] randomArray in randomArrays )
        {
            long t = System.currentTimeMillis()
            algBean.sort( randomArray, option )
            print "[${ System.currentTimeMillis() - t }] ms, "
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

        println "Ok"
    }
}
