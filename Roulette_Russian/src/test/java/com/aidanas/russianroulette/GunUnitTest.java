package com.aidanas.russianroulette;

import com.aidanas.russianroulette.game.Gun;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test class to test Gun class behavior.
 */
public class GunUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void gunEventuallyFires() throws Exception {

        boolean hasFired = false;
        for (int i = 0; i < 10; i++) {
            Gun g = new Gun(6);
            g.loadBullets(1);
            g.spinCylinder();
            if (g.pullTheTrigger()){
                hasFired = true;
                break;
            }
        }
        assertTrue(hasFired);
    }
}