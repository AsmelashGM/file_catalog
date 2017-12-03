package server.startup;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import server.controller.ServerImplementation;
import server.view.ServerView;

public class ServerMain {
    private static ServerView view;
    public ServerMain(){
        view = new ServerView();
    }
    public static void main(String[] args) {
        try{
            new ServerMain().startRegistry();
            ServerImplementation obj = new ServerImplementation(); 
            Naming.rebind(ServerImplementation.REG_SERVER_NAME, obj);
            
            view.errln("Now, it's running..."); 
        }
        catch(RemoteException | MalformedURLException ex) {
            System.err.println("RMI exception: "+ex.getMessage());
        }
    }
    private void startRegistry() throws RemoteException{
        try{
            LocateRegistry.getRegistry().list();
        }
        catch(RemoteException ex){
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
        view.println("Server is started."); 
    }
}
