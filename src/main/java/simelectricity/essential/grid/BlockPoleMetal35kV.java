package simelectricity.essential.grid;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import rikka.librikka.DirHorizontal8;
import rikka.librikka.IMetaBase;
import rikka.librikka.block.BlockBase;
import rikka.librikka.item.ItemBlockBase;
import rikka.librikka.multiblock.BlockMapping;
import rikka.librikka.multiblock.IMultiBlockTile;
import rikka.librikka.multiblock.MultiBlockStructure;
import rikka.librikka.multiblock.MultiBlockTileInfo;
import simelectricity.api.SEAPI;
import simelectricity.api.tile.ISEGridTile;
import simelectricity.essential.api.ISEHVCableConnector;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.Item.Properties;

public class BlockPoleMetal35kV extends BlockBase implements EntityBlock, ISEHVCableConnector {
	public final MultiBlockStructure structureTemplate;
	public final MultiBlockStructure structureTemplate45;
	public final static EnumProperty<Type> propType = EnumProperty.create("type", Type.class);

	public static enum Type implements IMetaBase, StringRepresentable {
		pole,
		collisionbox_full,
		collisionbox_half,
		collisionbox_quarter,
		host;

		@Override
		public String getSerializedName() {
			return name();
		}
	}

	private BlockPoleMetal35kV(int type) {
		super("pole_metal_35kv_" + String.valueOf(type), BlockBehaviour.Properties.of(Material.STONE)
        		.strength(0.2F, 10.0F)
        		.sound(SoundType.METAL)
        		.isRedstoneConductor((a,b,c)->false),
        		ItemBlock.class,
        		(new Item.Properties()).tab(SEAPI.SETab));

		this.structureTemplate = this.createStructureTemplate();
		this.structureTemplate45 = this.createStructureTemplate45();
	}

	public static BlockPoleMetal35kV[] create() {
		return new BlockPoleMetal35kV[] {new BlockPoleMetal35kV(0), new BlockPoleMetal35kV(1)};
	}

	public final static Vec3i hostOffset = new Vec3i(5, 18, 2);
	public final static Vec3i hostOffset45 = new Vec3i(4, 18, 4);
    protected MultiBlockStructure createStructureTemplate() {
        //y,z,x facing NORTH(Z-), do not change
        BlockMapping[][][] configuration = new BlockMapping[19][][];

        BlockMapping p = blockMappingFromType(Type.pole);
        BlockMapping cf = blockMappingFromType(Type.collisionbox_full);
        BlockMapping cxn = blockMappingFromType(Type.collisionbox_half);
        BlockMapping cxp = cxn;
        BlockMapping czn = cxn;
        BlockMapping czp = cxn;
        BlockMapping cq = blockMappingFromType(Type.collisionbox_quarter);
        BlockMapping cw = cq;
        BlockMapping cs = cq;
        BlockMapping ca = cq;
        BlockMapping h = blockMappingFromType(Type.host);
        //  .-->x+ (East)
        //  |                           Facing/Looking at North(z-)
        // \|/
        //  z+ (South)
        configuration[0] = new BlockMapping[][]{
        	{null, null, null, p   , null, null, null, p   , null, null, null},
        	{null, null, null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null, null, null},
        	{null, null, null, p   , null, null, null, p   , null, null, null},
        };

        for (int i=1; i<18; i++)
        configuration[i] = new BlockMapping[][]{
        	{null, null, null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null, null, null},
        };

        configuration[18] = new BlockMapping[][]{
        	{null, null, null, null, null, null, null, null, null, null, null},
        	{cq  , czn , czn , czn , czn , czn , czn , czn , czn , czn , cw},
        	{cxn , cf  , cf  , cf  , cf  , h   , cf  , cf  , cf  , cf  , cxp},
        	{ca  , czp , czp , czp , czp , czp , czp , czp , czp , czp , cs},
        	{null, null, null, null, null, null, null, null, null, null, null},
        };


        return new MultiBlockStructure(configuration);
    }

    protected MultiBlockStructure createStructureTemplate45() {
        //y,z,x facing NORTH(Z-), do not change
        BlockMapping[][][] configuration = new BlockMapping[19][][];

        BlockMapping p = blockMappingFromType45(Type.pole);
        BlockMapping c = blockMappingFromType45(Type.collisionbox_full);
        BlockMapping h = blockMappingFromType45(Type.host);
        //  .-->x+ (East)
        //  |                           Facing/Looking at North(z-)
        // \|/
        //  z+ (South)
        configuration[0] = new BlockMapping[][]{
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, p   , null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, p   , null, null, null, null, null, p   , null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, p   , null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        };

        for (int i=1; i<18; i++)
        configuration[i] = new BlockMapping[][]{
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        	{null, null, null, null, null, null, null, null, null},
        };

        configuration[18] = new BlockMapping[][]{
        	{null, c   , null, null, null, null, null, null, null},
        	{c   , c   , c   , null, null, null, null, null, null},
        	{null, c   , c   , c   , null, null, null, null, null},
        	{null, null, c   , c   , c   , null, null, null, null},
        	{null, null, null, c   , h   , c   , null, null, null},
        	{null, null, null, null, c   , c   , c   , null, null},
        	{null, null, null, null, null, c   , c   , c   , null},
        	{null, null, null, null, null, null, c   , c   , c   },
        	{null, null, null, null, null, null, null, c   , null},
        };


        return new MultiBlockStructure(configuration);
    }

    private BlockMapping blockMappingFromType(Type type) {
    	BlockState toState = this.defaultBlockState().setValue(propType, type);
    	final Block blockThis = this;

    	return new BlockMapping(toState) {
    		@SuppressWarnings("deprecation")
			@Override
    	    protected boolean cancelPlacement(BlockState state) {
    			return !state.isAir();
    		}

    		@Override
    		protected boolean cancelRestore(BlockState state) {
    			return state.getBlock() != blockThis;
    		}

    		@Override
    	    protected BlockState getStateForPlacement(Direction facing) {
    	    	return super.getStateForPlacement(facing).setValue(DirHorizontal8.prop, DirHorizontal8.fromDirection4(facing));
    	    }
    	};
    }

    private BlockMapping blockMappingFromType45(Type type) {
    	BlockState toState = this.defaultBlockState().setValue(propType, type);
    	final Block blockThis = this;

    	return new BlockMapping(toState) {
    		@SuppressWarnings("deprecation")
			@Override
    	    protected boolean cancelPlacement(BlockState state) {
    			return !state.isAir();
    		}

    		@Override
    		protected boolean cancelRestore(BlockState state) {
    			return state.getBlock() != blockThis;
    		}

    		@Override
    	    protected BlockState getStateForPlacement(Direction facing) {
    	    	return super.getStateForPlacement(facing).setValue(DirHorizontal8.prop, DirHorizontal8.fromDirection4(facing).clockwise());
    	    }
    	};
    }

    public static DirHorizontal8 getFacing(BlockState blockstate) {
    	return blockstate.getValue(DirHorizontal8.prop);
    }


    /**
     * Standing at center, looking at facing
     * return	meaning (when is45 is true)<p>
     * 0		Upper-Right<p>
     * 1		Lower-Right<p>
     * 2		Lower-Left<p>
     * 3		Upper-Left<p>
     *
     * @param xOffset
     * @param zOffset
     * @param xCenter
     * @param zCenter
     * @return the relative position to the center block
     */
    public static int getPartId(int xOffset, int zOffset, int xCenter, int zCenter) {
    	int partId = -1;

		if (xOffset < xCenter) {
			if (zOffset < zCenter) {
				partId = 3;
			} else {
				partId = 2;
			}
		} else {
			if (zOffset < zCenter) {
				partId = 0;
			} else {
				partId = 1;
			}
		}

		return partId;
    }

    public static int getPartId45(int xOffset, int zOffset, int xCenter, int zCenter) {
    	int partId = -1;

		if (xOffset == xCenter) {
			if (zOffset < zCenter) {
				partId = 3;
			} else {
				partId = 1;
			}
		} else {
			if (xOffset < xCenter) {
				partId = 2;
			} else {
				partId = 0;
			}
		}

		return partId;
    }

    /**
     * @param blockstate
     * @param xOffset the x of a given block in the blueprint
     * @param zOffset the z of a given block in the blueprint
     * @param isOnAxis the default facing of the blueprint is N rather than NE
     * @return the relative position to the center block
     */
    public static int getPartId(DirHorizontal8 facing, int xOffset, int zOffset) {
		return facing.isOnAxis() ?
				getPartId(xOffset, zOffset, BlockPoleMetal35kV.hostOffset.getX(), BlockPoleMetal35kV.hostOffset.getZ()) :
				getPartId45(xOffset, zOffset, BlockPoleMetal35kV.hostOffset45.getX(), BlockPoleMetal35kV.hostOffset45.getZ());
    }


    @Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(DirHorizontal8.prop, propType);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		Type type = state.getValue(propType);
		if (type == Type.host)
			return new TilePoleMetal35kV(pos, state);
		else if (type == Type.pole)
			return new TilePoleMetal35kV.Bottom(pos, state);
		else
			return new TileMultiBlockPlaceHolder(pos, state);
	}

    @Override
    public boolean removedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te != null && !world.isClientSide) {
        	DirHorizontal8 facing = state.getValue(DirHorizontal8.prop);
        	if (facing.isOnAxis())
        		this.structureTemplate.restoreStructure(te, state, true);
        	else
        		this.structureTemplate45.restoreStructure(te, state, true);
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    ///////////////////
    /// BoundingBoxes
    ///////////////////
    private final static VoxelShape[] poleShapes = new VoxelShape[4];		// partId
    private final static VoxelShape[] poleShapes45 = new VoxelShape[4];		// partId
    private final static VoxelShape[] quarterCollShape = new VoxelShape[4];	// partId
    private final static VoxelShape[] halfCollShape = new VoxelShape[4];	// NESW
    private final static VoxelShape fullCollShape = Shapes.box(0, 0, 0, 1, 0.3, 1);
    private static void initPoleShapes() {
		double x1 = 0.4, y1 = 0, z1 = 0;
		double x2 = 1, y2 = 0.8, z2 = 0.6;

		poleShapes[0] = Shapes.box(x1, y1, z1, x2, y2, z2);
		poleShapes[1] = Shapes.box(x1, y1, 1 - z2, x2, y2, 1 - z1);
		poleShapes[2] = Shapes.box(1 - x2, y1, 1 - z2, 1 - x1, y2, 1 - z1);
		poleShapes[3] = Shapes.box(1 - x2, y1, z1, 1 - x1, y2, z2);
    }

    private static void initPoleShapes45() {
		double x1 = 0.4, y1 = 0, z1 = 0.2;
		double x2 = 1, y2 = 0.8, z2 = 0.8;

		poleShapes45[0] = Shapes.box(x1, y1, z1, x2, y2, z2);
		poleShapes45[1] = Shapes.box(z1, y1, x1, z2, y2, x2);
		poleShapes45[2] = Shapes.box(1 - x2, y1, 1 - z2, 1 - x1, y2, 1 - z1);
		poleShapes45[3] = Shapes.box(1 - z2, y1, 1 - x2, 1 - z1, y2, 1 - x1);
    }

    static {
    	initPoleShapes();
    	initPoleShapes45();

    	quarterCollShape[0] = Shapes.box(0, 0, 0.4, 0.6, 0.3, 1);
    	quarterCollShape[1] = Shapes.box(0, 0, 0, 0.6, 0.3, 0.6);
    	quarterCollShape[2] = Shapes.box(0.4, 0, 0, 1, 0.3, 0.6);
    	quarterCollShape[3] = Shapes.box(0.4, 0, 0.4, 1, 0.3, 1);

    	halfCollShape[0] = Shapes.box(0, 0, 0.4, 1, 0.3, 1);	//N
    	halfCollShape[1] = Shapes.box(0, 0, 0, 0.6, 0.3, 1);	//E
    	halfCollShape[2] = Shapes.box(0, 0, 0, 1, 0.3, 0.6);	//S
    	halfCollShape[3] = Shapes.box(0.4, 0, 0, 1, 0.3, 1);	//W
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
    	Type type = state.getValue(propType);
    	DirHorizontal8 facing = getFacing(state);
    	BlockEntity te = world.getBlockEntity(pos);

    	if (!(te instanceof IMultiBlockTile))
    		return Shapes.empty();

    	MultiBlockTileInfo mbInfo = ((IMultiBlockTile) te).getMultiBlockTileInfo();
    	BlockPos centerPos = mbInfo.getPartPos(facing.isOnAxis()?hostOffset:hostOffset45);
		int partId = getPartId(pos.getX(), pos.getZ(), centerPos.getX(), centerPos.getZ());

		VoxelShape vs = fullCollShape;
        if (type == Type.pole) {
        	if (facing.isOnAxis()) {
        		vs = poleShapes[partId];
        	} else {
        		vs = poleShapes45[partId];
        	}
        } else if (type == Type.collisionbox_quarter) {
        	vs = quarterCollShape[partId];
        } else if (type == Type.collisionbox_half) {
    		if (facing == DirHorizontal8.NORTH || facing == DirHorizontal8.SOUTH) {
    			if (pos.getZ() == centerPos.getZ()) {
    				if (pos.getX() < centerPos.getX())
    					vs = halfCollShape[3];	//W
    				else
    					vs = halfCollShape[1];	//E
    			} else {
        			if (partId == 0 || partId == 3)
    					vs = halfCollShape[0];	//N
    				else
    					vs = halfCollShape[2];	//S
    			}
    		}  else if (facing == DirHorizontal8.EAST || facing == DirHorizontal8.WEST) {
    			if (pos.getX() == centerPos.getX()) {
        			if (pos.getZ() < centerPos.getZ())
    					vs = halfCollShape[0];	//N
    				else
    					vs = halfCollShape[2];	//S
    			} else {
        			if (partId == 2 || partId == 3)
    					vs = halfCollShape[3];	//W
    				else
    					vs = halfCollShape[1];	//E
    			}
    		}
        }

        return vs;
    }

    ////////////////////////////////////
    /// Rendering
    ////////////////////////////////////
	@Override
	public RenderShape getRenderShape(BlockState state) {
		Type type = state.getValue(propType);
		return type==Type.pole||type==Type.host ? RenderShape.MODEL:RenderShape.INVISIBLE;
	}

    //////////////////////////////////////
    /// ISEHVCableConnector
    //////////////////////////////////////
    @Override
    public ISEGridTile getGridTile(Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);

        if (te instanceof ISEGridTile)
        	return (ISEGridTile) te;
        else if (te instanceof IMultiBlockTile) {
        	DirHorizontal8 facing = world.getBlockState(pos).getValue(DirHorizontal8.prop);
        	BlockPos hostPos = ((IMultiBlockTile) te).getMultiBlockTileInfo()
        			.getPartPos(facing.isOnAxis()?hostOffset:hostOffset45);
        	BlockEntity host = world.getBlockEntity(hostPos);

        	if (host instanceof ISEGridTile)
        		return (ISEGridTile) host;
        }

        return null;
    }

    //////////////////////////////////////
    /// BlockItem
    //////////////////////////////////////
    public static class ItemBlock extends ItemBlockBase {
		public ItemBlock(Block block, Properties props) {
			super(block, props);
		}

		@Override
		protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
			Level world = context.getLevel();
			BlockPos pos = context.getClickedPos();
			DirHorizontal8 facing = DirHorizontal8.fromSight(context.getPlayer());
			BlockPoleMetal35kV block = (BlockPoleMetal35kV) this.getBlock();

			MultiBlockStructure.Result result = null;
			if (facing.isOnAxis())
				result = block.structureTemplate.attempToBuild(world, pos, facing.toDirection4());
			else
				result = block.structureTemplate45.attempToBuild(world, pos, facing.anticlockwise().toDirection4());

			if (result == null)
				return false;

			if (!world.isClientSide)
				result.createStructure();
			return true;
		}
    }
}
