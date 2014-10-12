package com.tt.mongoexplorer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class ErrorDialog extends JDialog implements ActionListener {

	private static final int DIALOG_WIDTH = 640;
	
	private static final int DIALOG_HEIGHT = 480;
	
	private JButton close;
	
	private Exception exception;
	
	public ErrorDialog(JDialog parent, Exception exception) {
		super(parent);
		this.exception = exception;
		setTitle("Oops!");
		
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		
		int x = parent.getX() + parent.getWidth() / 2 - DIALOG_WIDTH / 2;
		int y = parent.getY() + parent.getHeight() / 2 - DIALOG_HEIGHT / 2;
		setLocation(x, y);
		
		setModal(true);
	
		setLayout(new BorderLayout());
		
		add(createLabelPanel(), BorderLayout.NORTH);
		add(createContentPanel(), BorderLayout.CENTER);
		add(createButtonsPanel(), BorderLayout.SOUTH);
		
		setVisible(true);
	}
	
	public ErrorDialog(JFrame parent, Exception exception) {
		super(parent);
		this.exception = exception;
		setTitle("Oops!");
		
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		
		int x = parent.getX() + parent.getWidth() / 2 - DIALOG_WIDTH / 2;
		int y = parent.getY() + parent.getHeight() / 2 - DIALOG_HEIGHT / 2;
		setLocation(x, y);
		
		setModal(true);
	
		setLayout(new BorderLayout());
		
		add(createLabelPanel(), BorderLayout.NORTH);
		add(createContentPanel(), BorderLayout.CENTER);
		add(createButtonsPanel(), BorderLayout.SOUTH);
		
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("close".equals(e.getActionCommand())) {
			dispose();
		}
	}

	public JPanel createLabelPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));
		panel.add(new JLabel("Looks like something unexpected happened. You might find the reason below.", new ImageIcon("resources/large/oops.png"), SwingConstants.LEFT));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return (panel);
	}
	
	public JPanel createContentPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));
		
		StringBuilder sb = new StringBuilder();
		sb.append("EXCEPTION CLASS\n\n");
		sb.append(exception.getClass().getName());
		sb.append("\n\nMESSAGE\n\n");
		sb.append(exception.getMessage());
		sb.append("\n\nSTACKTRACE\n\n");
		for (StackTraceElement ste : exception.getStackTrace()) {
			sb.append(ste.getClassName());
			sb.append(" ");
			sb.append(ste.getMethodName());
			sb.append(" ");
			sb.append(ste.getLineNumber());
			sb.append("\n");
		}
		
		JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setForeground(Color.DARK_GRAY);
		area.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		area.setText(sb.toString());
		area.setCaretPosition(0);
		
		panel.add(new JScrollPane(area));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		return (panel);
	}
	
	public JPanel createButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		close = new JButton("Close");
		close.setActionCommand("close");
		close.addActionListener(this);
		panel.add(close);
		return (panel);
	}
	
}
