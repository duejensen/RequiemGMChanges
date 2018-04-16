package org.reqiuem.mods.gmchanges;

import javassist.*;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements several things that are meant to make a GameMaster's life a bit
 * easier, by providing utility and convenience.
 */
public class BetterGamemasters {
    private static final Logger _logger = Logger.getLogger(BetterGamemasters.class.getName());


    protected static void SpawnTowerGuards() {
        _logger.info("Trying to mal pollGuards() methods public.");
        
        try {
            CtMethod method = HookManager.getInstance().getClassPool().get("com.wurmonline.server.kingdom.GuardTower")
                    .getMethod("pollGuards", "()V");
            method.setModifiers((method.getModifiers() & ~Modifier.PRIVATE) | Modifier.PUBLIC);
            
            method = HookManager.getInstance().getClassPool().get("com.wurmonline.server.villages.GuardPlan")
                    .getMethod("pollGuards", "()V");
            method.setModifiers((method.getModifiers() & ~Modifier.PRIVATE) | Modifier.PUBLIC);
            
            _logger.info("pollGuards() methods are now public.");
        }
        catch (Exception e) {
            _logger.log(Level.SEVERE, "Couldn't make pollGuards() methods public.", e);
        }
    }

    /**
     * Does not currently work
     */
    protected static void NoDropItemLimit() {
        _logger.info("Applying patch for no drop item limit.");
        
        try {
            CodeIterator it =
                HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.MethodsItems")
                    .getMethod("drop", "(Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;Z)[Ljava/lang/String;")
                    .getMethodInfo()
                    .getCodeAttribute()
                    .iterator();
            
            while (it.hasNext()) {
                int index = it.next();
                int opcode = it.byteAt(index);
                
                if (opcode == Opcode.BIPUSH && it.byteAt(index + 1) == 120) {
                    _logger.log(Level.INFO, "Found opcode at index #{0}.", index);
                    it.write(new byte[] { 0x00, 0x00 }, index);
                    it.insertAt(index, new byte[] { 0x11, 32767 >> 8, (byte)(32767 & 0xFF) } );
                    break;
                }
            }
            
            _logger.info("Removed drop limit for non-players.");
        }
        catch (NotFoundException | BadBytecode e) {
            _logger.log(Level.SEVERE, "Can't apply patch for no drop item limit.", e);
        }           
    }
    
    /**
     * Effectively makes players with the set MGMT power or above able to pick
     * up items beyond their body strength limit.
     * 
     * Technically, this is done by overriding the canCarry(I)Z method,
     * returning true whenever their MGMT power is high enough.
     */
    static void NoCarryWeightLimit() {
        try {
            CtClass ctClass = HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature");
            CtClass[] parameters = new CtClass[]{CtPrimitiveType.intType};
            CtMethod method = ctClass.getMethod("canCarry", Descriptor.ofMethod(CtPrimitiveType.booleanType, parameters));
            method.insertBefore("{ if (this.getPower() >= " + AllInOne._noCarryWeightLimitPower + ") return true; }");
            
            method = ctClass.getMethod("getCarryCapacityFor", Descriptor.ofMethod(CtPrimitiveType.intType, parameters));
            method.insertBefore("{ if (this.getPower() >= " + AllInOne._noCarryWeightLimitPower + ") return 0x7FFFFFFF; }");
            
            method = ctClass.getMethod("getCarryingCapacityLeft", Descriptor.ofMethod(CtPrimitiveType.intType, new CtClass[0]));
            method.insertBefore("{ if (this.getPower() >= " + AllInOne._noCarryWeightLimitPower + ") return 0x7FFFFFFF; }");
            
        } catch (Exception ex) {
            throw new HookException(ex);
        }
    }

    /**
     * Makes characters with a set MGMT power or above to be not slowed down
     * in movement speed by the weight they're carrying.
     * 
     * Technically this is done by setting the thresholds for "move slow",
     * "encumbered" and "cantmove" to 2^31-1, or 0x7FFFFFFF, or to be precise
     * to exactly "about 2 billion grams", the maximum positive signed 32 bit
     * integer value.
     */
    protected static void NotSlowedByWeight() {
        try {
            CtClass ctClass = HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature");
            CtClass[] parameters = new CtClass[0];
            CtMethod method = ctClass.getMethod("setMoveLimits", Descriptor.ofMethod(CtPrimitiveType.voidType, parameters));

            method.insertBefore("{ if (this.getPower() >= " + AllInOne._notSlowedByWeightPower + ") { this.moveslow = this.encumbered = this.cantmove = 0x7FFFFFFF; return; } }");
            
            method = null;
            parameters = null;
            ctClass = null;
        } catch (CannotCompileException | NotFoundException ex) {
            throw new HookException(ex);
        }
    }
    
    /**
     * ========== DOES NOT WORK CURRENTLY =========
     *
     * Effectively makes any item that is owned by a players with the set
     * MGMT power take no damage at all. That includes tools through use,
     * weapons, even fences or house wall. Everything that is a DbItem taking
     * damage through the DbItem.setDamage(FZ)Z method.
     * 
     * Technically this is done by inserting a code snippet that can be found
     * in com.wurmonline.server.items.Item.getOwnerOrNull()LCreature; returning
     * the owner or null, then polling the getPower()I MGMT power value.
     */
    protected static void NoDamageOnGamemasterOwnedItems() {
        try {
            CtClass ctClass = HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.DbItem");
            CtClass[] parameters = {
                CtPrimitiveType.floatType,
                CtPrimitiveType.booleanType
            };
            CtMethod method = ctClass.getMethod("setDamage",
                    Descriptor.ofMethod(CtPrimitiveType.booleanType, parameters));

            // Needs fully qualified names or fails compiling.
            method.insertBefore(
                    "{"
                        + "try {" 
                        + "long bgOwnerID = this.getOwnerId();"
                        + "if (bgOwnerID != -10L && com.wurmonline.server.Server.getInstance().getCreature(bgOwnerID).getPower() >= " + AllInOne._noDamageOnGamemasterOwnedItems + ") return false;"
                        + "} catch (com.wurmonline.server.creatures.NoSuchCreatureException nsc) {"
                        + "com.wurmonline.server.items.DbItem.logger.log(java.util.logging.Level.WARNING, nsc.getMessage(), (Throwable)nsc); "
                        + "} catch (com.wurmonline.server.NoSuchPlayerException nsp) {"
                        + "if (com.wurmonline.server.players.PlayerInfoFactory.getPlayerInfoWithWurmId(this.getOwnerId()) == null)"
                        + "com.wurmonline.server.items.DbItem.logger.log(java.util.logging.Level.WARNING, nsp.getMessage(), (Throwable)nsp); }"
                    + "}");
            
            method = null;
            parameters = null;
            ctClass = null;
            
        } catch (NotFoundException | CannotCompileException ex) {
            throw new HookException(ex);
        }
    }
    
    /**
     * Effectively makes Gamemasters of MGMT powers 2 and higher require no
     * skill or material building floors below, above, floors with opening,
     * staircases on floors below, and roofs.
     * 
     * Technically this is done by having the "boolean insta" variable in the
     * floorBuilding(LAction;LCreature;LItem;LFloor;SF)Z method set to true.
     * Normally this applies only on test servers, we replace the call to
     * Servers.isThisATestServer() with true.
     */
    protected static void NoFloorBuildingRequirements() {
        try {
            CtClass ctClass = HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.FloorBehaviour");
            CtClass[] parameters = {
                HookManager.getInstance().getClassPool().get("com.wurmonline.server.behaviours.Action"),
                HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature"),
                HookManager.getInstance().getClassPool().get("com.wurmonline.server.items.Item"),
                HookManager.getInstance().getClassPool().get("com.wurmonline.server.structures.Floor"),
                CtPrimitiveType.shortType,
                CtPrimitiveType.floatType
            };
            CtMethod method = ctClass.getMethod("floorBuilding",
                    Descriptor.ofMethod(CtPrimitiveType.booleanType, parameters));
            
            // Changes the "Servers.isThisATestServer()" call to "true".
            method.instrument(new ExprEditor() {
                @Override
                public void edit(MethodCall methodCall) throws CannotCompileException {
                    String methodName = methodCall.getMethodName();
                    
                    if (methodName.equals("isThisATestServer"))
                        methodCall.replace("$_ = true;");
                }
            });
            
            method = null;
            parameters = null;
            ctClass = null;
        } catch (NotFoundException | CannotCompileException ex) {
            throw new HookException(ex);
        }
    }
}
