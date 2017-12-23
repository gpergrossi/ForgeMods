package com.gpergrossi.aerogen.generator.regions.features.river;

import java.util.ArrayList;
import java.util.List;

import com.gpergrossi.aerogen.generator.islands.IslandCell;
import com.gpergrossi.util.geom.shapes.LineSeg;

public class River {
		
	List<RiverCell> cells;
	List<RiverWaterfall> waterfalls;
	
	public River() {
		this.cells = new ArrayList<>();
		this.waterfalls = new ArrayList<>();
	}

	public RiverCell addCell(IslandCell isleCell) {
		RiverCell cell = new RiverCell(this, isleCell);
		isleCell.addRiverCell(cell);
		int index = cells.size();
		cell.setIndex(index);
		cells.add(index, cell);
		return cell;
	}
	
	public RiverCell addWaterfall(LineSeg edge, IslandCell destination) {
		RiverCell previousCell = cells.get(cells.size()-1);
		RiverCell currentCell = addCell(destination);
		RiverWaterfall waterfall = new RiverWaterfall(this, previousCell, currentCell, edge);
		if (previousCell.waterfallOut != null) throw new IllegalStateException("River cells can only have one waterfall!");
		previousCell.waterfallOut = waterfall;
		currentCell.waterfallIn = waterfall;
		waterfalls.add(waterfall);
		return currentCell;
	}
	
	public List<RiverCell> getCells() {
		return cells;
	}

	public RiverCell getHead() {
		return getCell(0);
	}
	
	public RiverCell getTail() {
		return getCell(cells.size()-1);
	}
	
	public RiverCell getCell(int i) {
		if (i < 0 || i >= cells.size()) return null;
		return cells.get(i);
	}

	public int numCells() {
		return cells.size();
	}

	public List<RiverWaterfall> getWaterfalls() {
		return waterfalls;
	}

}
