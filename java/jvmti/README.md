# Jar Package Encrypt and Decrypt
## JAR包反编译
   使用 [JD-GUI](https://github.com/java-decompiler/jd-gui/releases) 打开JAR包，就能获取JAR包的源码， 这样如果对于想保护源码的开发者或厂商来说是一件烦恼的事情；
   
## JAR包反编译保护
   方案大概有以下几种：  
   1. 使用 [ProGuard](https://www.guardsquare.com/en/proguard) 工具进行代码混淆， 但有点耐心的人还是能够分析出来的；【不做介绍】 
   2. 对class进行加密，再自定义ClassLoader进行解密，但自定义的ClassLoader本身就很容易成为突破口；【不做介绍】
   3. 对class进行加密，使用 JVM Tool Interface 对JAR包进行解密，这样不容易被轻易突破， 但也能够进行突破如果能遇上反编译大牛的话，能逆向动态库，看懂汇编那就另说了，当然还可以对so（dll）进行再次加壳（加密）操作，有兴趣的同学可以去Google下，推荐个关于安全的论坛[看雪](https://bbs.pediy.com/)； 

## JVM Tool Interface 介绍
JVM工具接口(JVM TI)是开发和监视工具使用的编程接口； 可以通用编写一个Agent进行对JVM进行一些监控，比如：监控函数入口、线程、堆栈等操作，当然也可以编写类加载HOOK对class进行解密操作。

文档： [《JVM Tool Interface 1.2》](https://docs.oracle.com/javase/8/docs/platform/jvmti/jvmti.html)

## JVM Tool Interface 使用
 参考IBM[《JVMTI 和 Agent实现》](https://www.ibm.com/developerworks/cn/java/j-lo-jpda2/index.html), 这边就不过多进行介绍。
 
### Encrypt
加密方法这边通过简单的位置互换和异或操作来达到目的， 当然也可以使用加密算法，对称加密或非对称加密；

>将JAR包中指定的类进行加密，生成一个新的JAR包
```java
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
```
> 加密算法采用CPP编写
```C
extern "C" JNIEXPORT jbyteArray JNICALL Java_com_tool_Encrypt_encrypt(
	JNIEnv * jni_env, 
	jobject  arg, 
	jbyteArray _buf){
	jbyte * jbuf = jni_env->GetByteArrayElements(_buf, 0);
	jsize length = jni_env->GetArrayLength( _buf);
	
	jbyte *dbuf = (jbyte *)malloc(length);
	int index = 0;
	for( ;index < length-1; ){
		dbuf[index]	 = jbuf[index+1] ^ 0x07;
		dbuf[index+1]= jbuf[index  ] ^ 0x08;
		index += 2;
	}
	if ( (0 == index  && 1 == length) || length-1 == index){	// size 1 || size 2(index-1) + 1
		dbuf[index] = jbuf[index] ^ 0x09;
	}
	jbyteArray dst = jni_env->NewByteArray(length);
	jni_env->SetByteArrayRegion(dst, 0, length, dbuf);
	free(dbuf);
	return dst;
} 
```
### Decrypt
解密利用 JVM TI来完成， 通过编写一个Agent，capability 设置 can_generate_all_class_hook_events 为 1， 再注册一个ClassFileLoadHook函数指针来进行回调，每当JVM进行类加载时，都会进行调用该方法；
```C++
void JvmTIAgent::AddCapability() const throw(AgentException)
{
    // 创建一个新的环境
    jvmtiCapabilities caps;
    memset(&caps, 0, sizeof(caps));
	// 可以生成 方法进入事件
    //caps.can_generate_method_entry_events = 1;
	// 可以对每个加载要类生成 ClassFileLoadHook 事件
    caps.can_generate_all_class_hook_events = 1;
	
    // 设置当前环境
    jvmtiError error = m_jvmti->AddCapabilities(&caps);
	CheckException(error);
}
  
void JvmTIAgent::RegisterEvent() const throw(AgentException)
{
    // 创建一个新的回调函数
    jvmtiEventCallbacks callbacks;
    memset(&callbacks, 0, sizeof(callbacks));
	// 设置方法进入函数指针
    //callbacks.MethodEntry = &JvmTIAgent::HandleMethodEntry;
	// 设置类加载方法函数指针
	callbacks.ClassFileLoadHook = &JvmTIAgent::HandleClassFileLoadHook;
    
    // 设置回调函数
    jvmtiError error;
    error = m_jvmti->SetEventCallbacks(&callbacks, static_cast<jint>(sizeof(callbacks)));
	CheckException(error);

	// 开启事件监听
	error = m_jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, 0);
	error = m_jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_CLASS_FILE_LOAD_HOOK, 0);
	CheckException(error);
}

void JNICALL JvmTIAgent::HandleClassFileLoadHook(
	jvmtiEnv *jvmti_env, 
	JNIEnv* jni_env, 
	jclass class_being_redefined, 
	jobject loader, 
	const char* name, 
	jobject protection_domain,
    jint class_data_len, 
	const unsigned char* class_data, 
	jint* new_class_data_len, 
	unsigned char** new_class_data)
{
	try{
		// class 文件长度赋值
		*new_class_data_len = class_data_len;
		jvmtiError error;
		// 申请 新的class 字符空间
		error = m_jvmti->Allocate(class_data_len, new_class_data);
		CheckException(error);
		unsigned char *pNewClass = *new_class_data;
		unsigned int selfPackageLen = strlen(g_SelfJavaPackageName);
		if (strlen(name) > selfPackageLen && 0 == strncmp(name, g_SelfJavaPackageName, selfPackageLen)){
			// 进行 class 解密 ：规则（奇数异或 0x07, 偶数 异或 0x08, 并换位， 最后一位奇数异或0x09）
			int index = 0;
			for(; index < class_data_len-1 ; ){
				*pNewClass++ = class_data[index+1] ^ 0x08;
				*pNewClass++ = class_data[index]   ^ 0x07;
				index += 2; 
			}
			
			if ( (0 == index  && 1 == class_data_len) ||  class_data_len-1 == index ){	// size 1 || size 2n + 1
				*pNewClass = class_data[index] ^ 0x09;
			}
			cout << "[JVMTI Agent] Decrypt class (" << name << ") finished !" <<endl;
		}else{
			memcpy(pNewClass, class_data, class_data_len);
		}
		
	}catch(AgentException& e) {
		cerr << "Error when enter HandleClassFileLoadHook: " << e.what() << " [" << e.ErrCode() << "]";
	}	 
}
```

##JVM TI Example
代码 [github]()

__注意__ jvm-tool-cpp 目录下的 makefile 中 **INCLUDE** 参数修改为本地的JDK头文件的所在的目录

> 编译
```bash
cd jvmti && make
```
[![](/image/build.png "编译")][build]

> 对JAR进行反编译
[![](/image/before-encrypt.png "JD-GUI 反编译效果")][before-encrypt]
[![](/image/before-encrypt-01.png "JD-GUI 反编译效果")][before-encrypt-01]


```bash
java -Djava.library.path=./ -cp jvm-ti.jar com.borey.JvmTITest
java -Djava.library.path=./ -cp jvm-ti.jar  com.tool.Encrypt jvm-ti.jar 
java -Djava.library.path=./ -cp jvm-ti-encrypted.jar com.borey.JvmTITest
java -Djava.library.path=./ -agentpath:./libagent.so=show -cp jvm-ti-encrypted.jar com.borey.JvmTITest
```
