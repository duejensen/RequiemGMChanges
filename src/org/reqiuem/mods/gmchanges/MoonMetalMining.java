package org.reqiuem.mods.gmchanges;

import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.*;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;

import java.lang.reflect.InvocationHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Webba
 */
public class MoonMetalMining {
    private static final Logger _logger = Logger.getLogger(BetterGamemasters.class.getName());
    public static int newVeinCap = 10000;
    public static int newHomeVeinCap = 100;

     static void addRandomMoonMetalDrop(){
        HookManager.getInstance().registerHook("com.wurmonline.server.behaviours.TileRockBehaviour", "createGem", AllInOne.createGemMethodDesc, new InvocationHandlerFactory(){
            @Override
            public InvocationHandler createInvocationHandler(){
                return (object, method, args) -> {
                    int tilex = (int)args[0];
                    int tiley = (int)args[1];
                    int createtilex = (int)args[2];
                    int createtiley = (int)args[3];
                    Creature performer = (Creature)args[4];
                    double power = (double)args[5];
                    boolean surfaced = (boolean)args[6];
                    Action act = (Action)args[7];
                    final byte rarity = (byte)((act != null) ? act.getRarity() : 0);
                    int tempid = 0;
                    if(AllInOne.staticRandomAdamantiteChance > 0){
                        if (Server.rand.nextInt(AllInOne.staticRandomAdamantiteChance) == 0) {
                            tempid = 693;
                        }
                    }
                    if(AllInOne.staticRandomGlimmersteelChance > 0){
                        if (tempid == 0 && Server.rand.nextInt(AllInOne.staticRandomGlimmersteelChance) == 0) {
                            tempid = 697;
                        }
                    }
                    if(AllInOne.staticRandomSeryllChance > 0){
                        if (tempid == 0 && Server.rand.nextInt(AllInOne.staticRandomSeryllChance) == 0) {
                            tempid = 837;
                        }
                    }
                    if (tempid != 0){
                        if (tilex < 0 && tiley < 0) {
                            final Item metal = ItemFactory.createItem(tempid, (float)power, (String)null);
                            metal.setLastOwnerId(performer.getWurmId());
                            return metal;
                        }
                        final Item metal = ItemFactory.createItem(tempid, (float)power, (float)(createtilex * 4 + Server.rand.nextInt(4)), (float)(createtiley * 4 + Server.rand.nextInt(4)), Server.rand.nextFloat() * 360.0f, surfaced, rarity, -10L, (String)null);
                        metal.setLastOwnerId(performer.getWurmId());
                        performer.getCommunicator().sendNormalServerMessage("You find a chunk of a mysterious metal.");
                    }
                    return method.invoke(object, args);
                };
            }
        });
    }

    static void removeMoonMetalVeinCap(){
        try{
            _logger.log(Level.INFO, "Changing Moon Metal vein ammount cap");
            ClassPool cp = HookManager.getInstance().getClassPool();
            CtClass caveWallClass = cp.get("com.wurmonline.server.behaviours.CaveWallBehaviour");
            
            MethodInfo mi = caveWallClass.getMethod("action", AllInOne.actionMethodDesc).getMethodInfo();
            CodeAttribute ca = mi.getCodeAttribute();
            ConstPool constPool= ca.getConstPool();
            int capRef = constPool.addIntegerInfo(newVeinCap);

            CodeIterator codeIterator = ca.iterator();
            /*
            while(codeIterator.hasNext()) {

                int pos = codeIterator.next();
                int op = codeIterator.byteAt(pos);
                int siPusharg = codeIterator.u16bitAt(pos+1);

                //bipush(2) if_icmple(3) iload(2)  <pos>sipush(3) if_icmpeq(3) iload(2) sipush(3) if_icmpne(3) TOOVERWRITE(bipush(2) to iload(2)) TOGET(istore(2)) overwritepos: 14-15 getpos = 17

                if (op == CodeIterator.SIPUSH && siPusharg == 693){
                    logger.log(Level.INFO, "Found bytecode pattern for moon metal veins");
                    codeIterator.insertGap(pos+14, 1);
                    codeIterator.writeByte(CodeIterator.LDC_W, pos+14);
                    codeIterator.write16bit(capRef, pos+15);
                    codeIterator.insertGap(pos-7, 1);
                    codeIterator.writeByte(CodeIterator.LDC_W, pos-7);
                    codeIterator.write16bit(capRef, pos-6);
                    logger.log(Level.INFO, "Moon metal vein cap changed");
                    break;
                }
            }
            */
            while(codeIterator.hasNext()) {

                int pos = codeIterator.next();
                int op = codeIterator.byteAt(pos);
                if (op == CodeIterator.SIPUSH)
                {
                    int siPusharg = codeIterator.u16bitAt(pos+1);
                    if (siPusharg == 693)
                    {
                        _logger.log(Level.INFO, "Found bytecode pattern for moon metal veins");
                        codeIterator.insertGap(pos+14, 1);
                        codeIterator.writeByte(CodeIterator.LDC_W, pos+14);
                        codeIterator.write16bit(capRef, pos+15);
                        codeIterator.insertGap(pos-7, 1);
                        codeIterator.writeByte(CodeIterator.LDC_W, pos-7);
                        codeIterator.write16bit(capRef, pos-6);
                        _logger.log(Level.INFO, "Moon metal vein cap changed");
                        break;
                    }
                }
            }
            mi.rebuildStackMap(cp);
        }
        catch(NotFoundException e)
        {
            throw new HookException(e);
        }
        catch(BadBytecode e){
            System.out.println("BAD BYTECODE ERROR ----- ");
            e.printStackTrace();
        }
    }
    public static void changeHomeServerVeinCap(){
        try{
            _logger.log(Level.INFO, "Changing home server vein quality cap");
            ClassPool cp = HookManager.getInstance().getClassPool();
            CtClass tileRockClass = cp.get("com.wurmonline.server.behaviours.TileRockBehaviour");
            
            MethodInfo mi = tileRockClass.getConstructor(Descriptor.ofConstructor(new CtClass[]{})).getMethodInfo();
            CodeAttribute ca = mi.getCodeAttribute();
            ConstPool cpool = ca.getConstPool();
            

            CodeIterator codeIterator = ca.iterator();
            while(codeIterator.hasNext()) {

                int pos = codeIterator.next();
                int op = codeIterator.byteAt(pos);
                int biPusharg = codeIterator.byteAt(pos+1);
                int nextop = codeIterator.byteAt(pos+2);
                int nextarg = codeIterator.u16bitAt(pos+3);
                
                if (op == CodeIterator.BIPUSH && biPusharg == 50 && nextop == CodeIterator.PUTSTATIC && cpool.getFieldrefName(nextarg).equals("MAX_QL")){

                    _logger.log(Level.INFO, "Found bytecode pattern for home server metal veins");
                    codeIterator.writeByte((byte)newHomeVeinCap, pos+1);
                    _logger.log(Level.INFO, "Home server vein cap changed");
                    break;
                }
            }
            mi.rebuildStackMap(cp);
        }
        catch(NotFoundException e)
        {
            throw new HookException(e);
        }
        catch(BadBytecode e){
            System.out.println("BAD BYTECODE ERROR ----- ");
            e.printStackTrace();
        }
    }
}