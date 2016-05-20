package com.apkscanner.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import com.apkscanner.resource.Resource;

public class ImageControlPanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = -391185152837196160L;
	
	JScrollPane scroll;
	ImageViewPanel imagepanel;
	Dimension Imagearea;
	JLabel ImageInfo;
	Dimension ViewPortsize;
	JPanel scrollpanel;
	double positionx, positiony;
    private int oldVPos = 0;
    private int oldHPos = 0;
	
	int x, y;
	int beforx,befory;
	private float scale = 1;
	private float DefalutMinscale =1;

	Image imageBackground;
	
	public ImageControlPanel() {
		
		imagepanel = new ImageViewPanel();
		JPanel imageInfoPanel = new JPanel();
		
		ImageInfo = new JLabel("");
		
		scrollpanel = new JPanel(new GridBagLayout());
		//imagepanel.setLayout(new GridLayout());
		setLayout(new BorderLayout());
		scrollpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		scrollpanel.setBackground(Color.BLACK);
		scrollpanel.setAutoscrolls(true);
		
		scroll = new JScrollPane(scrollpanel);
		scroll.repaint();
		scrollpanel.add(imagepanel);
		
		imageInfoPanel.add(ImageInfo);
		
	    JRadioButton MonoWhiteRadioButton  = new JRadioButton("White");
	    MonoWhiteRadioButton.addActionListener(this);
	    
	    JRadioButton MonoDarkRadioButton  = new JRadioButton("Dark");
	    MonoDarkRadioButton.addActionListener(this);
		
	    imageInfoPanel.add(MonoWhiteRadioButton);
	    imageInfoPanel.add(MonoDarkRadioButton);
	    
        ButtonGroup group = new ButtonGroup();
        group.add(MonoWhiteRadioButton);
        group.add(MonoDarkRadioButton);
        
        MonoWhiteRadioButton.setSelected(true);
		
		add(scroll, BorderLayout.CENTER);
		add(imageInfoPanel, BorderLayout.SOUTH);
				
		scrollpanel.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
//				   JViewport viewPort = scroll.getViewport();
//		           Point vpp = viewPort.getViewPosition();
//		           vpp.translate((int)((beforx-e.getX())*0.1), (int)((befory-e.getY())*0.1));
//		           Log.d("x = " + (int)(beforx-e.getX()) + "    y = " + (int)(befory-e.getY()));
//		           scrollpanel.scrollRectToVisible(new Rectangle(vpp, viewPort.getSize()));					
//					
					int deltaX = beforx - e.getX();
	                int deltaY = befory - e.getY();
	
	                JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, scrollpanel);
	                Rectangle view = viewPort.getViewRect();
	                view.x += deltaX;
	                view.y += deltaY;
	
	                scrollpanel.scrollRectToVisible(view);
				
					revalidate();
					repaint();
			}
		});
		
		scrollpanel.addMouseListener(imagepanel);
		scrollpanel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double delta = (-e.getPreciseWheelRotation() * 0.1 + 1);
                scale *= delta;
                
                if(scale > 20) scale = 20f;
                else if(scale < 0.02f) scale = 0.02f;
                
                revalidate();
                repaint();
                
                //Log.d("scale : " + scale);                
            }
        });

//	    AdjustmentListener adjustmentListener = new AdjustmentListener() {
//	    	
//	        @Override
//	        public void adjustmentValueChanged(AdjustmentEvent e) {
//	            int vPos = scroll.getVerticalScrollBar().getValue(), 
//	                hPos = scroll.getHorizontalScrollBar().getValue();
//	 
//	            if (e.getSource().equals(scroll.getVerticalScrollBar()) 
//	                    && vPos != oldVPos) {
//	                Log.d("Vertical Scroll Bar changed position to "
//	                        + scroll.getVerticalScrollBar().getValue());
//	                oldVPos = vPos;
//	            }
//	            if (e.getSource().equals(scroll.getHorizontalScrollBar())
//	                    && hPos != oldHPos) {
//	                Log.d("Horizontal Scroll Bar changed position to "
//	                        + scroll.getHorizontalScrollBar().getValue());
//	                oldHPos = hPos;
//	            }
//	            
//	            imagepanel.repaint();
//	        }
//	    };	    
//	    scroll.getVerticalScrollBar().addAdjustmentListener(
//                adjustmentListener);
//	    scroll.getHorizontalScrollBar().addAdjustmentListener(
//                adjustmentListener);
		
		imageBackground = Resource.IMG_RESOURCE_IMG_BACKGROUND.getImageIcon().getImage();
		
	}

	public void setImage(ImageIcon img) {		
		imagepanel.setImage(img);
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		imagepanel.repaint();
		imagepanel.revalidate();
	}
	
	private class ImageViewPanel extends JPanel implements MouseListener{

		BufferedImage bi;
		BufferedImage bgbi;
		
		public ImageViewPanel() {
			setBackground(Color.white);		
			
		}
		public void setImage(ImageIcon img) {
			Image image = img.getImage();
			bi = new BufferedImage(image.getWidth(this), image.getHeight(this), BufferedImage.TYPE_INT_ARGB);
			Graphics2D big = bi.createGraphics();
			big.drawImage(image, 0, 0, this);

			x = 0;
			y = 0;

			beforx = befory = 0;
			scale = 1;	
			
			ViewPortsize = new Dimension(scroll.getViewport().getWidth()-10,scroll.getViewport().getHeight()-10);
			
			//ViewPortsize.setSize(new Dimension((int)ViewPortsize.getWidth()-10, (int)ViewPortsize.getHeight()-10));
			
			if(ViewPortsize.getWidth()<(image.getWidth(this)) || ViewPortsize.getHeight()<(image.getHeight(this))) {
			scale = DefalutMinscale = (float) ((ViewPortsize.getWidth()/(image.getWidth(this)) <= ViewPortsize.getHeight()/(image.getHeight(this)))?
					(ViewPortsize.getWidth()/image.getWidth(this)) :
				(ViewPortsize.getHeight()/image.getHeight(this)));
			} else {
				
			}
			//Log.d("DefalutMinscale : " + DefalutMinscale);
			//Log.d("ViewPortsize.getWidth() : "+ViewPortsize.getWidth() +"  ViewPortsize.getHeight() = "+ ViewPortsize.getHeight());
			Imagearea = new Dimension(x,y);
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2D = (Graphics2D) g;
			ViewPortsize = new Dimension(scroll.getViewport().getWidth()-10,scroll.getViewport().getHeight()-10);
						
			AffineTransform at = new AffineTransform();
			if(bi!=null) {
				Rectangle Rect = g2D.getClipBounds();
				
				
				positionx = (int)((ViewPortsize.getWidth()-bi.getWidth() * scale)/2);
				positiony = (int)((ViewPortsize.getHeight()-bi.getHeight() * scale)/2);
				
				
				//at.translate(positionx, positiony);
				
				TexturePaint paint;
			    
			    bgbi = new BufferedImage(imageBackground.getWidth(this), imageBackground.getHeight(this), BufferedImage.TYPE_INT_ARGB);
			    bgbi.createGraphics().drawImage(imageBackground, 0, 0, this);
			    
				paint = new TexturePaint(bgbi, new Rectangle(0, 0, bgbi.getWidth(), bgbi.getHeight()));
				
				g2D.setPaint(paint);
				//g2D.fillRect((int)positionx, (int)positiony, (int)(bi.getWidth()* scale), (int)(bi.getHeight() * scale));
				
				Rectangle2D rect = new Rectangle2D.Double(0, 0, (bi.getWidth()* scale), (bi.getHeight() * scale));
				
				//og.d("positionx : " + positionx + " positiony : " + positiony +  "scale : " + scale);
				//Log.d("bi.getWidth()* scale : " + bi.getWidth()* scale + "    bi.getHeight() * scale : " +(bi.getHeight() * scale) );
				
				g2D.fill(rect);;
				
		        //at.scale(scale, scale);	        
				String text = "W : " + bi.getWidth() + "      H : " + bi.getHeight() + "     Scale : " + Math.round(scale * 100) + "%";		
				//g2D.drawImage(bi, at, this);
				g2D.drawImage(bi, (int)0, (int)0, (int)(bi.getWidth()* scale), (int)(bi.getHeight() * scale), this);
		        //g2D.setColor(Color.WHITE);
		        
				//g2D.drawChars(text.toCharArray(), 0, text.length(), 10,10);		        
		        
				ImageInfo.setText(text);
				
		        Imagearea.setSize(bi.getWidth()*scale,bi.getHeight()*scale);
		        
		        
		        double tempx = Math.abs((int)(positionx));
		        double tempy = Math.abs((int)(positiony));
				setPreferredSize(Imagearea);
				
				//Log.d("tempx : "+tempx +" tempy = "+ tempy);
				//Log.d("positionx : "+positionx +" positiony = "+ positiony);
				//Log.d("ViewPortsize.getWidth() : "+ViewPortsize.getWidth() +"  ViewPortsize.getHeight() = "+ ViewPortsize.getHeight());
				//Log.d("(int)(bi.getWidth()*scale : "+bi.getWidth()*scale +"  bi.getHeight()*scale = "+ bi.getHeight()*scale);
				//scrollRectToVisible(new Rectangle((int)(tempx+x),(int)(tempy+y),(int)(ViewPortsize.getWidth()),(int)(ViewPortsize.getHeight())));
				//Log.d("tempx + x : "+(int)(tempx + x) +" tempy + y = "+ (int)(tempy+y));
				//Log.d("scroll value : " + scroll.getVerticalScrollBar().getValue());
			}
		}
		
		@Override
		public void mouseClicked(java.awt.event.MouseEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(java.awt.event.MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(java.awt.event.MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(java.awt.event.MouseEvent arg0) {
			// TODO Auto-generated method stub
			
			beforx = arg0.getX();
			befory = arg0.getY();
			
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}

		@Override
		public void mouseReleased(java.awt.event.MouseEvent arg0) {
			// TODO Auto-generated method stub
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		boolean isWhiteMono = arg0.getActionCommand().equals("White");
		if(isWhiteMono) {
			imageBackground = Resource.IMG_RESOURCE_IMG_BACKGROUND.getImageIcon().getImage();
		} else {
			imageBackground = Resource.IMG_RESOURCE_IMG_BACKGROUND_DARK.getImageIcon().getImage();
		}
		repaint();
	}
}