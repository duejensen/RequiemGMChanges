// 
// Decompiled by Procyon v0.5.30
// 

package org.reqiuem.mods.gmchanges.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.Actions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.reqiuem.mods.gmchanges.AllInOne;
import org.reqiuem.mods.gmchanges.maze.Maze;
import org.reqiuem.mods.gmchanges.utils.Cooldowns;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LabyrinthAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    private static String effectName;
    private static long cooldown;
    public static boolean isGMonly;
    public static int requiredGMlevel;
    
    static {
        LabyrinthAction.logger = Logger.getLogger(LabyrinthAction.class.getName());
        LabyrinthAction.effectName = "labyrinth";
        LabyrinthAction.cooldown = 36000000L;
        LabyrinthAction.isGMonly = true;
        LabyrinthAction.requiredGMlevel = AllInOne.commandPowerLevel;
    }
    
    public static short getActionId() {
        return LabyrinthAction.actionId;
    }
    
    public LabyrinthAction() {
        LabyrinthAction.logger.log(Level.INFO, "LabyrinthAction()");
        LabyrinthAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(LabyrinthAction.actionId,
                "GM Labyrinth", "creating", new int[] {
                Actions.ABILITY_MAGICIAN,
                Actions.ABILITY_NORN
        }));
    }
    
    boolean isAllowed(final Creature performer) {
        return (!LabyrinthAction.isGMonly || performer.getPower() >= LabyrinthAction.requiredGMlevel) && (performer.getPower() >= LabyrinthAction.requiredGMlevel/* || Vampires.isVampire(performer.getWurmId())*/);
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return (BehaviourProvider)new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Creature target) {
                return this.getBehavioursFor(performer, target.getTileX(), target.getTileY(), target.isOnSurface(), target.getCurrentTileNum());
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final Creature target) {
                return this.getBehavioursFor(performer, target.getTileX(), target.getTileY(), target.isOnSurface(), target.getCurrentTileNum());
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile, final int dir) {
                if (!LabyrinthAction.this.isAllowed(performer)) {
                    return null;
                }
                return Arrays.asList(LabyrinthAction.this.actionEntry);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final int tilex, final int tiley, final boolean onSurface, final int tile) {
                if (!LabyrinthAction.this.isAllowed(performer)) {
                    return null;
                }
                return Arrays.asList(LabyrinthAction.this.actionEntry);
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile) {
                if (!LabyrinthAction.this.isAllowed(performer)) {
                    return null;
                }
                return Arrays.asList(LabyrinthAction.this.actionEntry);
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return (ActionPerformer)new ActionPerformer() {
            public short getActionId() {
                return LabyrinthAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Creature target, final short action, final float counter) {
                if (!performer.isOnSurface() || !target.isOnSurface()) {
                    performer.getCommunicator().sendNormalServerMessage("You can only do this above ground.");
                    return true;
                }
                return this.action(act, performer, target.getTileX(), target.getTileY(), target.isOnSurface(), target.getCurrentTileNum(), action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final Creature target, final short action, final float counter) {
                if (!performer.isOnSurface() || !target.isOnSurface()) {
                    performer.getCommunicator().sendNormalServerMessage("You can only do this above ground.");
                    return true;
                }
                return this.action(act, performer, target.getTileX(), target.getTileY(), target.isOnSurface(), target.getCurrentTileNum(), action, counter);
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final int tilex, final int tiley, final boolean onSurface, final int heightOffset, final int tile, final short action, final float counter) {
                this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile, final short action, final float counter) {
                if (!LabyrinthAction.this.isAllowed(performer)) {
                    return true;
                }
                if (!performer.isPlayer() || performer.getVehicle() != -10L) {
                    return true;
                }
                if (!performer.isOnSurface()) {
                    performer.getCommunicator().sendNormalServerMessage("You can only do this above ground.");
                    return true;
                }
                final String playerEffect = String.valueOf(performer.getName()) + LabyrinthAction.effectName;
                if (!AllInOne.isTestEnv() && performer.getPower() < LabyrinthAction.requiredGMlevel && Cooldowns.isOnCooldown(playerEffect, LabyrinthAction.cooldown)) {
                    performer.getCommunicator().sendNormalServerMessage("You did this too recently.");
                    return true;
                }
                Cooldowns.setUsed(playerEffect);
                performer.getCommunicator().sendNormalServerMessage("Labyrinth!");
                final Maze m = new Maze(tilex, tiley, 21, StructureConstantsEnum.HEDGE_FLOWER3_HIGH);
                m.create(true, false);
                return true;
            }
        };
    }
}
