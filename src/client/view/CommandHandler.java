package client.view;
public class CommandHandler {
    
    private static final String DELIMITER = " ";
    public static final String HELP_MESSAGE 
            = "Use \"help\" command for further help."; 
    public static final String INVALID_COMMAND_MESSAGE
            = "Invalid command. Please try again.\n";
    public static final String QUIT_MESSAGE
            = "The program is quitting...\n"+
              "Goodbye!";   
    public static final String FILE_METADATA 
            = "Id\tFile name \t\tSize\t\tOwner\tpri/pub\t\tr/w";
    public String[] splitCommand(String arg){
        String[] parts = arg.split(DELIMITER);
        for(int i=2; i < parts.length; i++){
            parts[1] += parts[i];
        }
        String arg0 = parts[0].toUpperCase(), 
                arg1 = "";
        if(parts.length > 1)
            arg1 = parts[1];
        
        //Verify the command 
        if(!verifyCommand(arg0))
            arg0 = "NOCMD";
        
        String[] cmd = {arg0, arg1};
        return cmd;
    }
    private boolean verifyCommand(String str){
        for (Command cmd : Command.values()) {
            if (cmd.name().equals(str)) {
                return true;
            }
        }
        return false;
    }
    
    public static String help1(){
        String message = 
                ".................................................\n"+
                "Use the following commands. replace [username] ..\n"+
                ".................................................\n"+
                "reg [username]         to register             ..\n"+
                "login [username]       to login                ..\n"+
                "quit                   to quit                 ..\n"+
                ".................................................\n";
        return message;
    }
    public static String help2(){
        String message = 
                ".................................................\n"+
                "Use the following commands. replace [filename] ..\n"+
                ".................................................\n"+
                "upload [filename]      to add a new file       ..\n"+
                "download [filename]    to download a file      ..\n"+
                "update [filename]      to modify a file        ..\n"+
                "delete [filename]      to delete a file        ..\n"+
                "show                   to list all your files  ..\n"+
                "showall                to list all files       ..\n"+
                "info [filename]        to show file info       ..\n"+
                "notify                 to show notification    ..\n"+
                "unreg                  to unregister           ..\n"+
                "logout                 to logout               ..\n"+
                "quit                   to quit                 ..\n"+
                ".................................................";
        return message;
    }
}