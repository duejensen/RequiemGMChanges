package org.reqiuem.mods.gmchanges.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActSpawnTowerGuard implements ModAction, ActionPerformer, BehaviourProvider {
    private final short _actionID;
    private final ActionEntry _actionEntry;
    
    public ActSpawnTowerGuard() {
        _actionID = (short)ModActions.getNextActionId();
        _actionEntry = ActionEntry.createEntry(_actionID, "GM Spawn guard", "spawning guard", new int[] { });
        ModActions.registerAction(_actionEntry);
    }
    
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
        return getBehavioursFor(performer, null, target);
    }
    
    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item activated, Item target) {
        if (performer.getPower() > 0 && (IsGuardTower(target.getTemplateId()) || IsSettlementToken(target.getTemplateId())))
            return Arrays.asList(_actionEntry);
        
        return null;
    }
    
    @Override
    public short getActionId() {
        return _actionID;
    }
    
    @Override
    public boolean action(Action action, Creature performer, Item activated, Item target, short num, float counter) {
        if (!(performer instanceof Player))
            return true;
        
        try {
            if (IsGuardTower(target.getTemplateId())) {
                GuardTower tower = Kingdoms.getTower(target);

                if (tower == null) {
                    performer.getCommunicator().sendNormalServerMessage("This is not a guard tower.");
                    return true;
                }

                tower.getClass().getMethod("pollGuards").invoke(tower);
            }
            else if (IsSettlementToken(target.getTemplateId())) {
                Village village = Villages.getVillage(target.getTilePos(), target.isOnSurface());
                if (village == null) {
                    performer.getCommunicator().sendNormalServerMessage("Village for settlement token could not be found.");
                    return true;
                }

                GuardPlan.class.getMethod("pollGuards").invoke(village.plan);
            }
        }
        catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Logger.getLogger(ActSpawnTowerGuard.class.getName())
                .log(Level.SEVERE, "Manual tower guard spawning failed.", e);
        }

        return true;
    }
    
    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        return action(action, performer, null, target, num, counter);
    }

    public boolean IsGuardTower(int templateID) {
        return templateID == 384 || templateID == 430 || templateID == 528 || templateID == 638;

    }

    public boolean IsSettlementToken(int templateID) {
        return (templateID == 236);
    }
}
