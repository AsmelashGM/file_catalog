package client.view;

import static client.view.Command.*;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import common.ServerInterface;
import common.UserModel;
import common.FileModel;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.Naming;

public class ClientView implements Runnable {
    
    //Declaration of attributes 
    private static String prompt;
    private static BufferedReader userInputReader;
    private static String userInput;
    private static Output view;
    private UserModel user;
    private FileModel file;
    private ServerInterface rmiServer;
    private CommandHandler cmd;
   
    //Methods implementations 
    public void start() throws RemoteException, NotBoundException, MalformedURLException {
        resetPrompt();
        userInputReader = new BufferedReader(new InputStreamReader(System.in));
        view = new Output();
        user = new UserModel();
        file = new FileModel();
        cmd = new CommandHandler();
        
        lookupRmiServer(ServerInterface.HOST);
        view.println("**** Welcome to the File Catalog. *****");
        view.println(cmd.HELP_MESSAGE);
        view.err(prompt);
        
        new Thread(this).start(); //Call run method
    }
    
    @Override
    public void run(){
        boolean success = false;
        try {
            userInput = userInputReader.readLine();
            String[] args = cmd.splitCommand(userInput);
            while(Command.valueOf(args[0]) != QUIT){
                switch (Command.valueOf(args[0])){
                    case REG: //User registration menu selected
                        if(args[1] == ""){
                            view.println("Please, specify valid username.");
                            break;
                        }
                        success = register(args[1]);
                        if(success)
                            manageFileCatalog();
                        break;

                    case LOGIN: //Login menu selected
                        if(args[1] == ""){
                            view.println("Please, specify valid username.");
                            break;
                        }
                        success = login(args[1]);
                        if(success)
                            manageFileCatalog();
                        break;

                    case HELP: //help
                        view.print(CommandHandler.help1());
                        break;

                    default: //Invalid inputs
                        view.print(CommandHandler.INVALID_COMMAND_MESSAGE);
                        break;
                }
                if(userInput.equalsIgnoreCase("quit")) break;
                //Accept another user input
                view.err(prompt);
                userInput = userInputReader.readLine();
                args = cmd.splitCommand(userInput);
            }
            view.println(CommandHandler.QUIT_MESSAGE);
            
        } catch (IOException | Base64DecodingException | IllegalArgumentException ex) {
            view.errln("Exception: " + ex.getMessage());
        }
    }
    private void manageFileCatalog() throws IOException, Base64DecodingException, IllegalArgumentException{
        boolean success = false;
        view.err(prompt);
        userInput = userInputReader.readLine();
        String[] args = cmd.splitCommand(userInput);
        while(Command.valueOf(args[0]) != QUIT && user != null){
            switch(Command.valueOf(args[0])){
                case UPLOAD:
                    if(args[1] == ""){
                        view.println("Please, specify valid file name.");
                        break;
                    }
                    addNewFile(args[1]);
                    break;
                case DOWNLOAD:
                    if(args[1] == ""){
                        view.println("Please, specify valid file name.");
                        break;
                    }
                    fileDownload(args[1]);
                    break;
                case UPDATE:
                    if(args[1] == ""){
                        view.println("Please, specify valid file name.");
                        break;
                    }
                    modifyFile(args[1]);
                    break;
                case DELETE:
                    if(args[1] == ""){
                        view.println("Please, specify valid file name.");
                        break;
                    }
                    deleteFile(args[1]);
                    break;
                case SHOW:
                    listAllMyFiles();
                    break;
                case SHOWALL:
                    listAllFiles();
                    break;
                case INFO:
                    if(args[1] == ""){
                        view.println("Please, specify valid file name.");
                        break;
                    }
                    showFileInfo(args[1]);
                    break;
                case NOTIFY:
                    notifyEvents();
                    break;
                case UNREG:
                    unregister(user.getUsername());
                    break;
                case LOGOUT:
                    logout();
                    view.println("***** You're logged out. *****");
                    break;
                case HELP: //help
                        view.println(CommandHandler.help2());
                        break;
                case NOCMD: //Invalid command
                    view.print(CommandHandler.INVALID_COMMAND_MESSAGE);
                    break;
            }
            if(user == null) break;
            view.err(prompt);
            userInput = userInputReader.readLine();
            args = cmd.splitCommand(userInput);
        }
    }
    
    //User management
    private void logout()throws IOException{
        if(rmiServer.logout(user.getUsername())){ //Call RMI interface
            resetPrompt();
            user = null;
        }
    }
    private boolean unregister(String uname)  throws IOException{
        if(!user.getLogin()) return false;                     
        view.println("***** Confirm to unregister. *****");
        view.print("Password: ");
        String pass = userInputReader.readLine();
        boolean success =rmiServer.unregister(uname, pass); //Call RMI interface
        if(success) {
            resetPrompt();
            logout();
            view.println("***** You're unregistered! *****");   
            return true;
        }
        else {
            view.println("Invalid password, please try agin!");
            return false;
        }
    }
    private boolean register(String uname) throws IOException, RemoteException{
        view.print("Password: ");
        String pass = userInputReader.readLine();
            
        user = new UserModel(uname, pass);
        boolean success = rmiServer.register(uname, pass); //Call RMI interface (user)
        if(success) {
            user.setLogin(true);
            setPrompt(user.getUsername());
            view.println("You are registered and logged in as <<"+user.getUsername()+">>");
            view.println("Use \"help\" command for further help");
            return true;
        }
        else {
            view.println("Username is occupied, please try another username!");
            return false;
        }
    }
    private boolean login(String uname)throws IOException{
        view.print("Password: ");
        String pass = userInputReader.readLine();

        user = new UserModel(uname, pass);
        boolean success = rmiServer.login(uname, pass); //Call RMI interface (user)
        if(success) {
            user.setLogin(true);
            setPrompt(user.getUsername());
            view.println("You are logged in.");
            view.println("Use \"help\" command for further help");
            return true;
        }
        else{
            view.println("Invalid username or password, please try again!");
            return false;
        }
    }
    
    //File management 
    private void addNewFile(String fileName) throws IOException{
        file = getFileMetaData(fileName);
        File inFile = new File(file.getName());
        if(!inFile.isFile())
            view.println("Ivalid file path. Try agian.");
        else{
            String filePath = file.getName();
            view.println(" *File path: " + filePath);
            view.println(" *File size: " + String.valueOf(inFile.length()/1024) + " KB");
            
            file.setName(fileName);
            file.setSize(inFile.length());
            String fileContent = getFileContent(filePath); //Get file content in base64 format 
            file.setContent(fileContent);
            file.setOwner(user.getUsername());
            
            view.println("Uploading file...");
            boolean success = rmiServer.addNewFile(file);
            if(success)
                view.println("***** File is uploaded. *****\n");
            else 
                view.println("***** Something went wrong! *****\n");
        }
    }
    private void fileDownload(String fileName) throws IOException, Base64DecodingException{
        String content = rmiServer.downloadFile(user.getUsername(), fileName);
        if(content == ""){
            view.println("Invalid file name. Try again");
        }
        else{
            view.print("Location to download use \"/\": ");
            String dirPath = userInputReader.readLine();
            byte[] decoded = Base64.decode(content);
            Files.write(Paths.get(dirPath + "/" +fileName), decoded);
            view.println("File is downloaded!");
        }
    }
    private void deleteFile(String fileName)  throws IOException{
        boolean success = rmiServer.deleteFile(user, fileName);
        if(success)
            view.println("***** File is deleted. *****\n");
        else
            view.println("***** Something went wrong! *****\n");
    }
    private void modifyFile(String fileName) throws IOException{
        file = rmiServer.showFileDetails(user.getUsername(), fileName);
        if(file!=null){
            int id = file.getId();
            String owner = file.getOwner();
            String fileContent = null;
            view.println(fileInfo(file));

            file = getFileMetaData(fileName);
            File inFile = new File(file.getName());
            if(!inFile.isFile())
                view.println("New file is not to be uploaded.");
            else{
                String filePath = file.getName();
                view.println(" *File path: " + filePath);
                view.println(" *File size: " + String.valueOf(inFile.length()/1024) + " KB");
                file.setSize(inFile.length());
                view.println("Uploading file...");
                fileContent = getFileContent(filePath); //Get file content in base64 format 
            }
            file.setId(id);
            file.setName(fileName);
            file.setContent(fileContent);
            file.setOwner(owner);

            boolean success = rmiServer.updateFile(user, file);
            if(success)
                view.println("***** File is updated. *****\n");
            else 
                view.println("***** Something went wrong! *****\n");
        }
        else
            view.println("No file with such name");
    }
    private void showFileInfo(String fileName) throws IOException{
        view.println("***** File information display request. *****");
        file = rmiServer.showFileDetails(user.getUsername(), fileName);
        view.println(fileInfo(file));
    }
    private void listAllMyFiles() throws RemoteException{
        view.println(CommandHandler.FILE_METADATA);
        String message = rmiServer.showMyFiles(user.getUsername());
        view.println(message);
    }
    private void listAllFiles() throws RemoteException{
        view.println(CommandHandler.FILE_METADATA);
        String message = rmiServer.showPublicFiles(user.getUsername());
        view.println(message);
    }
    private void notifyEvents() throws RemoteException{
        String message = rmiServer.notifyEvent(user.getUsername());
        view.println(message);
    }
    
    private String fileInfo(FileModel file){
        String message = 
                "File name: " + file.getName() + "\n" +
                "File size: " + file.getSize()/1024 + " KB" + "\n" +
                "File access permission: " + file.getAccessType() + "\n" +
                "File permission (r/w): " + file.getPermission() + "\n";
        return message;
    }
    private FileModel getFileMetaData(String fileName) throws IOException{
        file = null;
        view.print("File Location use \"/\": ");
        String fileLocation = userInputReader.readLine();
        String filePath = fileLocation + "/" + fileName;
        view.print("Access permission (public/private): ");
        String accessType = userInputReader.readLine();
        String accessPermission = "-";
        if(accessType.equals("public")){
            view.print("Access permission (r/w): ");
            accessPermission = userInputReader.readLine();
        }
        file = new FileModel(filePath, 0, null,
                             accessType, accessPermission.charAt(0), null);
        return file;
    }
    //Getter functions (specifically for menu items)
    private static void setPrompt(String username){
        prompt = username + "@" + "ThisPC:~$ ";
    }
    private static void resetPrompt(){
        prompt = "ThisPC:~$ ";
    }
    private String getFileContent(String filePath) throws IOException{
        byte[] fileArray = Files.readAllBytes(Paths.get(filePath));
        String fileContent = Base64.encode(fileArray);
        return fileContent;
    }
    //RMI server lookup
    private void lookupRmiServer(String host) throws RemoteException, NotBoundException, MalformedURLException {
        String rmiUri = "//" + host + "/" + ServerInterface.REG_SERVER_NAME;
        rmiServer = (ServerInterface) Naming.lookup(rmiUri);
    }
}
