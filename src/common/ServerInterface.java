package common;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerInterface extends Remote {
    public static final String REG_SERVER_NAME = "FILE_CATALOG";
    public static final String HOST = "localhost";
    
    //User management
    boolean register(String username, String password)throws RemoteException;
    boolean unregister(String username, String password) throws RemoteException;
    boolean login(String username, String password)throws RemoteException;
    boolean logout(String username)throws RemoteException;
    
    //File management
    public String downloadFile(String fileName, String username) throws RemoteException, IOException, Base64DecodingException;
    boolean addNewFile(FileModel file)throws RemoteException;
    boolean updateFile(UserModel user, FileModel file)throws RemoteException;
    boolean deleteFile(UserModel user, String fileName)throws RemoteException;
    FileModel showFileDetails(String username, String fileName)throws RemoteException;
    String showMyFiles(String username)throws RemoteException;
    String showPublicFiles(String username)throws RemoteException;
    String notifyEvent(String username)throws RemoteException;
}
