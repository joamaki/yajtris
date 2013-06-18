/* -*- mode: java; c-basic-offset: 4; indent-tabs-mode: nil;  -*- */

/**
 * HighScores provides saving, loading and drawing of high scores.
 *
 * @author Jussi Mäki
 */

import java.awt.*;
import java.io.*;
import java.util.Scanner;

class HighScores {

      //{{{ Attributes and inner classes

      private class HighScoreEntry {
            private String name;
            private int score;

            public HighScoreEntry (String name, int score) {
                  this.name = name;
                  this.score = score;
            }

            public String toString () {
                  return this.name + " " + this.score;
            }

            public String getName () {
                  return this.name;
            }

            public int getScore () {
                  return this.score;
            }

      }

      /** File to save and load high scores to/from */
      private File file;

      /** Coordinates for drawing scores */
      private int x, y;

      /** Number of scores stored */
      private int nScores;

      /** Font to draw scores with */
      private Font font;

      /** Table of high scores in memory */
      private HighScoreEntry scores[];

      //}}}

      //{{{ Constructors
      /**
       * Constructs new HighScores instance.
       *
       * @param filename Filename to save data to
       * @param nScores Number of scores to store
       * @param x X coordinate for drawing
       * @param y Y coordinate for drawing
       */

      public HighScores (String filename, Font font, int nScores, int x, int y) {
            if (filename == null || font == null)
                  throw new IllegalArgumentException();

            this.file = new File(filename);
            this.font = font;
            this.x = x;
            this.y = y;
            this.nScores = nScores;

            scores = new HighScoreEntry[nScores];

            if (file.canRead()) {
                  try {
                        Scanner s = new Scanner(file);

                        for(int i=0; i<nScores; i++) {
                              String name; int score;

                              if (!s.hasNext()) break;
                              name = s.next();
                              if (!s.hasNextInt()) break;
                              score = s.nextInt();

                              scores[i] = new HighScoreEntry (name, score);
                        }
                  } catch (FileNotFoundException e) {
                  }
            }

      }
      //}}}

      //{{{ Public methods

      /**
       * Returns a string representing the score data
       *
       * @return String containing score data
       */
      public String toString () {
            String out = "HighScores:\n";

            for (int i=0; i<nScores && scores[i] != null; i++) {
                  out += scores[i].toString() + "\n";
            }

            return out;
      }

      /**
       * Returns the place of the score on the high score table or -1 if not a high score.
       *
       * @param score Score to check
       * @return Place on high score table or -1 if not high score
       */
      public int place (int score) {
            for (int i=0; i<nScores; i++) {
                  if (scores[i] == null || scores[i].getScore() < score)
                        return i+1;
            }
            return -1;
      }

      /**
       * Adds a score to high score table
       *
       * @param name Name
       * @param score Score
       */
      public void addScore (String name, int score) {
            for (int i=0; i<nScores; i++) {
                  if (scores[i] == null || scores[i].getScore() < score) {
                        // Shift table down
                        for (int j=nScores-1; j>i; j--) {
                              scores[j] = scores[j-1];
                        }

                        scores[i] = new HighScoreEntry(name, score);

                        break;
                  }
            }
      }

      /**
       * Draws high score on graphics context at coordinates this.x, this.y
       *
       * @param g Graphics context
       */
      public void draw (Graphics2D g) {
            g.setColor(Color.blue);
            g.setFont(font);

            int ascent = g.getFontMetrics().getAscent()+4;
            for (int i=0; i<nScores; i++) {
                  if (scores[i] != null) {
                        g.drawString(i+1 + ". " + ( (((i+1)%10) == 0) ? "" : " " ) +
                                     scores[i].getName() + " - " +
                                     scores[i].getScore(), x, y+(i * ascent));
                  } else {
                        g.drawString(i+1 + ". " + ( (((i+1)%10) == 0) ? "" : " " ) +
                                     "??? - 0", x, y+(i * ascent));
                  }
            }
      }

      /**
       * Saves scores to file
       */
      public void save () {
            System.out.println("Writing out high scores...");

            try {
                  FileWriter fw = new FileWriter(file);
                  for (int i=0; i<nScores && scores[i] != null; i++) {
                        fw.write (scores[i].toString() + "\n");
                  }
                  fw.close();
            } catch (Exception e) {
                  System.out.println("Error occured while writing high scores: " + e);
            }

      }

      //}}}

}
