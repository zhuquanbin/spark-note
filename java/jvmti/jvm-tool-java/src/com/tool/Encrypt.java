package com.tool;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Created by Borey.Zhu on 2017/9/8.
 */
public class Encrypt {

    native byte[] encrypt(byte[] _buf);

    static {
        System.loadLibrary("encrypt");
    }

    // 需要加密类包的前缀
    private static String encryptClassPrefix = "com/borey/";

    public static void main(String []args){

        int argc = args.length;
        if (0 == argc){
            System.out.println("usage:\n\t java -Djava.library.path=./ -cp jvm-ti.jar  com.tool.Encrypt  [*.jar]");
            System.exit(0);
        }
        for (String package_name : args) {
            String encryptedJar = encryptJarFile(package_name);
            System.out.println(String.format("Encrypt Jar Package: %s  -->  %s", package_name, encryptedJar));
        }
    }

    /**
     * @todo  加密jar包返回加密后的jar 包路径
     *
     * @param jarSrcName: Jar 包路径
     *
     * @return  encrypt jar 路径
     * */
    public static String encryptJarFile(String jarSrcName){
        String jarEncrypt_path = null;
        try{
            if (!jarSrcName.endsWith(".jar")){
                System.err.println(String.format("Error: %s is not a jar package!", jarSrcName));;
            }
            Encrypt encrypt = new Encrypt();

            // 生成新的 加密jar包
            jarEncrypt_path = jarSrcName.substring(0, jarSrcName.length() - 4) + "-encrypted.jar";
            JarOutputStream encryptJar = new JarOutputStream(new FileOutputStream(jarEncrypt_path));


            byte[] buf = new byte[2048];
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            // 读取 jar 包
            JarFile jf_src = new JarFile(jarSrcName);

            for (Enumeration<JarEntry> enumeration = jf_src.entries(); enumeration.hasMoreElements();) {
                JarEntry    entry = enumeration.nextElement();
                InputStream input = jf_src.getInputStream(entry);
                int len;

                while ( (len = input.read(buf, 0, 2048)) != -1){
                    output.write(buf, 0, len);
                }

                String className = entry.getName();
                byte[] classBytes = output.toByteArray();


                if (className.endsWith(".class") && className.startsWith(encryptClassPrefix)){
                    classBytes = encrypt.encrypt(classBytes);
                    System.out.println(String.format("Encrypted [package]:%s, [class]:%s ", jarSrcName, className));
                }else{
                    System.out.println(String.format("Appended  [package]:%s, [class]:%s ", jarSrcName, className));
                }

                // 将处理完后的class 写入新的 Jar 包中
                encryptJar.putNextEntry(new JarEntry(className));
                encryptJar.write(classBytes);
                output.reset();
            }

            encryptJar.close();
            jf_src.close();

        }catch (IOException e){
            e.printStackTrace();
        }

        return jarEncrypt_path;
    }
}
