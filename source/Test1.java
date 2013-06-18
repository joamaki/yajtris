
import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class Test1 extends JFrame {

      /** Drawing canvas and buffer image */
      JPanel canvas; 
      Image bImage;

      /** Canvas size parameters */
      final int playBoardWidth = 60;
      final int playBoardHeight = 60;

      final int squareSize = 8;      // Size of a square on board
      final int width = squareSize * (playBoardWidth);
      final int height = squareSize * (playBoardHeight);

      Board board;		// Play board

      boolean running = false;

      public Test1() {
	    super("YaJTris - Test1");

	    setResizable(false);

	    /** Create canvas and add it to the frame */
	    canvas = new JPanel();
	    canvas.setBackground(Color.black);
	    canvas.setPreferredSize(new Dimension(width, height));
	    getContentPane().add(canvas);
	    pack();

	    bImage = createImage(width, height);

	    setVisible(true);

	    board = new Board (0, 0, playBoardWidth, playBoardHeight, squareSize);

      }

      public void run() {
	    running = true;

	    Graphics2D g = (Graphics2D) canvas.getGraphics();
	    Graphics2D bg = (Graphics2D) bImage.getGraphics();

	    Tetromino[] t = new Tetromino[100];
	    
	    for (int i=0; i<t.length; i++)
		  t[i] = new Tetromino(board);

	    int[] dirx = new int[t.length];
	    int[] diry = new int[t.length];

	    for (int i=0; i<dirx.length; i++) {
		  dirx[i] = Tetromino.LEFT;
		  diry[i] = Tetromino.DOWN;
	    }

	    Random rnd = new Random();

	    System.out.println("Testing moving and rotating for 1000 iterations...");
	    for(int count=0; count<1000; count++) {
		  /** Clear buffer image */
		  bg.setColor(Color.black);
		  bg.fillRect(0, 0, width, height);

		  for (int i=0; i<t.length; i++) {
			t[i].draw(bg);		  
			if (rnd.nextInt(2) % 2 == 1) t[i].rotate();

			if (rnd.nextInt(3) % 3 == 1) {
			      if (!t[i].move(dirx[i])) {
				    if (dirx[i] == Tetromino.LEFT) dirx[i] = Tetromino.RIGHT;
				    else dirx[i] = Tetromino.LEFT;
			      }
			}
			if (rnd.nextInt(3) % 3 == 1) {			      
			      if (!t[i].move(diry[i])) {
				    if (diry[i] == Tetromino.DOWN) diry[i] = Tetromino.UP;
				    else diry[i] = Tetromino.DOWN;
			      }
			}
		  }

		  /** Draw the buffer on to the screen */
		  g.drawImage(bImage, 0, 0, null);
	    }


      }

      public static void main(String[] args) {
	    Test1 g = new Test1();
	    
	    try {
		  g.run();
	    } catch (Exception e) {
		  System.err.println("Test1: Test failed: Caught exception: " + e);
		  System.exit(1);
	    }

	    System.out.println("Test1: passed (but make sure it was rendering correctly)");
	    System.exit(0);
      }

}
