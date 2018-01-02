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
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
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

    void apply(Project project) {
        println '==============blackhand apply start=================='

        //处理配置文件
        meetyouConfiguration.process();
        meetyouConfiguration.print();


        def android = project.extensions.getByType(AppExtension);
        android.registerTransform(this)

        project.afterEvaluate {
            project.android.applicationVariants.each { variant ->
                println variant.name
                //替换keep文件
                def collectTask = project.getTasks().findByName("merge${variant.name.capitalize()}Resources")//collectZroTestDebugMultiDexComponents
                if (collectTask != null) {
                    collectTask.doLast(new Action<Task>() {
                        @Override
                        void execute(Task task) {
                            println "collect${variant.name.capitalize()}MultiDexComponents action execute!mini main dex生效了!!!!" + project.getProjectDir()
                            def dir = new File(project.getProjectDir().absolutePath + "/build/intermediates/res/merged/${variant.dirName}/layout");
                            String[] s = dir.list();
                            for (String s1 : s) {
                                println "--->" + s1
                            }
                        }
                    })
                }
            }
        }

        println '==============blackhand apply end=================='
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

    void processMetas4Delete(BlackhandClassVisitor cv) throws Exception{
//        println cv.mClazzName + "---" + cv.mSuperName
//        for(BlackhandMethodInfo info : cv.mMethods){
//            println info.toString()
//        }

        HashMap<String,ArrayList<ConfigurationDO>> configurations = meetyouConfiguration.getMap();
        for (Map.Entry<String,ArrayList<ConfigurationDO>> entry : configurations.entrySet()) {
            String key = entry.key;
            String[] key_words = key.trim().split(" ");
            if(key_words[0].startsWith("delete")){
                if(key_words[1].startsWith("method")){
                    //删除方法
//                    println "Blackhand working ----- > processing this clazz "
                    for(ConfigurationDO configurationDO : entry.value){

                        String[] con = configurationDO.strings

                        //将解析的删除操作放入
                        cv.mNeedDeletes.add(con);

                    }
                }
            }
        }
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
        println cv.mClazzName + ":" + cv.mSuperName
//        for(BlackhandMethodInfo info : cv.mMethods){
//            println info.toString()
//        }

        HashMap<String,ArrayList<ConfigurationDO>> configurations = meetyouConfiguration.getMap();
        for (Map.Entry<String,ArrayList<ConfigurationDO>> entry : configurations.entrySet()) {
            String key = entry.key;
            String[] key_words = key.trim().split(" ");
            if(key_words[0].startsWith("extends")){
                //继承处理
                String clazz = key_words[1];
                if(cv.mSuperName.equals(clazz)){
                    processTheClazz(entry, cv, classWriter)
                }
            }else if(key_words[0].startsWith("implements")){
                //实现处理
                String clazz = key_words[1];
                for(String interface_item : cv.mInterfaces){
                    if(interface_item.replace("/",".").equals(clazz)){
                        //符合处理判断
                        processTheClazz(entry, cv, classWriter)
                    }
                }
            }else if(key_words[0].startsWith("class")){
                //源文件处理
                String clazz = key_words[1];
                if(cv.mClazzName.equals(clazz)){
                    //处理
                    processTheClazz(entry, cv, classWriter)
                }
            }else if(key_words[0].startsWith("replaceExtends")){
                //替换准则有几种
                if(key_words[1].startsWith("whichExtends")){
                    //替换继承
                    String clazz = key_words[2];
                    if(cv.mSuperName.equals(clazz)){
                        processTheClazzForChangeExtends(entry, cv, classWriter)
                    }
                }else if(key_words[1].startsWith("whichImplements")){
                    //替换实现接口
                    String clazz = key_words[2];
                    if(cv.mInterfaces != null){
                        for(String im : cv.mInterfaces){
                            if(im.replace("/",".").equals(clazz)){
                                processTheClazzForChangeExtends(entry, cv, classWriter)
                            }
                        }

                    }

                }
            }
        }
    }

    //修改class extends
    void processTheClazzForChangeExtends(Map.Entry<String,ArrayList<ConfigurationDO>> entry,BlackhandClassVisitor cv, ClassWriter classWriter) {
        println "Blackhand working processTheClazzForChangeExtends----- > processing this clazz "
        for(ConfigurationDO configurationDO : entry.value){
            String[] con = configurationDO.strings
            String superClazz = con[0].replace(".","/");
            println "Blackhand working ----- > processing"
            classWriter.visit(cv.mVersion,cv.mAccess,cv.mName,cv.mSignature,superClazz,cv.mInterfaces)
        }
    }

    void processTheClazz(Map.Entry<String,ArrayList<ConfigurationDO>> entry,BlackhandClassVisitor cv, ClassWriter classWriter){
        println "Blackhand working processTheClazz----- > processing this clazz "
        for(ConfigurationDO configurationDO : entry.value){

            println "Blackhand working ----- > processing"
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

            //处理,暂时不支持抛出异常
            MethodVisitor mw = classWriter.visitMethod(access,
                    methodName,
                    params + return_type,
                    null,
                    null);
            println "Blackhand working insert----- > " + methodName
            mw.visitCode();
            if(return_v != null){
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

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        println '==================blackhand transform start=================='

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
                                    processMetas4Delete(cv)
                                    classReader.accept(cv, EXPAND_FRAMES)

                                    //处理类数据
                                    println cv.mClazzName + ";" + cv.mSuperName
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
                            processMetas4Delete(cv)
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
                println 'Blackhand-----> copy to Jar:' + dest.absolutePath
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

        println '==================blackhand transform end=================='

    }
}