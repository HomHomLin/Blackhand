package com.meetyou.blackhand;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Created by Linhh on 17/6/15.
 */

public class BlackhandUtil {
    public static void returnResult(MethodVisitor mv, String returnType){
        //判断是否有返回值，代码不同
        if("V".equals(returnType)){
            mv.visitInsn(Opcodes.RETURN);
        }else{
            //强制转化类型
            if(!castPrimateToObj(mv, returnType)){
                //这里需要注意，如果是数组类型的直接使用即可，如果非数组类型，就得去除前缀了,还有最终是没有结束符;
                //比如：Ljava/lang/String; ==》 java/lang/String
                String newTypeStr = null;
                int len = returnType.length();
                if(returnType.startsWith("[")){
                    newTypeStr = returnType.substring(0, len);
                }else{
                    newTypeStr = returnType.substring(1, len-1);
                }
                mv.visitTypeInsn(Opcodes.CHECKCAST, newTypeStr);
            }

            //这里还需要做返回类型不同返回指令也不同
            mv.visitInsn(getReturnTypeCode(returnType));
        }
    }

    private static boolean castPrimateToObj(MethodVisitor mv, String typeS){
        if("Z".equals(typeS)){
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");//强制转化类型
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
            return true;
        }
        if("B".equals(typeS)){
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte");//强制转化类型
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
            return true;
        }
        if("C".equals(typeS)){
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character");//强制转化类型
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "intValue", "()C");
            return true;
        }
        if("S".equals(typeS)){
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short");//强制转化类型
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
            return true;
        }
        if("I".equals(typeS)){
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer");//强制转化类型
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
            return true;
        }
        if("F".equals(typeS)){
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float");//强制转化类型
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
            return true;
        }
        if("D".equals(typeS)){
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double");//强制转化类型
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
            return true;
        }
        if("J".equals(typeS)){
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long");//强制转化类型
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
            return true;
        }
        return false;
    }

    private static int getReturnTypeCode(String typeS){
        if("Z".equals(typeS)){
            return Opcodes.IRETURN;
        }
        if("B".equals(typeS)){
            return Opcodes.IRETURN;
        }
        if("C".equals(typeS)){
            return Opcodes.IRETURN;
        }
        if("S".equals(typeS)){
            return Opcodes.IRETURN;
        }
        if("I".equals(typeS)){
            return Opcodes.IRETURN;
        }
        if("F".equals(typeS)){
            return Opcodes.FRETURN;
        }
        if("D".equals(typeS)){
            return Opcodes.DRETURN;
        }
        if("J".equals(typeS)){
            return Opcodes.LRETURN;
        }
        return Opcodes.ARETURN;
    }
}
