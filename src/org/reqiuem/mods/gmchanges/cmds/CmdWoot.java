package org.reqiuem.mods.gmchanges.cmds;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import org.reqiuem.mods.gmchanges.utils.WurmCmd;

public class CmdWoot extends WurmCmd {

    public CmdWoot () {
        super("#woot",1);
    }

    @Override
    public boolean runWurmCmd(Creature actor, String[] argv) {
        Communicator comm = actor.getCommunicator();
        comm.sendNormalServerMessage("Woot to you: " + actor.getName());
        return true;
    }

}
