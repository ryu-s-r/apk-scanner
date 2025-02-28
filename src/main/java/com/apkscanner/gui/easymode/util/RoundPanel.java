package com.apkscanner.gui.easymode.util;

import java.awt.BorderLayout;
import java.awt.Color;

import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class RoundPanel extends JPanel {
	private static final long serialVersionUID = -7828985011164806202L;
	int len = 7;
	private Color roundrectcolor;
	
	
	public RoundPanel() {
		setLayout(new BorderLayout());
		//setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
		//contentpanel = new JPanel();
		//contentpanel.setBackground(getBackground());
		setBorder(BorderFactory.createEmptyBorder(1, len/2, len, len/2));
		setBackground(Color.WHITE);
		//contentpanel.add(new JButton("aa"), BorderLayout.CENTER);
		//add(contentpanel, BorderLayout.CENTER);		
	}
	
	
	public RoundPanel(BorderLayout borderLayout) {
		this();
		setLayout(borderLayout);
		//setBorder(BorderFactory.createEmptyBorder(0, 2, 2, 2));
		//contentpanel = new JPanel();
		//contentpanel.setBackground(getBackground());
		
	}


	
	public void setRoundrectColor(Color rowundColor) {
		roundrectcolor = rowundColor;
	}
	public Color getRoundrectColor() {
		return roundrectcolor;
	}
	
	public void setshadowlen(int setlen) {
		this.len = setlen;
		setBorder(BorderFactory.createEmptyBorder(1, len/2, len, len/2));
		repaint();
	}
	
    @Override
	protected void paintComponent(Graphics gr)
    {
        super.paintComponent(gr);
        
        if(len ==0) return;
        
        Graphics2D g = (Graphics2D)gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
//        Rectangle2D r = new Rectangle2D.Double(len, len, getWidth() - len *2, getHeight() - len *2);
//        draw(g, r, len);
        
        
        g.setColor(roundrectcolor);
        g.fillRoundRect(len / 2, len / 2, getWidth()-len, getHeight()-len, 20,15);
        
    }

    @SuppressWarnings("unused")
	private static void draw(Graphics2D g, Rectangle2D r, double s)
    {	
        Color c0 = new Color(100,100,100);
        Color c1 = new Color(100,100,100,0);

        double x0 = r.getMinX();
        double y0 = r.getMinY();
        double x1 = r.getMaxX();
        double y1 = r.getMaxY();
        double w = r.getWidth();
        double h = r.getHeight();

        // Left
        g.setPaint(new GradientPaint(
            new Point2D.Double(x0, y0), c0,
            new Point2D.Double(x0 - s, y0), c1));
        //g.fill(new Rectangle2D.Double(x0 - s, y0, s, h));
        g.fillRoundRect((int)(x0 - s), (int)y0, (int)s, (int)h, 10, 10);
        //g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
        
        // Right
        g.setPaint(new GradientPaint(
            new Point2D.Double(x1, y0), c0,
            new Point2D.Double(x1 + s, y0), c1));
        g.fill(new Rectangle2D.Double(x1, y0, s, h));

        // Top
        g.setPaint(new GradientPaint(
            new Point2D.Double(x0, y0), c0,
            new Point2D.Double(x0, y0 - s), c1));
        g.fill(new Rectangle2D.Double(x0, y0 - s, w, s));

        // Bottom
        g.setPaint(new GradientPaint(
            new Point2D.Double(x0, y1), c0,
            new Point2D.Double(x0, y1 + s), c1));
        g.fill(new Rectangle2D.Double(x0, y1, w, s));

        float fractions[] = new float[] { 0.0f, 1.0f };
        Color colors[] = new Color[] { c0, c1 };

        // Top Left
        g.setPaint(new RadialGradientPaint(
            new Rectangle2D.Double(x0 - s, y0 - s, s + s, s + s), 
            fractions, colors, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(x0 - s, y0 - s, s, s));

        // Top Right
        g.setPaint(new RadialGradientPaint(
            new Rectangle2D.Double(x1 - s, y0 - s, s + s, s + s), 
            fractions, colors, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(x1, y0 - s, s, s));

        // Bottom Left
        g.setPaint(new RadialGradientPaint(
            new Rectangle2D.Double(x0 - s, y1 - s, s + s, s + s), 
            fractions, colors, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(x0 - s, y1, s, s));

        // Bottom Right
        g.setPaint(new RadialGradientPaint(
            new Rectangle2D.Double(x1 - s, y1 - s, s + s, s + s), 
            fractions, colors, CycleMethod.NO_CYCLE));
        g.fill(new Rectangle2D.Double(x1, y1, s, s));
    }

}
