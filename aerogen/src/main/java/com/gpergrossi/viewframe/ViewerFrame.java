package com.gpergrossi.viewframe;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class ViewerFrame extends JFrame {

	private static final long serialVersionUID = -3995478660714859610L;

	private ViewerPane viewerPane;

	/**
	 * Create the frame.
	 */
	public ViewerFrame(View view) {
		setTitle("Viewer");
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		viewerPane = new ViewerPane(view);
		view.setViewerPane(viewerPane);
		
		viewerPane.setFocusable(true);
		setContentPane(viewerPane);
		pack();
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ViewerFrame.this.close();
			}
		});
		
		viewerPane.start();
	}

	public void close() {
		System.out.println("Closing");
		ViewerFrame.this.viewerPane.stop();
		ViewerFrame.this.dispose();
	}

}
