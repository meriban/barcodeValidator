package com.meriban.barcodevalidator.managers;

import junit.framework.TestCase;

public class PropertiesManagerTest extends TestCase {

    public void testUpdateApplicationProperties() {
        PropertiesManager propertiesManager= PropertiesManager.getInstance();
        propertiesManager.updateApplicationProperty("regex", "bla");
        //propertiesManager.updateApplicationProperty("regex","bla");

    }

}