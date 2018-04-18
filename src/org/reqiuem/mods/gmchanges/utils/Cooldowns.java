// 
// Decompiled by Procyon v0.5.30
// 

package org.reqiuem.mods.gmchanges.utils;

import java.util.HashMap;

public class Cooldowns
{
    static HashMap<String, Long> lastUses;
    
    static {
        Cooldowns.lastUses = new HashMap<String, Long>();
    }
    
    public static boolean isOnCooldown(final String playerEffect, final long cooldown) {
        return Cooldowns.lastUses.containsKey(playerEffect) && System.currentTimeMillis() < Cooldowns.lastUses.get(playerEffect) + cooldown;
    }
    
    public static long getPreviousUse(final String playerEffect) {
        if (!Cooldowns.lastUses.containsKey(playerEffect)) {
            return 0L;
        }
        return Cooldowns.lastUses.get(playerEffect);
    }
    
    public static void setUsed(final String playerEffect) {
        Cooldowns.lastUses.put(playerEffect, System.currentTimeMillis());
    }
}
