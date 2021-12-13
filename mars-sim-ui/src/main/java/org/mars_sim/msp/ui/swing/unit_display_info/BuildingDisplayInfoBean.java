package org.mars_sim.msp.ui.swing.unit_display_info;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Icon;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.ImageLoader;

public class BuildingDisplayInfoBean implements UnitDisplayInfo {

	private Icon buttonIcon;
	
	public BuildingDisplayInfoBean() {
		// Needs changing
        buttonIcon = ImageLoader.getIcon("SettlementIcon");
	}

	@Override
	public boolean isMapDisplayed(Unit unit) {
		return false;
	}

	@Override
	public Icon getSurfMapIcon(Unit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Icon getTopoMapIcon(Unit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Icon getGeologyMapIcon(Unit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isMapBlink(Unit unit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Color getSurfMapLabelColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getTopoMapLabelColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getGeologyMapLabelColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font getMapLabelFont() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getMapClickRange() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isGlobeDisplayed(Unit unit) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Color getSurfGlobeColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getTopoGlobeColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getGeologyGlobeColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Icon getButtonIcon(Unit unit) {
		return buttonIcon;
	}

	@Override
	public String getSound(Unit unit) {
		// TODO Auto-generated method stub
		return null;
	}

}
