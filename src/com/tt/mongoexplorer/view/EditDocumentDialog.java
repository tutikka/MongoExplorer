package com.tt.mongoexplorer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.utils.MongoUtils;
import com.tt.mongoexplorer.utils.UIUtils;

@SuppressWarnings("serial")
public class EditDocumentDialog extends JDialog implements ActionListener {

	private static final int DIALOG_WIDTH = 640;
	
	private static final int DIALOG_HEIGHT = 480;
	
	private JTextArea content;
	
	private JButton cancel;
	
	private JButton save;
	
	private Collection collection;
	
	private DBObject object;

    private ExecutorService es = Executors.newSingleThreadExecutor();

	public EditDocumentDialog(JFrame parent, Collection collection, DBObject object) {
		super(parent);
		this.collection = collection;
		this.object = object;
		setTitle("Edit Document");
		
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("cancel".equals(e.getActionCommand())) {
			dispose();
		}
		if ("save".equals(e.getActionCommand())) {
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					save.setEnabled(false);
					boolean result = handleEditObject();
					save.setEnabled(true);
					if (result) {
						dispose();
					}
				}
			};
			es.submit(runnable);
		}
	}
	
	// *********
	// private
	// *********
	
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 3));
		panel.add(new JLabel(collection.getDatabase().getHost().toString(), UIUtils.icon("resources/small/host.png"), SwingConstants.LEFT));
		panel.add(new JLabel(collection.getDatabase().toString(), UIUtils.icon("resources/small/database.png"), SwingConstants.CENTER));
		panel.add(new JLabel(collection.toString(), UIUtils.icon("resources/small/collection.png"), SwingConstants.RIGHT));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
		return (panel);
	}
	
	private JPanel createContentPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 1));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content = new JTextArea(UIUtils.prettyPrint(object));
		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		content.setBackground(Color.DARK_GRAY);
		content.setForeground(Color.LIGHT_GRAY);
		content.setCaretColor(Color.WHITE);
		content.setCaretPosition(0);
		JScrollPane jsp = new JScrollPane(content);
		jsp.setBackground(Color.DARK_GRAY);
		panel.add(jsp);
		return (panel);
	}
	
	private JPanel createButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		cancel = new JButton("Cancel");
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		panel.add(cancel);
		save = new JButton("Save");
		save.setActionCommand("save");
		save.addActionListener(this);
		panel.add(save);
		return (panel);
	}
	
	private boolean handleEditObject() {
		MongoClient client = null;
		try {
			client = MongoUtils.getMongoClient(collection.getDatabase().getHost());
			client.getDB(collection.getDatabase().getName()).getCollection(collection.getName()).save((DBObject) JSON.parse(content.getText())); 
			client.fsync(false);
			return (true);
		} catch (Exception e) {
			new ErrorDialog(this, e);
			return (false);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}
	
}
