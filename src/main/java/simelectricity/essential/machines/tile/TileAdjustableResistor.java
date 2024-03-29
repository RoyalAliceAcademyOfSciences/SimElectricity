package simelectricity.essential.machines.tile;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rikka.librikka.blockentity.INamedMenuProvider;
import rikka.librikka.blockentity.ITickableBlockEntity;
import simelectricity.api.ISEEnergyNetUpdateHandler;
import simelectricity.api.components.ISEVoltageSource;
import simelectricity.essential.common.semachine.SESinglePortMachine;
import simelectricity.essential.machines.gui.ContainerAdjustableResistor;

public class TileAdjustableResistor extends SESinglePortMachine<ISEVoltageSource> implements
		ISEVoltageSource, ISEEnergyNetUpdateHandler, ITickableBlockEntity, INamedMenuProvider {
    public TileAdjustableResistor(BlockEntityType<?> beType, BlockPos pos, BlockState blockState) {
		super(beType, pos, blockState);
	}

	//Component parameters
    public double resistance = 100;

    //Calculated values
    public double voltage;
    public double current;
    public double powerLevel;
    public double bufferedEnergy;

    ///////////////////////////////////
    /// BlockEntity
    ///////////////////////////////////
    @Override
    public void tick() {
        if (this.level.isClientSide)
            return;

        this.bufferedEnergy += this.powerLevel / 20;
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);

        this.resistance = tagCompound.getDouble("resistance");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
    	nbt.putDouble("resistance", this.resistance);

        super.saveAdditional(nbt);
    }

    ///////////////////////////////////
    /// ISEVoltageSource
    ///////////////////////////////////
    @Override
    public double getOutputVoltage() {
        return 0;
    }

    @Override
    public double getResistance() {
        return this.resistance;
    }

    @Override
    public boolean isOn() {
        return true;
    }

    ///////////////////////////////////
    /// ISEEnergyNetUpdateHandler
    ///////////////////////////////////
    @Override
    public void onEnergyNetUpdate() {
        this.voltage = this.circuit.getVoltage();

        //Get the resistance (in the state) when the voltage is calculated
        double resistance = this.cachedParam.getResistance();
        this.current = this.voltage / resistance;
        this.powerLevel = this.voltage * this.current;
    }

    ///////////////////////////////////
    /// ISESocketProvider
    ///////////////////////////////////
    @Override
    @OnlyIn(Dist.CLIENT)
    public int getSocketIconIndex(Direction side) {
        return side == this.functionalSide ? 0 : -1;
    }

    ///////////////////////////////////
    /// MenuProvider
    ///////////////////////////////////
	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
		return new ContainerAdjustableResistor(this, windowId, player);
	}
}
