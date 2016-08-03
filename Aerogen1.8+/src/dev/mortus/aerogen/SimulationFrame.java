package dev.mortus.aerogen;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SimulationFrame extends JFrame {

	private static final long serialVersionUID = 8885425412131782229L;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch(ClassNotFoundException | InstantiationException
						| IllegalAccessException
						| UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}
				try {
					SimulationFrame frame = new SimulationFrame();
					frame.setVisible(true);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static int NUM_WORKER_THREADS = 5;
	public static int LOADING_TIME = 10;
	public static int UNLOADING_TIME = 10;
	
	/**
	 * Create the frame.
	 */
	public SimulationFrame() {
		setTitle("Settings");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 229, 166);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		final JSpinner spnrWorkerThreads = new JSpinner();
		spnrWorkerThreads.setModel(new SpinnerNumberModel(NUM_WORKER_THREADS, 0, 100, 1));
		spnrWorkerThreads.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				NUM_WORKER_THREADS = (Integer) spnrWorkerThreads.getValue();
			}
		});
		spnrWorkerThreads.setBounds(119, 11, 82, 20);
		contentPane.add(spnrWorkerThreads);
		
		JLabel lblWorkerThreads = new JLabel("Worker Threads:");
		lblWorkerThreads.setBounds(10, 14, 110, 14);
		contentPane.add(lblWorkerThreads);
		
		JLabel lblLoadingTime = new JLabel("Loading Time (ms):");
		lblLoadingTime.setBounds(10, 42, 110, 14);
		contentPane.add(lblLoadingTime);
		
		final JSpinner spnrLoadingTime = new JSpinner();
		spnrLoadingTime.setModel(new SpinnerNumberModel(LOADING_TIME, null, null, new Integer(1)));
		spnrLoadingTime.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				LOADING_TIME = (Integer) spnrLoadingTime.getValue();
			}
		});
		spnrLoadingTime.setBounds(119, 39, 82, 20);
		contentPane.add(spnrLoadingTime);
		
		JLabel lblUnloadingTime = new JLabel("Unloading Time (ms):");
		lblUnloadingTime.setBounds(10, 70, 110, 14);
		contentPane.add(lblUnloadingTime);
		
		final JSpinner spnrUnloadingTime = new JSpinner();
		spnrUnloadingTime.setModel(new SpinnerNumberModel(UNLOADING_TIME, null, null, new Integer(1)));
		spnrUnloadingTime.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				UNLOADING_TIME = (Integer) spnrUnloadingTime.getValue();
			}
		});
		spnrUnloadingTime.setBounds(119, 67, 82, 20);
		contentPane.add(spnrUnloadingTime);
		
		JButton btnSimulate = new JButton("Simulate");
		btnSimulate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new ViewerFrame().setVisible(true);
			}
		});
		btnSimulate.setBounds(63, 95, 89, 23);
		contentPane.add(btnSimulate);
	}
}
