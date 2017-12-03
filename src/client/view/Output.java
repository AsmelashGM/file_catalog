package client.view;

public class Output{
    
    //Thread safe standard output
    public synchronized void print(String output) {
        System.out.print(output);
    }
    public synchronized void println(String output) {
        System.out.println(output);
    }
    public void err(String output) {
        System.err.print(output);
    }
    public synchronized void errln(String output) {
        System.err.println(output);
    }
}


