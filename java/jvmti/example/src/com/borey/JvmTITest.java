package com.borey;

/**
 * Created by Borey.Zhu on 2017/9/8.
 */

public class JvmTITest {

    public static void main(String[] args){

        StringBuilder options = new StringBuilder();
        for(String arg : args){
            options.append(arg + " ");
        }
        System.out.println("Applcation args: " +  options.toString());

        JvmTITest test = new JvmTITest();
        test.show();
    }

    public void show(){
        System.out.println("Application method show: JVM Tool Interface Test !!!");
    }

}
