package com.tt.mongoexplorer.utils;

import java.awt.Component;
import java.io.File;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mongodb.DBObject;

public class UIUtils {

	public static String prettyPrint(DBObject object) {
		if (object == null) {
			return ("");
		}
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(object.toString());
			return (gson.toJson(element));
		} catch (Exception e) {
			return (object.toString());
		}
	}
	
	public static boolean isMac() {
		String os = System.getProperty("os.name");
		return (os != null && os.toLowerCase().contains("mac"));
	}
	
	public static void about(JFrame parent) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><b><font color='black'>");
		sb.append(Constants.NAME);
		sb.append("</font></b><p>");
		sb.append("<html><font color='gray'>Version ");
		sb.append(Constants.VERSION);
		sb.append("</font><p>");
		sb.append("<html><font color='gray'>Released ");
		sb.append(Constants.RELEASED);
		sb.append("</font>");
		sb.append("</html>");
		JOptionPane.showMessageDialog(parent, sb.toString(), "About", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void info(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Heads Up!", JOptionPane.INFORMATION_MESSAGE, UIUtils.icon("resources/large/info.png"));
	}
	
	public static void error(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "Oops!", JOptionPane.ERROR_MESSAGE, UIUtils.icon("resources/large/error.png"));
	}
	
	public static ImageIcon icon(String path) {
		File file = new File(path);
		if (file.exists()) {
			return (new ImageIcon(path));
		} else {
			return (new ImageIcon(UIUtils.class.getResource("/" + path)));
		}
	}
	
}
