package com.meetyou.blackhand;

import java.util.List;
import org.objectweb.asm.Type;

/**
 * Created by Linhh on 17/6/15.
 */

public class BlackhandMethodInfo {
    public String mMethodName;
    public List<Type> mMethodParms;
    public BlackhandMethodInfo(String methodName ,List<Type> methodParms){
        mMethodName = methodName;
        mMethodParms = methodParms;
    }

    public String getMethodName(){
        return mMethodName;
    }

    public String getMethodParms(){
        String stype = "(";
        for(Type type : mMethodParms){
            stype = stype + type.toString();
        }
        stype = stype + ")";
        return stype;
    }

    @Override
    public String toString() {
        String stype = "";
        for(Type type : mMethodParms){
            stype = stype + type.toString() + "---";
        }
        return mMethodName + ":" + stype;
    }
}
