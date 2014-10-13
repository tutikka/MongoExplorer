package com.tt.mongoexplorer.view;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.tt.mongoexplorer.callback.ConnectCallback;
import com.tt.mongoexplorer.callback.NavigationCallback;
import com.tt.mongoexplorer.domain.Collection;
import com.tt.mongoexplorer.domain.Connections;
import com.tt.mongoexplorer.domain.Database;
import com.tt.mongoexplorer.domain.Host;
import com.tt.mongoexplorer.utils.MongoUtils;
import com.tt.mongoexplorer.utils.UIUtils;

@SuppressWarnings("serial")
public class NavigationPanel extends JPanel implements ConnectCallback, TreeSelectionListener {

	private MainFrame parent;
	
	private Connections connections = new Connections();
	
	private JTree tree;
	
	private DefaultTreeModel treeModel;
	
	private DefaultMutableTreeNode root = new DefaultMutableTreeNode(connections);
	
	private CustomTreeCellRenderer customTreeCellRenderer = new CustomTreeCellRenderer();
	
	private Set<NavigationCallback> callbacks = new HashSet<>();
	
	private Host selectedHost;
	
	private Database selectedDatabase;
	
	private Collection selectedCollection;
	
	public NavigationPanel(MainFrame parent) {
		this.parent = parent;
		
		setLayout(new GridLayout(1, 1));
		
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JScrollPane jsp = new JScrollPane(createTree());
		
		add(jsp);
		
	}
	
	public void addNavigationCallback(NavigationCallback callback) {
		callbacks.add(callback);
	}
	
	public void removeNavigationCallback(NavigationCallback callback) {
		callbacks.remove(callback);
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
		if (object instanceof Host) {
			selectedHost = (Host) object;
		}
		if (object instanceof Database) {
			selectedDatabase = (Database) object;
			selectedHost = selectedDatabase.getHost();
		}
		if (object instanceof Collection) {
			selectedCollection = (Collection) object;
			selectedDatabase = selectedCollection.getDatabase();
			selectedHost = selectedDatabase.getHost();
		}
	}
	
	@Override
	public void onRequestConnect(final Host host) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				MongoClient client = null;
				try {
					client = MongoUtils.getMongoClient(host);
					DefaultMutableTreeNode hostNode = new DefaultMutableTreeNode(host);
					for (String databaseName : client.getDatabaseNames()) {
						Database database = new Database(databaseName, host);
						DefaultMutableTreeNode databaseNode = new DefaultMutableTreeNode(database);
						hostNode.add(databaseNode);
						DB db = client.getDB(databaseName);
						for (String collectionName : db.getCollectionNames()) {
							Collection collection = new Collection(collectionName, database);
							DefaultMutableTreeNode collectionNode = new DefaultMutableTreeNode(collection);
							databaseNode.add(collectionNode);
						}
					}
					root.add(hostNode);
					treeModel.nodeStructureChanged(root);
					connections.getHosts().add(host);
				} catch (Exception e) {
					new ErrorDialog(parent, e);
				} finally {
					if (client != null) {
						client.close();
					}
				}
			}
		};
		SwingUtilities.invokeLater(runnable);
	}
	
	// *********
	// private
	// *********
	
	private JTree createTree() {
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);
		tree.setEditable(false);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRootVisible(true);
		tree.addTreeSelectionListener(this);
		tree.setCellRenderer(customTreeCellRenderer);
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
				if (object instanceof Collection && e.getClickCount() == 2 && !e.isConsumed()) {
					e.consume();
					for (NavigationCallback callback : callbacks) {
						callback.onFindAllDocumentsRequested(selectedCollection);
					}
				}
				if (object instanceof Connections && e.getClickCount() == 2 && !e.isConsumed()) {
					ConnectDialog connectDialog = new ConnectDialog(parent);
					connectDialog.addConnectCallback(NavigationPanel.this);
					connectDialog.setVisible(true);
				}
				if (SwingUtilities.isRightMouseButton(e)) {
					if (object instanceof Host) {
						createMenuForHost().show(tree, e.getX(), e.getY());
					}
					if (object instanceof Database) {
						createMenuForDatabase().show(tree, e.getX(), e.getY());
					}
					if (object instanceof Collection) {
						createMenuForCollection().show(tree, e.getX(), e.getY());
					}
				}
			}
		});
		return (tree);
	}
	
	private JPopupMenu createMenuForHost() {
		JPopupMenu menu = new JPopupMenu();
		menu.setInvoker(tree);
		JMenuItem createDatabase = new JMenuItem("Create database");
		createDatabase.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createDatabase();
			}
		});
		menu.add(createDatabase);
		menu.setLightWeightPopupEnabled(true);
		menu.setOpaque(true);
		return (menu);
	}
	
	private void createDatabase() {
		Object object = JOptionPane.showInputDialog(parent, "Enter the database name:", "Create database", JOptionPane.INFORMATION_MESSAGE, UIUtils.icon("resources/large/database.png"), null, null);
		if (object != null) {
			MongoClient client = null;
			try {
				client = MongoUtils.getMongoClient(selectedHost);
				client.getDB((String) object).getCollectionNames();
				CommandResult result = client.fsync(false);
				System.out.println(result);
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if (dmtn.getUserObject() instanceof Host) {
					DefaultMutableTreeNode database = new DefaultMutableTreeNode(new Database((String) object, (Host) dmtn.getUserObject()));
					treeModel.insertNodeInto(database, dmtn, dmtn.getChildCount());
					tree.setSelectionPath(new TreePath(database.getPath()));
				}
			} catch (Exception e) {
				new ErrorDialog(parent, e);
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	}
	
	private JPopupMenu createMenuForDatabase() {
		JPopupMenu menu = new JPopupMenu();
		menu.setInvoker(tree);
		JMenuItem createCollection = new JMenuItem("Create collection");
		createCollection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createCollection();
			}
		});
		menu.add(createCollection);
		JMenuItem dropDatabase = new JMenuItem("Drop database");
		dropDatabase.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropDatabase();
			}
		});
		menu.add(dropDatabase);
		menu.setLightWeightPopupEnabled(true);
		menu.setOpaque(true);
		return (menu);
	}
	
	private void createCollection() {
		Object object = JOptionPane.showInputDialog(parent, "Enter the collection name:", "Create collection", JOptionPane.INFORMATION_MESSAGE, UIUtils.icon("resources/large/collection.png"), null, null);
		if (object != null) {
			MongoClient client = null;
			try {
				client = MongoUtils.getMongoClient(selectedHost);
				client.getDB(selectedDatabase.getName()).createCollection((String) object, new BasicDBObject());
				CommandResult result = client.fsync(false);
				System.out.println(result);
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				if (dmtn.getUserObject() instanceof Database) {
					DefaultMutableTreeNode collection = new DefaultMutableTreeNode(new Collection((String) object, (Database) dmtn.getUserObject()));
					treeModel.insertNodeInto(collection, dmtn, dmtn.getChildCount());
					tree.setSelectionPath(new TreePath(collection.getPath()));
				}
			} catch (Exception e) {
				new ErrorDialog(parent, e);
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	}
	
	private void dropDatabase() {
		int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to drop the database?", "Drop database", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, UIUtils.icon("resources/large/database.png"));
		if (option == JOptionPane.YES_OPTION) {
			MongoClient client = null;
			try {
				client = MongoUtils.getMongoClient(selectedHost);
				client.dropDatabase(selectedDatabase.getName());
				CommandResult result = client.fsync(false);
				System.out.println(result);
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dmtn.getParent();
				if (dmtn.getUserObject() instanceof Database) {
					treeModel.removeNodeFromParent(dmtn);
					tree.setSelectionPath(new TreePath(parent.getPath()));
				}
			} catch (Exception e) {
				new ErrorDialog(parent, e);
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	}
	
	private JPopupMenu createMenuForCollection() {
		JPopupMenu menu = new JPopupMenu();
		menu.setInvoker(tree);
		JMenuItem findAllDocuments = new JMenuItem("Find all documents");
		findAllDocuments.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				findAllDocuments();
			}
		});
		menu.add(findAllDocuments);
		JMenuItem openQueryWindow = new JMenuItem("Open query window");
		openQueryWindow.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openQueryWindow();
			}
		});
		menu.add(openQueryWindow);
		JMenuItem insertDocument = new JMenuItem("Insert document...");
		insertDocument.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				insertDocument();
			}
		});
		menu.add(insertDocument);
		JMenuItem deleteAllDocuments = new JMenuItem("Delete all documents");
		deleteAllDocuments.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteAllDocuments();
			}
		});
		menu.add(deleteAllDocuments);
		JMenuItem exportCollection = new JMenuItem("Export collection...");
		exportCollection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportCollection();
			}
		});
		menu.add(exportCollection);
		JMenuItem dropCollection = new JMenuItem("Drop collection");
		dropCollection.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dropCollection();
			}
		});
		menu.add(dropCollection);
		menu.setLightWeightPopupEnabled(true);
		menu.setOpaque(true);
		return (menu);
	}
	
	private void openQueryWindow() {
		for (NavigationCallback callback : callbacks) {
			callback.onOpenQueryWindowRequested(selectedCollection);
		}
	}
	
	private void findAllDocuments() {
		for (NavigationCallback callback : callbacks) {
			callback.onFindAllDocumentsRequested(selectedCollection);
		}
	}
	
	private void insertDocument() {
		new InsertDocumentDialog(parent, selectedCollection);
	}
	
	private void deleteAllDocuments() {
		int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to delete all documents from the collection?", "Delete all objects", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, UIUtils.icon("resources/large/collection.png"));
		if (option == JOptionPane.YES_OPTION) {
			MongoClient client = null;
			try {
				client = MongoUtils.getMongoClient(selectedHost);
				client.getDB(selectedDatabase.getName()).getCollection(selectedCollection.getName()).remove(new BasicDBObject());
				CommandResult result = client.fsync(false);
				System.out.println(result);
			} catch (Exception e) {
				new ErrorDialog(parent, e);
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	}
	
	private void exportCollection() {
		new ExportCollectionDialog(parent, connections);
	}
	
	private void dropCollection() {
		int option = JOptionPane.showConfirmDialog(parent, "Are you sure you want to drop the collection?", "Drop collection", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, UIUtils.icon("resources/large/collection.png"));
		if (option == JOptionPane.YES_OPTION) {
			MongoClient client = null;
			try {
				client = MongoUtils.getMongoClient(selectedHost);
				client.getDB(selectedDatabase.getName()).getCollection(selectedCollection.getName()).drop();
				CommandResult result = client.fsync(false);
				System.out.println(result);
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) dmtn.getParent();
				if (dmtn.getUserObject() instanceof Collection) {
					treeModel.removeNodeFromParent(dmtn);
					tree.setSelectionPath(new TreePath(parent.getPath()));
				}
			} catch (Exception e) {
				new ErrorDialog(parent, e);
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	}
	
	private class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) value;
			Object userObject = dmtn.getUserObject();
			
			if (userObject instanceof Connections) {
				setIcon(UIUtils.icon("resources/small/connections.png"));
				setText("Connections");
			}
			
			if (userObject instanceof Host) {
				Host host = (Host) userObject;
				setIcon(UIUtils.icon("resources/small/host.png"));
				setText(host.toString());
			}
			
			if (userObject instanceof Database) {
				Database database = (Database) userObject;
				setIcon(UIUtils.icon("resources/small/database.png"));
				setText(database.toString());
			}
			
			if (userObject instanceof Collection) {
				Collection collection = (Collection) userObject;
				setIcon(UIUtils.icon("resources/small/collection.png"));
				setText(collection.toString());
			}

			return (this);
		}
		
	}
	
}
