package simelectricity.energynet.components;

import net.minecraft.tileentity.TileEntity;
import simelectricity.api.components.ISESwitchData;
import simelectricity.api.node.ISESubComponent;

public class SwitchA extends SEComponent.Tile<ISESwitchData> implements ISESubComponent{
	public boolean isOn;
	public double resistance;
	
	public SwitchB B;
	
	public SwitchA(ISESwitchData dataProvider, TileEntity te) {
		super(dataProvider, te);
		this.B = new SwitchB(this, te);
	}

	@Override
	public ISESubComponent getComplement() {
		return B;
	}

	@Override
	public void updateComponentParameters() {
		this.isOn = dataProvider.isOn();
		this.resistance = dataProvider.getResistance();
	}
}
