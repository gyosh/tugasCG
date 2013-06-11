import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class TugasCG012 extends JFrame {
	// This application relies on the PointsPanel component

	private PointsPanel pane;
	private JLabel counterDisplay, areaDisplay, closestPairDisplay;
	private ActionListener listener;
	private JCheckBox convexHull, closestPair, voronoi;

	public static void main(String[] args) throws Exception {

		TugasCG012 app = new TugasCG012();

		app.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		app.setSize(750, 500);
		app.setVisible(true);
	}

	/*
	 * This constructor creates the GUI for this application.
	 */
	public TugasCG012() {
		super("Tugas Geometri Komputasional 03");
		// All content of a JFrame (except for the menubar) 
		// goes in the Frame's internal "content pane", 
		// not in the frame itself.

		Container contentPane = this.getContentPane();

		// Specify a layout manager for the content pane
		contentPane.setLayout(new BorderLayout());

		// Create a JLabel for displaying number of points inside the frame
		// and current area calculated from the convex hull.
		counterDisplay = new JLabel();
		areaDisplay = new JLabel();
		closestPairDisplay = new JLabel();
		
		// Create the main component, give it a border, and
		// a background color, and add it to the content pane
		pane = new PointsPanel(counterDisplay, areaDisplay, closestPairDisplay);
		pane.setBorder(new BevelBorder(BevelBorder.LOWERED));
		contentPane.add(pane, BorderLayout.CENTER);

		// Create a menubar and add it to this window.  
		JMenuBar menubar = new JMenuBar();  // Create a menubar
		this.setJMenuBar(menubar);  // Display it in the JFrame

		// Create menus and add to the menubar
		JMenu filemenu = new JMenu("File");
		menubar.add(filemenu);

		/*
		 * Create some Action objects for use in the menus and toolbars. An
		 * Action combines a menu title and/or icon with an ActionListener.
		 * These Action classes are defined as inner classes below.
		 */
		listener = new CheckBoxesListener();

		convexHull = new JCheckBox("Convex Hull");
		convexHull.addActionListener(listener);

		closestPair = new JCheckBox("Closest Pair");
		closestPair.addActionListener(listener);

		voronoi = new JCheckBox("Voronoi Diagram");
		voronoi.addActionListener(listener);

		Action clear = new ClearAction();
		Action quit = new QuitAction();
		Action findArea = new FindAreaAction();
		
		// Populate the menus using Action objects
		filemenu.add(quit);

		// Now create a toolbar, add actions to it, and add 
		// it to the top of the frame (where it appears 
		// underneath the menubar)
		JToolBar toolbar = new JToolBar();
		toolbar.add(clear);
		toolbar.add(findArea);

		JPanel checkbar = new JPanel();
		checkbar.setLayout(new GridLayout(1, 3));
		checkbar.add(convexHull);
		checkbar.add(closestPair);
		checkbar.add(voronoi);
	
		JPanel upperPane = new JPanel();
		upperPane.setLayout(new BorderLayout());

		upperPane.add(toolbar, BorderLayout.WEST);
		upperPane.add(checkbar, BorderLayout.EAST);

		contentPane.add(upperPane, BorderLayout.NORTH);
		
		// Create separated space for displaying points counter and area of convex hull
		JPanel lowerPanel = new JPanel();
		lowerPanel.setLayout(new BorderLayout());
		lowerPanel.add(counterDisplay, BorderLayout.NORTH);
		lowerPanel.add(areaDisplay, BorderLayout.CENTER);
		lowerPanel.add(closestPairDisplay, BorderLayout.SOUTH);
		contentPane.add(lowerPanel, BorderLayout.SOUTH);
	}

	/*
	 * This inner class defines the "clear" action
	 */
	class ClearAction extends AbstractAction {

		public ClearAction() {
			super("Clear");  // Specify the name of the action
		}

		public void actionPerformed(ActionEvent e) {
			pane.clear();
		}
	}

	/*
	 * This inner class defines the "quit" action
	 */
	class QuitAction extends AbstractAction {

		public QuitAction() {
			super("Quit");
		}

		public void actionPerformed(ActionEvent e) {
			// Use JOptionPane to confirm that the user 
			// really wants to quit
			int response = JOptionPane.showConfirmDialog(TugasCG012.this, "Benar mau quit?");
			if (response == JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		}
	}
	
	class FindAreaAction extends AbstractAction {

		public FindAreaAction() {
			super("Find Area");
		}

		public void actionPerformed(ActionEvent e) {
			pane.findArea();
		}
	}

	class CheckBoxesListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			pane.updateConvexHullFlag(convexHull.isSelected());
			pane.updateClosestPairFlag(closestPair.isSelected());
			pane.updateVoronoiFlag(voronoi.isSelected());
		}
	}
}
