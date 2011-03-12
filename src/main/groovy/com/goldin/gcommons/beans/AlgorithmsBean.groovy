package com.goldin.gcommons.beans


/**
 * Implementation of various algorithms
 */
class AlgorithmsBean extends BaseBean
{

    int[] sort ( int[] data, SortOption option = SortOption.Merge, boolean validate = true )
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
     * Moves elements N positions right in the range starting at "start" and ending at "end" indexes.
     */
    private void moveRight( int[] data, int start, int end, int positions = 1 )
    {
        assert ( end > start ), "moverRight( $data, $start, $end ): [$end] <= [$start]"
        for ( int j in (( end - 1 ) .. start )) { data[ j + positions ] = data[ j ]}
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
        if (( arrayEnd - arrayStart ) < 2 )
        {
            return data
        }
        
        /**
         * Finds index to insert the element specified in the range ending at "end" index.
         */
        def findInsertIndex = {
            int elem, int start, int end ->
            
            for ( int j in ( end ..< start )) { if ( data[ j - 1 ] <= elem ){ return j }}
            start
        }

        assert ( arrayEnd > arrayStart )
        for ( int j in (( arrayStart + 1 ) ..< arrayEnd ))
        {
            int elem        = data[ j ]
            int insertIndex = findInsertIndex( elem, arrayStart, j )

            if ( insertIndex < j )
            {   // If there's a need to move, elem can be at the right place already
                moveRight( data, insertIndex, j )
                data[ insertIndex ] = elem
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
    private int[] mergeSort( int[]   data,
                             int     start           = 0,
                             int     middle          = -1,
                             int     end             = data.length,
                             int[]   helper          = new int[ data.length ],
                             Closure findMiddleIndex = { int s, int e -> (( s + e ) >> 1 ) } )
    {
        if ( middle < 0 )
        {
            /**
             * First invocation
             */
            mergeSort( data, start, findMiddleIndex( start, end ), end, helper, findMiddleIndex )
        }
        else
        {
            /**
             * Sorting two "halfs"
             */

            if (( middle - start ) < 10 )
            {
                insertionSort( data, start,  middle )
                insertionSort( data, middle, end    )
            }
            else
            {
                mergeSort( data, start,  findMiddleIndex( start, middle ), middle, helper, findMiddleIndex )
                mergeSort( data, middle, findMiddleIndex( middle, end ),   end,    helper, findMiddleIndex )
            }

            /**
             * Merging two "halfs"
             */
            
            if (( middle > start ) && ( data[ middle ] < data [ middle - 1 ] ))
            {
                def leftIndex  = start
                def rightIndex = middle
                
                for ( helperIndex in ( start ..< end ))
                {
                    def dataIndex = (( leftIndex < middle ) && (( rightIndex == end ) || ( data[ leftIndex ] < data[ rightIndex ] ))) ?
                                    leftIndex++ :
                                    rightIndex++

                    helper[ helperIndex ] = data[ dataIndex ]
                }

                assert ( leftIndex == middle ) && ( rightIndex == end )
                System.arraycopy( helper, start, data, start, ( end - start ))
            }
        }

        data
    }


    /**
     * "Quick sort" of data specified, modifies the original array.
     * http://en.wikipedia.org/wiki/Quicksort
     *
     * @param data input array to sort
     * @return same array object with its elements sorted in increasing order
     */
    private int[] quickSort( int[] data, int start = 0, int end = data.length )
    {
        assert end >= start
        if (( end - start ) < 8 ) { return insertionSort( data, start, end ) }

        int pivot      = data[ start ]
        int pivotIndex = start

        assert (( end - start ) > 1 )
        for ( int j in (( start + 1 ) ..< end ))
        {
            int elem = data[ j ]
            if ( elem < pivot )
            {
                moveRight( data, pivotIndex, j )
                data[ pivotIndex ] = elem
                pivotIndex++
            }

            if ( pivotIndex < end ) { assert data[ pivotIndex ] == pivot }
        }

        if ( pivotIndex > ( start + 1 )){ quickSort( data, start, pivotIndex   ) }
        if ( pivotIndex < ( end - 2   )){ quickSort( data, pivotIndex + 1, end ) }
        
        data
    }


    /**
     * Makes a binary search on data sorted, looking for element provided.
     * 
     * @param data input data
     * @param elem element to search for
     * @param start search start index
     * @param end   search end index, exclusive
     * @return      element index or -1 if not found
     */
    int binarySearch ( int[] data, int elem, int start = 0, int end = data.length )
    {
        if ( start == end ) { return -1 }

        int middle = (( start + end ) >> 1 )
        
        (( elem == data[ middle ] ) ? middle :
         ( elem <  data[ middle ] ) ? binarySearch( data, elem, start, middle   ) :
                                      binarySearch( data, elem, middle + 1, end ))
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
