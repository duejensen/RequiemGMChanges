package org.reqiuem.mods.gmchanges;

import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.reqiuem.mods.gmchanges.actions.ActGmProtect;
import org.reqiuem.mods.gmchanges.actions.ActSpawnTowerGuard;
import org.reqiuem.mods.gmchanges.actions.LabyrinthAction;
import org.reqiuem.mods.gmchanges.cmds.*;
import org.reqiuem.mods.gmchanges.contrib.ArgumentTokenizer;
import org.reqiuem.mods.gmchanges.utils.CmdTool;
import org.reqiuem.mods.gmchanges.utils.ItemHelper;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AllInOne implements WurmServerMod, Configurable, PreInitable, Initable, ServerStartedListener, BehaviourProvider, PlayerMessageListener {
    private static final Logger _logger = Logger.getLogger(AllInOne.class.getName() + " v0.7");
    public static int commandPowerLevel = 5;
    public static boolean addGmProtect = true;
    public static boolean gmFullFavor = true;
    public static boolean gmFullStamina = true;
    public static boolean itemHolyBook = true;
    public static boolean itemTownScroll = true;
    public static boolean stfuNpcs = true;
    public static boolean hidePlayerGodInscriptions = true;
    boolean _noCarryWeightLimit = true;
    static int _noCarryWeightLimitPower = 1;
    boolean _notSlowedByWeight = true;
    static int _notSlowedByWeightPower = 1;
    static boolean _noDamageOnGamemasterOwnedItems = true;
    int _noDamageOnGamemasterOwnedItemsPower = 1;
    boolean _noFloorBuildingRequirements = true;
    boolean _noItemLimit = true;
    int _noItemLimitPower = 1;
    boolean _noDropItemLimit = true;
    public CmdTool cmdtool = null;
    private boolean useMoonMetalMiningMod = false;
    public boolean changeVeinCap = false;
    private boolean changeHomeVeinCap = false;
    public boolean randomMoonMetalDrops = false;
    static int staticRandomGlimmersteelChance = 3000;
    static int staticRandomAdamantiteChance = 3000;
    static int staticRandomSeryllChance = 3000;
    public static String actionMethodDesc = "(Lcom/wurmonline/server/behaviours/Action;Lcom/wurmonline/server/creatures/Creature;Lcom/wurmonline/server/items/Item;IIZIIISF)Z";
    public static String createGemMethodDesc = "(IIIILcom/wurmonline/server/creatures/Creature;DZLcom/wurmonline/server/behaviours/Action;)Lcom/wurmonline/server/items/Item;";

    @Override
    public void preInit() {
        _logger.log(Level.INFO,"preInit()");
        ModActions.init();
        BetterGamemasters.SpawnTowerGuards();
        //ModActions.init();
        if (_noCarryWeightLimit) BetterGamemasters.NoCarryWeightLimit();
        if (_notSlowedByWeight) BetterGamemasters.NotSlowedByWeight();
        if (_noDamageOnGamemasterOwnedItems) BetterGamemasters.NoDamageOnGamemasterOwnedItems();
        if (_noFloorBuildingRequirements) BetterGamemasters.NoFloorBuildingRequirements();
        if (_noDropItemLimit) BetterGamemasters.NoDropItemLimit();
        if(useMoonMetalMiningMod) {
            if (changeVeinCap) {
                MoonMetalMining.removeMoonMetalVeinCap();
            }
            if (randomMoonMetalDrops) {
                MoonMetalMining.addRandomMoonMetalDrop();
            }
            if (changeHomeVeinCap) {
                MoonMetalMining.changeHomeServerVeinCap();
            }
        }
        // TODO  need to be configurable
        fixFatigueActions();
     
        }
    
    static void fixFatigueActions()  {
    	try {
    		// - Remove fatiguing actions requiring you to be on the ground - //
    		ClassPool classPool = HookManager.getInstance().getClassPool();
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
    	} catch ( NotFoundException |  CannotCompileException e )   {
    		 _logger.log(Level.SEVERE, "Can't apply patch for isFatigue.", e);
        	
        }       
    }
    
    
    @Override
    public void init() {

        _logger.log(Level.INFO,"init()");

        HookManager hooks = HookManager.getInstance();

        hooks.getClassPool();

        try {

            if ( stfuNpcs ) {
                hooks.registerHook("com.wurmonline.server.creatures.ai.ChatManager",
                                   "answerLocalChat",
                                   "(Lcom/wurmonline/server/Message;Ljava/lang/String;)V",
                                    () -> (proxy, method, args) -> null);

                hooks.registerHook("com.wurmonline.server.creatures.ai.ChatManager",
                                   "getSayToCreature",
                                   "(Lcom/wurmonline/server/creatures/Creature;)Ljava/lang/String;",
                                    () -> (proxy, method, args) -> null);
            }

            if ( hidePlayerGodInscriptions ) {
                hooks.registerHook("com.wurmonline.server.deities.Deities",
                                   "getRandomNonHateDeity",
                                   "()Lcom/wurmonline/server/deities/Deity;",
                                   () -> (proxy, method, args) -> null);
            }

            if ( gmFullFavor ) {
                hooks.registerHook("com.wurmonline.server.players.Player",
                                   "depleteFavor",
                                   "(FZ)V",
                                   () -> (proxy, method, args) -> {
                    if (proxy instanceof Player) {
                        Player player = (Player) proxy;
                        if ( player.getPower() >= commandPowerLevel ) {
                            return null;
                        }
                    }
                    return method.invoke(proxy,args);
                });
            }

            hooks.registerHook("com.wurmonline.server.players.Player",
                               "increaseAffinity",
                               "(II)V",
                               () -> (proxy, method, args) -> {
                _logger.log(Level.SEVERE, String.format("incAff: %d %d", args[0], args[1]));
                return method.invoke(proxy,args);
            });

            /*
            if ( gmFullStamina ) {
                hooks.registerHook("com.wurmonline.server.creatures.CreatureStatus",
                                   "modifyStamina2",
                                   "(F)V",
                                   () -> (proxy, method, args) -> {

                    CreatureStatus status = (CreatureStatus) proxy;

                    if ( status.statusHolder.getPower() >= 5 ) {
                        args[0] = 100.0f;
                    }

                    return method.invoke(proxy,args);

                });
            }
            */

        } catch (Throwable e) {
            _logger.log(Level.SEVERE, "Error in init()", e);
        }
    }

    @Override
    public void configure(Properties props){
        try {
            commandPowerLevel = Integer.valueOf(props.getProperty("commandPowerLevel", Integer.toString(commandPowerLevel)));
            addGmProtect = Boolean.valueOf( props.getProperty("addGmProtect",Boolean.toString(addGmProtect)) );
            gmFullFavor = Boolean.valueOf( props.getProperty("gmFullFavor",Boolean.toString(gmFullFavor)) );
            gmFullStamina = Boolean.valueOf( props.getProperty("gmFullStamina",Boolean.toString(gmFullStamina)) );
            itemHolyBook = Boolean.valueOf( props.getProperty("itemHolyBook", Boolean.toString(itemHolyBook)) );
            itemTownScroll = Boolean.valueOf(props.getProperty("itemTownScroll", Boolean.toString(itemTownScroll)) );
            stfuNpcs = Boolean.valueOf( props.getProperty("stfuNpcs",Boolean.toString(stfuNpcs)) );
            hidePlayerGodInscriptions = Boolean.valueOf( props.getProperty("hidePlayerGodInscriptions",Boolean.toString(hidePlayerGodInscriptions)) );
            _noCarryWeightLimit = Boolean.valueOf(props.getProperty("noCarryWeightLimit", Boolean.toString(_noCarryWeightLimit)));
            _noCarryWeightLimitPower = Integer.valueOf(props.getProperty("noCarryWeightLimitPower", Integer.toString(_noCarryWeightLimitPower)));
            _notSlowedByWeight = Boolean.valueOf(props.getProperty("notSlowedByWeight", Boolean.toString(_notSlowedByWeight)));
            _notSlowedByWeightPower = Integer.valueOf(props.getProperty("notSlowedByWeightPower", Integer.toString(_notSlowedByWeightPower)));
            _noDamageOnGamemasterOwnedItems = Boolean.valueOf(props.getProperty("noDamageOnGamemasterOwnedItems", Boolean.toString(_noDamageOnGamemasterOwnedItems)));
            _noDamageOnGamemasterOwnedItemsPower = Integer.valueOf(props.getProperty("noDamageOnGamemasterOwnedItemsPower", Integer.toString(_noDamageOnGamemasterOwnedItemsPower)));
            _noFloorBuildingRequirements = Boolean.valueOf(props.getProperty("noFloorBuildingRequirements", Boolean.toString(_noFloorBuildingRequirements)));
            _noItemLimit = Boolean.valueOf(props.getProperty("noItemLimit", Boolean.toString(_noItemLimit)));
            _noItemLimitPower = Integer.valueOf(props.getProperty("noItemLimitPower", String.valueOf(_noItemLimitPower)));
            _noDropItemLimit = Boolean.valueOf(props.getProperty("noDropItemLimit", String.valueOf(_noDropItemLimit)));
            Log("No carry weight limit: ", _noCarryWeightLimit, _noCarryWeightLimitPower);
            Log("Not slowed by inventory weight: ", _notSlowedByWeight, _notSlowedByWeightPower);
            Log("No damage on GM owned items: ", _noDamageOnGamemasterOwnedItems, _noDamageOnGamemasterOwnedItemsPower);
            Log("No Floor building requirements: ", _noFloorBuildingRequirements, commandPowerLevel);
            Log("No item limit: ", _noItemLimit, _noItemLimitPower);
            useMoonMetalMiningMod = Boolean.valueOf(props.getProperty("useMoonMetalMiningMod", Boolean.toString(useMoonMetalMiningMod)));
            changeVeinCap = Boolean.valueOf(props.getProperty("changeVeinCap", Boolean.toString(changeVeinCap)));
            changeHomeVeinCap = Boolean.valueOf(props.getProperty("changeHomeVeinCap", Boolean.toString(changeHomeVeinCap)));
            MoonMetalMining.newVeinCap = Integer.valueOf(props.getProperty("newVeinCap", Integer.toString(MoonMetalMining.newVeinCap)));
            MoonMetalMining.newVeinCap = Math.max(1, MoonMetalMining.newVeinCap);
            MoonMetalMining.newHomeVeinCap = Integer.valueOf(props.getProperty("newHomeVeinCap", Integer.toString(MoonMetalMining.newHomeVeinCap)));
            MoonMetalMining.newHomeVeinCap = Math.max(1, Math.min(100, MoonMetalMining.newHomeVeinCap));
            randomMoonMetalDrops = Boolean.valueOf(props.getProperty("randomMoonMetalDrops", Boolean.toString(randomMoonMetalDrops)));
            staticRandomGlimmersteelChance = Integer.valueOf(props.getProperty("randomGlimmersteelDropChance", Integer.toString(staticRandomGlimmersteelChance)));
            staticRandomAdamantiteChance = Integer.valueOf(props.getProperty("randomAdamantiteDropChance", Integer.toString(staticRandomGlimmersteelChance)));
            staticRandomSeryllChance = Integer.valueOf(props.getProperty("randomSeryllDropChance", Integer.toString(staticRandomSeryllChance)));
        } catch (Throwable e) {
            _logger.log(Level.SEVERE, "Error in configure()", e);
        }
    }

    private void Log(String forFeature, boolean activated, int power) {
        /*
         * Logs as "Feature name: true for MGMT powers n and above",
         * or "Feature name: false".
         */
        _logger.log(Level.INFO, forFeature + activated +
                (activated
                        ? (" for MGMT powers " + power + " and above.")
                        : (".")));
    }

    @Override
    public boolean onPlayerMessage(Communicator communicator, String message) {

          final Player player = communicator.getPlayer();
          try {
        	  String[] argv = ArgumentTokenizer.tokenize( message ).toArray(new String[0]);
                if ( cmdtool.runWurmCmd( player, argv ) ) {
                    return true;
                }

            } catch (Throwable e) {
            	communicator.sendNormalServerMessage( String.format("Cmd Err (%s) %s", message, e.toString()) );
                return false;
            }

        return false;
    }

    @Override
    public void onServerStarted() {
        try {

            if (addGmProtect) ModActions.registerAction(new ActGmProtect());
            ModActions.registerAction(new LabyrinthAction());

            ModActions.registerAction(new ActSpawnTowerGuard());

            /* allow gifting coins as mission rewards */
            ItemHelper.makeMissionItem( ItemList.coinIron );
            ItemHelper.makeMissionItem( ItemList.coinCopper );
            ItemHelper.makeMissionItem( ItemList.coinSilver );
            ItemHelper.makeMissionItem( ItemList.coinGold );
            ItemHelper.makeMissionItem( ItemList.coinCopperFive );
            ItemHelper.makeMissionItem( ItemList.coinIronFive );
            ItemHelper.makeMissionItem( ItemList.coinSilverFive );
            ItemHelper.makeMissionItem( ItemList.coinGoldFive );
            ItemHelper.makeMissionItem( ItemList.coinCopperTwenty );
            ItemHelper.makeMissionItem( ItemList.coinIronTwenty );
            ItemHelper.makeMissionItem( ItemList.coinSilverTwenty );
            ItemHelper.makeMissionItem( ItemList.coinGoldTwenty );
            ItemHelper.makeMissionItem( ItemList.riftCrystal );
            ItemHelper.makeMissionItem( ItemList.shakerOrb );
            ItemHelper.makeMissionItem( ItemList.rodTransmutation );
            ItemHelper.makeMissionItem( ItemList.goldBar );
            ItemHelper.makeMissionItem( ItemList.silverBar );
            ItemHelper.makeMissionItem( ItemList.ironBar );
            ItemHelper.makeMissionItem( ItemList.copperBar );
            ItemHelper.makeMissionItem( ItemList.zincBar );
            ItemHelper.makeMissionItem( ItemList.leadBar );
            ItemHelper.makeMissionItem( ItemList.emerald );
            ItemHelper.makeMissionItem( ItemList.ruby );
            ItemHelper.makeMissionItem( ItemList.diamond );
            ItemHelper.makeMissionItem( ItemList.sapphire );
            ItemHelper.makeMissionItem( ItemList.opal );
            ItemHelper.makeMissionItem( ItemList.sapphireStar );
            ItemHelper.makeMissionItem( ItemList.diamondStar );
            ItemHelper.makeMissionItem( ItemList.emeraldStar );
            ItemHelper.makeMissionItem( ItemList.rubyStar );
            ItemHelper.makeMissionItem( ItemList.opalBlack );

            cmdtool = new CmdTool();
            cmdtool.addWurmCmd( new CmdGoTo() );
            cmdtool.addWurmCmd( new CmdWoot() );
            cmdtool.addWurmCmd( new CmdAddAff() );
            cmdtool.addWurmCmd( new CmdSendToHome());
            cmdtool.addWurmCmd( new CmdFillUp());
            cmdtool.addWurmCmd( new CmdMovePlayer());
            cmdtool.addWurmCmd( new CmdTraderReset() );

        } catch (Throwable e) {
            _logger.log(Level.SEVERE, "Error in onServerStarted()", e);
        }
    }

    public static boolean isTestEnv() {
        return Servers.localServer.getName().equals("Jubaroo");
    }

}


