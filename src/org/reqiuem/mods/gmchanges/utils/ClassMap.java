package org.reqiuem.mods.gmchanges.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

class ClassMap {

    private HashMap<String,Class> basemap;
    private static final Logger logger = Logger.getLogger("ClassMap");

    ClassMap() {
        basemap = new HashMap<>();
        basemap.put("int",int.class);
        basemap.put("float",float.class);
    }

    Class getClass(String name) {

        Class retval = basemap.get(name);
        if ( retval != null ) {
            return retval;
        }

        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE,"ClassMap Couldn't Find: " + name);
        }

        return null;

    }

    Class[] getClassArray(String... names) {
        LinkedList<Class> clss = new LinkedList<>();

        for ( String name : names ) {
            Class pcls = getClass(name);
            if (pcls == null) {
                return null;
            }
            clss.add( pcls );
        }
        return clss.toArray(new Class[0]);
    }
}
