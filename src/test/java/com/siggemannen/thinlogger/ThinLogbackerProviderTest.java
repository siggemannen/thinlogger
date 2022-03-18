package com.siggemannen.thinlogger;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test {@link ThinLogbackerProvider}
 */
public class ThinLogbackerProviderTest
{
    @Test
    public void test_provider()
    {
        Logger l = LoggerFactory.getLogger(ThinLogbackerProviderTest.class);
        l.info("Test");
        l.info("Test2");
        l.error("Error terror", new Exception("Error"));
        l.debug("Hi");
    }
}
