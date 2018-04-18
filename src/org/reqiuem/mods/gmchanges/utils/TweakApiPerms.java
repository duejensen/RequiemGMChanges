package org.reqiuem.mods.gmchanges.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TweakApiPerms {

    public static final Logger logger = Logger.getLogger("TweakApiPerms");
    public static final ClassMap classmap = new ClassMap();

    public static Class getClass(String name) {
        return classmap.getClass(name);
    }

    public static boolean setItemField( Object item, String fieldName, Object valu ) {

        Field field = null;
        Class clas = item.getClass();

        try {
            field = clas.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            logger.log(Level.SEVERE,String.format("No Such Field: %s on %s", fieldName, clas.getName()) );
            return false;
        }

        field.setAccessible(true);
        try {
            field.set(item,valu);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE,"setItemField Illegal: " + fieldName);
            return false;
        }
        return true;

    }

    public static Method getClassMeth(String cname, String mname, String ... params) {

        Method meth = null;

        Class [] ptypes = classmap.getClassArray(params);
        if ( ptypes == null ) {
            return null;
        }

        Class clas = classmap.getClass(cname);
        if ( clas == null ) {
            return null;
        }

        try {
            meth = clas.getDeclaredMethod(mname, ptypes);
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE,"Method Not Found: " + mname);
            return null;
        }

        meth.setAccessible(true);
        return meth;
    }
}
