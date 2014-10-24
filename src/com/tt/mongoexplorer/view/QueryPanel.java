package com.tt.mongoexplorer.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.utils.MongoUtils;
import com.tt.mongoexplorer.utils.UIUtils;

@SuppressWarnings("serial")
public class QueryPanel extends JPanel implements ActionListener, TreeSelectionListener {

	private MainFrame parent;
	
	private JTextField query;
	
	private JTextField start;
	
	private JTextField end;
	
	private JButton find;
	
	private JTree tree;
	
	private DefaultTreeModel treeModel;
	
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(new CustomNode("", ""));
	
	private CustomTreeCellRenderer customTreeCellRenderer = new CustomTreeCellRenderer();
	
	private JTextArea area;
	
	private JLabel host;
	
	private JLabel database;
	
	private JLabel collection;
	
	private JLabel time;
	
	private Collection selectedCollection;
	
	public QueryPanel(MainFrame parent, Collection collection) {
		this.parent = parent;
		this.selectedCollection = collection;
		
		setLayout(new BorderLayout());
		
		add(createQueryPanel(), BorderLayout.NORTH);
		add(createResultsPanel(), BorderLayout.CENTER);
		add(createInfoPanel(), BorderLayout.SOUTH);
		
		updateInfo();
	}
	
	public void openQueryWindow() {
		query.setText("{  }");
		query.setCaretPosition(2);
		query.requestFocus();
	}
	
	public void findAllDocuments() {
		query.setText("{  }");
		query.setCaretPosition(2);
		query.requestFocus();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				find.setEnabled(false);
				handleQuery();
				find.setEnabled(true);
			}
		};
		SwingUtilities.invokeLater(runnable);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("find".equals(e.getActionCommand())) {
			if (selectedCollection == null) {
				UIUtils.error(this, "Please select the collection");
				return;
			}
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					find.setEnabled(false);
					handleQuery();
					find.setEnabled(true);
				}
			};
			SwingUtilities.invokeLater(runnable);
		}
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (dmtn == null) {
			return;
		}
		Object object = dmtn.getUserObject();
		if (object == null) {
			return;
		}
		if (object instanceof CustomNode) {
			CustomNode customNode = (CustomNode) object;
			if (customNode.object instanceof DBObject) {
				area.setText(UIUtils.prettyPrint((DBObject) customNode.object));
			} else {
				area.setText(customNode.object.toString());
			}
			area.setCaretPosition(0);
		}
	}
	
	// *********
	// private
	// *********
	
	private JPanel createQueryPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		panel.add(new JLabel("Query:"), c);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0, 5, 0, 0);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1.0;
		query = new JTextField("");
		panel.add(query, c);
		c.insets = new Insets(0, 5, 0, 0);
		c.gridx = 3;
		c.gridy = 0;
		c.weightx = 0.0;
		start = new JTextField("0", 4);
		panel.add(start, c);
		c.insets = new Insets(0, 5, 0, 0);
		c.gridx = 4;
		c.gridy = 0;
		c.weightx = 0.0;
		panel.add(new JLabel("-"), c);
		c.insets = new Insets(0, 5, 0, 0);
		c.gridx = 5;
		c.gridy = 0;
		c.weightx = 0.0;
		end = new JTextField("99", 4);
		panel.add(end, c);
		c.insets = new Insets(0, 5, 0, 0);
		c.gridx = 6;
		c.gridy = 0;
		c.weightx = 0.0;
		find = new JButton("Find");
		find.setActionCommand("find");
		find.addActionListener(this);
		panel.add(find, c);
		panel.setBorder(new EmptyBorder(10, 10, 0, 10));
		return (panel);
	}
	
	private JSplitPane createResultsPanel() {
		
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setEditable(false);
		tree.setCellRenderer(customTreeCellRenderer);
		tree.addTreeSelectionListener(this);
		tree.setBackground(Color.DARK_GRAY);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if (dmtn == null) {
					return;
				}
				Object object = dmtn.getUserObject();
				if (object == null) {
					return;
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					createMenuForDocument().show(tree, e.getX(), e.getY());
				}
			}
		});
		
		area = new JTextArea();
		area.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JScrollPane jspTree = new JScrollPane(tree);
		jspTree.setBackground(Color.DARK_GRAY);
		JScrollPane jspArea = new JScrollPane(area);
		
		JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jspTree, jspArea);
		jsp.setOpaque(false);
		jsp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		jsp.setResizeWeight(0.9d);

		return (jsp);
	}
	
	private JPanel createInfoPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridLayout(1, 4));
		host = new JLabel("-", UIUtils.icon("resources/small/host.png"), SwingConstants.LEFT);
		panel.add(host);
		database = new JLabel("-", UIUtils.icon("resources/small/database.png"), SwingConstants.CENTER);
		panel.add(database);
		collection = new JLabel("-", UIUtils.icon("resources/small/collection.png"), SwingConstants.CENTER);
		panel.add(collection);
		time = new JLabel("-", UIUtils.icon("resources/small/time.png"), SwingConstants.RIGHT);
		panel.add(time);
		panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
		return (panel);
	}
	
	private JPopupMenu createMenuForDocument() {
		JPopupMenu menu = new JPopupMenu();
		menu.setInvoker(tree);
		JMenuItem editDocument = new JMenuItem("Edit document...");
		editDocument.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editDocument();
			}
		});
		menu.add(editDocument);
		JMenuItem deleteDocument = new JMenuItem("Delete document");
		deleteDocument.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteDocument();
			}
		});
		menu.add(deleteDocument);
		menu.setLightWeightPopupEnabled(true);
		menu.setOpaque(true);
		return (menu);
	}
	
	private void editDocument() {
		DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (dmtn == null) {
			return;
		}
		TreeNode[] path = dmtn.getPath();
		if (path == null) {
			return;
		}
		if (path.length > 0) {
			DefaultMutableTreeNode root = (DefaultMutableTreeNode) path[1];
			if (root == null) {
				return;
			}
			CustomNode customNode = (CustomNode) root.getUserObject();
			new EditDocumentDialog(parent, selectedCollection, (DBObject) customNode.object);
		}
	}
	
	private void deleteDocument() {
		int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete the document?", "Delete document", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, UIUtils.icon("resources/large/document.png"));
		if (option == JOptionPane.YES_OPTION) {
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (dmtn == null) {
				return;
			}
			TreeNode[] path = dmtn.getPath();
			if (path == null) {
				return;
			}
			if (path.length > 0) {
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) path[1];
				if (root == null) {
					return;
				}
				CustomNode customNode = (CustomNode) root.getUserObject();
				MongoClient client = null;
				try {
					client = MongoUtils.getMongoClient(selectedCollection.getDatabase().getHost());
					client.getDB(selectedCollection.getDatabase().getName()).getCollection(selectedCollection.getName()).remove((DBObject) customNode.object);
					client.fsync(false);
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							find.setEnabled(false);
							handleQuery();
							find.setEnabled(true);
						}
					};
					SwingUtilities.invokeLater(runnable);
				} catch (Exception e) {
					new ErrorDialog(parent, e);
				} finally {
					if (client != null) {
						client.close();
					}
				}
			}
		}
	}
	
	private void walk(DBObject dbo, DefaultMutableTreeNode parent) {
		for (String key : dbo.keySet()) {
			Object object = dbo.get(key);
			if (object instanceof DBObject) {
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new CustomNode(key, object));
				treeModel.insertNodeInto(dmtn, parent, parent.getChildCount());
				walk((DBObject) object, dmtn);
			} else {
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new CustomNode(key, object));
				parent.add(dmtn);
			}
		}
		
	}
	
	private void handleQuery() {
		// parse start range
		int start = 0;
		try {
			start = Integer.parseInt(this.start.getText());
			if (start < 0) {
				UIUtils.error(this, "Start range must be > 0");
				return;
			}
		} catch (Exception e) {
			UIUtils.error(this, "Unable to parse start range");
			return;
		}
		// parse end range
		int end = 99;
		try {
			end = Integer.parseInt(this.end.getText());
			if (end < 0) {
				UIUtils.error(this, "End range must be > 0");
				return;
			}
			if (end < start) {
				UIUtils.error(this, "End range must be >= start range");
				return;
			}
		} catch (Exception e) {
			UIUtils.error(this, "Unable to parse end range");
			return;
		}
		root.removeAllChildren();
		treeModel.nodeStructureChanged(root);
		area.setText("");
		MongoClient client = null;
		try {
			client = MongoUtils.getMongoClient(selectedCollection.getDatabase().getHost());
			long s = System.currentTimeMillis();
			DBCursor cursor = null;
			if (query.getText() != null && query.getText().length() > 0) {
				DBObject ref = (DBObject) JSON.parse(query.getText());
				cursor = client.getDB(selectedCollection.getDatabase().getName()).getCollection(selectedCollection.getName()).find(ref).skip(start).limit(end - start + 1);
			} else {
				cursor = client.getDB(selectedCollection.getDatabase().getName()).getCollection(selectedCollection.getName()).find().skip(start).limit(end - start + 1);
			}
			long e = System.currentTimeMillis();
			int count = 0;
			while (cursor.hasNext()) {
				DBObject dbo = cursor.next();
				String key = dbo.get("_id") == null ? "" : dbo.get("_id").toString();
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(new CustomNode(key, dbo));
				root.add(dmtn);
				walk(dbo, dmtn);
				count++;
			}
			treeModel.nodeStructureChanged(root);
			time.setText((e - s) + " ms | " + count + "/" + cursor.count() + " document(s)");
		} catch (Exception e) {
			new ErrorDialog(parent, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}
	
	private void updateInfo() {
		host.setText(selectedCollection.getDatabase().getHost().toString());
		database.setText(selectedCollection.getDatabase().toString());
		collection.setText(selectedCollection.toString());
	}
	
	private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
			Object userObject = dmtn.getUserObject();
			CustomNode customNode = (CustomNode) userObject;

			setIcon(null);
			setBackground(Color.DARK_GRAY);
			setBackgroundNonSelectionColor(Color.DARK_GRAY);
			setBackgroundSelectionColor(Color.LIGHT_GRAY);
			setBorderSelectionColor(Color.LIGHT_GRAY);
			
			if (customNode.object instanceof DBObject) {
				setText("<html><font color='gray'>{ </font><font color='orange'>" + customNode.key + " </font><font color='gray'> }</font></html>");
			} else {
				setText("<html><font color='white'>" + customNode.key + "</font> <font color='yellow'>" + customNode.object.toString() + "</font> <font color='gray'>" + customNode.object.getClass().getSimpleName() + "</font></html>");
			}
			
			return (this);
		}
		
	}
	
	private class CustomNode {
		
		public CustomNode(String key, Object object) {
			this.key = key;
			this.object = object;
		}
		
		private String key;
		
		private Object object;
		
	}

}
