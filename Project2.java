import java.util.concurrent.Semaphore;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Project2{

    // Global variables for all classes
    private final static int NUMCUST = 50; // Total number of customers
    private static Semaphore[] custSem = new Semaphore[NUMCUST]; // Semaphore for each customer
    private static boolean soldOut; // Variable for whether a movie is sold out
    private static int[] movieTicketCount; // Array for the the amount of tickets are available for each movie
    private static Customer customer[] = new Customer[NUMCUST]; // Customer array for use in project

    // Semaphores for Theater Workers
    private static Semaphore BOA_ready = new Semaphore(2, true); 
    private static Semaphore TT_ready = new Semaphore(1, true);
    private static Semaphore CSW_ready = new Semaphore(1, true);

    // Semaphores necessary for Customer Info
    private static Semaphore giveMovie = new Semaphore(0, true); // Semaphore for BOA use
    private static Semaphore giveTicket = new Semaphore(0, true); // Semaphore for TT use
    private static Semaphore giveOrder = new Semaphore(0, true); // Semaphore for CSW use

    // Semaphores necessary for Customer Info
    private static Semaphore setBOACustInfo = new Semaphore(1, true); // Protection for BOA Queue
    private static Semaphore getBOACustInfo = new Semaphore(1, true); // Protection for BOA Queue
    private static Semaphore setTTCustInfo = new Semaphore(1, true); // Protection for TT Queue
    private static Semaphore getTTCustInfo = new Semaphore(1, true); // Protection for TT Queue
    private static Semaphore setCSWCustInfo = new Semaphore(1, true); // Protection for CSW Queue
    private static Semaphore getCSWCustInfo = new Semaphore(1, true); // Protection for CSW Queue
    private static Semaphore setTicketInfo = new Semaphore(1, true); // Setter Semaphore for SoldOut; Mutex
    private static Semaphore getTicketInfo = new Semaphore(0, true); // Getter Semaphore for SoldOut; Mutex

    // Queues for Customers in line
    private static Queue<Customer> BOACustomers = new LinkedList<>();
    private static Queue<Customer> TTCustomers = new LinkedList<>();
    private static Queue<Customer> CSWCustomers = new LinkedList<>();

    // Customer Class
    static class Customer implements Runnable{

        // Customer Variables
        private int num;
        private int movieIndex;
        private String movieChoice;
        private boolean snack;
        private String snackChoice;

        // Class Thread
        public void run(){
            // Intial Creation Statement
            System.out.println("Customer " + num + " created, buying ticket to " + movieChoice);

            // Wait for set for queue to be ready
            try{
                setBOACustInfo.acquire();
            }catch (InterruptedException e){}

            // Add customer to BOA Queue
            BOACustomers.add(customer[num]);

            // Signal that set for queue is free
            setBOACustInfo.release();

            // Wait on Box Office Agents to be free
            try{
                BOA_ready.acquire();
            }catch (InterruptedException e){}

            // Signal that the customer is ready
            giveMovie.release();

            // Wait for Box Office Agent to give ticket
            try{
                custSem[num].acquire();
            }catch (InterruptedException e){}

            // Wait for the ticket info to be set and signaled
            try{
                getTicketInfo.acquire();
            }catch (InterruptedException e){}

            // Signal that Box Office Agent is free
            BOA_ready.release();

            // If movie is sold out leaves, otherwise continue
            if(!soldOut){

                // Signal that setter for tickets is free now
                setTicketInfo.release();
                
                // Signify that customer is in line for ticket taker
                System.out.println("Customer " + num + " in line to see ticket taker");

                // Wait for set for queue to be ready
                try{
                    setTTCustInfo.acquire();
                }catch (InterruptedException e){}

                // Add customer to TT Queue
                TTCustomers.add(customer[num]);

                // Signal that set for queue is free
                setTTCustInfo.release();

                // Wait for Ticket Taker to be ready
                try{
                    TT_ready.acquire();
                }
                catch (InterruptedException e){}

                // Signal that ticket is given
                giveTicket.release();

                // Wait for Ticket Taker to tear ticket
                try{
                    custSem[num].acquire();
                }catch (InterruptedException e){}

                // Signal that Ticket Taker is free now
                TT_ready.release();

                // If customer wants snack then get in line
                // Otherwise enter theater
                if(snack){
                    // Signify that customer is in line for Concession Stand Worker
                    System.out.println("Customer " + num + " in line to buy " + snackChoice);

                    // Wait for set for queue to be ready
                    try{
                        setCSWCustInfo.acquire();
                    }catch (InterruptedException e){}
            
                    // Add customer to CSW Queue
                    CSWCustomers.add(customer[num]);

                    // Signal that set for queue is free
                    setCSWCustInfo.release();

                    // Wait for Concession Stand Worker to be free
                    try{
                        CSW_ready.acquire();
                    }catch (InterruptedException e){}

                    // Signal that customer have given order
                    giveOrder.release();

                    // Wait for CSW to fill order for customer
                    try{
                        custSem[num].acquire();
                    }catch (InterruptedException e){}

                    // Signal that Concession Stand Worker is free
                    CSW_ready.release();

                }
                
                // Customer enters theater
                System.out.println("Customer " + num + " enters theater to watch " + movieChoice);
            }else{
                // Signal that setter is free now
                setTicketInfo.release();

                // Customer Leaves
                System.out.println("Customer " + num + " leaves");
            }    
        }

        // Customer Constructor
        Customer(int num, int movieIndex, String movieChoice, boolean snack, String snackChoice){
            this.num = num;
            this.movieIndex = movieIndex;
            this.movieChoice = movieChoice;
            this.snack = snack;
            this.snackChoice = snackChoice;
        }
    }

    // Box Office Agent Class
    static class BoxOfficeAgent implements Runnable{

        // Box Office Agent Variables
        private int num;
        private Customer customer;

        // Box Office Agent Thread
        public void run(){
            // Signify creation of Box Office Agent
            System.out.println("Box office agent " + num + " created");
            while(true){

                // Wait for Customer to be ready
                try{
                    giveMovie.acquire();
                }catch (InterruptedException e){}

                // Wait for queue to be free
                try{
                    getBOACustInfo.acquire();
                }catch (InterruptedException e){}

                // Remove from top of BOA queue
                customer = BOACustomers.remove();

                // Signal that queue is free 
                getBOACustInfo.release();

                // Signify what customer the Box office agent is serving
                System.out.println("Box office agent " + num + " serving customer " + customer.num);

                // Sleep for 1.5 seconds to simulate getting the ticket
                try{
                    Thread.sleep(1500);
                }catch (InterruptedException e){}

                // Wait for ticket setter to be free
                try{
                    setTicketInfo.acquire();
                }catch (InterruptedException e){}

                // Check for is the movie is sold out
                if(movieTicketCount[customer.movieIndex] > 0){
                    // Signify that ticket has been sold to customer
                    System.out.println("Box office agent " + num + " sold ticket for " + customer.movieChoice + " to customer " + customer.num);
                    // Take a ticket from the count and set movie to not sold out
                    movieTicketCount[customer.movieIndex]--;
                    soldOut = false;
                }else{
                    // Signify that the BOA has told the customer the movie is sold out
                    System.out.println("Box office agent " + num + " informs customer " + customer.num + " that " + customer.movieChoice + " is sold out");
                    // Set the movie to sold out
                    soldOut = true;
                }

                // Signal that ticket getter is free now
                getTicketInfo.release();
                
                // Signal that ticket has been given
                custSem[customer.num].release();
            }
            
        }

        // Box Office Agent Constructor
        BoxOfficeAgent(int num){
            this.num = num;
        }

    }

    // Ticket Taker Class
    static class TicketTaker implements Runnable{

        // Ticket Taker Variables
        private static Customer customer;

        // Ticket Taker Thread
        public void run(){
            // Signify creationg of Ticket Taker
            System.out.println("Ticket taker created");
            while(true){
                // Wait for customer to give ticket to ticket taker
                try{
                    giveTicket.acquire();
                }catch (InterruptedException e){}

                // Wait for queue to be free
                try{
                    getTTCustInfo.acquire();
                }catch (InterruptedException e){}

                // Remove from top of TT queue
                customer = TTCustomers.remove();

                // Signal that queue is free 
                getTTCustInfo.release();

                // Sleep for .25 seconds and simlulate the ticket tear
                try{
                    Thread.sleep(250);
                }catch (InterruptedException e){}

                // Signify ticket was taken and teared
                System.out.println("Ticket taken from customer " + customer.num);
                
                // Signal that customer ticket was teared and can go in the theater
                custSem[customer.num].release();
            }
        }

        // Ticket Taker Constructor
        TicketTaker(){}
    }

    // Concession Stand Worker Class
    static class ConcesionStandWorker implements Runnable{

        // Concession Stand Worker Variables
        private Customer customer;

        // Concession Stand Worker Thread
        public void run(){
            // Signify creation of Concession Stand Worker
            System.out.println("Concession stand worker created");
            while(true){
                // Wait for customer to give order
                try{
                    giveOrder.acquire();
                }catch (InterruptedException e){}
                
                // Wait for queue to be free
                try{
                    getCSWCustInfo.acquire();
                }catch (InterruptedException e){}

                // Remove from top of CSW queue
                customer = CSWCustomers.remove();

                // Signal that queue is free 
                getCSWCustInfo.release();

                // Signify that order has been taken
                System.out.println("Order for " + customer.snackChoice + " taken from customer " + customer.num);

                // Sleep for 3 seconds to simulate order being made for customer
                try{
                    Thread.sleep(3000);
                }catch (InterruptedException e){}

                // Signify that customer received order
                System.out.println(customer.snackChoice + " given to customer " + customer.num);

                // Signal that customer received order
                custSem[customer.num].release();
            }
        }

        // Concession Stand Worker Constructor
        ConcesionStandWorker(){}

    }

    // Main class
    public static void main(String[] args) throws FileNotFoundException{

        // File for movie list
        File movies = new File(args[0]);

        // Scanner for movies and size to keep count
        Scanner count = new Scanner(movies);
        int size = 0;
        String string = "";

        // Count the size of the movie list
        while(count.hasNext()){
            string = count.nextLine();
            size++;
        }

        // Scanner for movies to be read in to project 2
        Scanner scan = new Scanner(movies);
        int index = 0;
        String movieName = "";
        int tickets = 0;

        // Arrays necessary for Customer variables
        String[] movieList = new String[size];
        movieTicketCount = new int[size];
        String[] snacks = {"Popcorn", "Soda", "Popcorn and Soda"};

        // for loop to read in the movies and ticket counts
        for(int j = 0; j < size; j++){
            string = scan.nextLine();
            index = string.length() - 1;
            // Necessary to separate movies and ticket count
            while(Character.isDigit(string.charAt(index))){
                index--;
            } 
            // Assign the movie and ticket count
            movieName = string.substring(0, index);
            tickets = Integer.parseInt(string.substring(index + 1));
            movieList[j] = movieName;
            movieTicketCount[j] = tickets;
        }
        
        // 2 Box Office Agent Threads
        BoxOfficeAgent BOA0 = new BoxOfficeAgent(0);
        BoxOfficeAgent BOA1 = new BoxOfficeAgent(1);
        Thread BOA0thr = new Thread(BOA0);
        Thread BOA1thr = new Thread(BOA1);
        BOA1thr.setDaemon(true); // Set threads to Daemon so the program can end
        BOA0thr.setDaemon(true);
        BOA0thr.start(); // Start both threads
        BOA1thr.start();

        // Ticket Taker Thread
        TicketTaker TT = new TicketTaker();
        Thread TTthr = new Thread(TT);
        TTthr.setDaemon(true);
        TTthr.start();

        // Concession Stand Worker Thread
        ConcesionStandWorker CSW = new ConcesionStandWorker();
        Thread CSWthr = new Thread(CSW);
        CSWthr.setDaemon(true);
        CSWthr.start();

        // Signify theater is open
        System.out.println("Theater is Open");

        // Random object for future use
        Random rand = new Random();

        // 50 Customer and customer thread arrays
        Thread custThr[] = new Thread[NUMCUST];

        // Customer variables
        int movieChoice = 0;
        boolean snack = false;
        int snackChoice = 0;

        // for loop for each customer 
        for(int i = 0; i < NUMCUST; i++){
            // Setting random options
            movieChoice = rand.nextInt(size); // Random Choice of the movie list
            snack = false;
            snackChoice = rand.nextInt(3); // Random Choice from snack list
            // Random choice to get a snack or not
            if(rand.nextInt(2) == 1)
               snack = true;

            // Give each customer a semaphore
            custSem[i] = new Semaphore(0, true);
            // Make a customer and give it a thread to run on
            customer[i] = new Customer(i, movieChoice, movieList[movieChoice], snack, snacks[snackChoice]);
            custThr[i] = new Thread(customer[i]);
            custThr[i].start();
        }

        // for loop for joining customers
        for(int i = 0; i < NUMCUST; i++){
            try{
                custThr[i].join();
                System.out.println("Joined customer " + i);
            }catch (InterruptedException e){}
        }
        
        // Close both scanners
        count.close();
        scan.close();

    }
}
