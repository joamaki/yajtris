/* -*- mode: java; c-basic-offset: 4; indent-tabs-mode: nil;  -*- */

/**
 * Game class is the center of YaJTRis. It handels keyboard events,
 * draws objects and manages the game.
 *
 * @author Jussi Mäki
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;

import java.util.concurrent.*;
import java.util.*;

public class Game extends Frame {

      //{{{ Attributes

      /** Drawing canvas */
      Panel canvas;

      /** Buffer image for double buffering */
      BufferedImage bImage;

      /** Graphics contexts for drawing */
      Graphics2D g, bg;

      /** Playing board size in squares */
      final int playBoardWidth = 10, playBoardHeight = 20;

      /** Preview board size in squares */
      final int preBoardWidth = 6;
      final int preBoardHeight = 4;

      /** Size of square in pixels */
      final int squareSize = 26;

      /** Width of the canvas */
      final int width = squareSize * (playBoardWidth+preBoardWidth);
      /** Height of the canvas */
      final int height = squareSize * (playBoardHeight);

      /** Current game score */
      int score = 0;
      /** Current game level */
      int level = 1;

      /** The count of lines cleared on this level */
      int nthLine = 0;

      /** The starting level time */
      double startTime = 500;
      /** The time in ms to wait to move a piece on this level */
      double levelTime = startTime;

      /** Boolean to indicate to not wait for <code>levelTime</code> milliseconds with the current piece */
      boolean skipSleep = false;

      /** Speed up per level in ms */
      int levelTimeDecrement = 15;
      /** Lines needed to advance to next level */
      int levelLines = 10;

      /** Playing board */
      Board board;
      /** Preview board */
      Board preBoard;

      /** High scores */
      HighScores hs;

      /** Current and next tetrominoes */
      Tetromino tetro, tetroNext;

      boolean running = false;
      boolean gameRunning = false;
      boolean paused = false;

      Thread gameThread;

      /** Index of the current character in high score name */
      int highScoreNameIndex = -1;
      /** Current high score name being entered */
      char[] highScoreName = new char[3];

      /** Fonts */
      Font largeFont  = new Font( "Times New Roman", Font.PLAIN, 13 ),
           fixedFont  = new Font( "Monospace"    , Font.PLAIN, 12 ),
            smallFont  = new Font( "Monospace", Font.PLAIN, 10);

      /** Queue for keyboard events */
      BlockingQueue <KeyEvent> keyEvents;

      //}}}

      //{{{ Constructors

      /**
       * Constructs the Game class.
       */
      public Game() {
            super("YaJTris");
            final Game game = this;
            gameThread = Thread.currentThread();

            setResizable(false);
            setVisible(true);

            // Add window listener to handle window closing
            addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent evt) {
                              // Exit the application
                              System.exit(0);
                        }
                  });

            // Create canvas and add it to the frame
            canvas = new Panel();
            canvas.setBackground(Color.black);
            canvas.setPreferredSize(new Dimension(width, height));
            add(canvas); pack();

            // Queue and keylistener for keyboard events
            keyEvents = new ArrayBlockingQueue<KeyEvent>(10);
            canvas.addKeyListener (new KeyAdapter() {
                        public void keyPressed(KeyEvent e) {
                              try {
                                    // Append event to key event queue
                                    keyEvents.put(e);
                              } catch (InterruptedException ex) {
                                    System.out.println("kbdevent queue full");

                                    // Queue was probably full, but this is quite unlikely (since you would
                                    // have to be pushing the keys real fast to get it full) and
                                    // doesn't affect the game so we don't care
                              }
                        }
                  });

            // Add focuslistener for pausing the game
            canvas.addFocusListener(new FocusListener() {
                        public void focusGained(FocusEvent e) {
                              game.pause(false);
                        }

                        public void focusLost(FocusEvent e) {
                              game.pause(true);
                        }
                  });

            // Create image buffer for double buffering
            bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            g = (Graphics2D) canvas.getGraphics();
            bg = (Graphics2D) bImage.getGraphics();

            // Create game objects
            board = new Board (0, 0, playBoardWidth, playBoardHeight,
                               squareSize);
            preBoard = new Board (playBoardWidth*squareSize, squareSize*2,
                                  preBoardWidth, preBoardHeight, squareSize);
            hs = new HighScores("yajtris.dat", fixedFont, 10,
                                playBoardWidth*squareSize+20,
                                (preBoardHeight*2+2)*squareSize);

      }

      //}}}

      //{{{ Public methods

      /**
       * Toggles the game pause.
       *
       * @param doPause Set to <code>true</code> if to be paused.
       */
      public void pause (boolean doPause) {
            if (gameRunning) {
                  this.paused = doPause;
                  gameThread.interrupt();
            }
      }

      /**
       * Updates the screen
       */
      public void update() {
            if (gameRunning) {
                  updateGame();
                  /** Draw the buffer on to screen */
                  g.drawImage(bImage, 0, 0, null);
            } else if (highScoreNameIndex >= 0) {
                  g.clearRect(0, 0, width, height);


                  String str = "You made it " + hs.place(score) + ". in high scores! Enter your initials: ";
                  g.setFont(largeFont);
                  FontMetrics fm = g.getFontMetrics();

                  g.setColor(Color.white);
                  g.drawString(str, width/2 - fm.stringWidth(str)/2, playBoardHeight*squareSize/2);

                  g.drawChars(highScoreName, 0, highScoreNameIndex,
                              width/2 - fm.stringWidth("AAA"),
                              height/2 + fm.getHeight()*2) ;
            }
      }


      //}}}

      //{{{ Protected methods

      protected void pauseGame() {
            AlphaComposite ac;
            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.3f);
            g.setComposite(ac);

            g.clearRect(0, 0, width, height);
            g.drawImage(bImage, 0, 0, this);

            ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
            g.setComposite(ac);

            g.setColor(Color.white);
            while (paused) {
                  g.drawString("Paused.", width/2 - g.getFontMetrics().stringWidth("Paused.")/2, height/2);
                  pollEvent(100);

            }
      }

      /**
       * Game main loop
       */
      protected void run() {
            intro();

            running = true;
            while (running) {

                  // Clear events so that any prior unprocessed key press doesn't affect the new game
                  clearEvents();

                  tetro = new Tetromino(board);
                  tetroNext = new Tetromino(preBoard);

                  board.clear();

                  score = 0;
                  level = 0;
                  levelTime = startTime;
                  nthLine = 0;

                  gameRunning = true;
                  skipSleep = false;
                  update();

                  while (gameRunning) {
                        /** Move the current tetromino down */
                        if (!tetro.move(Tetromino.DOWN)) {
                              tetro.retire();
                              tetro = tetroNext;
                              tetro.setBoard(board);
                              tetroNext = new Tetromino(preBoard);

                              int lines = board.clearFullLines();
                              nthLine += lines;
                              if (nthLine >= levelLines) {
                                    level++;
                                    levelTime *= 0.95;
                                    nthLine = 0;
                              }

                              // This implements the nintendo tetris scoring
                              switch (lines) {
                              case 1: score += 40 * (level + 1); break;
                              case 2: score += 100 * (level + 1); break;
                              case 3: score += 300 * (level + 1); break;
                              case 4: score += 1200 * (level + 1); break;
                              }

                              if (!tetro.move(Tetromino.DOWN)) {
                                    // Game over! Draw the last piece and end the game
                                    tetro.draw(bg);
                                    gameRunning = false;
                              }
                        }

                        update();

                        int sleepTime = (int) levelTime;
                        long was = System.currentTimeMillis();
                        while(!skipSleep) {
                              pollEvent(sleepTime);
                              if (paused) {
                                    pauseGame();
                                    break;
                              } else {
                                    sleepTime = (int) (System.currentTimeMillis() - was);
                                    if (sleepTime < (int) levelTime) continue;
                                    else break;
                              }
                        }
                        skipSleep = false;
                  }

                  g.clearRect(0, 0, width, height);
                  bg.clearRect(0, 0, width, height);
                  g.drawImage(bImage, 0, 0, this);

                  if (hs.place(score) > 0) {
                        highScoreNameIndex = 0;
                        update();

                        // Wait until we have the name
                        while (highScoreNameIndex < 3) pollEvent(-1);

                        hs.addScore(new String(highScoreName), score);
                        highScoreNameIndex = -1;

                        try { Thread.sleep(1000); }
                        catch(InterruptedException e) { }

                        g.clearRect(0, 0, width, height);
                  }

                  g.setFont(largeFont);
                  g.setColor(Color.white);
                  String str = "Game over! Press any key for another or 'Esc' to quit...";
                  g.drawString(str, width/2 - g.getFontMetrics().stringWidth(str)/2, height/2);
                  clearEvents();
                  pollEvent(-1);
                  bg.clearRect(0, 0, getWidth(), getHeight());
            }

            hs.save();
            System.exit(0);
      }
      //}}}

      //{{{ Private methods

      /**
       * Intro "animation"
       */
      private void intro() {
            Board introBoard = new Board (0, 0, width/10, height/10, 10);

            bg.setFont(largeFont);
            bg.clearRect(0,0,width,height);

            String[] text = {"Press any key to start playing", "YaJTris", "A tetris clone by Jussi Mäki"};

            Tetromino[] t = new Tetromino[100];

            for (int i=0; i<t.length; i++)
                  t[i] = new Tetromino(introBoard);

            int[] dirx = new int[t.length];
            int[] diry = new int[t.length];

            for (int i=0; i<dirx.length; i++) {
                  dirx[i] = Tetromino.LEFT;
                  diry[i] = Tetromino.DOWN;
            }

            FontMetrics fm = bg.getFontMetrics();
            double startIndex = -1 * fm.getHeight() * text.length;
            double yindex = startIndex;
            double tdiry = 1.5;
            double xindex = 0;
            double tdirx = 1.5;

            Random rnd = new Random();
            while (true) {
                  long now = System.currentTimeMillis();

                  /** Clear buffer image */
                  bg.setColor(Color.black);
                  bg.fillRect(0, 0, width, height);

                  for (int i=0; i<t.length; i++) {
                        t[i].draw(bg);
                        if (rnd.nextInt(5) % 5 == 1) t[i].rotate();

                        if (rnd.nextInt(2) % 2 == 1) {
                              if (!t[i].move(dirx[i])) {
                                    t[i].rotate();
                                    if (dirx[i] == Tetromino.LEFT) dirx[i] = Tetromino.RIGHT;
                                    else dirx[i] = Tetromino.LEFT;
                              }
                        } else {
                              if (!t[i].move(diry[i])) {
                                    t[i].rotate();
                                    if (diry[i] == Tetromino.DOWN) diry[i] = Tetromino.UP;
                                    else diry[i] = Tetromino.DOWN;
                              }
                        }
                  }

                  yindex += tdiry;
                  xindex += tdirx;
                  if (yindex > height - (fm.getHeight() * 5) || yindex < startIndex) {
                        startIndex = fm.getHeight();
                        tdiry = tdiry * -1;
                  }
                  if (xindex > (width-fm.stringWidth(text[0])) || xindex < 0) {
                        tdirx = tdirx * -1;
                  }

                  bg.setColor(Color.white);
                  for (int i=0; i<text.length; i++) {
                        bg.drawString(text[i],
                                      (int)xindex + (fm.stringWidth(text[0])/2) - (fm.stringWidth(text[i])/2),
                                      (int)yindex + (fm.getHeight() * 2 * i));
                  }

                  /** Draw the buffer on to the screen */
                  g.drawImage(bImage, 0, 0, null);

                  if (keyEvents.peek() != null) break;
                  try {
                        long sleepTime = 20 - (System.currentTimeMillis() - now);
                        if (sleepTime > 0) Thread.sleep(sleepTime);
                  } catch(InterruptedException e) {break;}
            }
      }

      /**
       * Clears events in the key event queue
       */
      private void clearEvents() {
            while (!keyEvents.isEmpty()) keyEvents.remove();
      }

      /**
       * Polls for an event for <code>timeout</code> milliseconds. When
       * event arrives this method calls <code>keyPressed()</code> to handle
       * the event.
       *
       * @param timeout Timeout in milliseconds. -1 for infinite timeout.
       */
      private void pollEvent(long timeout) {
            KeyEvent e;

            try {
                  if (timeout < 0) {
                        e = keyEvents.take();
                  } else {
                        e = keyEvents.poll(timeout, TimeUnit.MILLISECONDS);
                  }
                  if (e != null) {
                        keyPressed(e);
                  }
            } catch (InterruptedException ex) {
                  System.out.println("poll interrupted: " + ex);
            }
      }

      /**
       * Processes all the events in the key event queue
       */
      private void processEvents() {
            while (!keyEvents.isEmpty()) {
                  KeyEvent e = keyEvents.remove();
                  keyPressed(e);
                  // todo: make keyPressed signal whether or not we should sleep
                  //       the full time after the keypress...
            }
      }
      /**
       * Handels key presses.
       *
       * @param e AWT key event
       */
      private void keyPressed (KeyEvent e) {
            int keyCode = e.getKeyCode();

            if (!running) {
                  return;
            }

            if (highScoreNameIndex >= 0) {
                  // Retrieving high score name
                  if (keyCode == KeyEvent.VK_BACK_SPACE) {
                        if (highScoreNameIndex > 0) highScoreNameIndex--;
                        update();
                  } else {
                        char c = e.getKeyChar();
                        if (Character.isLetter(c)) {
                              highScoreName[highScoreNameIndex++] = Character.toUpperCase(c);
                              update();
                        }
                  }
                  if (highScoreNameIndex >= 3) {
                        update();
                  }
                  return;
            }

            if (keyCode == KeyEvent.VK_ESCAPE) {
                  if (!gameRunning) running = false;

                  else gameRunning = false;
                  return;
            }

            if (gameRunning) {
                  if (keyCode == KeyEvent.VK_UP)
                        tetro.rotate();
                  else if (keyCode == KeyEvent.VK_LEFT)
                        tetro.move(Tetromino.LEFT);
                  else if (keyCode == KeyEvent.VK_RIGHT)
                        tetro.move(Tetromino.RIGHT);
                  else if (keyCode == KeyEvent.VK_DOWN) {
                        tetro.move(Tetromino.DOWN);
                        tetro.move(Tetromino.DOWN);
                  } else if (keyCode == KeyEvent.VK_SPACE) {
                        while (tetro.move(Tetromino.DOWN)) ;
                        // Skip the sleep for this piece so that full lines get processed immediately
                        skipSleep = true;
                        tetro.retire();
                  }
                  update();
            }
      }

      /**
       * Redraws game objects
       */
      private void updateGame() {

            // Clear screen
            bg.clearRect(0, 0, width, height);
            bg.setFont(largeFont);

            FontMetrics fm = bg.getFontMetrics();
            int fh = fm.getHeight(); // font height


            // Draw board outlines
            bg.setColor(Color.white);
            bg.draw3DRect(0, 0, width, height, true);
            bg.drawLine(playBoardWidth*squareSize, 0, playBoardWidth*squareSize, playBoardHeight*squareSize);
            bg.drawLine(playBoardWidth*squareSize, preBoardHeight*squareSize,
                        playBoardWidth*squareSize + preBoardWidth*squareSize,
                        preBoardHeight*squareSize);
            bg.drawLine(playBoardWidth*squareSize, preBoardHeight*squareSize*2,
                        playBoardWidth*squareSize + preBoardWidth*squareSize,
                        preBoardHeight*squareSize*2);

            bg.drawLine(playBoardWidth*squareSize,
                        (preBoardHeight*2+1)*squareSize + 12*fh,
                        playBoardWidth*squareSize + preBoardWidth*squareSize,
                        (preBoardHeight*2+1)*squareSize + 12*fh);


            // Draw stats and scores
            bg.drawString("Score: " + score, playBoardWidth*squareSize + 35, preBoardHeight*squareSize + (preBoardHeight)*squareSize/2 - fm.getHeight());
            bg.drawString("Level: " + level, playBoardWidth*squareSize + 35, preBoardHeight*squareSize + (preBoardHeight)*squareSize/2 + fm.getHeight());

            bg.drawString("High score", playBoardWidth*squareSize + (preBoardWidth-1)*squareSize/2 - fm.stringWidth("High score")/2, (preBoardHeight*2+1)*squareSize);
            hs.draw(bg);

            bg.setFont(smallFont);
            bg.setColor(Color.gray);
            fm = bg.getFontMetrics();

            String[] instr = {"Instructions:", "LEFT and RIGHT to move", "UP to rotate", "SPACE to drop", "ESC to end game"};

            for (int i=0; i<instr.length; i++) {
                  bg.drawString(instr[i],
                                playBoardWidth*squareSize + 20,
                                (preBoardHeight*2+1)*squareSize + (13 * fh)+fh/2 + i * (fm.getHeight()+1) );
            }

            // Draw pieces
            board.draw(bg);
            tetro.draw(bg);
            tetroNext.draw(bg);
      }

      //}}}

      /**
       * Program entry point
       */
      public static void main(String[] args) {
            Game g = new Game();
            g.run();
      }
}
