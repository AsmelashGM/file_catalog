package server.controller;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import server.integration.FileCatalogDOA;
import common.FileModel;
import common.UserModel;
import server.view.ServerView;
import common.ServerInterface;
import java.io.IOException;
import java.util.List;

public class ServerImplementation extends UnicastRemoteObject implements ServerInterface{
    private final ServerView view;
    
    public ServerImplementation() throws RemoteException{
        view = new ServerView();
    }
    
    @Override
    public synchronized boolean register(String username, String password) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        try{
            UserModel user = new UserModel(username, password);
            int success = db.createAccount(user);
            return success==1;
        }
        catch(SQLException ex){
            view.errln("Exception " + ex.getMessage());
        }
        return false;
    }
    @Override
    public synchronized boolean unregister(String username, String password) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        try{
            UserModel user = new UserModel(username, password);
            int success = db.unregister(user);
            return success==1;
        }
        catch(SQLException ex){
            view.errln("Exception " + ex.getMessage());
        }
        return false;
    }
    @Override
    public synchronized boolean login(String username, String password) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        try {
            UserModel user = new UserModel(username, password);
            if(db.verifyUser(user))
                user.setLogin(true);
            else
                user.setLogin(false);
            return user.getLogin();
        } catch (SQLException ex) {
            view.errln("Exception " + ex.getMessage());
        }
        return false;
    }
    @Override
    public synchronized boolean logout(String username) throws RemoteException {
        //user = null;
        return true;
    }

    @Override
    public synchronized boolean addNewFile(FileModel file) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        try {
            int success = db.addNewFile(file);
            
            return success == 1;
        } catch (Base64DecodingException  | SQLException | IOException ex) {
            view.errln("Exception " + ex.getMessage());
        }
        return false;
    }

    @Override
    public synchronized boolean updateFile(UserModel user, FileModel file) throws RemoteException {
        if(user.getUsername().equals(file.getOwner()) || 
                (file.getAccessType().equals("public") && file.getPermission()=='w')){
            FileCatalogDOA db = new FileCatalogDOA();
            try {
            if(file.getContent() != null){
                db.deleteFile(user, file.getName(), "Update");
                addNewFile(file);
            }
            else
                db.updateFile(file);
            //Add this to event logger
            db.addEvent(file.getOwner(), user.getUsername(), file.getName(), "Update");
            } 
            catch (SQLException ex) {
                view.errln("Exception " + ex.getMessage());
            }
            return true;
        }
        else
            return false;
    } 
    @Override
    public synchronized boolean deleteFile(UserModel user, String fileName) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        try{
            int success = db.deleteFile(user, fileName, "Delet");
            return success==1;
        }
        catch(SQLException ex){
            view.errln("Exception " + ex.getMessage());
        }
        return false;
    }
    @Override
    public synchronized FileModel showFileDetails(String username, String fileName) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        FileModel file = null;
        String message = "";
        try{
            file = db.showFileDetails(username, fileName);
        }
        catch(SQLException ex){
            view.errln("Exception " + ex.getMessage());
        }
        return file;
    }

    @Override
    public synchronized String showMyFiles(String username) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        String message = "";
        try{
            List<FileModel> files = db.showMyFiles(username);
            int count=0;
            for(FileModel file : files){
                count++;
                message +=  String.valueOf(count) + "\t" + 
                        file.getName() + "\t\t" + 
                        file.getSize()+ "\t" + 
                        file.getOwner() + "\t" + 
                        file.getAccessType() + "\t\t" + 
                        file.getPermission() + "\n";
            }
        }
        catch(SQLException ex){
            view.errln("Exception " + ex.getMessage());
        }
        return message;
    }
    @Override
    public synchronized String showPublicFiles(String username) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        String message = "";
        try{
            List<FileModel> files = db.showPublicFiles(username);
            int count = 0;
            for(FileModel file : files){
                String owner = file.getOwner();
                count++;
                message +=  String.valueOf(count) + "\t" + 
                        file.getName() + "\t\t" + 
                        file.getSize()+ "\t" + 
                        owner + "\t" + 
                        file.getAccessType() + "\t\t" + 
                        file.getPermission() + "\n";
                //Add this to event logger
                if(username.equals(owner)) continue;
                db.addEvent(owner, username, file.getName(), "View");
            }
        }
        catch(SQLException ex){
            view.errln("Exception " + ex.getMessage());
        }
        return message;
    }

    @Override
    public synchronized String downloadFile(String fileName, String username) throws RemoteException, IOException, Base64DecodingException{
        
        FileCatalogDOA db = new FileCatalogDOA();
        String content = "";
        try{
            content = db.downloadFile(fileName, username);
        }
        catch(SQLException ex){
            view.errln("Exception " + ex.getMessage());
        }
        return content;
    }

    @Override
    public String notifyEvent(String username) throws RemoteException {
        FileCatalogDOA db = new FileCatalogDOA();
        String message = "";
        try{
            message = db.eventNotification(username);
        }
        catch(SQLException ex){
            view.errln("Exception " + ex.getMessage());
        }
        return message;
    }
    
}
