package server.integration;

import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import common.UserModel;
import common.FileModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FileCatalogDOA {
    private static final String DBURI = "jdbc:derby://localhost:1527/FileCatalog";
    private static final String DBUSER = "root";
    private static final String DBPASS = "toor";
    private static final String TABLE_FILES = "Files";
    private static final String TABLE_USERS = "Users";
    private static final String EVENT_LOGGER = "Event_logger";
    private static Connection con;
    private PreparedStatement preStmt;

    public FileCatalogDOA(){
        try {
            con = createDBConnection();
            createTable(TABLE_FILES);
            createTable(TABLE_USERS);
            createTable(EVENT_LOGGER);
        } catch (SQLException ex) {
            System.err.println("Exception: " + ex.getMessage());
        }
    }

    //Files management
    public List<FileModel> showPublicFiles(String username) throws SQLException {      
        preStmt = prepareStatement(TABLE_FILES, 'p', false);
        preStmt.setString(1, username);
        preStmt.setString(2, username);
        ResultSet files = preStmt.executeQuery();
        List<FileModel> fileObjs = new ArrayList<FileModel>();
        while(files.next()){
            fileObjs.add( 
                    new FileModel(
                        files.getInt(1), files.getString(2),
                        files.getLong(3), files.getString(4),
                        files.getString(5), files.getString(6).charAt(0), null
                    )
                );
        }
        return  fileObjs;
    }  
    public List<FileModel> showMyFiles(String username) throws SQLException {      
        preStmt = prepareStatement(TABLE_FILES, 'o', false);
        preStmt.setString(1, username);
        ResultSet files = preStmt.executeQuery();
        List<FileModel> fileObjs = new ArrayList<FileModel>();
        while(files.next()){
            fileObjs.add( 
                    new FileModel(
                        files.getInt(1), files.getString(2),
                        files.getLong(3), files.getString(4),
                        files.getString(5), files.getString(6).charAt(0), null
                    )
                );
        }
        return  fileObjs;
    }    
    public int deleteFile(UserModel user, String fileName, String action) throws SQLException{
        int result = 0;
        preStmt = prepareStatement(TABLE_FILES, 'r', false);
        preStmt.setString(1, fileName);
        ResultSet files =  preStmt.executeQuery();
        files.next();
        
        String owner = files.getString(4); 
        String access = files.getString(5);
        char permission = files.getString(6).charAt(0);
        
        if(verifyUser(user)){
            if(user.getUsername().equals(owner) || (access.equals("public") && permission=='w')){ 
                preStmt = prepareStatement(TABLE_FILES, 'd', false);
                preStmt.setString(1, fileName);
                result = preStmt.executeUpdate();
            }
        }
        if(result==1){
            File file = new File("data/" + fileName);
            
            //Add this to event logger
            if(file.delete() && (!user.getUsername().equals(owner)));
                result = addEvent(owner, user.getUsername(), fileName, action);
        }
        return result;
    }
    public FileModel showFileDetails(String username, String fileName) throws SQLException{
        FileModel file = new FileModel();
        
        preStmt = prepareStatement(TABLE_FILES, 'r', false);
        preStmt.setString(1, fileName);
        ResultSet files =  preStmt.executeQuery();
        files.next();
        
        String owner = files.getString(4); 
        String access = files.getString(5);
        char permission = files.getString(6).charAt(0);
        
        if(username.equals(owner) || (access.equals("public") && permission=='w')){ 
            file = new FileModel(fileName, files.getLong(3),
                    owner, access, permission, null);
            file.setId(files.getInt(1));
            //Add this to event logger
            if(!username.equals(owner))
                addEvent(owner, username, fileName, "View");
        }
        
        return file;
    }
    public int addNewFile(FileModel file) throws SQLException, Base64DecodingException, IOException{
        preStmt = prepareStatement(TABLE_FILES, 'w', false);
        preStmt.setString(1, file.getName());
        preStmt.setDouble(2, file.getSize());
        preStmt.setString(3, file.getOwner());
        preStmt.setString(4, file.getAccessType());
        preStmt.setString(5, String.valueOf(file.getPermission()));
        int result = preStmt.executeUpdate();
        result = uploadFile(file.getContent(), file.getName());
        return result; 
    }
    public int uploadFile(String fileContent, String fileName) throws Base64DecodingException, IOException{
        String filePath = "data/"+fileName;
        
        byte[] decoded = Base64.decode(fileContent);
        Files.write(Paths.get(filePath), decoded);
        
        return 1;
    }
    public String downloadFile(String username, String fileName) throws Base64DecodingException, IOException, SQLException{
        String fileContent = "";
        preStmt = prepareStatement(TABLE_FILES, 'r', false);
        preStmt.setString(1, fileName);
        
        ResultSet files =  preStmt.executeQuery();
        files.next();
        String owner = files.getString(4);
        if(username.equals(owner) || 
                (files.getString(5).equals("public") && files.getString(6).charAt(0)=='r')){ 
            String filePath = "data/"+fileName;

            byte[] fileArray = Files.readAllBytes(Paths.get(filePath));
            fileContent = Base64.encode(fileArray);
            
            //Add this to event logger
            if(!(fileContent.equals("") || username.equals(owner)));
                addEvent(owner, username, fileName, "Download");
        }
        files.close();
        return fileContent;
    }
    public int updateFile(FileModel file) throws SQLException{
        preStmt = prepareStatement(TABLE_FILES, 'u', false);
        preStmt.setString(1, file.getName());
        preStmt.setString(2, file.getAccessType());
        preStmt.setString(3, String.valueOf(file.getPermission()));
        preStmt.setInt(4, file.getId());
        return preStmt.executeUpdate();
    }
    public int unregister(UserModel user) throws SQLException{
        if(verifyUser(user)){ //Check iIf the user can login
            preStmt = prepareStatement(TABLE_USERS, 'd', false);
            preStmt.setString(1, user.getUsername());
            return preStmt.executeUpdate();
        }
        else return 0;
    }
    public int createAccount(UserModel user) throws SQLException { 
        if(!userExists(user)){
            preStmt = prepareStatement(TABLE_USERS, 'w', false);
            preStmt.setString(1, user.getUsername());
            preStmt.setString(2, user.getPassword());
            int result = preStmt.executeUpdate();
            return result; 
        }
        return 0;
    } 
    public boolean verifyUser(UserModel user) throws SQLException {      
        String username = user.getUsername();
        String password = user.getPassword();

        preStmt = prepareStatement(TABLE_USERS, 'r', false);
        preStmt.setString(1, username);
        ResultSet users = preStmt.executeQuery();
        
        while(users.next()){
            if((users.getString(2).equals(username)) && (users.getString(3).equals(password))){
                return true; //return user.setLogin(true);
            }
        }
        return false;
    } 
    public boolean userExists(UserModel user) throws SQLException {      
        String username = user.getUsername();
        
        preStmt = prepareStatement(TABLE_USERS, 'r', false);
        preStmt.setString(1, username);
        
        ResultSet users = preStmt.executeQuery();
        while(users.next()){
            if(users.getString(2).equals(username)){
                return true; 
            }
        }
        return false;
    } 
    
    //Event logger management 
    public int addEvent(String owner, String logger, String fileName, String action) throws SQLException { 
        if(owner != logger){
            preStmt = prepareStatement(EVENT_LOGGER, 'w', false);
            preStmt.setString(1, owner);
            preStmt.setString(2, logger);
            preStmt.setString(3, fileName);
            preStmt.setString(4, action);
            int result = preStmt.executeUpdate();
            return result;
        }
        else return 0;
    } 
    public String eventNotification(String username) throws SQLException{
        preStmt = prepareStatement(EVENT_LOGGER, 'r', false);
        preStmt.setString(1, username);
        ResultSet events = preStmt.executeQuery();
        
        String message = "";
        while(events.next()){
            message += "*" + events.getString(3) + " " + 
                    events.getString(5).toLowerCase() +"ed your <<" +
                    events.getString(4) +">> file @" +events.getString(6) + "\n";
            //Update file as seen
            preStmt = prepareStatement(EVENT_LOGGER, 'u', false);
            preStmt.setInt(1, events.getInt(1));
            preStmt.executeUpdate();

        }
        return message;
    }
    //General purpose functions
    public PreparedStatement prepareStatement(String table, char operation, boolean all) throws SQLException{
        String sql = "";
        switch(operation){
            case 'w': //Write to a user or file
                sql = "INSERT INTO " + table;
                if(table.equalsIgnoreCase(TABLE_USERS))
                    sql += " (username, password) VALUES (?, ?)";
                else if(table.equalsIgnoreCase(TABLE_FILES))
                    sql += " (name, size, owner, accesstype, permission) VALUES (?, ?, ?, ?, ?)";
                else if(table.equalsIgnoreCase(EVENT_LOGGER))
                    sql += " (owner, logger, filename, action) VALUES (?, ?, ?, ?)";
                break;
            case 'r': //Read a file, a user, event logger
                sql = "SELECT * FROM "+table;
                if(!all && table.equalsIgnoreCase(TABLE_USERS))
                    sql += " WHERE username = ?";
                else if(!all && table.equalsIgnoreCase(TABLE_FILES))
                    sql += " WHERE name = ?";
                else if(!all && table.equalsIgnoreCase(EVENT_LOGGER))
                    sql += " WHERE owner = ? AND status = 0";
                break;
            case 'o': //Read own files
                sql = "SELECT * FROM " + TABLE_FILES + " WHERE owner = ?";
                break;
            case 'p': //Read public files
                sql = "SELECT * FROM " + TABLE_FILES + " WHERE owner = ? OR (owner<> ? AND accesstype='public')";
                break;
            case 'u': //Update an event logger or a file table
                if(table.equalsIgnoreCase(EVENT_LOGGER))
                    sql = "UPDATE " + EVENT_LOGGER + " SET Status=1 WHERE id = ?";
                else if(table.equalsIgnoreCase(TABLE_FILES))
                    sql = "UPDATE " + TABLE_FILES + " SET name = ?, accesstype = ?, permission = ? WHERE id = ?";
                break;
            case 'd': //Delete a user or a file
                sql = "DELETE FROM "+ table;
                if(!all && table.equalsIgnoreCase(TABLE_USERS))
                    sql += " WHERE username = ?";
                else if(!all && table.equalsIgnoreCase(TABLE_FILES))
                    sql += " WHERE name = ?";
                break;
        }
        return con.prepareStatement(sql);
    }
    public final void createTable(String table) throws SQLException {
        if(!tableExists(table)){
            Statement stmt = con.createStatement();
            String sql = ""; 
            if(table.equalsIgnoreCase(TABLE_FILES)){
                sql = "CREATE TABLE " + TABLE_FILES + "(ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " name VARCHAR(100) unique, size BIGINT, owner VARCHAR(100), accessType VARCHAR(100), permission VARCHAR(5) DEFAULT '-')";
            }else if(table.equalsIgnoreCase(TABLE_USERS)){
                sql = "CREATE TABLE " + TABLE_USERS + "(ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " username VARCHAR(100) unique, password VARCHAR(200))";
            }else if(table.equalsIgnoreCase(EVENT_LOGGER)){
                sql = "CREATE TABLE " + EVENT_LOGGER + "(ID INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                    + " owner VARCHAR(100), logger VARCHAR(100), filename VARCHAR(100), action VARCHAR(100), time TIMESTAMP default CURRENT_TIMESTAMP,"
                    + " status int default 0)";
            }
            stmt.executeUpdate(sql);
            System.out.println("Table created!");
        }
    }
    public boolean tableExists(String table) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet tableMetaData = metaData.getTables(null, null, null, null);
        while(tableMetaData.next()){
            String tableName = tableMetaData.getString(3);
            if(tableName.equalsIgnoreCase(table)){
                return true;
            }
        }
        return false;
    }
    public final Connection createDBConnection()throws SQLException{
        return DriverManager.getConnection(DBURI, DBUSER, DBPASS);
    }   
}
