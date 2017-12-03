package server.view;

public class ServerView implements Runnable {
    public void start() {
        new Thread(this).start();
    }
	
    @Override
    public void run() {
       //
    }

    //Thread safe standard output
    public synchronized void println(String output) {
        System.out.println(output);
    }
    public synchronized void errln(String output) {
        System.err.println(output);
    }
}
