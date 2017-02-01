package simElectricity.Common.EnergyNet.Components;

import java.util.LinkedList;

import net.minecraft.tileentity.TileEntity;

import simElectricity.API.EnergyTile.ISESimulatable;

public abstract class SEComponent implements ISESimulatable{
	public LinkedList<SEComponent> neighbors = new LinkedList<SEComponent>();
	public TileEntity te;
	public boolean visited;
	public LinkedList<SEComponent> optimizedNeighbors = new LinkedList<SEComponent>();
	public LinkedList<Double> optimizedResistance = new LinkedList<Double>();
}