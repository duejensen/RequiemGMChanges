package org.reqiuem.mods.gmchanges.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.Wall;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.reqiuem.mods.gmchanges.utils.WurmConst;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActGmProtect implements ModAction, BehaviourProvider, ActionPerformer {

    public static final Logger logger = Logger.getLogger("ActGmProtect");

    public final short actionId;
    public final ActionEntry actionEntry;

    public ActGmProtect() {

        actionId = (short) ModActions.getNextActionId();

        int[] types = {
            WurmConst.ACTION_TYPE_QUICK,
            WurmConst.ACTION_TYPE_NEVER_USE_ACTIVE_ITEM,
        };

        actionEntry = ActionEntry.createEntry( actionId, "GM Protect", "Protecting", types);
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return this;
    }

    @Override
    public ActionPerformer getActionPerformer() {
        return this;
    }

    @Override
    public short getActionId() {
        return actionId;
    }

    public List<ActionEntry> getBehavForAll(Creature actor, Object targ) {

        if ( targ == null ) {
            return null;
        }

        if ( actor.getPower() < 5 ) {
            return null;
        }

        return Collections.singletonList(actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, Item source, Item target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, Item target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, Item subject, Fence target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, Fence target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, Item subject, Wall target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, Wall target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, boolean onSurface, Floor target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, Item item, boolean onSurface, Floor target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, boolean aOnSurface, BridgePart target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature actor, Item item, boolean aOnSurface, BridgePart target) {
        return getBehavForAll(actor,target);
    }

    @Override
    public boolean action(Action action, Creature actor, Item source, Item target, short num, float counter) {
        Communicator comm = actor.getCommunicator();
        try { 

            if ( actor.getPower() < 5 ) {
                comm.sendNormalServerMessage("newp", (byte) 3);
                return true;
            }

            target.setHasNoDecay(true);
            target.setIsIndestructible(true);

            target.setIsNoPut(true);
            target.setIsNoDrag(true);
            target.setIsNoDrop(true);
            target.setIsNoMove(true);
            target.setIsNoTake(true);
            target.setIsPlanted(true);
            target.setIsNoRepair(true);
            target.setIsNoImprove(true);
            target.setIsNotLockable(true);
            target.setIsNotTurnable(true);
            target.setIsNotPaintable(true);
            target.setIsNotSpellTarget(true);
            target.setIsNotLockpickable(true);
            target.savePermissions();

            comm.sendNormalServerMessage("Protect Action: " + target.getName(), (byte) 3);
            return true;

        } catch (Throwable e) {
            logger.log(Level.SEVERE,"action error: " + e.toString());
            comm.sendNormalServerMessage("action error: " + e.toString());
            return true;
        }
    }

    @Override
    public boolean action(Action action, Creature actor, Item target, short num, float counter) {
        return action(action, actor, null, target, num, counter);
    }


    @Override
    public boolean action(Action act, Creature actor, Item sorc, Wall targ, short action, float counter) {
        Communicator comm = actor.getCommunicator();

        try {

            if ( actor.getPower() < 5 ) {
                comm.sendNormalServerMessage("newp", (byte) 3);
                return true;
            }

            targ.setHasNoDecay(true);
            targ.setIsIndestructible(true);

            targ.setIsNoRepair(true);
            targ.setIsNoImprove(true);
            targ.setIsNotSpellTarget(true);
            targ.savePermissions();

            comm.sendNormalServerMessage("GM Protect: wall");
            return true;

        } catch (Throwable e) {
            logger.log(Level.SEVERE,"action error: " + e.toString());
            comm.sendNormalServerMessage("action error: " + e.toString());
            return true;
        }

    }

    @Override
    public boolean action(Action act, Creature actor, Wall targ, short action, float counter) {
        return action(act,actor,null,targ,action,counter);
    }

    @Override
    public boolean action(Action act, Creature actor, Item sorc, boolean onsurf, Floor targ, int encTile, short action, float counter) {
        Communicator comm = actor.getCommunicator();

        try {

            if ( actor.getPower() < 5 ) {
                comm.sendNormalServerMessage("newp", (byte) 3);
                return true;
            }

            targ.setHasNoDecay(true);
            targ.setIsIndestructible(true);

            targ.setIsNoRepair(true);
            targ.setIsNoImprove(true);
            targ.setIsNotSpellTarget(true);
            targ.savePermissions();

            comm.sendNormalServerMessage("GM Protect: floor");
            return true;

        } catch (Throwable e) {
            logger.log(Level.SEVERE,"action error: " + e.toString());
            comm.sendNormalServerMessage("action error: " + e.toString());
            return true;
        }

    }

    @Override
    public boolean action(Action act, Creature actor, boolean onsurf, Floor targ, int encTile, short action, float counter) {
        return action(act,actor,null,onsurf,targ,encTile,action,counter);
    }

    @Override
    public boolean action(Action act, Creature actor, Item sorc, boolean onsurf, Fence targ, short action, float counter) {
        Communicator comm = actor.getCommunicator();

        try {

            if ( actor.getPower() < 5 ) {
                comm.sendNormalServerMessage("newp", (byte) 3);
                return true;
            }

            targ.setHasNoDecay(true);
            targ.setIsIndestructible(true);

            targ.setIsNoRepair(true);
            targ.setIsNoImprove(true);
            targ.setIsNotSpellTarget(true);
            targ.savePermissions();

            comm.sendNormalServerMessage("GM Protect: fence");
            return true;

        } catch (Throwable e) {
            logger.log(Level.SEVERE,"action error: " + e.toString());
            comm.sendNormalServerMessage("action error: " + e.toString());
            return true;
        }

    }

    @Override
    public boolean action(Action act, Creature actor, boolean onsurf, Fence targ, short action, float counter) {
        return action(act,actor,null,onsurf,targ,action,counter);
    }

    @Override
    public boolean action(Action act, Creature actor, Item sorc, boolean onsurf, BridgePart targ, int encTile, short action, float counter) {
        Communicator comm = actor.getCommunicator();

        try {

            if ( actor.getPower() < 5 ) {
                comm.sendNormalServerMessage("newp", (byte) 3);
                return true;
            }

            targ.setHasNoDecay(true);
            targ.setIsIndestructible(true);

            targ.setIsNoRepair(true);
            targ.setIsNoImprove(true);
            targ.setIsNotSpellTarget(true);
            targ.savePermissions();

            comm.sendNormalServerMessage("GM Protect: bridge");
            return true;

        } catch (Throwable e) {
            logger.log(Level.SEVERE,"action error: " + e.toString());
            comm.sendNormalServerMessage("action error: " + e.toString());
            return true;
        }

    }

    @Override
    public boolean action(Action act, Creature actor, boolean onsurf, BridgePart targ, int encTile, short action, float counter) {
        return action(act,actor,null,onsurf,targ,encTile,action,counter);
    }

}
