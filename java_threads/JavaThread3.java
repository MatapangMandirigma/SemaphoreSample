
// Greg Ozbirn, University of Texas at Dallas, 2007
// Java version of matrix multiply 
// adapted from example in C.

public class JavaThread3 implements Runnable
{
   private int col;

   private final int MATSIZE;
   private int mat1[][];
   private int mat2[][];
   private int result[][];

   JavaThread3( int input, int size, int first[][], int second[][], int answer[][] )
   {
      col = input;
      MATSIZE = size;
      mat1 = first;
      mat2 = second;
      result = answer;
   }

   public void run()
   {
     int i, j;
     int val;

     for( i = 0; i < MATSIZE; ++i ) 
     {
       result[i][col] = 0;
       for( j = 0; j < MATSIZE; ++j ) 
       {
	 result[i][col] += mat1[i][j] * mat2[j][col];
       }
     }
   }


   public static void main(String args[])
   {
      int i, j;
      final int MATSIZE=4;

      JavaThread3 thr[] = new JavaThread3[MATSIZE];
      Thread myThread[] = new Thread[MATSIZE];


      int mat2[][] =
	 { { 1, 2, 3, 4 },
	   { 4, 5, 6, 7 },
	   { 7, 8, 9, 10 },
	   { 10, 11, 12, 13 } };
      int mat1[][] =
	 { { 9, 8, 7, 6 },
	   { 6, 5, 4, 3 },
	   { 3, 2, 1, 0 },
	   { 0, -1, -2, -3 } };

      int result[][] = new int[MATSIZE][MATSIZE];


      // create threads
      for( i = 0; i < MATSIZE; ++i ) 
      {
	 thr[i] = new JavaThread3(i, MATSIZE, mat1, mat2, result);
         myThread[i] = new Thread( thr[i] );
         myThread[i].start();
      }

      for( i = 0; i < MATSIZE; ++i ) 
      {
	 try
	 {
	    myThread[i].join();
	 }
	 catch (InterruptedException e)
	 {
	 }
      }

      for( i = 0; i < MATSIZE; ++i ) 
      {
	 System.out.print("| ");
	 for( j = 0; j < MATSIZE; ++j ) 
	 {
	    System.out.printf("%3d ", mat1[i][j]);
	 }
	 System.out.print("| ");
	 System.out.print((i==MATSIZE/2 ? 'x' : ' '));
	 System.out.print(" | ");
	 for( j = 0; j < MATSIZE; ++j ) 
	 {
	    System.out.printf("%3d ", mat2[i][j]);
	 }
	 System.out.print("| ");
	 System.out.print((i==MATSIZE/2 ? '=' : ' '));
	 System.out.print(" | ");
	 for( j = 0; j < MATSIZE; ++j ) 
	 {
	    System.out.printf("%3d ", result[i][j]);
	 }
	 System.out.println("|");
      }

   }
}

