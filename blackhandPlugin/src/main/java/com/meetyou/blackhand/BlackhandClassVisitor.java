package com.meetyou.blackhand;


import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.RemappingAnnotationAdapter;
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
    public int mVersion;
    public int mAccess;
    public String mSignature;
    public String mName;
    public ArrayList<String[]> mNeedDeletes = new ArrayList<>();
    public ArrayList<String[]> mPrintInfoItems = new ArrayList<>();
    public ArrayList<String> mPrintInfoList = new ArrayList<>();

    public BlackhandClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        mClazzName = name.replace("/",".");
        mName = name;
        mSuperName = superName.replace("/",".");
        mInterfaces = interfaces;
        mVersion = version;
        mAccess = access;
        mSignature = signature;
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        org.objectweb.asm.AnnotationVisitor annotationVisitor = cv.visitAnnotation(desc, visible);

        annotationVisitor = new AnnotationVisitor(Opcodes.ASM5, annotationVisitor){
            @Override
            public void visit(String name, Object value) {
                super.visit(name, value);
            }

            @Override
            public void visitEnum(String name, String desc, String value) {
                super.visitEnum(name, desc, value);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return super.visitAnnotation(name, desc);
            }
        };
        return annotationVisitor;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }


    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature,
                                     String[] exceptions) {
//        for(String[] con : mPrintInfoItems){
//            //返回类型
//            String return_type = con[1];
//
//            //方法名
//            String methodName = con[2];
//
//            //参数
//            String params = con[3];
//
//            if(name.equals(methodName) && desc.equals(params + return_type)){
//                //该方法需要被删除
//                mPrintInfoList.add(mClazzName);
//            }
//        }
        for(String[] con : mNeedDeletes){
            //返回类型
            String return_type = con[1];

            //方法名
            String methodName = con[2];

            //参数
            String params = con[3];

            if(name.equals(methodName) && desc.equals(params + return_type)){
                //该方法需要被删除
                return null;
            }

        }
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

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                for(String[] con : mPrintInfoItems){
                    //返回类型
                    String return_type = con[1];

                    //方法名
                    String methodName = con[2];

                    //参数
                    String params = con[3];

                    if(name.equals(methodName) && desc.equals(params + return_type)){
                        //该方法需要被删除
                        mPrintInfoList.add(mClazzName + ":" + owner + "," + methodName);
                    }
                }
            }
        };
        return methodVisitor;

    }
}
