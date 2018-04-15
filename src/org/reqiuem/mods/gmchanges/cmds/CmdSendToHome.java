
package org.reqiuem.mods.gmchanges.cmds;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import org.reqiuem.mods.gmchanges.AllInOne;
import org.reqiuem.mods.gmchanges.utils.GoTo;
import org.reqiuem.mods.gmchanges.utils.WurmCmd;

public class CmdSendToHome extends WurmCmd {

    public CmdSendToHome() {
        super("#sendhome",AllInOne.commandPowerLevel);
    }

    @Override
    public boolean runWurmCmd(Creature actor, String[] argv) {
        Communicator comm = actor.getCommunicator();

        if ( argv.length != 2 ) {
            comm.sendNormalServerMessage("usage: #sendhome <player>");
            return true;
        }

        if ( GoTo.sendPlayerHome(actor, argv[1]) ) {
            return true;
        }

       
        return true;
    }

}
