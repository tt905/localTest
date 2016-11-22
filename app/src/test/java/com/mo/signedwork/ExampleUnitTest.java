package com.mo.signedwork;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test(){
        try {
            Class c1 = Class.forName("android.text.TextUtils");
            System.out.println(c1.getSimpleName());
           Method[] m1 = c1.getDeclaredMethods();
            for (Method m : m1) {
                System.out.println(m.getName());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}