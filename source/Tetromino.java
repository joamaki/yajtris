/* -*- mode: java; c-basic-offset: 4; indent-tabs-mode: nil;  -*- */

import java.awt.*;
import java.util.*;

/**
 * Tetromino class presents a piece consisting from 4 squares.
 * A piece is selected randomly from 7 different shapes. The piece
 * can then be drawn on a board, moved and rotated. A piece can be "retired"
 * to place it on a board.
 */
class Tetromino {

      //{{{ Attributes

      /** Random number generator */
      private static Random rnd = new Random();

      /** Move directions for <code>move()</code>*/
      public static final int LEFT = 0, RIGHT = 1, DOWN = 2, UP = 3;

      /** Current tetromino coordinates */
      private int x = 0, y = -2;

      /** The board the tetromino is drawed on */
      private Board board;

      /** Color of the tetromino */
      private Color color;

      /** True if the piece can rotate */
      private boolean rotates = true;

      /** A 5x5 array that describes the piece */
      private int[][] squares;

      /** True if tetromino has been retired */
      private boolean retired = false;

      //}}}

      //{{{ Constructors

      /**
       * Constructs a new random tetromino, placing it on a board.
       *
       * @param board Board to place the tetromino on
       */
      public Tetromino (Board board) {
            this.board = board;

            switch (rnd.nextInt(6)) {
            case 0: // I-piece
                  color = Color.RED;
                  squares = new int[][] {{0,0,0,0,0}, {0,0,0,0,0}, {0,1,1,1,1}, {0,0,0,0,0}, {0,0,0,0,0}};
                  break;
            case 1: // J-piece
                  color = Color.YELLOW;
                  squares = new int[][] {{0,0,0,0,0}, {0,1,0,0,0}, {0,1,1,1,0}, {0,0,0,0,0}, {0,0,0,0,0}};
                  break;
            case 2: // L-piece
                  color = Color.MAGENTA;
                  squares = new int[][] {{0,0,0,0,0}, {0,0,0,1,0}, {0,1,1,1,0}, {0,0,0,0,0}, {0,0,0,0,0}};
                  break;
            case 3: // O-piece
                  color = Color.BLUE;
                  squares = new int[][] {{0,0,0,0,0}, {0,1,1,0,0}, {0,1,1,0,0}, {0,0,0,0,0}, {0,0,0,0,0}};
                  rotates = false;
                  break;
            case 4: // S-piece
                  color = Color.CYAN;
                  squares = new int[][] {{0,0,0,0,0}, {0,0,1,1,0}, {0,1,1,0,0}, {0,0,0,0,0}, {0,0,0,0,0}};
                  break;
            case 5: // T-piece
                  color = Color.GREEN;
                  squares = new int[][] {{0,0,0,0,0}, {0,0,1,0,0}, {0,1,1,1,0}, {0,0,0,0,0}, {0,0,0,0,0}};
                  break;
            case 6: // Z-piece
                  color = Color.ORANGE;
                  squares = new int[][] {{0,0,0,0,0}, {0,1,1,0,0}, {0,0,1,1,0}, {0,0,0,0,0}, {0,0,0,0,0}};
                  break;
            }

            this.x = (board.getWidth() / 2) - width()/2 - 1;
      }
      //}}}

      //{{{ Private methods

      /**
       * Returns the width of the tetromino
       *
       * @return Width of the tetromino
       */
      private int width () {
            int start=5;
            int end=-1;
            for (int row=0; row<5; row++) {
                  for (int col=0; col<5; col++) {
                        if (squares[row][col] == 1) {
                              if (col < start) start = col;
                              break;
                        }
                  }

                  for (int col=4; col>=0; col--) {
                        if (squares[row][col] == 1) {
                              if (col > end) end = col;
                              break;
                        }
                  }
            }
            return end - start + 1;
      }

      /** Checks with the board that we can move there */
      private boolean canMove(int x, int y) {
            for (int row=0; row<5; row++) {
                  for (int col=0; col<5; col++) {
                        if (squares[row][col] == 1) {
                              if (board.blockAvailable(x+col, y+row) == false) return false;
                        }
                  }
            }
            return true;
      }
      //}}}

      //{{{ Public methods

      /**
       * Sets the board on which the tetromino lives
       *
       * @param board The Board
       */
      public void setBoard (Board board) {
            this.board = board;
            this.x = (board.getWidth() / 2) - width()/2 - 1;
            this.y = -2;
      }

      /**
       * Draws the tetromino on a board
       */
      public void draw (Graphics2D g) {
            for (int row=0; row<5; row++) {
                  for (int col=0; col<5; col++) {
                        if (squares[row][col] == 1) {
                              board.drawSquare(g, x+col, y+row, color);
                        }
                  }
            }
      }

      /** Retires the piece, ie. moves it to squares on board */
      public void retire () {
            retired = true;
            for (int row=0; row<5; row++) {
                  for (int col=0; col<5; col++) {
                        if (squares[row][col] == 1) {
                              board.put(x+col, y+row, color);
                        }
                  }
            }
      }


      /** Moves block, returns true if successful. */
      public synchronized boolean move(int direction) {
            if (retired) return false;
            switch (direction) {
            case LEFT:
                  if (canMove(x-1, y)) {
                        x--;
                        return true;
                  } else return false;
            case RIGHT:
                  if (canMove(x+1, y)) {
                        x++;
                        return true;
                  } else return false;
            case DOWN:
                  if (canMove(x, y+1)) {
                        y+=1;
                        return true;
                  } else return false;
            case UP:
                  /** Just so we can bounced around the board :-) */
                  if (canMove(x, y-1)) {
                        y--;
                        return true;
                  } else return false;
            default:
                  assert false;
                  // NOT REACHED
                  return false;
            }
      }

      /** Rotates the block clockwise */
      public void rotate () {
            if (retired || !rotates) return;

            int[][] newSquares = new int[5][5];

            for (int row=0; row<squares.length; row++) {
                  for (int col=0; col<squares[row].length; col++) {
                        newSquares[(squares.length-1)-col][row] = squares[row][col];
                  }
            }

            // Check that rotation was legal
            for (int row=0; row<squares.length; row++) {
                  for (int col=0; col<squares[row].length; col++) {
                        if (newSquares[row][col] == 1) {
                              if (board.blockAvailable(x+col, y+row) == false) return;
                        }
                  }
            }

            squares = newSquares;
      }

      //}}}

}
