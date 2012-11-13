package rosick;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Visit https://github.com/rosickteam/OpenGL for project info, updates and license terms.
 * 
 * @author integeruser
 */
public class TutorialChooser extends JPanel implements TreeSelectionListener {
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Click on a tutorial to run it");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel tutorialsViewer = new TutorialChooser();
		frame.setContentPane(tutorialsViewer);
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		
		if (node != null && node.isLeaf()) {
			final Tutorial selectedTutorial = (Tutorial) node.getUserObject();

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						// Get .class file of the selected tutorial
		  			    Class<?> cls = selectedTutorial.cls;
		  			    Method method = cls.getMethod("main", String[].class);
		  			    
		  			    // Invoke main(String args[])
					    String[] params = null; 
						method.invoke(null, (Object) params);
					} catch (IllegalAccessException| IllegalArgumentException| InvocationTargetException 
							| NoSuchMethodException | SecurityException e) {
							e.printStackTrace();
					}
					
					System.gc();
				}
			}).start();
		}	
	}
	
	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */		
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */	
		
	private static final long serialVersionUID = -2872009239013384414L;
	private JTree tree;
    

	private TutorialChooser() {
		super();
		
		DefaultMutableTreeNode top = new DefaultMutableTreeNode("LWJGL tutorials");
		createNodes(top);
		
		tree = new JTree(top);
		tree.addTreeSelectionListener(this);

		JScrollPane treeView = new JScrollPane(tree);
		treeView.setPreferredSize(new Dimension(350, 350));
		add(treeView);
	}

	
	
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
	 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
	
	private class Tutorial {
		Class<?> cls;
		String displayedName;
		
		Tutorial(Class<?> cls, String displayedName) {
			this.cls = cls;
			this.displayedName = displayedName;
		}
		
		@Override
		public String toString() {
			return displayedName;
		}
	}
		
	
	private void createNodes(DefaultMutableTreeNode top) {
		addCategory(top, "/rosick/mckesson/I", 		"I. The Basics");
		addCategory(top, "/rosick/mckesson/II", 	"II. Positioning");
		addCategory(top, "/rosick/mckesson/III", 	"III. Illumination");
		addCategory(top, "/rosick/mckesson/IV", 	"IV. Texturing");
	}

	private void addCategory(DefaultMutableTreeNode top, String categoryPath, String categoryDisplayedName) {
		URL url = this.getClass().getResource(categoryPath);

		DefaultMutableTreeNode category = new DefaultMutableTreeNode(categoryDisplayedName);
		File categoryDirectory = new File(url.getFile());
		
		if (categoryDirectory.exists()) {
			// we are running this code from filesystem
			addTutorialsFromFileSystem(category, categoryDirectory);
		} else {
			// we are running this code from jar
			String categoryPathInJar = categoryPath.substring(1, categoryPath.length());
			addTutorialsFromJar(category, categoryPathInJar, url);
		}

		top.add(category);
	}
	
	
	private void addTutorialsFromFileSystem(DefaultMutableTreeNode category, File baseFile) {
		for (File file : baseFile.listFiles()) {
			String fileName = file.getName();
		    
			if (file.isDirectory()) {
				if (isDirectoryNameValid(fileName)) {
					DefaultMutableTreeNode tutorialFolder = new DefaultMutableTreeNode(fileName);
					addTutorialsFromFileSystem(tutorialFolder, file);
					category.add(tutorialFolder);
				}
			}
			else {
				if (isFileNameValid(fileName)) {					
					addTutorial(category, file.getPath());
				}
			}
		}
	}
	
	private void addTutorialsFromJar(DefaultMutableTreeNode category, String categoryPath, URL url) {
		try {
			DefaultMutableTreeNode tutorialFolder = null;
			URLConnection urlConnection = url.openConnection();
			
			if (urlConnection instanceof JarURLConnection) {
				JarURLConnection jarUrlConnection = (JarURLConnection) urlConnection;

				JarFile jarFile = jarUrlConnection.getJarFile();
				Enumeration<JarEntry> jarFileEntries = jarFile.entries();

				while (jarFileEntries.hasMoreElements()) {
					JarEntry jarEntry = jarFileEntries.nextElement();
					String jarEntryName = jarEntry.getName();
					
					if (jarEntryName.startsWith(categoryPath + "/tut")) {
						if (jarEntry.isDirectory()) {
							String directoryName = jarEntryName.substring(0, jarEntryName.length() - 1);
							directoryName = directoryName.substring(directoryName.lastIndexOf('/') + 1);
							
							if (isDirectoryNameValid(directoryName)) {
								tutorialFolder = new DefaultMutableTreeNode(directoryName);
								category.add(tutorialFolder);
							}
						} else {
							String fileName = jarEntryName.substring(jarEntryName.lastIndexOf("/") + 1);
							
							if (isFileNameValid(fileName)) {
								addTutorial(tutorialFolder, jarEntryName);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean isDirectoryNameValid(String directoryName) {
		return directoryName.startsWith("tut");
	}
	
	private boolean isFileNameValid(String fileName) {
		return fileName.endsWith(".class")											// is a class
				&& !fileName.contains("$")											// is not a nested class
				&& Pattern.compile("[0-9]").matcher(fileName).find();				// contains a digit
	}
	
		
	private void addTutorial(DefaultMutableTreeNode top, String classFilePath) {
		try {
			String classFilePathInPackage = classFilePath;
			classFilePathInPackage = classFilePathInPackage.substring(classFilePathInPackage.indexOf("rosick"));
			classFilePathInPackage = classFilePathInPackage.replace(".class", "");
			classFilePathInPackage = classFilePathInPackage.replace("\\", ".");
			classFilePathInPackage = classFilePathInPackage.replace("/", ".");

			Class<?> cls = Class.forName(classFilePathInPackage);
			Tutorial tutorial = new Tutorial(cls, cls.getSimpleName());
			
			DefaultMutableTreeNode tutorialNode = new DefaultMutableTreeNode(tutorial);
			top.add(tutorialNode);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}