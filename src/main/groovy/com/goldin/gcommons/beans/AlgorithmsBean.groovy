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
                                                             null
        if ( validate )
        {
            for ( j in ( 0 ..< data.length ))
            {
                if ( j > 0 )                 { assert sortedData[ j - 1 ] <= sortedData[ j ] }
                if ( j < ( data.length - 1 )){ assert sortedData[ j + 1 ] >= sortedData[ j ] }
            }
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
     * "Insert sort" of data specified, modifies the original array.
     * http://en.wikipedia.org/wiki/Insert_sort
     *
     * @param data input array to sort
     * @return same array object with its elements sorted in increasing order
     */
    private int[] insertionSort ( int[] data )
    {
        /**
         * Finds index to insert the element specified in the range ending at "end" index.
         */
        def findInsertIndex = {
            int elem, int end ->
            for ( j in (( end - 1 ) .. 0 )) { if ( data[ j ] <= elem ){ return ( j + 1 ) }}
            0
        }

        /**
         * Move elements right in the range starting at "start" and ending at "end" indexes.
         */
        def moveRight = {
            int start, int end ->

            assert ( end > start ), "moverRight( $data, $start, $end ): [$end] <= [$start]"
            for ( int j in (( end - 1 ) .. start )) { data[ j + 1 ] = data[ j ]}
        }

        if ( data )
        {
            for ( j in ( 1 ..< data.length ))
            {
                int elem        = data[ j ]
                int insertIndex = findInsertIndex( elem, j )

                if ( insertIndex < j )
                {   // If there's a need to move, elem can be at the right place already
                    moveRight( insertIndex, j )
                    data[ insertIndex ] = elem
                }
            }
        }

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
    * http://en.wikipedia.org/wiki/Quicksort
    */
    Quick,

   /**
    * http://en.wikipedia.org/wiki/Merge_sort
    */
    Merge,
}
