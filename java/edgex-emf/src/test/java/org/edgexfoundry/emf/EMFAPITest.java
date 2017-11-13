package org.edgexfoundry.emf;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EMFAPITest {

    @Test
    public void getInstanceTest() {
        EMFAPI instance = EMFAPI.getInstance();
        assertNotNull(instance);
    }

    @Test
    public void initializeTest() {
        EMFAPI instance = EMFAPI.getInstance();
        assertEquals(EMFErrorCode.EMF_OK, instance.initialize());
    }

    @Test
    public void terminateTest() {
        EMFAPI instance = EMFAPI.getInstance();
        assertEquals(EMFErrorCode.EMF_OK, instance.initialize());
        assertEquals(EMFErrorCode.EMF_OK, instance.terminate());
    }

    @Test
    public void getContextTest() {
        EMFAPI instance = EMFAPI.getInstance();
        assertEquals(EMFErrorCode.EMF_OK, instance.initialize());
        assertNotNull(instance.getContext());
    }
}
