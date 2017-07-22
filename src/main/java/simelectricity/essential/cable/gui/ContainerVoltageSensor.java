package simelectricity.essential.cable.gui;

import java.util.Iterator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import simelectricity.essential.cable.VoltageSensorPanel;
import simelectricity.essential.utils.network.ISEButtonEventHandler;
import simelectricity.essential.utils.network.ISEContainerUpdate;
import simelectricity.essential.utils.network.MessageContainerSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ContainerVoltageSensor extends Container implements ISEContainerUpdate, ISEButtonEventHandler{
	public boolean emitRedstoneSignal;
	public boolean inverted;
	public double thresholdVoltage;
	
	private final VoltageSensorPanel panel;
	
	public ContainerVoltageSensor(VoltageSensorPanel panel, TileEntity te){
		this.panel = panel;
	}
	
	@Override
	public ItemStack slotClick(int p_75144_1_, int p_75144_2_, int p_75144_3_, EntityPlayer p_75144_4_){
		return null;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return true;
	}
	
	@Override
	public void detectAndSendChanges() {
		boolean emitRedstoneSignal = panel.emitRedStoneSignal;
		boolean inverted = panel.inverted;
		double thresholdVoltage = panel.thresholdVoltage;
		
		//Look for any changes
		if (this.emitRedstoneSignal == emitRedstoneSignal &&
			this.inverted == inverted &&
			this.thresholdVoltage == thresholdVoltage)
			return;
		
		this.emitRedstoneSignal = emitRedstoneSignal;
		this.inverted = inverted;
		this.thresholdVoltage = thresholdVoltage;
		
		//Send change to all crafter
    	Iterator<ICrafting> iterator = this.crafters.iterator();
    	while (iterator.hasNext()) {
    		ICrafting crafter = iterator.next();
    		
    		if (crafter instanceof EntityPlayerMP){
    			MessageContainerSync.sendToClient((EntityPlayerMP)crafter, emitRedstoneSignal, inverted, thresholdVoltage);
    		}
    	}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onDataArrivedFromServer(Object[] data) {
		emitRedstoneSignal = (Boolean) data[0];
		inverted = (Boolean) data[1];
		thresholdVoltage = (Double) data[2];
	}
	
	@Override
	public void onButtonPressed(int buttonID, boolean isCtrlPressed) {
		double thresholdVoltage = this.thresholdVoltage;
		boolean inverted = this.inverted;
		
		switch (buttonID){
		case 0:
			thresholdVoltage -= 100;
			break;
		case 1:
			thresholdVoltage -= 10;
			break;
		case 2:
			if (isCtrlPressed)
				thresholdVoltage -= 0.1;
			else
				thresholdVoltage -= 1;
			break;
		case 3:
			if (isCtrlPressed)
				thresholdVoltage += 0.1;
			else
				thresholdVoltage += 1;
			break;
		case 4:
			thresholdVoltage += 10;
			break;
		case 5:
			thresholdVoltage += 100;
			break;
			
		case 6:
			inverted = !inverted;
			break;
		}
		
        if (thresholdVoltage < 0.1)
        	thresholdVoltage = 0.1;
        if (thresholdVoltage > 500)
        	thresholdVoltage = 500;
        
        this.panel.thresholdVoltage = thresholdVoltage;
        this.panel.inverted = inverted;
        
        this.panel.checkRedStoneSignal();
	}
}
