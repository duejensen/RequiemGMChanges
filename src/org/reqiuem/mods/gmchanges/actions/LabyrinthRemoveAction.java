// 
// Decompiled by Procyon v0.5.30
// 

package org.reqiuem.mods.gmchanges.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.reqiuem.mods.gmchanges.maze.Maze;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LabyrinthRemoveAction implements ModAction
{
    private static Logger logger;
    private static short actionId;
    private final ActionEntry actionEntry;
    
    static {
        LabyrinthRemoveAction.logger = Logger.getLogger(LabyrinthRemoveAction.class.getName());
    }
    
    public static short getActionId() {
        return LabyrinthRemoveAction.actionId;
    }
    
    public LabyrinthRemoveAction() {
        LabyrinthRemoveAction.logger.log(Level.INFO, "SprintAction()");
        LabyrinthRemoveAction.actionId = (short)ModActions.getNextActionId();
        ModActions.registerAction(this.actionEntry = ActionEntry.createEntry(LabyrinthRemoveAction.actionId, "Remove GM Labyrinth", "removing", new int[] { 23, 29 }));
    }
    
    public BehaviourProvider getBehaviourProvider() {
        return (BehaviourProvider)new BehaviourProvider() {
            public List<ActionEntry> getBehavioursFor(final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile, final int dir) {
                if (performer.getPower() >= LabyrinthAction.requiredGMlevel) {
                    return Arrays.asList(LabyrinthRemoveAction.this.actionEntry);
                }
                return null;
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final Item subject, final int tilex, final int tiley, final boolean onSurface, final int tile) {
                if (performer.getPower() >= LabyrinthAction.requiredGMlevel) {
                    return Arrays.asList(LabyrinthRemoveAction.this.actionEntry);
                }
                return null;
            }
            
            public List<ActionEntry> getBehavioursFor(final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile) {
                if (performer.getPower() >= LabyrinthAction.requiredGMlevel) {
                    return Arrays.asList(LabyrinthRemoveAction.this.actionEntry);
                }
                return null;
            }
        };
    }
    
    public ActionPerformer getActionPerformer() {
        return (ActionPerformer)new ActionPerformer() {
            public short getActionId() {
                return LabyrinthRemoveAction.actionId;
            }
            
            public boolean action(final Action act, final Creature performer, final Item source, final int tilex, final int tiley, final boolean onSurface, final int heightOffset, final int tile, final short action, final float counter) {
                this.action(act, performer, tilex, tiley, onSurface, tile, action, counter);
                return true;
            }
            
            public boolean action(final Action act, final Creature performer, final int tilex, final int tiley, final boolean onSurface, final int tile, final short action, final float counter) {
                if (performer.getPower() < LabyrinthAction.requiredGMlevel) {
                    return true;
                }
                performer.getCommunicator().sendNormalServerMessage("Remove Labyrinth!");
                final Maze m = new Maze(tilex, tiley, 60, (byte)126);
                m.clear();
                return true;
            }
        };
    }
}
