package client.startup;

import client.view.ClientView;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class ClientMain {
    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        new ClientView().start();
    }
}
