package com.hpe.junit.testreport.generator;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {

    @Test
    public void testApp() {
        assertTrue(true);
    }

    @Test
    public void testApp2() {
        assertTrue(true);
    }

    @Test
    public void testIngoredTest() {
        String obj = (String) new Object();
    }
}
