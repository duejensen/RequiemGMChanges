package org.reqiuem.mods.gmchanges.utils;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GoTo {

    private static final Logger logger = Logger.getLogger("GoTo");

    public static void whups() {
    }

    public static boolean sendToVillage(Creature actor, String villageName) {

        try {

            Communicator comm = actor.getCommunicator();

            Village vill = Villages.getVillage(villageName);
            Item tokn = vill.getToken();

            short x = (short)((int)tokn.getPosX() >> 2);
            short y = (short)((int)tokn.getPosY() >> 2);

            comm.sendNormalServerMessage(String.format("Teleporting To Deed: %s (%d,%d)", villageName, x, y));

            actor.setTeleportPoints(x, y, 0, 0);
            actor.startTeleporting();

            comm.sendTeleport(false);

            return true;

        } catch (NoSuchVillageException e) {
            return false;

        } catch (Throwable e) {
            logger.log(Level.SEVERE, "sendToVillage: " + e.toString() );
            return false;
        }

    }

    public static boolean sendToPlayer(Creature actor, String playerName) {
        Communicator comm = actor.getCommunicator();

        try {

            Player dest = Players.getInstance().getPlayer(playerName);

            CreatureStatus status = dest.getStatus();

            short x = (short)((int)status.getPositionX() >> 2);
            short y = (short)((int)status.getPositionY() >> 2);

            int layer = status.getLayer();

            comm.sendNormalServerMessage(String.format("Teleporting To Player: %s (%d,%d)", playerName, x, y));

            actor.setTeleportPoints(x, y, layer, 0);
            actor.startTeleporting();

            comm.sendTeleport(false);

            return true;

        } catch (NoSuchPlayerException e) {
            return false;

        } catch (Throwable e) {
            logger.log(Level.SEVERE, "sendToPlayer: " + e.toString() );
            return false;
        }
    }

    public static boolean sendPlayerHome(Creature actor, String playerName) {
    	Communicator comm = actor.getCommunicator();
    	try {
    		Player p = Players.getInstance().getPlayer(playerName);
    		String villageName= p.getVillageName();
    		if (villageName.length()>0) {
    			
    			Village vill = Villages.getVillage(villageName);
    	        Item tokn = vill.getToken();

    	        short x = (short)((int)tokn.getPosX() >> 2);
    	        short y = (short)((int)tokn.getPosY() >> 2);

    	        comm.sendNormalServerMessage(String.format("Teleporting player %s to Deed: %s (%d,%d)",  playerName, villageName, x, y));
    	        Communicator playercomm = p.getCommunicator();
    	        
    	        p.setTeleportPoints(x, y, 0, 0);
    	        p.startTeleporting();
    	        String actorName = actor.getName();
    	        playercomm.sendNormalServerMessage(String.format("Teleported home to Deed %s by %s", villageName, actorName));
    	        playercomm.sendTeleport(false);
    	                			
    		} else {
    			comm.sendNormalServerMessage(String.format("Player %s has no homevillage", playerName));
    		}
    	
    	} catch (NoSuchPlayerException e) {
    		comm.sendNormalServerMessage(String.format("Player %s not found", playerName));
    		return false;
    	}
    	catch (Throwable e) {
            logger.log(Level.SEVERE, "sendToHome: " + e.toString() );
            return false;
        }
        return false;
    }


    public static boolean sendPlayerHere(Creature actor, String playerName) {
    	Communicator comm = actor.getCommunicator();
    	try {
    		Player p = Players.getInstance().getPlayer(playerName);

    		short x = (short)((int)actor.getPosX() >> 2);
	        short y = (short)((int)actor.getPosY() >> 2);
    			
    		
    		Communicator playercomm = p.getCommunicator();
    	        
    	    p.setTeleportPoints(x, y, 0, 0);
    	    p.startTeleporting();
    	    String actorName = actor.getName();
    	    playercomm.sendNormalServerMessage(String.format("Teleported by %s", actorName));
    	    playercomm.sendTeleport(false);
    	
    	} catch (NoSuchPlayerException e) {
    		comm.sendNormalServerMessage(String.format("Player %s not found", playerName));
    		return false;
    	}
    	catch (Throwable e) {
            logger.log(Level.SEVERE, "sendToHome: " + e.toString() );
            return false;
        }
        return false;
    }


}
