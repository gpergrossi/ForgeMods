package dev.mortus.aerogen;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class ViewerFrame extends JFrame {

	private static final long serialVersionUID = -3995478660714859610L;
	private View view;
	private ViewerPane viewPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ViewerFrame frame = new ViewerFrame();
					frame.setVisible(true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ViewerFrame() {
		setTitle("Viewer");
		setSize(new Dimension(800, 800));
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		view = new View(10, 10, 100, 100);
		viewPane = new ViewerPane(view);
		view.pane = viewPane;
		
		viewPane.setFocusable(true);
		viewPane.setPreferredSize(new Dimension(1280, 720));
		add(viewPane);
		pack();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("Closing");
				ViewerFrame.this.viewPane.stop();
				ViewerFrame.this.dispose();
			}
		});
		
		viewPane.start();
	}

}
