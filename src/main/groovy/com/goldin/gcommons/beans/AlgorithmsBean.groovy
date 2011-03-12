package com.goldin.gcommons.beans


/**
 * Implementation of various algorithms
 */
class AlgorithmsBean extends BaseBean
{

    int[] sort ( int[] data, SortOption option = SortOption.Quick, boolean validate = true )
    {
        int[] sortedData = ( option == SortOption.Selection ) ? selectionSort( data ) :
                           ( option == SortOption.Insertion ) ? insertionSort( data ) :
                           ( option == SortOption.Merge     ) ? mergeSort    ( data ) :
                           ( option == SortOption.Quick     ) ? quickSort    ( data ) :
                                                                null
        assert ( sortedData != null ), "Unknown sorting option [$option], should be one of [${ SortOption.values() }]"

        if ( validate && data.length )
        {
            for ( j in ( 1 ..< data.length )) { assert sortedData[ j ] >= sortedData[ j - 1 ] }
        }

        sortedData
    }


    /**
     * "Selection sort" of data specified, modifies the original array.
     * http://en.wikipedia.org/wiki/Selection_sort
     * 
     * @param data input array to sort
     * @return same array object with its elements sorted in increasing order
     */
    private int[] selectionSort ( int[] data )
    {
        /**
         * Finds index of the minimal element in the range starting at "startIndex".
         */
        Closure<Integer> findMinIndex = {
            int startIndex ->
            
            int minIndex = startIndex
            for ( j in (( startIndex + 1 ) ..< data.length ))
            {
                minIndex = ( data[ j ] < data[ minIndex ] ? j : minIndex )
            }

            minIndex
        }

        for ( j in ( 0 ..< data.length ))
        {
            int minIndex = findMinIndex( j )
            
            if ( minIndex != j )
            {
                int temp         = data[ j ]
                data[ j ]        = data[ minIndex ]
                data[ minIndex ] = temp
            }
        }

        data
    }


    /**
     * "Insertion sort" of data specified, modifies the original array.
     * http://en.wikipedia.org/wiki/Insert_sort
     *
     * @param data input array to sort
     * @return same array object with its elements sorted in increasing order
     */
    private int[] insertionSort ( int[] data, int arrayStart = 0, int arrayEnd = data.length )
    {
        /**
         * Finds index to insert the element specified in the range ending at "end" index.
         */
        def findInsertIndex = {
            int elem, int start, int end ->
            
            for ( int j in (( end - 1 ) .. start )) { if ( data[ j ] <= elem ){ return ( j + 1 ) }}
            start
        }

        /**
         * Move elements right in the range starting at "start" and ending at "end" indexes.
         */
        def moveRight = {
            int start, int end ->

            assert ( end > start ), "moverRight( $data, $start, $end ): [$end] <= [$start]"
            for ( int j in (( end - 1 ) .. start )) { data[ j + 1 ] = data[ j ]}
        }

        if ( arrayEnd - arrayStart )
        {
            for ( j in (( arrayStart + 1 ) ..< arrayEnd ))
            {
                int elem        = data[ j ]
                int insertIndex = findInsertIndex( elem, arrayStart, j )

                if ( insertIndex < j )
                {   // If there's a need to move, elem can be at the right place already
                    moveRight( insertIndex, j )
                    data[ insertIndex ] = elem
                }
            }
        }

        data
    }

    
    /**
     * "Merge sort" of data specified, modifies the original array.
     * http://en.wikipedia.org/wiki/Merge_sort
     *
     * @param data input array to sort
     * @return same array object with its elements sorted in increasing order
     */
    private int[] mergeSort( int[] data )
    {
        int[] helper        = new int[ data.length ]
        def findMiddleIndex = { int start, int end -> ( start + (( end - start ) >> 1 )) }

        /**
         * Sorts and merges two "halfs" specified:
         * - First half starts at index "start" and ends at index "middle - 1"
         * - Second half starts at index "middle" and ends at index "end - 1"
         */
        Closure mergeSortHelper
        mergeSortHelper = {
            int start, int middle, int end ->

            if (( middle - start ) < 8 )
            {
                insertionSort( data, 0, middle   )
                insertionSort( data, middle, end )
            }
            else
            {
                mergeSortHelper( 0, findMiddleIndex( 0, middle ), middle )
                mergeSortHelper( middle, findMiddleIndex( middle, end ), end )
            }

            if (( middle > start ) && ( data[ middle ] < data [ middle - 1 ] ))
            {
                def leftIndex  = start
                def rightIndex = middle
                for ( helperIndex in ( start ..< end ))
                {
                    def dataIndex = ( leftIndex  >= middle ) ?                   rightIndex++ :
                                    ( rightIndex >= end    ) ?                   leftIndex++  :
                                    ( data[ leftIndex ] < data[ rightIndex ] ) ? leftIndex++  :
                                                                                 rightIndex++

                    helper[ helperIndex ] = data[ dataIndex ]
                }

                System.arraycopy( helper, start, data, start, ( end - start ))
            }
        }

        mergeSortHelper( 0, findMiddleIndex( 0, data.length ), data.length )
        data
    }


    /**
     * "Quick sort" of data specified, modifies the original array.
     * http://en.wikipedia.org/wiki/Quicksort
     *
     * @param data input array to sort
     * @return same array object with its elements sorted in increasing order
     */
    private int[] quickSort( int[] data )
    {
        data
    }



    int binarySearch ( int element, int[] data, boolean recursion = true )
    {

        null
    }
}


/**
 * Strategy for executing the sort()
 */
public enum SortOption
{
   /**
    * http://en.wikipedia.org/wiki/Selection_sort
    */
    Selection,

   /**
    * http://en.wikipedia.org/wiki/Insert_sort
    */
   Insertion,

    /**
     * http://en.wikipedia.org/wiki/Merge_sort
     */
     Merge,

   /**
    * http://en.wikipedia.org/wiki/Quicksort
    */
    Quick,
}
