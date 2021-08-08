package simelectricity.essential;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.IForgeRegistry;
import rikka.librikka.IMetaProvider;
import rikka.librikka.ITileMeta;
import rikka.librikka.block.BlockBase;
import rikka.librikka.container.ContainerHelper;
import rikka.librikka.tileentity.TileEntityHelper;
import simelectricity.essential.api.SEEAPI;
import simelectricity.essential.cable.*;
import simelectricity.essential.coverpanel.ContainerVoltageSensor;
import simelectricity.essential.grid.*;
import simelectricity.essential.grid.transformer.BlockDistributionTransformer;
import simelectricity.essential.grid.transformer.BlockPowerTransformer;
import simelectricity.essential.grid.transformer.TileDistributionTransformer;
import simelectricity.essential.grid.transformer.TilePowerTransformerPlaceHolder;
import simelectricity.essential.grid.transformer.TilePowerTransformerWinding;
import simelectricity.essential.machines.*;
import simelectricity.essential.machines.gui.*;

public class BlockRegistry {    
	public final static List<Class<? extends AbstractContainerMenu>> registeredGuiContainers = new LinkedList<>();
	private final static Set<Item> blockItems = new LinkedHashSet<>();
	
	public static BlockCable[] blockCable;
	public static BlockWire[] blockWire;

	public static BlockPoleMetal35kV[] metalPole35kV;
	public static BlockPoleConcrete35kV[] concretePole35kV;
    public static BlockCableJoint[] cableJoint;
    public static BlockPoleConcrete[] concretePole;
    public static BlockPowerTransformer[] powerTransformer;
    public static BlockDistributionTransformer[] distributionTransformer;

	public static BlockElectronics[] blockElectronics;
    public static BlockTwoPortElectronics[] blockTwoPortElectronics;

    public static void initBlocks() {
        BlockRegistry.blockCable = BlockCable.create();
        BlockRegistry.blockWire = BlockWire.create();

        BlockRegistry.cableJoint = BlockCableJoint.create();
        BlockRegistry.metalPole35kV = BlockPoleMetal35kV.create();
        BlockRegistry.concretePole35kV = BlockPoleConcrete35kV.create();
        BlockRegistry.concretePole = BlockPoleConcrete.create();
        
		BlockRegistry.powerTransformer = BlockPowerTransformer.create();
		BlockPowerTransformer.createBluePrint();
		BlockRegistry.distributionTransformer = BlockDistributionTransformer.create();
		BlockDistributionTransformer.createBluePrint();

		BlockRegistry.blockElectronics = BlockElectronics.create();
		BlockRegistry.blockTwoPortElectronics = BlockTwoPortElectronics.create();
    }
    
    public static void registerBlocks(final IForgeRegistry<Block> registry) {
    	registerBlocks(registry, blockCable);
    	registerBlocks(registry, blockWire);
    	SEEAPI.coverPanelRegistry.registerColoredFacadeHost(blockCable);
    	
    	registerBlocks(registry, metalPole35kV);
    	registerBlocks(registry, concretePole35kV);
    	registerBlocks(registry, concretePole);
    	registerBlocks(registry, cableJoint);
    	registerBlocks(registry, powerTransformer);
    	registerBlocks(registry, distributionTransformer);
    	
    	registerBlocks(registry, blockElectronics);
    	SEEAPI.coverPanelRegistry.registerColoredFacadeHost(blockElectronics);
    	registerBlocks(registry, blockTwoPortElectronics);
    	SEEAPI.coverPanelRegistry.registerColoredFacadeHost(blockTwoPortElectronics);
    }
    
    public static void registerBlockItems(final IForgeRegistry<Item> registry) {
    	blockItems.forEach(registry::register);
    }

    public static void registerTileEntities(final IForgeRegistry<BlockEntityType<?>> registry) {    	
    	TileEntityHelper.registerTileEntity(registry, TileCable.class, blockCable);
    	TileEntityHelper.registerTileEntity(registry, TileWire.class, blockWire);
    	
    	TileEntityHelper.registerTileEntity(registry, TileMultiBlockPlaceHolder.class, 
    			makeBlockArray(concretePole35kV, metalPole35kV, distributionTransformer));
    	TileEntityHelper.registerTileEntity(registry, TilePoleMetal35kV.class, metalPole35kV);
    	TileEntityHelper.registerTileEntity(registry, TilePoleMetal35kV.Bottom.class, metalPole35kV);
    	TileEntityHelper.registerTileEntity(registry, TilePoleConcrete35kV.class, concretePole35kV);

    	TileEntityHelper.registerTileEntity(registry, TilePoleConcrete.Pole10Kv.Type0.class, concretePole);
    	TileEntityHelper.registerTileEntity(registry, TilePoleConcrete.Pole10Kv.Type1.class, concretePole);
    	TileEntityHelper.registerTileEntity(registry, TilePoleConcrete.Pole415vType0.class, concretePole);
    	TileEntityHelper.registerTileEntity(registry, TilePoleBranch.Type10kV.class, concretePole);
    	TileEntityHelper.registerTileEntity(registry, TilePoleBranch.Type415V.class, concretePole);

    	TileEntityHelper.registerTileEntity(registry, TileCableJoint.Type10kV.class, cableJoint[BlockCableJoint.Type._10kv.ordinal()]);
    	TileEntityHelper.registerTileEntity(registry, TileCableJoint.Type415V.class, cableJoint[BlockCableJoint.Type._415v.ordinal()]);


    	TileEntityHelper.registerTileEntity(registry, TilePowerTransformerPlaceHolder.class, powerTransformer);
    	TileEntityHelper.registerTileEntity(registry, TilePowerTransformerPlaceHolder.Primary.class, powerTransformer);
    	TileEntityHelper.registerTileEntity(registry, TilePowerTransformerPlaceHolder.Secondary.class, powerTransformer);
    	TileEntityHelper.registerTileEntity(registry, TilePowerTransformerPlaceHolder.Render.class, powerTransformer);
    	TileEntityHelper.registerTileEntity(registry, TilePowerTransformerWinding.Primary.class, powerTransformer);
    	TileEntityHelper.registerTileEntity(registry, TilePowerTransformerWinding.Secondary.class, powerTransformer);

    	TileEntityHelper.registerTileEntity(registry, TileDistributionTransformer.Pole10kV.class, distributionTransformer);
    	TileEntityHelper.registerTileEntity(registry, TileDistributionTransformer.Pole415V.class, distributionTransformer);


    	
    	RegisterTEs(registry, blockElectronics);
    	RegisterTEs(registry, blockTwoPortElectronics);
    }
    
    public static void registerContainers(final IForgeRegistry<MenuType<?>> registry) {
    	registerGuiContainer(registry, ContainerVoltageMeter.class);
    	registerGuiContainer(registry, ContainerQuantumGenerator.class);
    	registerGuiContainer(registry, ContainerAdjustableResistor.class);
    	registerGuiContainer(registry, ContainerElectricFurnace.class);
    	registerGuiContainer(registry, ContainerSE2RF.class);
    	registerGuiContainer(registry, ContainerRF2SE.class);
    	
    	registerGuiContainer(registry, ContainerAdjustableTransformer.class);
    	registerGuiContainer(registry, ContainerCurrentSensor.class);
    	registerGuiContainer(registry, ContainerDiode.class);
    	registerGuiContainer(registry, ContainerSwitch.class);
    	registerGuiContainer(registry, ContainerRelay.class);
    	registerGuiContainer(registry, ContainerPowerMeter.class);
    	
    	registerGuiContainer(registry, ContainerVoltageSensor.class);
    }
    
    
    private static void registerBlocks(IForgeRegistry<Block> registry, BlockBase... blocks) {
    	registry.registerAll(blocks);

    	for (BlockBase block: blocks)
    		blockItems.add(block.asItem());
    }
    
    private static <T extends Block&IMetaProvider<ITileMeta>> void RegisterTEs(IForgeRegistry<BlockEntityType<?>> registry, T[] blocks) {
    	for (T block: blocks) {
    		TileEntityHelper.registerTileEntity(registry, block.meta().teCls(), block);
    	}
	}
    
    private static void registerGuiContainer(final IForgeRegistry<MenuType<?>> registry, Class<? extends AbstractContainerMenu> containerCls) {
    	ContainerHelper.register(registry, containerCls);
    	registeredGuiContainers.add(containerCls);
    }
    
    private static Block[] makeBlockArray(Block[]... blockArrays) {
    	int size = 0;
    	for (int i=0; i<blockArrays.length; i++)
    		size += blockArrays[i].length;

    	int k=0;
    	Block[] blocks = new Block[size];
    	for (int i=0; i<blockArrays.length; i++) {
    		for (int j=0; j<blockArrays[i].length; j++) {
    			blocks[k] = blockArrays[i][j];
    			k++;
    		}
    	}
    	
    	return blocks;
    }
}
