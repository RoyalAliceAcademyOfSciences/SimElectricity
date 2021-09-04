package simelectricity.essential.cable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataMap;
import rikka.librikka.MarkedBlockHitResult;
import simelectricity.api.ISEEnergyNetUpdateHandler;
import simelectricity.api.SEAPI;
import simelectricity.api.node.ISESimulatable;
import simelectricity.api.tile.ISECableTile;
import simelectricity.essential.api.ISEGenericCable;
import simelectricity.essential.api.ISEIuminousCoverPanelHost;
import simelectricity.essential.api.coverpanel.*;
import simelectricity.essential.common.CoverPanelUtils;
import simelectricity.essential.common.SEEnergyTile;

public class TileCable extends SEEnergyTile implements ISEGenericCable, ISEIuminousCoverPanelHost, ISECableTile, ISEEnergyNetUpdateHandler {
    public boolean emitRedstoneSignal;
    /**
     * Accessible from client
     */
    public byte lightLevel;
    private final ISESimulatable node = SEAPI.energyNetAgent.newCable(this, false);
    private int color;
    private double resistance = 10;
    private volatile double voltage;
    private final boolean[] connections = new boolean[6];
    private final ISECoverPanel[] installedCoverPanels = new ISECoverPanel[6];

    public TileCable(BlockEntityType<?> beType, BlockPos pos, BlockState blockState) {
		super(beType, pos, blockState);
	}

    ////////////////////////////////////////
    //Private functions
    ////////////////////////////////////////
    public void setResistanceOnPlace(double resistance) {
        this.resistance = resistance;
    }

    ////////////////////////////////////////
    //ISEGenericCable
    ////////////////////////////////////////
    @Override
    public void onRenderingUpdateRequested() {
        //Update connection
        Direction[] dirs = Direction.values();
        for (int i = 0; i < 6; i++) {
			this.connections[i] = SEAPI.energyNetAgent.canConnectTo(this, dirs[i]);
        }

        //Initiate Server->Client synchronization
		this.markTileEntityForS2CSync();
    }

    @Override
    public boolean connectedOnSide(Direction side) {
        return this.connections[side.ordinal()];
    }

    @Override
    public Direction getSelectedCoverPanel(Player player) {
        Block block = this.getBlockType();
        if (block instanceof BlockCable) {
        	MarkedBlockHitResult<Integer> result = ((BlockCable) block).rayTrace(this.level, this.worldPosition, player);

            if (result == null || result.getType() != MarkedBlockHitResult.Type.BLOCK)
            	return null;

            if (result.getBlockPos() != this.getBlockPos())
            	return null;	// The player is looking at somewhere else ???

            if (result.subHit < 7 || result.subHit > 12)
                return null;    //The player is not looking at any installed cover panel

            return Direction.from3DDataValue(result.subHit - 7);
        }
        return null;
    }

    @Override
    public ISECoverPanel getCoverPanelOnSide(Direction side) {
        return this.installedCoverPanels[side.ordinal()];
    }

    @Override
    public boolean installCoverPanel(Direction side, ISECoverPanel coverPanel, boolean simulated) {
    	if (this.installedCoverPanels[side.ordinal()] != null)
    		return false;

    	if (simulated)
    		return true;

		this.installedCoverPanels[side.ordinal()] = coverPanel;
        coverPanel.setHost(this, side);

        if (!coverPanel.isHollow()) {
            // If the cover panel is not hollow, it may block some connection
        	// The connection has to be check anyway, to keep the canConnection(side) record up to date
            SEAPI.energyNetAgent.updateTileConnection(this);
        }

        if (coverPanel instanceof ISEElectricalCoverPanel)
            ((ISEElectricalCoverPanel) coverPanel).onPlaced(this.voltage);

        if (coverPanel instanceof ISEElectricalLoadCoverPanel)
            SEAPI.energyNetAgent.updateTileConnection(this);

        level.updateNeighborsAt(worldPosition, getBlockType());

		this.onRenderingUpdateRequested();

		return true;
    }

    @Override
    public boolean removeCoverPanel(Direction side, boolean simulated) {
        if (side == null || this.installedCoverPanels[side.ordinal()] == null)
            return false;

        if (simulated)
        	return true;

        //Remove the panel
        ISECoverPanel coverPanel = this.installedCoverPanels[side.ordinal()];
		this.installedCoverPanels[side.ordinal()] = null;

        if (coverPanel instanceof ISEElectricalLoadCoverPanel || !coverPanel.isHollow())
            SEAPI.energyNetAgent.updateTileConnection(this);

		this.onLightValueUpdated();

		this.onRenderingUpdateRequested();

        level.updateNeighborsAt(worldPosition, getBlockType());

        return true;
    }

    ///////////////////////////////////////
    ///BlockEntity
    ///////////////////////////////////////
    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        AABB bb = BlockEntity.INFINITE_EXTENT_AABB;
        bb = new AABB(this.worldPosition, this.worldPosition.offset(1, 1, 1));
        return bb;
    }

    @Override
    public void load(CompoundTag tagCompound) {
        super.load(tagCompound);

		this.color = tagCompound.getInt("color");
		this.resistance = tagCompound.getDouble("resistance");
		CoverPanelUtils.coverPanelsFromNBT(this, tagCompound, this.installedCoverPanels);
    }

    @Override
    public CompoundTag save(CompoundTag tagCompound) {
        tagCompound.putInt("color", this.color);
        tagCompound.putDouble("resistance", this.resistance);
        CoverPanelUtils.coverPanelsToNBT(this, tagCompound);

        return super.save(tagCompound);
    }

    ///////////////////////////////////////
    ///ISECableTile
    ///////////////////////////////////////
    @Override
    public void setColor(int newColor) {
        this.color = newColor;
        this.updateTileConnection();
        this.onRenderingUpdateRequested();
        level.updateNeighborsAt(this.worldPosition, getBlockType());
    }

    @Override
    public int getColor() {
        return this.color;
    }

    @Override
    public double getResistance() {
        return this.resistance;
    }

    @Override
    public ISESimulatable getNode() {
        return this.node;
    }

    @Override
    public boolean canConnectOnSide(Direction direction) {
        ISECoverPanel coverPanel = this.getCoverPanelOnSide(direction);
        if (coverPanel == null)
            return true;
        else
            return coverPanel.isHollow();
    }

    @Override
    public boolean isGridLinkEnabled() {
        return false;
    }

    @Override
    public boolean hasShuntResistance() {
        boolean hasShuntResistance = false;
        for (ISECoverPanel coverPanel : installedCoverPanels)
            hasShuntResistance |= coverPanel instanceof ISEElectricalLoadCoverPanel;
        return hasShuntResistance;
    }

    @Override
    public double getShuntResistance() {
        double shuntConductance = 0;
        for (ISECoverPanel coverPanel : installedCoverPanels)
            if (coverPanel instanceof ISEElectricalLoadCoverPanel)
                shuntConductance += 1.0D / ((ISEElectricalLoadCoverPanel) coverPanel).getResistance();

        return 1.0D / shuntConductance;
    }

    ////////////////////////////////////////
    //Server->Client sync
    ////////////////////////////////////////
    @Override
    public void prepareS2CPacketData(CompoundTag nbt) {
        super.prepareS2CPacketData(nbt);

        byte bc = 0x00;
        if (this.connections[0]) bc |= 1;
        if (this.connections[1]) bc |= 2;
        if (this.connections[2]) bc |= 4;
        if (this.connections[3]) bc |= 8;
        if (this.connections[4]) bc |= 16;
        if (this.connections[5]) bc |= 32;

        nbt.putByte("connections", bc);
        nbt.putInt("color", color);

        CoverPanelUtils.coverPanelsToNBT(this, nbt);

        nbt.putByte("lightLevel", this.lightLevel);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onSyncDataFromServerArrived(CompoundTag nbt) {
        byte connectionsBinary = nbt.getByte("connections");

		connections[0] = (connectionsBinary & 1) > 0;
		connections[1] = (connectionsBinary & 2) > 0;
		connections[2] = (connectionsBinary & 4) > 0;
		connections[3] = (connectionsBinary & 8) > 0;
		connections[4] = (connectionsBinary & 16) > 0;
		connections[5] = (connectionsBinary & 32) > 0;
		this.color = nbt.getInt("color");

		CoverPanelUtils.coverPanelsFromNBT(this, nbt, this.installedCoverPanels);

        byte lightLevel = nbt.getByte("lightLevel");
        if (this.lightLevel != lightLevel) {
            this.lightLevel = lightLevel;
            //Detect change & proceed
			this.level.getLightEngine().checkBlock(this.worldPosition);
            //world.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);	//checkLightFor
        }

        // Flag 1 - update Rendering Only!
		this.markForRenderUpdate();
    }

    ////////////////////////////////////////
    //ISEEnergyNetUpdateHandler
    ////////////////////////////////////////
    @Override
    public void onEnergyNetUpdate() {
		this.voltage = this.node.getVoltage();

        for (ISECoverPanel coverPanel : installedCoverPanels) {
            if (coverPanel instanceof ISEElectricalCoverPanel)
                ((ISEElectricalCoverPanel) coverPanel).onEnergyNetUpdate(this.voltage);
        }
    }

    /////////////////////////////////
    ///ISEIuminousCoverPanelHost
    /////////////////////////////////
    @Override
    public void onLightValueUpdated() {
        byte lightLevel = 0;
        for (ISECoverPanel coverPanel : installedCoverPanels) {
            if (coverPanel instanceof ISEIuminousCoverPanel) {
                byte ll = ((ISEIuminousCoverPanel) coverPanel).getLightValue();
                if (ll > lightLevel)
                    lightLevel = ll;
            }
        }

        if (this.lightLevel != lightLevel) {
            this.lightLevel = lightLevel;
			markTileEntityForS2CSync();
        }
    }

    protected Block getBlockType() {
    	return this.getBlockState().getBlock();
    }

    @Override
    protected void collectModelData(ModelDataMap.Builder builder) {
    	builder.withInitial(ISEGenericCable.prop, this);
    }
}
