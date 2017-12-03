package common;

import java.io.Serializable;

public class UserModel implements Serializable{
    private String username;
    private String password;
    private boolean login;
    
    public UserModel(){}
    public UserModel(String username, String password){
        this.username = username;
        this.password = password;
        
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    public String getUsername(){
        return username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    public String getPassword(){
        return this.password;
    }
    
    public void setLogin(boolean login){
        this.login = login;
    }
    public boolean getLogin(){
        return this.login;
    }
}
