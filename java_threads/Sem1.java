
import java.util.concurrent.Semaphore;

/*
 * Sem1.java
 *
 * Demonstrate use of semaphores for synchronization.
 *
 * Greg Ozbirn, 2007, University of Texas at Dallas 
 * adapted from example in C.
 *
 */



public class Sem1 implements Runnable
{
   private int num;
   private static Semaphore sem = new Semaphore( 0, true );


   Sem1( int num ) 
   {
      this.num = num;
   }

   public void run()
   {
      System.out.println( "Thread " + num + " waiting" );
      try
      {
         sem.acquire();
      }
      catch (InterruptedException e)
      {
      }
      System.out.println( "Thread " + num + " resuming" );
   }

   public static void post()
   {
      sem.release();
   }

   public static void main(String args[])
   {
      int i=0;
      final int NUMTHREADS = 10;

      Sem1 thr[] = new Sem1[NUMTHREADS];
      Thread myThread[] = new Thread[NUMTHREADS];

      // create threads
      for( i = 0; i < NUMTHREADS; ++i ) 
      {
	      thr[i] = new Sem1(i);
         myThread[i] = new Thread( thr[i] );
         myThread[i].start();
      }

      try
      {
         Thread.sleep(2000);
      }
      catch (InterruptedException e)
      {
      }

      // awaken threads
      for( i = 0; i < NUMTHREADS; ++i ) 
      {
	 System.out.println("Posting from main");
         post(); 
      }

      for( i = 0; i < NUMTHREADS; ++i ) 
      {
	 try
	 {
	    myThread[i].join();
	 }
	 catch (InterruptedException e)
	 {
	 }
      }

   }
}





