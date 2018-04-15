package org.reqiuem.mods.gmchanges.cmds;

import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.sounds.SoundPlayer;
import org.reqiuem.mods.gmchanges.AllInOne;
import org.reqiuem.mods.gmchanges.utils.WurmCmd;

public class CmdFillUp extends WurmCmd {

    public CmdFillUp () {
        super("#fillup",AllInOne.commandPowerLevel);
    }

    @Override
    public boolean runWurmCmd(Creature actor, String[] argv) {
        Communicator comm = actor.getCommunicator();

        if ( argv.length != 2 ) {
            comm.sendNormalServerMessage("usage: #fillup <player> ");
            SoundPlayer.playSound("sound.fx.humm", actor, 1.0f);//added to make it sound cool!
            return true;
        }

        try {

            Player player = Players.getInstance().getPlayer(argv[1]);
            player.getStatus().refresh(0.99f,true);
            player.getStatus().removeWounds();
            player.getStatus().setMaxCCFP();
            String mesg = String.format("Player %s refreshed and healed", argv[1]);
            comm.sendNormalServerMessage(mesg);

    	} catch (NoSuchPlayerException e) {
    		comm.sendNormalServerMessage(String.format("Player %s not found", argv[1]));
    		return false;
   
        } catch (Throwable e) {
            comm.sendNormalServerMessage("error: " + e.toString());
            return true;
        }

        return true;
    }

}
