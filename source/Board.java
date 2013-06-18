/* -*- mode: java; c-basic-offset: 4; indent-tabs-mode: nil;  -*- */

import java.awt.*;

/**
 * Board class presents a board of squares. It provides methods for
 * drawing squares, putting squares and clearing lines and the board.
 *
 * @author Jussi Mäki
 */

class Board {
      //{{{ Attributes

      /** Board coordinates */
      private int x, y;

      /** Board size in squares */
      private int width, height;

      /** Size of square in pixels */
      private int squareSize;

      /** Board of colors as an array of size <code>width</code> times <code>height</code> */
      private Color[][] board;

      //}}}

      //{{{ Constructors

      /**
       * Constructs a new board
       *
       * @param x Board x coordinate
       * @param y Board y coordinate
       * @param width Board width in squares
       * @param height Board height in squares
       */
      public Board (int x, int y, int width, int height, int squareSize) {
            this.x = x; this.y = y;
            this.width = width; this.height = height;
            this.squareSize = squareSize;
            board = new Color[width][height];
      }

      //}}}

      //{{{ Public methods

      /**
       * Draws a square on board
       *
       * @param g Java2D Graphics context
       * @param x Square x coordinate on board
       * @param y Square y coordinate on board
       * @param color Square color as java.awt.Color
       */
      public void drawSquare (Graphics2D g, int x, int y, Color color) {
            g.setColor(color);
            g.fill3DRect(getAbsoluteX(x)+3, getAbsoluteY(y)+3, squareSize-5, squareSize-5, false);
            g.setColor(Color.gray);
            g.draw3DRect(getAbsoluteX(x)+1, getAbsoluteY(y)+1, squareSize-2, squareSize-2, true);

      }

      /**
       * Returns true if specified block of board is available
       *
       * @param x Block x coordinate
       * @param y Block y coordinate
       * @return <code>true</true> if block available
       */
      public boolean blockAvailable (int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) return false;
            return (board[x][y] == null);
      }

      /**
       * Returns board width in squares
       * @return width in squares
       */
      public int getWidth () { return width; }

      /**
       * Returns board height in squares
       * @return height in squares
       */
      public int getHeight () { return height; }


      /**
       * Clears full lines on board. Optionally animates line clearing
       * if Game was supplied when constructing this Board.
       *
       * @return number of lines cleared
       */
      public int clearFullLines () {
            int lines = 0;
      next:
            for (int ty=0; ty<height; ty++) {
                  for (int tx=0; tx<width; tx++) {
                        if (board[tx][ty] == null)
                              continue next; // This line is not full, skip it.
                  }
                  // This line was full, shift the board downwards to clear it
                  for (int _tx=0; _tx<width; _tx++) {
                        for (int _ty=ty; _ty>0; _ty--) {
                              board[_tx][_ty] = board[_tx][_ty-1];
                        }
                  }
                  lines++;
            }
            return lines;
      }


      /**
       * Puts a square on board
       *
       * @param x Square x coordinate
       * @param y Square y coordinate
       * @param color Square color as java.awt.Color
       */
      public void put (int x, int y, Color color) {
            if (x >= 0 && x <= width &&
                y >= 0 && y <= height &&
                board[x][y] == null) {
                  board[x][y] = color;
            }
      }

      /**
       * Clears the board
       */
      public void clear() {
            for (int row=0; row<height; row++) {
                  for (int col=0; col<width; col++) {
                        board[col][row] = null;
                  }
            }
      }

      /**
       * Draws the board
       *
       * @param g Java2D Graphics context
       */
      public void draw (Graphics2D g) {
            for (int row=0; row<height; row++) {
                  for (int col=0; col<width; col++) {
                        if (board[col][row] != null) drawSquare(g, col, row, board[col][row]);
                  }
            }
      }

      //}}}

      //{{{ Private methods

      /**
       * Returns absolute X coordinate for a square
       *
       * @param xSquares X Coordinate of square on board
       * @return Absolute X coordinate for square
       */
      private int getAbsoluteX (int xSquares) {
            return this.x + xSquares*squareSize;
      }

      /**
       * Returns absolute Y coordinate for a square
       *
       * @param ySquares Y Coordinate of square on board
       * @return Absolute Y coordinate for square
       */
      private int getAbsoluteY (int ySquares) {
            return this.y + ySquares*squareSize;
      }

      //}}}

}
