package com.apkscanner.gui.tabpanels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.apkscanner.Launcher;
import com.apkscanner.apkinfo.ApkInfo;
import com.apkscanner.core.AaptWrapper;
import com.apkscanner.gui.TabbedPanel.TabDataObject;
import com.apkscanner.gui.util.FilteredTreeModel;
import com.apkscanner.gui.util.ImageControlPanel;
import com.apkscanner.gui.util.ImageScaler;
import com.apkscanner.gui.util.JHtmlEditorPane;
import com.apkscanner.resource.Resource;
import com.apkscanner.util.ConsolCmd;
import com.apkscanner.util.FileUtil;
import com.apkscanner.util.Log;
import com.apkscanner.util.ZipFileUtil;

public class ImageResource extends JPanel implements TabDataObject, ActionListener
{
	private static final long serialVersionUID = -934921813626224616L;
    
	private static final String CONTENT_IMAGE_VIEWER = "ImageViewer";
	private static final String CONTENT_HTML_VIEWER = "HtmlViewer";
	private static final String CONTENT_TABLE_VIEWER = "TableViewer";
	
	private JPanel contentPanel;
	private ImageControlPanel imageViewerPanel;
	private JHtmlEditorPane htmlViewer;
	private JTable textTableViewer;
	
	private String[] nameList = null;
	private String apkFilePath = null;
	private String tempWorkPath = null;
	//private String appIconPath = null;
	private String[] resourcesWithValue = null;

	private JTree tree;
	private DefaultMutableTreeNode top;
	private DefaultMutableTreeNode[] eachTypeNodes;
	
	private FilteredTreeModel filteredModel;
	private JTextField textField;
	private Boolean firstClick=false;
	
	private ResourceObject currentSelectedObj = null;
	
	ResouceTreeCellRenderer renderer;
	
	private HashMap<String, Icon> fileIcon = new HashMap<String, Icon>();
	
	public enum ResourceType{
		ANIMATION(0),
		ANIM(1),
		COLOR(2),
		DRAWABLE(3),
		MIPMAP(4),
		LAYOUT(5),
		MENU(6),
		RAW(7),
		VALUES(8),
		XML(9),
		ASSET(10),
		ETC(11),
		COUNT(12);
		
		private int type;
		ResourceType(int type) {
			this.type = type;
		}
		
		int getInt() { return type; }
	}
	
	private class ResourceObject {
		public static final int ATTR_AXML = 1;
		public static final int ATTR_XML = 2;
		public static final int ATTR_IMG = 3;
		public static final int ATTR_QMG = 4;
		public static final int ATTR_TXT = 5;
		public static final int ATTR_ETC = 6;
		
		public String label;
		public Boolean isFolder;
		public String path;
		public String config;
		public ResourceType type;
		public int attr;
		public int childCount;
		public Boolean isLoading;
		
		public ResourceObject(String path, boolean isFolder) {
			this.path = path;
			this.isFolder = isFolder;
			this.isLoading = false;
			if(path.startsWith("res/animation")) {
				type = ResourceType.ANIMATION; 
			} else if(path.startsWith("res/anim")) {
				type = ResourceType.ANIM;
			} else if(path.startsWith("res/color")) {
				type = ResourceType.COLOR;
			} else if(path.startsWith("res/drawable")) {
				type = ResourceType.DRAWABLE;
			} else if(path.startsWith("res/mipmap")) {
				type = ResourceType.MIPMAP;
			} else if(path.startsWith("res/layout")) {
				type = ResourceType.LAYOUT;
			} else if(path.startsWith("res/menu")) {
				type = ResourceType.MENU;
			} else if(path.startsWith("res/raw")) {
				type = ResourceType.RAW;
			} else if(path.startsWith("res/values")) {
				type = ResourceType.VALUES;
			} else if(path.startsWith("res/xml")) {
				type = ResourceType.XML;
			} else if(path.startsWith("assets")) {
				type = ResourceType.ASSET;
			} else {
				type = ResourceType.ETC;
			}
			
			if(type.getInt() <= ResourceType.XML.getInt()) {
				if(path.startsWith("res/"+type.toString().toLowerCase()+"-"))
					config = path.replaceAll("res/"+type.toString().toLowerCase()+"-([^/]*)/.*", "$1");
			}
			
			if(path.endsWith(".xml")) {
				if(path.startsWith("res/") || path.equals("AndroidManifest.xml")) attr = ATTR_AXML;
				else attr = ATTR_XML;
			} else if(path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".gif")) {
				attr = ATTR_IMG;
			} else if(path.endsWith(".qmg")) {
				attr = ATTR_QMG;
			} else if(path.endsWith(".txt") || path.endsWith(".mk") 
					|| path.endsWith(".html") || path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".json")
					|| path.endsWith(".props") || path.endsWith(".properties")) {
				attr = ATTR_TXT;
			} else {
				attr = ATTR_ETC;
			}
			
			if(isFolder) {
				label = getOnlyFoldername(path);
			} else {
				label = getOnlyFilename(path); 
			}
			
			childCount = 0;
		}
		
		@Override
		public String toString() {
			String str = null;
			if(childCount > 0) {
				str = this.label + " (" + childCount + ")";;
			} else if(this.config != null) {
				str = this.label + " (" + this.config + ")";
			} else {
				str = this.label;
			}
		    return str;
		}
		public void setLoadingState(Boolean state) {
			isLoading =state;
		}
		public Boolean getLoadingState() {return isLoading;}
	}
	
	private class StringListTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1473513094076687257L;
		private String[] strings;

		StringListTableModel(String[] strings) {
			this.strings = strings;
		}
		
	    @Override
	    public int getColumnCount() {
	        return 1;
	    }

	    @Override
	    public int getRowCount() {
	        return strings.length;
	    }

	    @Override
	    public String getColumnName(int columnIndex) {
	        switch (columnIndex) {
	        case 0:
	            //return "#";
	        case 1:
	            return "Name";
	        }
	        return "";
	    }

	    @Override
	    public Object getValueAt(int rowIndex, int columnIndex) {
	        switch (columnIndex) {
	            case 0:                
	                //return String.format("%3d", rowIndex);
	            case 1:
	            	return strings[rowIndex];
	        }            
	        return "";
	    }
	}
	
	public ImageResource()
	{

	}
	
    private void makeTreeForm() {
    	top = new DefaultMutableTreeNode("Loading...");
    	
		tree = new JTree(new FilteredTreeModel(new DefaultTreeModel(top))) {
			@Override
			public void paintComponent(Graphics g) {
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
				if (getSelectionCount() > 0) {
					for (int i : getSelectionRows()) {
						Rectangle r = getRowBounds(i);
						g.setColor(((DefaultTreeCellRenderer)getCellRenderer()).getBackgroundSelectionColor());
						g.fillRect(0, r.y, getWidth(), r.height);
					}
				}
				super.paintComponent(g);
				if (getLeadSelectionPath() != null) {
					Rectangle r = getRowBounds(getRowForPath(getLeadSelectionPath()));
					g.setColor(hasFocus() ? ((DefaultTreeCellRenderer)getCellRenderer()).getBackgroundSelectionColor().darker() :	((DefaultTreeCellRenderer)getCellRenderer()).getBackgroundSelectionColor());
					g.drawRect(0, r.y, getWidth() - 1, r.height - 1);
				}
			}
		};
    	
		tree.setUI(new javax.swing.plaf.basic.BasicTreeUI() {
			@Override
			public Rectangle getPathBounds(JTree tree, TreePath path) {
				if (tree != null && treeState != null) {
					return getPathBounds(path, tree.getInsets(), new Rectangle());
				}
				return null;
			}

			private Rectangle getPathBounds(TreePath path, Insets insets, Rectangle bounds) {
				bounds = treeState.getBounds(path, bounds);
				if (bounds != null) {
					bounds.width = tree.getWidth();
					bounds.y += insets.top;
				}
				return bounds;
			}
		});
	    
        tree.setOpaque(false);
    	
    	
    	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }
    
    class ResouceTreeCellRenderer extends DefaultTreeCellRenderer implements FocusListener {
		private static final long serialVersionUID = 6248791058116909814L;
		//private ImageIcon iconApk = Resource.IMG_TREE_APK.getImageIcon();
    	//private ImageIcon iconFolder = Resource.IMG_TREE_FOLDER.getImageIcon();
    	
        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                Object value, boolean selected, boolean expanded,
                boolean isLeaf, int row, boolean focused) {
            Component c = super.getTreeCellRendererComponent(tree, value,
                    selected, expanded, isLeaf, row, focused);            
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) value;
            
            
            //((JLabel)c).setOpaque(true);
            
            if(nodo == top) {
            	setIcon(Resource.IMG_APK_FILE_ICON.getImageIcon());
            	return c;
            }
            
            if(nodo.getUserObject() instanceof ResourceObject) {
                ResourceObject tempObject = (ResourceObject)nodo.getUserObject();
                
                if(!tempObject.isFolder) {
                	ResourceObject temp = (ResourceObject)nodo.getUserObject();
                	String jarPath = "jar:file:"+apkFilePath.replaceAll("#", "%23")+"!/";
                	Icon icon = null;

                	switch(temp.attr) {
                	case ResourceObject.ATTR_IMG:
                		try {
                			Image tempImage = null;
							tempImage = ImageScaler.getScaledImage(new ImageIcon(new URL(jarPath+temp.path)),32,32);
							icon = new ImageIcon(tempImage);
							tempImage.flush();
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						}
                		break;
                	case ResourceObject.ATTR_AXML:
                	case ResourceObject.ATTR_XML:
                		//tempImage = ImageScaler.getScaledImage(Resource.IMG_RESOURCE_TREE_XML.getImageIcon(),16,16);
                		break;
                	case ResourceObject.ATTR_QMG:
                		//tempImage = ImageScaler.getScaledImage(Resource.IMG_QMG_IMAGE_ICON.getImageIcon(),32,32);
                		break;
                	case ResourceObject.ATTR_TXT:
                		break;
                	case ResourceObject.ATTR_ETC:
                		break;
                	default :		    				
                	}
    				
    				if(icon != null) {
	                	setIcon(icon);
    				} else {
    	                String suffix = tempObject.path.replaceAll(".*/", "");
    	                if(suffix.indexOf(".") > -1) {
    	                	suffix = suffix.replaceAll(".*\\.", ".");
    	                } else {
    	                	suffix = "";
    	                }
    	                if(temp.getLoadingState()) {
    	                	ImageIcon imageicon = Resource.IMG_LOADING.getImageIcon();	    	                	
    	                	imageicon.setImageObserver(new NodeImageObserver(tree, nodo));	    	                	
    	                	setIcon(imageicon);
    	                		    	     
    	                } else {
    	                	setIcon(getFileIcon(suffix));
    	                	
    	                }
    	                
    				}
                } 
            } else {
            	setIcon(getFileIcon("FOLDER"));
            }
            return c;
        }
        @Override public void focusGained(FocusEvent e) {
          e.getComponent().repaint();
        }
        @Override public void focusLost(FocusEvent e) {
          e.getComponent().repaint();
        }
        
    	private Icon getFileIcon(String suffix) {
    	    Icon icon = fileIcon.get(suffix);
    	    if(icon == null) {
    		    Log.v("getIcon " + suffix);
    		    Image tempImage = null;
    		    if("FOLDER".equals(suffix)) {
    		    	tempImage = ImageScaler.getScaledImage(Resource.IMG_TREE_FOLDER.getImageIcon(),16,16);
    		    	/*
                    UIDefaults defaults = UIManager.getDefaults( );
                    Icon computerIcon = defaults.getIcon( "FileView.computerIcon" );
                    Icon floppyIcon   = defaults.getIcon( "FileView.floppyDriveIcon" );
                    Icon diskIcon     = defaults.getIcon( "FileView.hardDriveIcon" );
                    Icon fileIcon     = defaults.getIcon( "FileView.fileIcon" );
                    Icon folderIcon   = defaults.getIcon( "FileView.directoryIcon" );
                    
                    icon = folderIcon;
                    */
    		    } else if(".xml".equals(suffix)) {
    		    	tempImage = ImageScaler.getScaledImage(Resource.IMG_RESOURCE_TREE_XML.getImageIcon(),16,16);
    		    } else if(".qmg".equals(suffix)) {
    		    	tempImage = ImageScaler.getScaledImage(Resource.IMG_QMG_IMAGE_ICON.getImageIcon(),32,32);
    		    } else if(".dex".equals(suffix)) {
    		    	icon = Resource.IMG_RESOURCE_TREE_CODE.getImageIcon();
    		    } else if(".arsc".equals(suffix)) {
    		    	icon = Resource.IMG_RESOURCE_TREE_ARSC.getImageIcon();
    		    } else {
    			    try {
    			        File file = File.createTempFile("icon", suffix);
    			        FileSystemView view = FileSystemView.getFileSystemView();
    			        icon = view.getSystemIcon(file);
    			        file.delete();
    			    } catch (IOException ioe) {
    			    }
    		    }
    		    if(tempImage != null) {
    		    	icon = new ImageIcon(tempImage);
    				tempImage.flush();
    		    }
    		    if(icon != null) {
    		    	fileIcon.put(suffix, icon);
    		    }
    	    }
    	    return icon;
    	}
      }
    
    
    
    
    
	private String getOnlyFilename(String str) {
		String separator = (str.indexOf(File.separator) > -1) ? separator = File.separator : "/";
		return str.substring(str.lastIndexOf(separator)+1, str.length());
	}
    
	private String getOnlyFoldername(String str) {
		String separator = (str.indexOf(File.separator) > -1) ? separator = File.separator : "/";
		return str.substring(0, str.lastIndexOf(separator));
	}
    
    private final List<DefaultMutableTreeNode> getSearchNodes(DefaultMutableTreeNode root) {
        List<DefaultMutableTreeNode> searchNodes = new ArrayList<DefaultMutableTreeNode>();

        Enumeration<?> e = root.preorderEnumeration();
        while(e.hasMoreElements()) {
            searchNodes.add((DefaultMutableTreeNode)e.nextElement());
        }
        return searchNodes;
    }
	
    public final DefaultMutableTreeNode findNode(String searchString) {

        List<DefaultMutableTreeNode> searchNodes = getSearchNodes((DefaultMutableTreeNode)tree.getModel().getRoot());
        DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        DefaultMutableTreeNode foundNode = null;
        int bookmark = -1;

        if( currentNode != null ) {
            for(int index = 0; index < searchNodes.size(); index++) {
                if( searchNodes.get(index) == currentNode ) {
                    bookmark = index;
                    break;
                }
            }
        }

        for(int index = bookmark + 1; index < searchNodes.size(); index++) {    
            if(searchNodes.get(index).toString().toLowerCase().contains(searchString.toLowerCase())) {
                foundNode = searchNodes.get(index);
                break;
            }
        }

        if( foundNode == null ) {
            for(int index = 0; index <= bookmark; index++) {    
                if(searchNodes.get(index).toString().toLowerCase().contains(searchString.toLowerCase())) {
                    foundNode = searchNodes.get(index);
                    break;
                }
            }
        }
        return foundNode;
    }

    public final DefaultMutableTreeNode findNode(DefaultMutableTreeNode node, String string, boolean ignoreCase, boolean recursively) {
    	DefaultMutableTreeNode ret = null;
    	if(node == null) {
    		node = (DefaultMutableTreeNode)tree.getModel().getRoot();
    		if(node == null) return null;
    	}
    	
    	DefaultMutableTreeNode childNode = null;
    	if(node.getChildCount() > 0) {
    		childNode = (DefaultMutableTreeNode)node.getFirstChild();
    	}
    	while(childNode != null) {
            ResourceObject resObj = null;
    		if(childNode.getUserObject() instanceof ResourceObject) {
    			resObj = (ResourceObject)childNode.getUserObject();
    		}
    		if(resObj.label.equals(string) 
    				|| (ignoreCase && resObj.label.equalsIgnoreCase(string))) {
    			ret = childNode;
    			break;
    		}
    		if(recursively && childNode.getDepth() > 0) {
    			ret = findNode(childNode, string, ignoreCase, recursively);
    			if(ret != null) break;
    		}
    		childNode = childNode.getNextSibling();
    	}

    	return ret;
    }
	
    private void setTreeForm(Boolean mode) {
    	tree.removeAll();
    	
    	top = new DefaultMutableTreeNode(getOnlyFilename(this.apkFilePath));
    	//FilteredTreeModel model = new FilteredTreeModel(new DefaultTreeModel(top));
    	tree.setModel(new DefaultTreeModel(top));
    	
    	ArrayList<DefaultMutableTreeNode> topFiles = new ArrayList<DefaultMutableTreeNode>();

    	eachTypeNodes = new DefaultMutableTreeNode[ResourceType.COUNT.getInt()];
    	for(int i=0; i<this.nameList.length; i++) {
    		if(this.nameList[i].endsWith("/")
    				|| this.nameList[i].startsWith("lib/")
    				|| this.nameList[i].startsWith("META-INF/")) continue;

    		ResourceObject resObj = new ResourceObject(this.nameList[i], false);
    		if(this.nameList[i].indexOf("/") == -1) {
    			topFiles.add(new DefaultMutableTreeNode(resObj));
    			continue;
    		}
    		
    		DefaultMutableTreeNode typeNode = eachTypeNodes[resObj.type.getInt()];

			if (typeNode == null) {
				typeNode = new DefaultMutableTreeNode(resObj.type.toString().toLowerCase());
				eachTypeNodes[resObj.type.getInt()] = typeNode;
				if(resObj.type != ResourceType.ETC) {
					top.add(typeNode);
				}
			}
			
			DefaultMutableTreeNode findnode = null;
    		if(resObj.type != ResourceType.ETC) {
        		String fileName = getOnlyFilename(this.nameList[i]);
       			findnode = findNode(typeNode, fileName, false, false);
    		}
    		
   			if(findnode != null) {
   				if(findnode.getChildCount() == 0) {
   					ResourceObject obj = (ResourceObject)findnode.getUserObject();
   					findnode.add(new DefaultMutableTreeNode(new ResourceObject(obj.path, false)));
   	   				((ResourceObject)findnode.getUserObject()).childCount++;
   				}
   				findnode.add(new DefaultMutableTreeNode(resObj));
   				((ResourceObject)findnode.getUserObject()).childCount++;
   			} else {
   				typeNode.add(new DefaultMutableTreeNode(resObj));
   			}
    	}
    	
    	if(eachTypeNodes[ResourceType.ETC.getInt()] != null) {
    		top.add(eachTypeNodes[ResourceType.ETC.getInt()]);
    	}
    	
    	for(DefaultMutableTreeNode node: topFiles) {
    		top.add(node);
    	}
    	
    	expandOrCollapsePath(tree, new TreePath(top.getPath()),1,0, true);
    }
    
    public static void expandOrCollapsePath (JTree tree,TreePath treePath,int level,int currentLevel,boolean expand) {
//      System.err.println("Exp level "+currentLevel+", exp="+expand);
      if (expand && level<=currentLevel && level>0) return;

      TreeNode treeNode = ( TreeNode ) treePath.getLastPathComponent();
      TreeModel treeModel=tree.getModel();
      if ( treeModel.getChildCount(treeNode) >= 0 ) {
         for ( int i = 0; i < treeModel.getChildCount(treeNode); i++  ) {
            TreeNode n = ( TreeNode )treeModel.getChild(treeNode, i);
            TreePath path = treePath.pathByAddingChild( n );
            expandOrCollapsePath(tree,path,level,currentLevel+1,expand);
         }
         if (!expand && currentLevel<level) return;
      }      
      if (expand) {
         tree.expandPath( treePath );
//         System.err.println("Path expanded at level "+currentLevel+"-"+treePath);
      } else {
         tree.collapsePath(treePath);
//         System.err.println("Path collapsed at level "+currentLevel+"-"+treePath);
      }
   }

    private void selectContent() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
        if(node == null) return;
		
        ResourceObject resObj = null;
		if(node.getUserObject() instanceof ResourceObject) {
			resObj = (ResourceObject)node.getUserObject();
		}
		
		if(resObj != null && resObj == currentSelectedObj) {
			Log.v("select same object");
			return;
		}
		currentSelectedObj = resObj;

		if(resObj == null || resObj.isFolder) {
			//htmlViewer.setText("");
			//((CardLayout)contentPanel.getLayout()).show(contentPanel, CONTENT_HTML_VIEWER);
		} else {
			switch(resObj.attr) {
			case ResourceObject.ATTR_IMG:
			case ResourceObject.ATTR_QMG:
				drawImageOnPanel(resObj);
				break;
			case ResourceObject.ATTR_AXML:
			case ResourceObject.ATTR_XML:
			case ResourceObject.ATTR_TXT:
			    setTextContentPanel(resObj);
				break;
			default:
			    setTextContentPanel(resObj);
				break;
			}
		}
    }
    
    private void drawImageOnPanel(ResourceObject obj) {
		try {
			imageViewerPanel.setImage(apkFilePath, obj.path);
			imageViewerPanel.repaint();
			 ((CardLayout)contentPanel.getLayout()).show(contentPanel, CONTENT_IMAGE_VIEWER);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} 
    }
    
    private void setTextContentPanel(ResourceObject obj) {
    	String content = null;
    	
		switch(obj.attr) {
		case ResourceObject.ATTR_AXML:
			String[] xmlbuffer = AaptWrapper.Dump.getXmltree(apkFilePath, new String[] {obj.path});
			StringBuilder sb = new StringBuilder();
			for(String s: xmlbuffer) sb.append(s+"\n");
			content = sb.toString();
			break;
		case ResourceObject.ATTR_XML:
		case ResourceObject.ATTR_TXT:
			ZipFile zipFile;
			try {
				zipFile = new ZipFile(apkFilePath);
				ZipEntry entry = zipFile.getEntry(obj.path);
				byte[] buffer = new byte[(int) entry.getSize()];
				zipFile.getInputStream(entry).read(buffer);
				content = new String(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case ResourceObject.ATTR_ETC:
			if("resources.arsc".equals(obj.path)) {
				if(resourcesWithValue != null) {
					textTableViewer.setModel(new StringListTableModel(resourcesWithValue));
					textTableViewer.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
					 ((CardLayout)contentPanel.getLayout()).show(contentPanel, CONTENT_TABLE_VIEWER);
					return;
				} else {
					content = "lodding...";
				}
				break;
			}
		default:
			content = "This type is unsupported by preview.";
			break;
		}
		
		if(content != null) {
			htmlViewer.setText("<pre>" + content.replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</pre>");
			//textViewerPanel.setText("<pre>" + content.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("[\r]\n", "<br/>") + "</pre>");
			htmlViewer.setCaretPosition(0);
			((CardLayout)contentPanel.getLayout()).show(contentPanel, CONTENT_HTML_VIEWER);
		}
    }
    
    private void openContent() {
        final DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                tree.getLastSelectedPathComponent();
        if(node == null 
        		|| !(node.getUserObject() instanceof ResourceObject)) {
        	return;
        }
		
        final ResourceObject resObj = (ResourceObject)node.getUserObject();
        String resPath = tempWorkPath + File.separator + resObj.path.replace("/", File.separator);
		File resFile = new File(resPath);
		if(!resFile.exists()) {
			if(!resFile.getParentFile().exists()) {
				if(FileUtil.makeFolder(resFile.getParentFile().getAbsolutePath())) {
					Log.i("sucess make folder : " + resFile.getParentFile().getAbsolutePath());
				}
			}
		}
		
		if(resObj != null && !resObj.isFolder) {
			String[] convStrings = null;
			if(resObj.attr == ResourceObject.ATTR_AXML) {
				convStrings = AaptWrapper.Dump.getXmltree(apkFilePath, new String[] {resObj.path});
			} else if("resources.arsc".equals(resObj.path)) {
				convStrings = resourcesWithValue;
				resPath += ".txt";
			} else {
				ZipFileUtil.unZip(apkFilePath, resObj.path, resPath);
			}
			
			if(convStrings != null) {
				StringBuilder sb = new StringBuilder();
				for(String s: convStrings) sb.append(s+"\n");
				try {
					FileWriter fw = new FileWriter(new File(resPath));
					fw.write(sb.toString());
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if(resObj.path.endsWith(".dex")) {
			Log.i("dex file");
			if(resObj.getLoadingState() == false) {
				resObj.setLoadingState(true);
								
				tree.repaint();
				DexLuncher.openDex(resPath, new DexLuncher.DexWrapperListener(){
				@Override
				public void OnError() {
					// TODO Auto-generated method stub					
				}
				@Override
				public void OnSuccess() {
					// TODO Auto-generated method stub
					resObj.setLoadingState(false);
					//tree.repaint();
				}});
			}	
		} else if(resObj.path.endsWith(".apk")) {
				Launcher.run(resPath);
		} else {
			String openner;
			if(System.getProperty("os.name").indexOf("Window") >-1) {
				openner = "explorer";
			} else {  //for linux
				openner = "xdg-open";
			}
	
				try {
					new ProcessBuilder(openner, resPath).start();
				} catch (IOException e1) { }
			}		
    }

	
    static public class DexLuncher {
    	public interface DexWrapperListener
    	{    		
    		public void OnError();
    		public void OnSuccess();
    	}
    	
    	static public void openDex(final String dexFilePath, final DexWrapperListener listener)
    	{			
    		new Thread(new Runnable() {
    			public void run()
    			{
    				String jarFilePath = dexFilePath.replaceAll("\\.dex$", ".jar");
    							
    				String[] cmdLog = null;
    				
    				Log.i("Start DEX2JAR");
    				if(System.getProperty("os.name").indexOf("Window") >-1) {
    					cmdLog =ConsolCmd.exc(new String[] {Resource.BIN_DEX2JAR_WIN.getPath(), 
    							dexFilePath, "-o", jarFilePath});
    				} else {  //for linux
    					cmdLog =ConsolCmd.exc(new String[] {"sh", Resource.BIN_DEX2JAR_LNX.getPath(), 
    							dexFilePath, "-o", jarFilePath});				
    				}
    				//open JD GUI
    				for( int i=0 ; i<cmdLog.length; i++)
    				{
    					Log.i("DEX2JAR Log : "+ cmdLog[i]);	
    				}
    				Log.i("End DEX2JAR");
    				
    				listener.OnSuccess();
    				
    				cmdLog =ConsolCmd.exc(new String[] {"java", "-jar", Resource.BIN_JDGUI.getPath(), jarFilePath});
    			}
    		}).start();
    	}    	    	
    }
	

    
	public class NodeImageObserver implements ImageObserver {
		JTree tree;
		DefaultTreeModel model;
		TreeNode node;

		NodeImageObserver(JTree tree, TreeNode node) {
			this.tree = tree;
			this.model = (DefaultTreeModel) tree.getModel();
			this.node = node;
		}

		public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
			
			Log.d("Imageupdate : " + flags);
			
			
			
			if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
				TreePath path = new TreePath(model.getPathToRoot(node));
				Rectangle rect = tree.getPathBounds(path);
				if (rect != null) {
					tree.repaint(rect);
				}
			} else {
				Log.d("else" + flags);
			}
			return (flags & (ALLBITS | ABORT)) == 0;
		}
	}
	
    private void TreeInit() {
    	
    	renderer = new ResouceTreeCellRenderer();    	
        tree.setCellRenderer(renderer);    			
	    tree.addFocusListener(renderer);
	    
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					selectContent();
				}
            }

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					openContent();
				}
			}
        });
        
        tree.addKeyListener(new KeyAdapter()
        {
        	public void keyReleased(KeyEvent ke) {
        		//if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
        		selectContent();
        		//}
        	}
        });       
    }
    
    private void expandTree(final JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
          tree.expandRow(i);
        }
      }
    
    @SuppressWarnings("unused")
	private void makefilter (String temp){
        filteredModel = (FilteredTreeModel) tree.getModel();
        filteredModel.setFilter(temp);
        DefaultTreeModel treeModel = (DefaultTreeModel) filteredModel.getTreeModel();
        treeModel.reload();
        
        
        expandTree(tree);
        forselectionTree ();
	}
    
    @SuppressWarnings("unused")
	private void forselectionTree () {
        DefaultMutableTreeNode currentNode = top.getNextNode();
        /*
        do {
        		if(currentNode.getLevel()==3 && filteredModel.getChildCount(currentNode) > 0) {
    		        	
    		        	TreePath temptreePath = new TreePath(((DefaultMutableTreeNode)(filteredModel.getChild(currentNode, 0))).getPath());
    		        	
		        		tree.setSelectionPath(temptreePath);
		        		tree.scrollPathToVisible(temptreePath);
		        		return;
		        }
	           currentNode = currentNode.getNextNode();
           }
        while (currentNode != null);
        */
    }
    
    
	@Override
	public void initialize()
	{
		this.setLayout(new GridLayout(1, 1));

		makeTreeForm();
		TreeInit();
		
		textField = new JTextField("");
		textField.addKeyListener(new KeyAdapter()
        {
            public void keyReleased(KeyEvent ke) {
            	
            	if(textField.getText().length() ==0) {
            		expandOrCollapsePath(tree, new TreePath(top.getPath()),2,0, true);
            		return;
            	}
            	
                if(!(ke.getKeyChar()==27||ke.getKeyChar()==65535))//this section will execute only when user is editing the JTextField
                {
                	//makefilter (textField.getText());
                }
            }
        });
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
			
			}			
			@Override
			public void focusGained(FocusEvent arg0) {
            	if(!firstClick) {
            		firstClick = true;
            		textField.setText("");
            	}
			}
		});
		
		
		// Tree navigator ----------
	    JRadioButton ForderModeRadioButton  = new JRadioButton("Folder");
	    ForderModeRadioButton.addActionListener(this);
	    
	    JRadioButton ImageModeRadioButton  = new JRadioButton("Resource");
	    ImageModeRadioButton.addActionListener(this);
	    ImageModeRadioButton.setSelected(true);
	    
	    //JLabel TreeModeLabel = new JLabel("Search");
	    //TreeModePanel.add(TreeModeLabel);

		JPanel TreeModePanel = new JPanel(new GridLayout(1,3));
	    TreeModePanel.add(ImageModeRadioButton);
	    TreeModePanel.add(ForderModeRadioButton);
	    TreeModePanel.add(textField);
	    
        ButtonGroup group = new ButtonGroup();
        group.add(ImageModeRadioButton);
        group.add(ForderModeRadioButton);
        // End Tree navigator ----------

		JScrollPane treeScroll = new JScrollPane(tree);
		treeScroll.setPreferredSize(new Dimension(300, 400));
		treeScroll.repaint();
		
	    AdjustmentListener adjustmentListener = new AdjustmentListener() {
	        @Override
	        public void adjustmentValueChanged(AdjustmentEvent e) {
	        	tree.repaint();
	        }
	    };	    
	    treeScroll.getVerticalScrollBar().addAdjustmentListener(
                adjustmentListener);
	    treeScroll.getHorizontalScrollBar().addAdjustmentListener(
                adjustmentListener);
		
		
		JPanel TreePanel = new JPanel(new BorderLayout());
		TreePanel.add(TreeModePanel, BorderLayout.NORTH);
		TreePanel.add(treeScroll, BorderLayout.CENTER);
		

		imageViewerPanel = new ImageControlPanel();
		//imageViewerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		//imageViewerPanel.setBackground(Color.BLACK);
		

		JLabel label = new JLabel();
		Font font = label.getFont();
		StringBuilder style = new StringBuilder("#basic-info, #perm-group {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#basic-info a {text-decoration:none; color:black;}");
		style.append("#perm-group a {text-decoration:none; color:#"+Integer.toHexString(label.getBackground().getRGB() & 0xFFFFFF)+";}");
		style.append(".danger-perm {text-decoration:none; color:red;}");
		style.append("#about {");
		style.append("font-family:" + font.getFamily() + ";");
		style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
		style.append("font-size:" + font.getSize() + "pt;}");
		style.append("#about a {text-decoration:none;}");

		htmlViewer = new JHtmlEditorPane();
		htmlViewer.setStyle(style.toString());
		htmlViewer.setBackground(Color.white);
		htmlViewer.setEditable(false);
		htmlViewer.setOpaque(true);
		JScrollPane htmlViewerScroll = new JScrollPane(htmlViewer);
		
		textTableViewer = new JTable();
		textTableViewer.setShowHorizontalLines(false);
		textTableViewer.setTableHeader(null);
		textTableViewer.setCellSelectionEnabled(true);
		textTableViewer.setRowSelectionAllowed(false);
		textTableViewer.setColumnSelectionAllowed(false);
		textTableViewer.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane textTableScroll = new JScrollPane(textTableViewer);
		
		contentPanel = new JPanel(new CardLayout());
		contentPanel.add(htmlViewerScroll, CONTENT_HTML_VIEWER);
		contentPanel.add(imageViewerPanel, CONTENT_IMAGE_VIEWER);
		contentPanel.add(textTableScroll, CONTENT_TABLE_VIEWER);
		
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(TreePanel);
        splitPane.setRightComponent(contentPanel);
        
        this.add(splitPane);
        
		
	}

	@Override
	public void setData(ApkInfo apkInfo)
	{
		if(tree == null)
			initialize();

		this.apkFilePath = apkInfo.filePath; 
		this.tempWorkPath = apkInfo.tempWorkPath;
		
		nameList = apkInfo.images;
		setTreeForm(false);
	}
	
	public void setExtraData(ApkInfo apkInfo)
	{
		if(apkInfo != null) {
			resourcesWithValue = apkInfo.resourcesWithValue;
			//if(apkInfo.manifest.application.icons != null && apkInfo.manifest.application.icons.length > 0) {
				//appIconPath = apkInfo.manifest.application.icons[apkInfo.manifest.application.icons.length - 1].name;
			//}
		} else {
			//appIconPath = null;
			resourcesWithValue = null;
		}
	}
    
	@Override
	public void reloadResource() {
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		boolean isFolderMode = !arg0.getActionCommand().equals("Resource");
		setTreeForm(isFolderMode);
	}
}