package com.goldin.gcommons.beans

import com.goldin.gcommons.BaseTest
import org.junit.Test

/**
 * Tests {@link AlgorithmsBean}
 */
class AlgorithmsTest extends BaseTest
{

    static final def arrays = [
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
        [[1, 3, 6, 43, 12, 78, 123, -98, -23, 0, 0, 2, 4, 5, 78, 93 ],
         [-98, -23, 0, 0, 1, 2, 3, 4, 5, 6, 12, 43, 78, 78, 93, 123]]
    ]


    void applySort( SortOption option )
    {
        for ( array in arrays )
        {
            int[] input    = new ArrayList( array[ 0 ] ) as int[] // Making a copy for println() below
            int[] expected = array[ 1 ] as int[]
            int[] output   = algBean.sort( array[ 0 ] as int[], option )

            assert output == expected, "$output != $expected"
            println "$input => $output"
        }
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
}
