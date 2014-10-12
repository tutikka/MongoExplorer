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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.mongodb.CommandResult;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.utils.MongoUtils;
import com.tt.mongoexplorer.utils.UIUtils;

@SuppressWarnings("serial")
public class InsertObjectDialog extends JDialog implements ActionListener {

	private static final int DIALOG_WIDTH = 640;
	
	private static final int DIALOG_HEIGHT = 480;
	
	private JTextArea content;
	
	private JButton cancel;
	
	private JButton ok;
	
	private Collection collection;
	
	public InsertObjectDialog(JFrame parent, Collection collection) {
		super(parent);
		this.collection = collection;
		setTitle("Insert Object");
		
		setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
		
		int x = parent.getX() + parent.getWidth() / 2 - DIALOG_WIDTH / 2;
		int y = parent.getY() + parent.getHeight() / 2 - DIALOG_HEIGHT / 2;
		setLocation(x, y);
		
		setModal(true);
	
		setLayout(new BorderLayout());
		
		add(createInfoPanel(), BorderLayout.NORTH);
		add(createContentPanel(), BorderLayout.CENTER);
		add(createButtonsPanel(), BorderLayout.SOUTH);
		
		setVisible(true);
	}
	
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 3));
		panel.add(new JLabel(collection.getDatabase().getHost().toString(), new ImageIcon("resources/small/host.png"), SwingConstants.LEFT));
		panel.add(new JLabel(collection.getDatabase().toString(), new ImageIcon("resources/small/database.png"), SwingConstants.CENTER));
		panel.add(new JLabel(collection.toString(), new ImageIcon("resources/small/collection.png"), SwingConstants.RIGHT));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		return (panel);
	}
	
	public JPanel createContentPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content = new JTextArea("{  }");
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.setBackground(Color.DARK_GRAY);
		content.setForeground(Color.LIGHT_GRAY);
		content.setCaretColor(Color.WHITE);
		content.setCaretPosition(2);
		panel.add(content);
		return (panel);
	}
	
	public JPanel createButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		panel.add(cancel);
		ok = new JButton("Ok");
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
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					handleInsertObject();
					dispose();
				}
			};
			SwingUtilities.invokeLater(runnable);
		}
	}
	
	private void handleInsertObject() {
		MongoClient client = null;
		try {
			client = MongoUtils.getMongoClient(collection.getDatabase().getHost());
			client.getDB(collection.getDatabase().getName()).getCollection(collection.getName()).insert((DBObject) JSON.parse(content.getText()));
			CommandResult result = client.fsync(false);
			System.out.println(result);
			UIUtils.info(this, "Object inserted");
		} catch (Exception e) {
			new ErrorDialog(this, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}
	
}
