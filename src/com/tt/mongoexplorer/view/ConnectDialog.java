package com.tt.mongoexplorer.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EmptyBorder;

import com.tt.mongoexplorer.callback.ConnectCallback;
import com.tt.mongoexplorer.domain.Host;

@SuppressWarnings("serial")
public class ConnectDialog extends JDialog implements ActionListener {
	
	private static final int DIALOG_WIDTH = 640;
	
	private static final int DIALOG_HEIGHT = 480;
	
	private JTextField ip;
	
	private JTextField port;
	
	private JTextField username;
	
	private JPasswordField password;
	
	private JTextField authenticationDatabase;
	
	private Set<ConnectCallback> callbacks = new HashSet<>();
	
	public ConnectDialog(JFrame parent) {
		super(parent);
		setTitle("Connect");
		
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		
		int x = parent.getX() + parent.getWidth() / 2 - DIALOG_WIDTH / 2;
		int y = parent.getY() + parent.getHeight() / 2 - DIALOG_HEIGHT / 2;
		setLocation(x, y);
		
		setModal(true);
	
		setLayout(new BorderLayout());
		
		JTabbedPane jtp = new JTabbedPane();
		jtp.setBorder(new EmptyBorder(15, 0, 0, 0));
		jtp.addTab("Host Info", createHostInfoPanel());
		jtp.addTab("Authentication", createAuthenticationPanel());
		
		add(jtp, BorderLayout.CENTER);
		add(createButtonsPanel(), BorderLayout.SOUTH);
	}
	
	public void addConnectCallback(ConnectCallback callback) {
		callbacks.add(callback);
	}
	
	public void removeConnectCallback(ConnectCallback callback) {
		callbacks.remove(callback);
	}
	
	private JPanel createHostInfoPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		
		JLabel ipLabel = new JLabel("Hostname or IP address");
		panel.add(ipLabel);
		
		ip = new JTextField("127.0.0.1", 24);
		panel.add(ip);
		
		JLabel portLabel = new JLabel("Port number");
		panel.add(portLabel);
		
		port = new JTextField("27017", 24);
		panel.add(port);
		
		SpringLayout sl = new SpringLayout();
		
		sl.putConstraint(SpringLayout.WEST, ipLabel, 10, SpringLayout.WEST, panel);
		sl.putConstraint(SpringLayout.NORTH, ipLabel, 10, SpringLayout.NORTH, panel);
		
		sl.putConstraint(SpringLayout.EAST, ip, -10, SpringLayout.EAST, panel);
		sl.putConstraint(SpringLayout.NORTH, ip, 10, SpringLayout.NORTH, panel);
		
		sl.putConstraint(SpringLayout.WEST, portLabel, 10, SpringLayout.WEST, panel);
		sl.putConstraint(SpringLayout.NORTH, portLabel, 10, SpringLayout.SOUTH, ip);
		
		sl.putConstraint(SpringLayout.EAST, port, -10, SpringLayout.EAST, panel);
		sl.putConstraint(SpringLayout.NORTH, port, 10, SpringLayout.SOUTH, ip);
		
		panel.setLayout(sl);
		
		return (panel);
	}
	
	private JPanel createAuthenticationPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		
		JLabel usernameLabel = new JLabel("Username");
		panel.add(usernameLabel);
		
		username = new JTextField("", 24);
		panel.add(username);
		
		JLabel passwordLabel = new JLabel("Password");
		panel.add(passwordLabel);
		
		password = new JPasswordField("", 24);
		panel.add(password);
		
		JLabel authenticationDatabaseLabel = new JLabel("Database");
		panel.add(authenticationDatabaseLabel);
		
		authenticationDatabase = new JTextField("", 24);
		panel.add(authenticationDatabase);
		
		SpringLayout sl = new SpringLayout();
		
		sl.putConstraint(SpringLayout.WEST, usernameLabel, 10, SpringLayout.WEST, panel);
		sl.putConstraint(SpringLayout.NORTH, usernameLabel, 10, SpringLayout.NORTH, panel);
		
		sl.putConstraint(SpringLayout.EAST, username, -10, SpringLayout.EAST, panel);
		sl.putConstraint(SpringLayout.NORTH, username, 10, SpringLayout.NORTH, panel);
		
		sl.putConstraint(SpringLayout.WEST, passwordLabel, 10, SpringLayout.WEST, panel);
		sl.putConstraint(SpringLayout.NORTH, passwordLabel, 10, SpringLayout.SOUTH, username);
		
		sl.putConstraint(SpringLayout.EAST, password, -10, SpringLayout.EAST, panel);
		sl.putConstraint(SpringLayout.NORTH, password, 10, SpringLayout.SOUTH, username);
		
		sl.putConstraint(SpringLayout.WEST, authenticationDatabaseLabel, 10, SpringLayout.WEST, panel);
		sl.putConstraint(SpringLayout.NORTH, authenticationDatabaseLabel, 10, SpringLayout.SOUTH, password);
		
		sl.putConstraint(SpringLayout.EAST, authenticationDatabase, -10, SpringLayout.EAST, panel);
		sl.putConstraint(SpringLayout.NORTH, authenticationDatabase, 10, SpringLayout.SOUTH, password);
		
		panel.setLayout(sl);
		
		return (panel);
	}
	
	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		panel.add(cancel);
		JButton ok = new JButton("Ok");
		ok.setActionCommand("ok");
		ok.addActionListener(this);
		panel.add(ok);
		return (panel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("cancel".equals(e.getActionCommand())) {
			dispose();
		}
		if ("ok".equals(e.getActionCommand())) {
			
			// validate port
			try {
				int i = Integer.parseInt(port.getText());
				if (i < 0 || i > 65535) {
					JOptionPane.showMessageDialog(this, "The port number must be in the range [0-65535]", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} catch (NumberFormatException nfe) {
				JOptionPane.showMessageDialog(this, "The port number must be in the range [0-65535]", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			Host host = new Host(ip.getText(), Integer.parseInt(port.getText()));
			host.setUsername(username.getText());
			host.setPassword(password.getPassword().toString());
			host.setAuthenticationDatabase(authenticationDatabase.getText());
			for (ConnectCallback callback : callbacks) {
				callback.onRequestConnect(host);
			}
			dispose();
		}
	}
	
}
