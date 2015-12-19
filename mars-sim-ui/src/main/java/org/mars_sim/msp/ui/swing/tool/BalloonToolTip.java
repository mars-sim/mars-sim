package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.ListItemBalloonTip;
import net.java.balloontip.TableCellBalloonTip;
import net.java.balloontip.BalloonTip.AttachLocation;
import net.java.balloontip.BalloonTip.Orientation;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.ModernBalloonStyle;
import net.java.balloontip.utils.FadingUtils;
import net.java.balloontip.utils.ToolTipUtils;

public class BalloonToolTip extends BalloonTip {

	private static final long serialVersionUID = 1L;

	private Color fillColor;
	private Color transparentFill;
	private ModernBalloonStyle style;

	public BalloonToolTip() {}

	public void setBackgroundColor() {

	}

	//public void setTextContents(String text){
	//	super.setTextContents(text);
	//}
	public void createListItemBalloonTip(JList<?> list, String s, int index) {
		fillColor = list.getBackground();
		transparentFill = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 240);//180);
		//style = new ModernBalloonStyle(10, 10, transparentFill, transparentFill, Color.ORANGE);
		style = new ModernBalloonStyle(10, 10, transparentFill, transparentFill, Color.ORANGE);

		style.setBorderThickness(3);
		style.enableAntiAliasing(true);
		ListItemBalloonTip bt = new ListItemBalloonTip(
				list,
				new JLabel(s),
				index,
				style,
				BalloonTip.Orientation.LEFT_ABOVE,
				BalloonTip.AttachLocation.ALIGNED,
				20,
				20,
				false);
		FadingUtils.fadeInBalloon(bt, null, 300, 24);
		FadingUtils.fadeOutBalloon(bt, null, 300, 24);
		ToolTipUtils.balloonToToolTip(bt, 300, 5000);
		SwingUtilities.updateComponentTreeUI(bt);
		//desktop.updateToolWindowLF();
	}

	public void createTableCellBalloonTip(JTable t, String s, int row, int column) {
		fillColor = t.getBackground();
		transparentFill = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 180);
		style = new ModernBalloonStyle(10, 10, transparentFill, transparentFill, Color.ORANGE);

		style.setBorderThickness(3);
		style.enableAntiAliasing(true);
		TableCellBalloonTip bt = new TableCellBalloonTip(
				t,
				new JLabel(s),
				row,
				column,
				style,
				BalloonTip.Orientation.LEFT_ABOVE,
				BalloonTip.AttachLocation.ALIGNED,
				20,
				20,
				false);
		FadingUtils.fadeInBalloon(bt, null, 300, 24);
		FadingUtils.fadeOutBalloon(bt, null, 300, 24);
		ToolTipUtils.balloonToToolTip(bt, 300, 5000);
		SwingUtilities.updateComponentTreeUI(bt);
		//desktop.updateToolWindowLF();
	}

	public void createBalloonTip(JComponent j, String s) {
		fillColor = j.getBackground();
		//System.out.println("fillColor is "+ fillColor);
		//fillColor = new Color(235, 235, 235);
		transparentFill = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), 180);
		style = new ModernBalloonStyle(10, 10, transparentFill, transparentFill, Color.ORANGE);
		//TexturedBalloonStyle style = null;
		//try {
		//	style = new TexturedBalloonStyle(10, 10, this.getClass().getResource("/net/java/balloontip/images/bgPattern.png"), Color.ORANGE);
		//} catch (IOException e) {e.printStackTrace();}
		style.setBorderThickness(3);
		style.enableAntiAliasing(true);
		BalloonTip t = new BalloonTip(
				j,
				new JLabel(s),
				style,
				BalloonTip.Orientation.LEFT_ABOVE,
				BalloonTip.AttachLocation.ALIGNED,
				30,
				10,
				false);

		//FadingUtils.fadeInBalloon(t, null, 300, 24); // not compatible with ToolTipUtils.balloonToToolTip()
		//FadingUtils.fadeOutBalloon(t, null, 300, 24); // not compatible with ToolTipUtils.balloonToToolTip()

		ToolTipUtils.balloonToToolTip(t, 300, 5000);
		SwingUtilities.updateComponentTreeUI(t);
		//desktop.updateToolWindowLF();
	}

}
