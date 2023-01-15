
public class JavaThread1 implements Runnable
{
   private String message;
  
   JavaThread1( String msg )
   {
      message = msg;
   }

   public void run()
   {
      System.out.println(message);
   }

   public static void main(String args[])
   {
      String message_main ="Hello, ";   
      String message_thread = "thread";

      // print hello 
      System.out.print(message_main);

      // create thread
      JavaThread1 thr = new JavaThread1(message_thread);
      Thread myThread = new Thread( thr );
      myThread.start();

      try
      {
         myThread.join();
      }
      catch (InterruptedException e)
      {
      }
   }
}

