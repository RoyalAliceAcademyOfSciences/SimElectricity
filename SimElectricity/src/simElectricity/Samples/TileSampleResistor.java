package simElectricity.Samples;

import net.minecraftforge.common.ForgeDirection;
import simElectricity.API.IEnergyTile;

public class TileSampleResistor extends TileSampleBaseComponent implements
		IEnergyTile {

	@Override
	public int getResistance() {
		return 10000;
	}

	@Override
	public void onOverloaded() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMaxPowerDissipation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getMaxSafeVoltage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onOverVoltage() {
		// TODO Auto-generated method stub

	}

	@Override
	public float getOutputVoltage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ForgeDirection getFunctionalSide() {
		// TODO Auto-generated method stub
		return null;
	}
}
