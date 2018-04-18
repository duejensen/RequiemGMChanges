package org.reqiuem.mods.gmchanges.utils;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CmdTool {

    public static final Logger logger = Logger.getLogger("CmdTool");

    public HashMap<String,WurmCmd> cmdmap = null;

    public CmdTool() {
        cmdmap = new HashMap<String,WurmCmd>();
    }

    public void addWurmCmd( WurmCmd cmd ) {
        logger.log(Level.INFO,String.format("addWurmCmd: ->%s<-", cmd.cmdName));
        cmdmap.put( cmd.cmdName, cmd );
    }

    public boolean runWurmCmd( Player player, String[] argv) {
        return runWurmCmd( (Creature)player, argv );
    }

    public boolean runWurmCmd( Creature player, String[] argv) {

        if (argv.length == 0) {
            return false;
        }

        String cmdName = argv[0];

        WurmCmd cmd = cmdmap.get( cmdName );
        if ( cmd == null ) {
        	// no need to log any command outside this mod 
            // logger.log(Level.INFO, String.format("cmdName not found: ->%s<-", cmdName));
            return false;
        }

        Communicator comm = player.getCommunicator();

        if ( player.getPower() < cmd.minPower ) {
        	// not needed either 
            // comm.sendNormalServerMessage("Newp No Perms: " + cmdName);
            return true;
        }

        try {

            cmd.runWurmCmd(player, argv);
            return true;

        } catch (Throwable e) {

            String mesg = String.format("WurmCmd Error: %s %s", cmdName, e.toString());
            logger.log(Level.INFO, mesg);
            
            return true;

        }
    }
}
