package com.goldin.gcommons.beans


/**
 * Implementation of various algorithms
 */
class AlgorithmsBean extends BaseBean
{

    int[] sort ( int[] data, SortOption option = SortOption.Quick, boolean validate = true )
    {
        int[] sortedData = ( option == SortOption.Select ) ? sortSelect( data ) :
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
     * "Selection sort" of data specified, modifies the array specified.
     * http://en.wikipedia.org/wiki/Selection_sort
     * 
     * @param data input array to sort
     * @return same array object with its elements sorted in increasing order
     */
    private int[] sortSelect ( int[] data )
    {
        /**
         * Finds index of the minimal element in the range starting at index specified
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
            int minIndex     = findMinIndex( j )
            int temp         = data[ j ]
            data[ j ]        = data[ minIndex ]
            data[ minIndex ] = temp
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
    Select,

   /**
    * http://en.wikipedia.org/wiki/Insert_sort
    */
    Insert,

   /**
    * http://en.wikipedia.org/wiki/Quicksort
    */
    Quick,

   /**
    * http://en.wikipedia.org/wiki/Merge_sort
    */
    Merge,
}
