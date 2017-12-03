package common;

import java.io.Serializable;

public class FileModel implements Serializable{
    private int id;
    private String name;
    private long size;
    private String owner;
    private String accessType;
    private char permission;
    private String content;
    
    public FileModel(){}
    public FileModel(String name, long size, String owner, 
                    String accessType, char permission, String content){
        this.name = name;
        this.size = size;
        this.owner = owner;
        this.accessType = accessType;
        this.permission = permission;
        this.content = content;
    }
    public FileModel(int id, String name, long size, String owner, 
                    String accessType, char permission, String content){
        this.id = id;
        this.name = name;
        this.size = size;
        this.owner = owner;
        this.accessType = accessType;
        this.permission = permission;
        this.content = content;
    }
    
    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    
    public void setSize(long size){
        this.size = size;
    }
    public double getSize(){
        return this.size;
    }
    
    public void setOwner(String owner){
        this.owner = owner;
    }
    public String getOwner(){
        return this.owner;
    }
    
    public void setAccessType(String accessType){
        this.accessType = accessType;
    }
    public String getAccessType(){
        return this.accessType;
    }
    
    public void setPermission(char permission){
        this.permission = permission;
    }
    public char getPermission(){
        return this.permission;
    }
    
    public void setId(int id){
        this.id = id;
    }
    public int getId(){
        return id;
    }
    
    public void setContent(String content){
        this.content = content;
    }
    public String getContent(){
        return this.content;
    }
}
