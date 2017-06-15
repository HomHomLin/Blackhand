package com.meetyou.blackhand;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Linhh on 17/6/8.
 */

public class BlackhandClassVisitor extends ClassVisitor {

    public String mSuperName;
    public String mClazzName;
    public String[] mInterfaces;
    public ArrayList<BlackhandMethodInfo> mMethods;

    public BlackhandClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mClazzName = name.replace("/",".");
        mSuperName = superName.replace("/",".");
        mInterfaces = interfaces;

    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        methodVisitor = new AdviceAdapter(Opcodes.ASM5, methodVisitor, access, name, desc) {

            @Override
            public void visitCode() {
                super.visitCode();
                if(mMethods == null){
                    mMethods = new ArrayList<>();
                }

                List<Type> paramsTypeClass = new ArrayList();
                Type[] argsType = Type.getArgumentTypes(desc);
                for (Type type : argsType) {
                    paramsTypeClass.add(type);
                }
                BlackhandMethodInfo methodInfo = new BlackhandMethodInfo(name, paramsTypeClass);
                mMethods.add(methodInfo);
            }
        };
        return methodVisitor;

    }
}
