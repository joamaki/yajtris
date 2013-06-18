/* -*- mode: java; c-basic-offset: 4; indent-tabs-mode: nil;  -*- */

/**
 * Test2: HighScores unit test
 */

import java.awt.Font;
import java.io.*;
public class Test2 {


      public static void main (String[] args) {
            boolean failed = false;
            Font font = new Font("Monospace", Font.PLAIN, 10);

            // Delete the test file from any previous tests
            File f = new File("test2.dat");
            f.delete();

            // Test for illegal arguments
            try {
                  HighScores hs = new HighScores (null, null, 10, 0, 0);
                  System.out.println("Test2: Argument test 1 failed!");
                  failed = true;
            } catch (IllegalArgumentException e) {
            }

            try {
                  HighScores hs = new HighScores ("test2.dat", null, 10, 0, 0);
                  System.out.println("Test2: Argument test 2 failed!");
                  failed = true;
            } catch (IllegalArgumentException e) {
            }

            try {
                  HighScores hs = new HighScores ("test2.dat", font, 100, 0, 0);
                  System.out.println("Test2: Argument test 3 failed!");
                  failed = true;
            } catch (IllegalArgumentException e) {
            }

            HighScores hs = new HighScores ("test2.dat", font, 10, 0, 0);

            System.out.println("Before adding:");
            System.out.println(hs.toString());

            // Test adding of scores in different orders
            for (int i=0; i<20; i++) {
                  hs.addScore("DDD", i);
            }
            for (int i=20; i>0; i--) {
                  hs.addScore("BBB", i);
            }
            for (int i=5; i<10; i++) {
                  hs.addScore("CCC", i*2);
            }

            System.out.println("After adding:");
            String s1 = hs.toString();
            System.out.println(s1);
            hs.save();

            System.out.println("After saving and reloading:");
            hs = new HighScores ("test2.dat", font, 10, 0, 0);
            String s2 = hs.toString();
            System.out.println(s2);

            if (!s1.equals(s2)) {
                  System.out.println("Test failed: Scores differ after saving!");
                  failed = true;
            }

            if (failed) {
                  System.out.println("Test2: One or more tests failed!");
                  System.exit(1);
            } else {
                  System.out.println("Test2: passed");
                  System.exit(0);
            }

      }
}