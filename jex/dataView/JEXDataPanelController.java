package jex.dataView;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jex.statics.DisplayStatics;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DataReader.ImageReader;
import Database.DataReader.LabelReader;
import Database.DataReader.ValueReader;

public class JEXDataPanelController{
	private static final long serialVersionUID = 1L;
	
	public JEXData  data ;
	public JEXEntry entry ;
	
	public JEXDataPanelController()
	{
		
	}

	/**
	 * Set the data of this JEXDataView
	 */
	public void setData(JEXData data){
		this.data = data;
	}
	
	/**
	 * Set the data of this JEXDataView
	 */
	public void setEntry(JEXEntry entry){
		this.entry = entry;
	}
	
	public JPanel panel()
	{
		if (data != null && data.getTypeName().getType().equals(JEXData.IMAGE))
		{
			return new JEXImageView();
		}
		else if (data != null && data.getTypeName().getType().equals(JEXData.VALUE))
		{
			return new JEXValueView();
		}
		else if (data != null && data.getTypeName().getType().equals(JEXData.LABEL))
		{
			return new JEXLabelView();
		}
		return new JPanel();
	}
	
	class JEXValueView extends JPanel
	{
		private static final long serialVersionUID = 1L;

		JEXValueView()
		{
			this.setBackground(DisplayStatics.lightBackground);
			this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
			
			String valueStr = ValueReader.readValueObject(data);
			JLabel valueLabel = new JLabel(valueStr);
			this.add(valueLabel);
		}
	}
	
	class JEXLabelView extends JPanel
	{
		private static final long serialVersionUID = 1L;

		JEXLabelView()
		{
			this.setBackground(DisplayStatics.lightBackground);
			this.setLayout(new BoxLayout(this,BoxLayout.LINE_AXIS));
			
			String valueStr = ""+ LabelReader.readLabelValue(data); 
			JLabel valueLabel = new JLabel(valueStr);
			this.add(valueLabel);
		}
	}
	
	class JEXImageView extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private double scale = 1.0;
		
		JEXImageView()
		{
			this.repaint();
		}
		
		public void paint(Graphics g){
			Graphics2D g2 = (Graphics2D)g;
			
			int wpane     = this.getWidth();
			int hpane     = this.getHeight();
			g2.setColor(DisplayStatics.lightBackground);
			g2.setColor(DisplayStatics.dividerColor);
			g2.fillRect(0, 0, wpane, hpane);
			
			if (data == null)
			{
				return;
			}
			ImagePlus source = ImageReader.readObjectToImagePlus(data);
			
			if (source != null && source.getProcessor() != null)
			{
				// Find the new scale of the image
				int w         = source.getWidth();
				int h         = source.getHeight();
				double scaleX = ((double)wpane)/((double)w);
				double scaleY = ((double)hpane)/((double)h);
				this.scale    = Math.min(scaleX, scaleY);
				int newW      = (int) (scale * w);
				int newH      = (int) (scale * h);
				
				// Center the image
				int yPos      = hpane/2 - newH/2;
				int xPos      = wpane/2 - newW/2;
				
				// resize the image
				ImageProcessor imp = source.getProcessor();
				if (imp == null) return;
				imp = imp.resize(newW);
				Image image = imp.getBufferedImage();
				
				// draw the image
				g2.setColor(DisplayStatics.lightBackground);
				g2.fillRect(0, 0, wpane, hpane);
				g2.drawImage(image, xPos, yPos,this);
				
			}
		}
	}
}

