package com.meetyou.blackhand.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.meetyou.anna.ConfigurationDO
import com.meetyou.blackhand.BlackhandClassVisitor
import com.meetyou.blackhand.BlackhandMethodInfo
import com.meetyou.blackhand.BlackhandUtil
import com.meetyou.blackhand.ConfigurationDO
import com.meetyou.blackhand.plugin.MeetyouConfiguration
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AnnotationNode

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES

/**
 * Created by Linhh on 17/5/31.
 */

public class PluginImpl extends Transform implements Plugin<Project> ,Opcodes{

    private MeetyouConfiguration meetyouConfiguration = new MeetyouConfiguration("blackhand.pro");
    private ArrayList<ConfigurationDO> mInclude;
    private int clazzindex = 1;
    private ArrayList<String> mBuildDirs = new ArrayList<>();
    private String mInjectPkg = "com/meetyou/anna/inject/support";
    private String minjectClass = "AnnaProjectInject"
    private boolean mNeedInject = false;
    //metas
    private HashMap<String, ArrayList<String>> mMetas = new HashMap<>();

    void apply(Project project) {
        println '==============anna apply start=================='

        //处理配置文件
        meetyouConfiguration.process();
        meetyouConfiguration.print();


        def android = project.extensions.getByType(AppExtension);
        android.registerTransform(this)

        println '==============anna apply end=================='
    }


    @Override
    public String getName() {
        return "Blackhand";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
    }

    String processReturn(String typeS){
        if("boolean".equals(typeS)){
            return "Z";
        }
//        if("boolean".equals(typeS)){
//            return "B";
//        }
        if("char".equals(typeS)){
            return "C";
        }
        if("short".equals(typeS)){
            return "S";
        }
        if("int".equals(typeS)){
            return "I";
        }
        if("float".equals(typeS)){
            return "F";
        }
        if("double".equals(typeS)){
            return "D";
        }
        if("long".equals(typeS)){
            return "J";
        }
        if("void".equals(typeS)){
            return "V";
        }
        //object
        return "L" + typeS.replace(".","/") + ";";
    }

    int processAccess(String src){
        int access = 0;
        //修饰符,以下划线_来切割
        String[] accesses = src.split("_")
        //处理修饰符
        for(String a : accesses){
            if(a.trim().startsWith("public")){
                access = access + ACC_PUBLIC
            }else if(a.trim().startsWith("private")){
                access = access + ACC_PRIVATE
            }else if(a.trim().startsWith("protected")){
                access = access + ACC_PROTECTED
            }else if(a.trim().startsWith("static")){
                access = access + ACC_STATIC
            }else if(a.trim().startsWith("native")){
                access = access + ACC_NATIVE
            }else if(a.trim().startsWith("final")){
                access = access + ACC_FINAL
            }else if(a.trim().startsWith("abstract")){
                access = access + ACC_ABSTRACT
            }else if(a.trim().startsWith("synchronized")){
                access = access + ACC_SYNCHRONIZED
            }
        }
        return access;
    }

    boolean needInsert(BlackhandClassVisitor cv, String methodName, String params){
        if(cv.mMethods != null){
            for(BlackhandMethodInfo info : cv.mMethods){
                if(info.getMethodName().equals(methodName) && info.getMethodParms().equals(params)){
                    //只要方法名和参数匹配,即说明存在,直接跳过额外处理
                    return false;
                }
            }
        }
        return true;
    }

    void processMetas(BlackhandClassVisitor cv, ClassWriter classWriter) throws Exception{
        println cv.mClazzName + "---" + cv.mSuperName
        for(BlackhandMethodInfo info : cv.mMethods){
            println info.toString()
        }

        HashMap<String,ArrayList<ConfigurationDO>> configurations = meetyouConfiguration.getMap();
        for (Map.Entry<String,ArrayList<ConfigurationDO>> entry : configurations.entrySet()) {
            String key = entry.key;
            String[] key_words = key.trim().split(" ");
            if(key_words[0].startsWith("extends")){
                //继承处理
                String clazz = key_words[1];
                if(cv.mSuperName.equals(clazz)){
                    println "Blackhand working ----- > processing this clazz "
                    for(ConfigurationDO configurationDO : entry.value){

                        String[] con = configurationDO.strings
                        //处理修饰符
                        int access = processAccess(con[0])
                        //返回类型
                        String return_type = con[1]

                        //方法名
                        String methodName = con[2]

                        //参数
                        String params = con[3]

                        String return_v = null;

                        if(!return_type.trim().equals("V")){
                            //返回值,返回非void的时候
                            return_v = con[4]
                        }

                        //遍历当前类,查看是否已经存在该插入方法
                        boolean isNeedInsert = needInsert(cv, methodName, params);
                        //存在方法直接跳过处理
                        if(!isNeedInsert){
                            continue
                        }

                        int return_num = 0 ;
                        int param_num = 0;

                        //处理,暂时不支持抛出异常
                        MethodVisitor mw = classWriter.visitMethod(access,
                                methodName,
                                params + return_type,
                                null,
                                null);
                        mw.visitCode();
                        if(return_v != null){
                            return_num = 1;
                            if(return_v.trim().equals("null") || return_v.trim().equals("NULL")){
                                mw.visitInsn(Opcodes.ACONST_NULL);
                            }else{
                                mw.visitLdcInsn(return_v.trim())
                            }
                        }

                        BlackhandUtil.returnResult(mw, return_type);
                        //第一个数字代表返回值情况,第二个代表几个入参
                        mw.visitMaxs(0, 0);
                        mw.visitEnd()

                    }
                }
            }else if(key_words[0].startsWith("implements")){
                //实现处理
                String clazz = key_words[1];
                if(cv.mSuperName.equals(clazz)){
                    //处理

                }
            }else if(key_words[0].startsWith("class")){
                //源文件处理
                String clazz = key_words[1];
                if(cv.mSuperName.equals(clazz)){
                    //处理

                }
            }
        }
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println '==================anna transform start=================='

        //遍历inputs里的TransformInput
        inputs.each { TransformInput input ->
            //遍历input里边的DirectoryInput
            input.directoryInputs.each {
                DirectoryInput directoryInput ->
                    //是否是目录
                    if (directoryInput.file.isDirectory()) {
                        //遍历目录
                        directoryInput.file.eachFileRecurse {
                            File file ->
                                def filename = file.name;
                                def name = file.name
                                //这里进行我们的处理 TODO
                                if (name.endsWith(".class")) {
                                    //类处理
                                    ClassReader classReader = new ClassReader(file.bytes)
                                    ClassWriter classWriter = new ClassWriter(classReader,ClassWriter.COMPUTE_MAXS)
                                    BlackhandClassVisitor cv = new BlackhandClassVisitor(Opcodes.ASM5,classWriter)
                                    classReader.accept(cv, EXPAND_FRAMES)

                                    //处理类数据
                                    processMetas(cv, classWriter);
                                    //写入
                                    byte[] code = classWriter.toByteArray()
                                    FileOutputStream fos = new FileOutputStream(
                                            file.parentFile.absolutePath + File.separator + name)
                                    fos.write(code)
                                    fos.close()
//                                    println 'Anna-----> inject file:' + file.getAbsolutePath()
                                }
//                                println 'Assassin-----> find file:' + file.getAbsolutePath()
                                //project.logger.
                        }
                    }
                    //处理完输入文件之后，要把输出给下一个任务
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
            }


            input.jarInputs.each { JarInput jarInput ->
                /**
                 * 重名名输出文件,因为可能同名,会覆盖
                 */
                def jarName = jarInput.name
//                println "Anna jarName:" + jarName + "; "+ jarInput.file.absolutePath
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {

                    jarName = jarName.substring(0, jarName.length() - 4)
                }

                File tmpFile = null;
                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                    JarFile jarFile = new JarFile(jarInput.file);
                    Enumeration enumeration = jarFile.entries();
                    tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_anna.jar");
                    //避免上次的缓存被重复插入
                    if(tmpFile.exists()) {
                        tmpFile.delete();
                    }
                    JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile));
                    //用于保存
                    ArrayList<String> processorList = new ArrayList<>();
                    while (enumeration.hasMoreElements()) {
                        JarEntry jarEntry = (JarEntry) enumeration.nextElement();
                        String entryName = jarEntry.getName();
                        ZipEntry zipEntry = new ZipEntry(entryName);

                        InputStream inputStream = jarFile.getInputStream(jarEntry);
                        //如果是inject文件就跳过
                        //anna插桩class
                        if (entryName.endsWith(".class")) {
                            //class文件处理
//                            println "entryName anna:" + entryName
                            jarOutputStream.putNextEntry(zipEntry);
                            ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                            ClassWriter classWriter = new ClassWriter(classReader,ClassWriter.COMPUTE_MAXS)
                            BlackhandClassVisitor cv = new BlackhandClassVisitor(Opcodes.ASM5,classWriter)
                            classReader.accept(cv, EXPAND_FRAMES)
                            //处理类数据
                            processMetas(cv, classWriter);

                            byte[] code = classWriter.toByteArray()
                            jarOutputStream.write(code);
                        } else if(entryName.contains("META-INF/services/javax.annotation.processing.Processor")){
                            if(!processorList.contains(entryName)){
//                                println "entryName no anna:" + entryName
                                processorList.add(entryName)
                                jarOutputStream.putNextEntry(zipEntry);
                                jarOutputStream.write(IOUtils.toByteArray(inputStream));
                            }else{
                                println "duplicate entry:" + entryName
                            }
                        }else {
//                            println "entryName no anna:" + entryName
                            jarOutputStream.putNextEntry(zipEntry);
                            jarOutputStream.write(IOUtils.toByteArray(inputStream));
                        }
                        jarOutputStream.closeEntry();
                    }
                    //结束
                    jarOutputStream.close();
                    jarFile.close();
//                    jarInput.file.delete();
//                    tmpFile.renameTo(jarInput.file);
                }
//                println 'Assassin-----> find Jar:' + jarInput.getFile().getAbsolutePath()

                //处理jar进行字节码注入处理 TODO

                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
                println 'Anna-----> copy to Jar:' + dest.absolutePath
                if(tmpFile == null) {
                    FileUtils.copyFile(jarInput.file, dest)
                }else{
                    FileUtils.copyFile(tmpFile, dest)
                    tmpFile.delete()
                }
            }
        }

        //创建meta数据
//        File meta_file = outputProvider.getContentLocation("anna_inject_metas", getOutputTypes(), getScopes(),
//                Format.JAR);
//        if(!meta_file.getParentFile().exists()){
//            meta_file.getParentFile().mkdirs();
//        }
//        if(meta_file.exists()){
//            meta_file.delete();
//        }

////        JarFile jarFile = new JarFile(meta_file);
//        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(meta_file));
//        ZipEntry addEntry = new ZipEntry("com/meetyou/anna/inject/support/AnnaInjectMetas.class");
//        jarOutputStream.putNextEntry(addEntry);
//        jarOutputStream.write(annaInjectWriter.makeMetas("com/meetyou/anna/inject/support/AnnaInjectMetas",mMetas));
//        jarOutputStream.closeEntry();
//        //结束
//        jarOutputStream.close();
////        jarFile.close();

        println '==================Anna transform end=================='

    }
}