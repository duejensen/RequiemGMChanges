package org.reqiuem.mods.gmchanges;

import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.webinterface.WcKingdomChat;
import javassist.*;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.Descriptor;
import javassist.bytecode.Opcode;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MiscChanges {
    public static Logger logger = Logger.getLogger(MiscChanges.class.getName());

    // Sets players move rates when encumbered
    public static void setNewMoveLimits(Creature cret){
        try {
            Skill strength = cret.getSkills().getSkill(102);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "moveslow"), strength.getKnowledge()*4000);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "encumbered"), strength.getKnowledge()*7000);
            ReflectionUtil.setPrivateField(cret, ReflectionUtil.getField(cret.getClass(), "cantmove"), strength.getKnowledge()*14000);
            MovementScheme moveScheme = cret.getMovementScheme();
            DoubleValueModifier stealthMod = ReflectionUtil.getPrivateField(moveScheme, ReflectionUtil.getField(moveScheme.getClass(), "stealthMod"));
            if (stealthMod == null) {
                stealthMod = new DoubleValueModifier((- 80.0 - Math.min(79.0, cret.getBodyControl())) / 100.0);
            } else {
                stealthMod.setModifier((- 80.0 - Math.min(79.0, cret.getBodyControl())) / 100.0);
            }
            ReflectionUtil.setPrivateField(moveScheme, ReflectionUtil.getField(moveScheme.getClass(), "stealthMod"), stealthMod);
        }
        catch (NoSuchSkillException | IllegalArgumentException | IllegalAccessException | ClassCastException | NoSuchFieldException nss) {
            logger.log(Level.WARNING, "No strength skill for " + cret, nss);
        }
    }

    public static void sendServerTabMessage(final String message, final int red, final int green, final int blue){
        Runnable r = () -> {
            com.wurmonline.server.Message mess;
            for(Player rec : Players.getInstance().getPlayers()){
                mess = new com.wurmonline.server.Message(rec, (byte)16, "Server", message, red, green, blue);
                rec.getCommunicator().sendMessage(mess);
            }
        };
        r.run();
    }

    public static long getTimedAffinitySeed(Item item){
        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithName(item.getCreatorName());
        if(pinf != null){
            return pinf.wurmId;
        }
        return 0;
    }

    public static void sendGlobalFreedomChat(final Creature sender, final String message, final int red, final int green, final int blue){
        Runnable r = () -> {
            com.wurmonline.server.Message mess;
            for(Player rec : Players.getInstance().getPlayers()){
                mess = new com.wurmonline.server.Message(sender, (byte)10, "GL-Freedom", "<"+sender.getName()+"> "+message, red, green, blue);
                rec.getCommunicator().sendMessage(mess);
            }
            if (message.trim().length() > 1) {
                WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(), sender.getWurmId(), sender.getName(), message, false, (byte) 4, red, green, blue);
                if (!Servers.isThisLoginServer()) {
                    wc.sendToLoginServer();
                } else {
                    wc.sendFromLoginServer();
                }
            }
        };
        r.run();
    }

    public static void sendImportantMessage(Creature sender, String message, int red, int green, int blue){
        sendServerTabMessage("<"+sender.getName()+"> "+message, red, green, blue);
        sendGlobalFreedomChat(sender, message, red, green, blue);
    }

    public static void preInit(){
        try{
            ClassPool classPool = HookManager.getInstance().getClassPool();
            CtClass ctSkill = classPool.get("com.wurmonline.server.skills.Skill");

            // - Create Server tab with initial messages - //
            CtClass ctPlayers = classPool.get("com.wurmonline.server.Players");
            CtMethod m = ctPlayers.getDeclaredMethod("sendStartGlobalKingdomChat");
            String infoTabTitle = "Server";
            // Initial messages:
            String[] infoTabLine = {"Server Messages will be displayed here",
                    "You cannot type into this channel"};
            String str = "{"
                    + "        com.wurmonline.server.Message mess;";
            for(int i = 0; i < infoTabLine.length; i++){
                str = str + "        mess = new com.wurmonline.server.Message(player, (byte)16, \"" + infoTabTitle + "\",\"" + infoTabLine[i] + "\", 0, 255, 0);"
                        + "        player.getCommunicator().sendMessage(mess);";
            }
            str = str + "}";
            m.insertAfter(str);

            // - Allow mailboxes and bell towers to be loaded - //
            CtClass ctItemTemplate = classPool.get("com.wurmonline.server.items.ItemTemplate");
            ctItemTemplate.getDeclaredMethod("isTransportable").setBody("{"
                    + "  return this.isTransportable || (this.getTemplateId() >= 510 && this.getTemplateId() <= 513) || this.getTemplateId() == 722 || this.getTemplateId() == 670;"
                    + "}");
            // But don't let mailboxes them be used while loaded...
            CtClass ctItem = classPool.get("com.wurmonline.server.items.Item");
            ctItem.getDeclaredMethod("moveToItem").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getOwnerId")) {
                        m.replace("$_ = $proceed($$);"
                                + "com.wurmonline.server.items.Item theTarget = com.wurmonline.server.Items.getItem(targetId);"
                                + "if(theTarget != null && theTarget.getTemplateId() >= 510 && theTarget.getTemplateId() <= 513){"
                                + "  if(theTarget.getTopParent() != theTarget.getWurmId()){"
                                + "    mover.getCommunicator().sendNormalServerMessage(\"Mailboxes cannot be used while loaded.\");"
                                + "    return false;"
                                + "  }"
                                + "}");
                        return;
                    }
                }
            });

            // - Fix de-priesting when gaining faith below 30 - //
            CtClass ctDbPlayerInfo = classPool.get("com.wurmonline.server.players.DbPlayerInfo");
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("min")) {
                        m.replace("if($2 == 20.0f && $1 < 30){"
                                + "  $_ = $proceed(30.0f, lFaith);"
                                + "}else{"
                                + "  $_ = $proceed($$);"
                                + "}");
                        return;
                    }
                }
            });
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("setPriest")) {
                        m.replace("$_ = $proceed(true);");
                        return;
                    }
                }
            });
            ctDbPlayerInfo.getDeclaredMethod("setFaith").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("sendAlertServerMessage")) {
                        m.replace("$_ = null;");
                        return;
                    }
                }
            });

            // - Remove requirement to bless for Libila taming - //
            CtClass ctMethodsCreatures = classPool.get("com.wurmonline.server.behaviours.MethodsCreatures");
            ctMethodsCreatures.getDeclaredMethod("tame").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isPriest")) {
                        m.replace("$_ = false;");
                        return;
                    }
                }
            });

            // - Remove fatiguing actions requiring you to be on the ground - //
            CtClass ctAction = classPool.get("com.wurmonline.server.behaviours.Action");
            CtConstructor[] ctActionConstructors = ctAction.getConstructors();
            for(CtConstructor constructor : ctActionConstructors){
                constructor.instrument(new ExprEditor(){
                    public void edit(MethodCall m) throws CannotCompileException {
                        if (m.getMethodName().equals("isFatigue")) {
                            m.replace("$_ = false;");
                            return;
                        }
                    }
                });
            }

            // - Allow all creatures to be displayed in the Mission Ruler - //
            CtClass ctMissionManager = classPool.get("com.wurmonline.server.questions.MissionManager");
            ctMissionManager.getDeclaredMethod("dropdownCreatureTemplates").instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                    if (Objects.equals("baseCombatRating", fieldAccess.getFieldName()))
                        fieldAccess.replace("$_ = 1.0f;");
                    //logger.info("Instrumented Mission Ruler to display all creatures.");
                }
            });

            //TODO find out how this "Affinity" Weekend works
            // - Affinity Weekend - //
            CtClass[] params4 = {
                    CtClass.doubleType,
                    CtClass.booleanType,
                    CtClass.floatType,
                    CtClass.booleanType,
                    CtClass.doubleType
            };
            String desc4 = Descriptor.ofMethod(CtClass.voidType, params4);
            ctSkill.getMethod("alterSkill", desc4).instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("hasSleepBonus")) {
                        m.replace("int timedAffinity = this.affinity + (com.wurmonline.server.skills.AffinitiesTimed.isTimedAffinity(pid, this.getNumber()) ? 4 : 0);"
                                + "advanceMultiplicator *= (double)(1.0f + (float)timedAffinity * 0.1f);"
                                + "$_ = $proceed($$);");
                        return;
                    }
                }
            });

            // - Double the rate at which charcoal piles produce items - //
            CtClass[] params5 = {
                    CtClass.booleanType,
                    CtClass.booleanType,
                    CtClass.longType
            };
            String desc5 = Descriptor.ofMethod(CtClass.booleanType, params5);
            ctItem.getMethod("poll", desc5).instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("createDaleItems")) {
                        m.replace("this.createDaleItems();"
                                + "decayed = this.setDamage(this.damage + 1.0f * this.getDamageModifier());"
                                + "$_ = $proceed($$);");
                        return;
                    }
                }
            });

            // - Allow lockpicking on PvP server, as well as treasure chests on PvE - //
            String actionDescriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[]
                    {classPool.get("com.wurmonline.server.behaviours.Action"), classPool.get("com.wurmonline.server.creatures.Creature"),
                            classPool.get("com.wurmonline.server.items.Item"), classPool.get("com.wurmonline.server.items.Item"), CtClass.shortType, CtClass.floatType});
            CtClass ctItemBehaviour = classPool.get("com.wurmonline.server.behaviours.ItemBehaviour");
            ctItemBehaviour.getMethod("action", actionDescriptor).instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("isInPvPZone")) {
                        m.replace("if(com.wurmonline.server.Servers.localServer.PVPSERVER){"
                                + "  $_ = true;"
                                + "}else{"
                                + "  $_ = target.getLastOwnerId() == -10 || target.getLastOwnerId() == 0 || target.getTemplateId() == 995;"
                                + "}");
                        return;
                    }
                }
            });
            CtClass ctMethodsItems = classPool.get("com.wurmonline.server.behaviours.MethodsItems");
            ctMethodsItems.getDeclaredMethod("picklock").instrument(new ExprEditor(){
                public void edit(MethodCall m) throws CannotCompileException {
                    if (m.getMethodName().equals("getLastOwnerId")) {
                        m.replace("$_ = $proceed($$);"
                                + "if($_ == -10 || $_ == 0){ ok = true; }");
                        return;
                    }
                }
            });
    //TODO Fix frozen class below
            /**
             * [11:36:45 PM] SEVERE org.gotti.wurmunlimited.serverlauncher.DelegatedLauncher: com.wurmonline.server.creatures.Creature class is frozen
             * java.lang.RuntimeException: com.wurmonline.server.creatures.Creature class is frozen
             * 	at javassist.CtClassType.checkModify(CtClassType.java:294)
             * 	at javassist.CtBehavior.insertBefore(CtBehavior.java:741)
             * 	at javassist.CtBehavior.insertBefore(CtBehavior.java:734)
             * 	at org.reqiuem.mods.gmchanges.MiscChanges.preInit(MiscChanges.java:284)
             * 	at org.reqiuem.mods.gmchanges.AllInOne.preInit(AllInOne.java:61)
             * 	at org.gotti.wurmunlimited.modloader.ModLoaderShared.lambda$loadModsFromModDir$6(ModLoaderShared.java:166)
             * 	at java.util.stream.ForEachOps$ForEachOp$OfRef.accept(ForEachOps.java:184)
             * 	at java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:175)
             * 	at java.util.ArrayList$ArrayListSpliterator.forEachRemaining(ArrayList.java:1374)
             * 	at java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:481)
             * 	at java.util.stream.AbstractPipeline.wrapAndCopyInto(AbstractPipeline.java:471)
             * 	at java.util.stream.ForEachOps$ForEachOp.evaluateSequential(ForEachOps.java:151)
             * 	at java.util.stream.ForEachOps$ForEachOp$OfRef.evaluateSequential(ForEachOps.java:174)
             * 	at java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:234)
             * 	at java.util.stream.ReferencePipeline.forEach(ReferencePipeline.java:418)
             * 	at org.gotti.wurmunlimited.modloader.ModLoaderShared.loadModsFromModDir(ModLoaderShared.java:164)
             * 	at org.gotti.wurmunlimited.serverlauncher.DelegatedLauncher.main(DelegatedLauncher.java:20)
             * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
             * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
             * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
             * 	at java.lang.reflect.Method.invoke(Method.java:498)
             * 	at javassist.Loader.run(Loader.java:288)
             * 	at org.gotti.wurmunlimited.serverlauncher.ServerLauncher.main(ServerLauncher.java:33)
             * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
             * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
             * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
             * 	at java.lang.reflect.Method.invoke(Method.java:498)
             * 	at PatchedLauncher.main(PatchedLauncher.java:20)
             */
            // - Effectively makes players with the set MGMT power or above able to pick
            //  up items beyond their body strength limit.
            //  Technically, this is done by overriding the canCarry(I)Z method,
            //  returning true whenever their MGMT power is high enough. -//
            CtClass ctClass = HookManager.getInstance().getClassPool().get("com.wurmonline.server.creatures.Creature");
            CtClass[] parameters = new CtClass[]{CtPrimitiveType.intType};
            CtMethod method = ctClass.getMethod("canCarry", Descriptor.ofMethod(CtPrimitiveType.booleanType, parameters));
            method.insertBefore("{ if (this.getPower() >= " + 1 + ") return true; }");
            method = ctClass.getMethod("getCarryCapacityFor", Descriptor.ofMethod(CtPrimitiveType.intType, parameters));
            method.insertBefore("{ if (this.getPower() >= " + 1 + ") return 0x7FFFFFFF; }");
            method = ctClass.getMethod("getCarryingCapacityLeft", Descriptor.ofMethod(CtPrimitiveType.intType, new CtClass[0]));
            method.insertBefore("{ if (this.getPower() >= " + 1 + ") return 0x7FFFFFFF; }");

             // -
             // Makes characters with a set MGMT power or above to be not slowed down
             // in movement speed by the weight they're carrying.
             //
             // Technically this is done by setting the thresholds for "move slow",
             // "encumbered" and "cantmove" to 2^31-1, or 0x7FFFFFFF, or to be precise
             // to exactly "about 2 billion grams", the maximum positive signed 32 bit
             // integer value.
             //
            CtClass[] parameters1 = new CtClass[0];
            CtMethod method1 = ctClass.getMethod("setMoveLimits", Descriptor.ofMethod(CtPrimitiveType.voidType, parameters1));
            method1.insertBefore("{ if (this.getPower() >= " + 1 + ") { this.moveslow = this.encumbered = this.cantmove = 0x7FFFFFFF; return; } }");
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
                    it.write(new byte[] { 0x00, 0x00 }, index);
                    it.insertAt(index, new byte[] { 0x11, 32767 >> 8, (byte)(32767 & 0xFF) } );
                    break;
                }
            }

        } catch (CannotCompileException | NotFoundException | IllegalArgumentException | ClassCastException e) {
            throw new HookException(e);
        } catch (BadBytecode badBytecode) {
            badBytecode.printStackTrace();
        }
    }
}
