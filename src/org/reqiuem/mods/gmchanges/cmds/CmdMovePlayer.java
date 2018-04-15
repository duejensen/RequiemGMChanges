
package org.reqiuem.mods.gmchanges.cmds;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import org.reqiuem.mods.gmchanges.AllInOne;
import org.reqiuem.mods.gmchanges.utils.GoTo;
import org.reqiuem.mods.gmchanges.utils.WurmCmd;

public class CmdMovePlayer extends WurmCmd {

    public CmdMovePlayer() {
        super("#moveplayer",AllInOne.commandPowerLevel);
    }

    @Override
    public boolean runWurmCmd(Creature actor, String[] argv) {
        Communicator comm = actor.getCommunicator();

        if ( argv.length != 2 ) {
            comm.sendNormalServerMessage("usage: #moveplayer <player>");
            return true;
        }

        if ( GoTo.sendPlayerHere(actor, argv[1]) ) {
            return true;
        }
        return true;
    }

}
