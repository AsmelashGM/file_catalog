package client.view;
public enum Command{
        //Before login
        REG,
        LOGIN,
        
        //After login
        UPLOAD,
        DOWNLOAD,
        UPDATE,
        DELETE,
        SHOW,
        SHOWALL,
        INFO,
        NOTIFY,
        UNREG,
        LOGOUT,
        
        //Common commands
        HELP,
        QUIT,
        NOCMD
}
