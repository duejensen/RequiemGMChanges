package org.reqiuem.mods.gmchanges.utils;

import com.wurmonline.server.items.ItemTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemHelper {

    public static final Logger logger = Logger.getLogger("ItemHelper");

    public static Method _getItemTemplate = TweakApiPerms.getClassMeth(
        "com.wurmonline.server.items.CreationWindowMethods",
        "getItemTemplate",
        "int");

    public static ItemTemplate getItemTemplate(int id) {
        if ( _getItemTemplate == null ) {
            return null;
        }
        try {
            return (ItemTemplate) _getItemTemplate.invoke(null,id);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE,"getItemTemplate: " + e.toString());
            return null;
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE,"getItemTemplate: " + e.toString());
            return null;
        }
    }

    public static boolean makeMissionItem( int tempid ) {
        if ( _getItemTemplate == null ) {
            return false;
        }

        ItemTemplate tplat = getItemTemplate(tempid);
        return TweakApiPerms.setItemField(tplat, "missions", true);
    }
}
