
package org.reqiuem.mods.gmchanges.cmds;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import org.reqiuem.mods.gmchanges.AllInOne;
import org.reqiuem.mods.gmchanges.utils.GoTo;
import org.reqiuem.mods.gmchanges.utils.WurmCmd;

public class CmdGoTo extends WurmCmd {

    public CmdGoTo() {
        super("#goto",AllInOne.commandPowerLevel);
    }

    @Override
    public boolean runWurmCmd(Creature actor, String[] argv) {
        Communicator comm = actor.getCommunicator();

        if ( argv.length != 2 ) {
            comm.sendNormalServerMessage("usage: #goto <deed|player>");
            return true;
        }


        if ( GoTo.sendToPlayer(actor, argv[1]) ) {
            return true;
        }

        if ( GoTo.sendToVillage(actor, argv[1]) ) {
            return true;
        }

        return true;
    }

}
