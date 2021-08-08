package simelectricity.essential.client.grid.transformer;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import rikka.librikka.math.Vec3f;
import rikka.librikka.model.CodeBasedModel;
import rikka.librikka.model.loader.EasyTextureLoader;
import rikka.librikka.model.quadbuilder.RawQuadCube;
import rikka.librikka.model.quadbuilder.RawQuadGroup;
import rikka.librikka.multiblock.IMultiBlockTile;
import rikka.librikka.multiblock.MultiBlockTileInfo;
import simelectricity.essential.client.ResourcePaths;
import simelectricity.essential.client.grid.PowerCableBakery;
import simelectricity.essential.client.grid.pole.Models;
import simelectricity.essential.grid.transformer.BlockDistributionTransformer;
import simelectricity.essential.grid.transformer.EnumDistributionTransformerRenderPart;

@OnlyIn(Dist.CLIENT)
public class DistributionTransformerFormedModel extends CodeBasedModel {
	public final static DistributionTransformerFormedModel instance = new DistributionTransformerFormedModel();
	public final static NoBaking instanceNoBaking = new NoBaking();

	@EasyTextureLoader.Mark(ResourcePaths.hv_cable)
	private final TextureAtlasSprite textureCable = null;

    @EasyTextureLoader.Mark("sime_essential:render/distribution/transformer_heatsink_front_back")
    private final TextureAtlasSprite textureHeatSink = null;
    @EasyTextureLoader.Mark("sime_essential:render/distribution/transformer_front_back")
    private final TextureAtlasSprite textureTransformerFrontBack = null;
    @EasyTextureLoader.Mark("sime_essential:render/distribution/transformer_side")
    private final TextureAtlasSprite textureTransformerSide = null;   
    
    @EasyTextureLoader.Mark(ResourcePaths.metal)
    private final TextureAtlasSprite textureMetal = null;
    @EasyTextureLoader.Mark(ResourcePaths.glass_insulator)
    private final TextureAtlasSprite textureInsulator = null;
    @EasyTextureLoader.Mark(ResourcePaths.concrete)
    private final TextureAtlasSprite textureConcrete = null;
    @EasyTextureLoader.Mark(ResourcePaths.ceramic_insulator)
    private final TextureAtlasSprite textureCeramic = null;   
    
    @SuppressWarnings("unchecked")
	private final List<BakedQuad>[][] quads = new List[EnumDistributionTransformerRenderPart.values().length][4];
	
    private DistributionTransformerFormedModel() {
    	for (EnumDistributionTransformerRenderPart part: EnumDistributionTransformerRenderPart.values()) {
    		for (int i=2; i<Direction.values().length; i++) {
    			this.quads[part.ordinal()][i-2] = new LinkedList<>();
    		}
    	}
    }

    protected boolean skipLegacyTextureRegistration() {
    	return true;
    }

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
    	if (side != null)
            return emptyQuadList;

    	IMultiBlockTile te = extraData.getData(IMultiBlockTile.prop);
    	if (te == null)
            return emptyQuadList;

    	MultiBlockTileInfo mbInfo = te.getMultiBlockTileInfo();
    	if (mbInfo == null)
            return emptyQuadList;
    		
    	EnumDistributionTransformerRenderPart part = mbInfo.lookup( BlockDistributionTransformer.renderParts);
    	Direction facing = mbInfo.facing;
    	
    	if (part == null || facing == null)
            return emptyQuadList;
    	
		return quads[part.ordinal()][facing.ordinal()-2];
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return textureConcrete;
	}

	@Override
	protected void bake(Function<ResourceLocation, TextureAtlasSprite> registry) {
        for (EnumDistributionTransformerRenderPart part: EnumDistributionTransformerRenderPart.values()) {
        	for (int i=2; i<Direction.values().length; i++) {
        		Direction facing = Direction.from3DDataValue(i);
        		quads[part.ordinal()][i-2].clear();
        		bakePart(part, facing, quads[part.ordinal()][i-2]);
        	}
        }
	}
	
	private void bakePart(EnumDistributionTransformerRenderPart part, Direction facing, List<BakedQuad> list) {
		RawQuadGroup insulator = null;
		RawQuadGroup model = new RawQuadGroup();
		switch (part) {
		case AuxLeft:
			insulator = Models.render10kVInsulatorSmall(textureMetal, textureInsulator);
			//10Kv insulator and cable
			model.merge(insulator.clone().translateCoord(-0.425F, 0.6F, 0.15F));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.425F,0.85F, 0.15F), new Vec3f(-0.425F,0.85F,2.85F), false, 0.1F, 0.03F, textureCable));
			model.merge(insulator.clone().translateCoord(-0.775F, 0.6F, 0.15F));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.775F,0.85F, 0.15F), new Vec3f(-0.775F,0.85F,2.85F), false, 0.1F, 0.03F, textureCable));
			model.merge(insulator.clone().translateCoord(-1.125F, 0.6F, 0.15F));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-1.125F,0.85F, 0.15F), new Vec3f(-1.125F,0.85F,2.85F), false, 0.1F, 0.03F, textureCable));
            
            model.add((new RawQuadCube(2.3F, 0.1F, 0.05F, textureMetal)).translateCoord(-0.275F, 0.5F, 0.15F));
            
            model.add((new RawQuadCube(0.1F, 0.05F, 1.625F, textureMetal)).translateCoord(0.5F, 0.6F, 0.6875F));
            model.add((new RawQuadCube(0.1F, 0.05F, 1.625F, textureMetal)).translateCoord(0.8F, 0.6F, 0.6875F));

            //Switch
			model.merge(Models.render415VSwitch(textureMetal, textureCeramic).translateCoord(0.5F, 0.4F, 1.25F));
			model.merge(Models.render415VSwitch(textureMetal, textureCeramic).translateCoord(0.5F, 0.4F, 0.75F));
			
			//Transformer cable
			model.merge(PowerCableBakery.renderParabolicCable(new Object[]{
					new Vec3f(0.8F, 0.375F, 0.75F),
					0.15F, new Vec3f(1.1F, 0.5F, 0.75F),
					-0.1F, new Vec3f(0.9F, 2.2F, 0.75F)}, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Object[]{
					new Vec3f(0.8F, 0.375F, 1.25F),
					0.15F, new Vec3f(1.1F, 0.5F, 1.25F),
					-0.1F, new Vec3f(0.8F, 1.25F, 1.25F),
					0.1F, new Vec3f(0.45F, 2.15F, 1.25F) }, 0.03F, textureCable));
            
            model.add(new RawQuadCube(0.25F, 1, 0.25F, textureConcrete));
			break;
		case AuxMiddle:
			insulator = Models.render10kVInsulatorSmall(textureMetal, textureInsulator);
			//10Kv insulator and cable
			model.merge(insulator.clone().translateCoord(-0.425F, 0.6F, -0.15F));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.425F,0.85F, -0.15F), new Vec3f(-0.425F,0.85F, 1.85F), false, 0.1F, 0.03F, textureCable));
			model.merge(insulator.clone().translateCoord(-0.775F, 0.6F, -0.15F));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.775F,0.85F, -0.15F), new Vec3f(-0.775F,0.85F, 1.85F), false, 0.1F, 0.03F, textureCable));
			model.merge(insulator.clone().translateCoord(-1.125F, 0.6F, -0.15F));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-1.125F,0.85F, -0.15F), new Vec3f(-1.125F,0.85F, 1.85F), false, 0.1F, 0.03F, textureCable));
            
            model.add((new RawQuadCube(2.3F, 0.1F, 0.05F, textureMetal)).translateCoord(-0.275F, 0.5F, -0.15F));
            
            model.add((new RawQuadCube(0.1F, 0.05F, 1.625F, textureMetal)).translateCoord(0.5F, 0.6F, -0.6875F));
            model.add((new RawQuadCube(0.1F, 0.05F, 1.625F, textureMetal)).translateCoord(0.8F, 0.6F, -0.6875F));
            
            //Switch
			model.merge(Models.render415VSwitch(textureMetal, textureCeramic).translateCoord(0.5F, 0.4F, -0.75F));
			model.merge(Models.render415VSwitch(textureMetal, textureCeramic).translateCoord(0.5F, 0.4F, -1.25F));
			
			//Transformer cable
			model.merge(PowerCableBakery.renderParabolicCable(new Object[]{
					new Vec3f(0.8F, 0.375F, -0.75F),
					0.08F, new Vec3f(1.1F, 0.5F, -0.75F),
					-0.15F, new Vec3f(0.8F, 1.25F, -0.75F),
					-0.05F, new Vec3f(0.2F, 1.4F, -0.75F),
					0.2F, new Vec3f(-0.9F, 2.1F, -0.75F)}, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Object[]{
					new Vec3f(0.8F, 0.375F, -1.25F),
					0.08F, new Vec3f(1.1F, 0.5F, -1.25F),
					-0.15F, new Vec3f(0.8F, 1.25F, -1.25F),
					-0.05F, new Vec3f(0.2F, 1.4F, -1.25F),
					0.25F, new Vec3f(-0.445F, 2.1F, -1.25F)}, 0.03F, textureCable));
            
            model.add(new RawQuadCube(0.25F, 1, 0.25F, textureConcrete));
			break;
		case AuxRight:
			insulator = Models.render10kVInsulatorSmall(textureMetal, textureInsulator);
			model.merge(insulator.clone().translateCoord(-0.425F, 0.6F, -0.15F));
			model.merge(insulator.clone().translateCoord(-0.775F, 0.6F, -0.15F));
			model.merge(insulator.clone().translateCoord(-1.125F, 0.6F, -0.15F));
			
            model.add((new RawQuadCube(1.5F, 0.1F, 0.05F, textureMetal)).translateCoord(-0.625F, 0.5F, 0.15F));
            model.add((new RawQuadCube(1.5F, 0.1F, 0.05F, textureMetal)).translateCoord(-0.625F, 0.5F, -0.15F));
            model.add((new RawQuadCube(0.05F, 0.1F, 1.4F, textureMetal)).translateCoord(-1.4F, 0.5F, 0));
			
            
            
            //Cable
			model.merge(Models.render10kVInsulatorSmall(textureMetal, textureInsulator).rotateAroundX(180).translateCoord(-0.45F, 0.55F, 0.15F));
			model.merge(Models.render10kVInsulatorSmall(textureMetal, textureInsulator).rotateAroundX(180).translateCoord(-0.85F, 0.55F, -0.15F));
			
			model.merge(PowerCableBakery.renderParabolicCable(new Object[] {
					new Vec3f(-1.5F, 0.275F, -0.65F),
					0.15F, new Vec3f(-1.125F, 0.775F, -0.65F)}, 0.03F, textureCable));
			
			model.merge(PowerCableBakery.renderParabolicCable(new Object[] {
					new Vec3f(-1.5F, 0.275F, 0),
					0.1F, new Vec3f(-0.85F, 0.3F, -0.15F),
					0.2F, new Vec3f(-0.775F, 0.775F, -0.65F)}, 0.03F, textureCable));
			
			model.merge(PowerCableBakery.renderParabolicCable(new Object[] {
					new Vec3f(-1.5F, 0.275F, 0.65F),
					0.1F, new Vec3f(-0.45F, 0.3F, 0.15F),
					0.2F, new Vec3f(-0.425F, 0.775F, -0.65F)}, 0.03F, textureCable));
            
			//Cable(upwards)
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-1.725F, 0.7F, 0.65F), new Vec3f(-1.65F,3.8F,0.65F), false, 0, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-1.725F, 0.7F, 0), new Vec3f(-1.65F,3.8F,0), false, 0, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-1.725F, 0.7F, -0.65F), new Vec3f(-1.65F,3.8F,-0.65F), false, 0, 0.03F, textureCable));
			
			//Fuse
			RawQuadGroup fuse = Models.render10kVSwitch(textureMetal, textureCeramic);
			fuse.rotateAroundZ(25).translateCoord(0, -0.2F, 0);
			model.merge(fuse.clone().translateCoord(-1.4F, 0.55F, 0.65F));
			model.merge(fuse.clone().translateCoord(-1.4F, 0.55F, 0));
			model.merge(fuse.clone().translateCoord(-1.4F, 0.55F, -0.65F));
            
			//Pole
            model.add(new RawQuadCube(0.25F, 1, 0.25F, textureConcrete));
			break;
			
		case Pole10kVLeft:
			insulator = Models.render10kVInsulator(textureMetal, textureInsulator);
            model.add(new RawQuadCube(0.15F, 0.1F, 1.6F, textureMetal));
            model.merge(insulator.clone().translateCoord(0, 0.05F, -0.74F));
            model.merge(insulator.clone().translateCoord(0, 0.05F, 0.74F));
            model.merge(insulator.clone().translateCoord(0, 1F, 0));
            model.rotateAroundY(90);
            
            //Cable
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.74F, 0.55F, 0), new Vec3f(-0.74F, 0.55F, 5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0 , 1.5F, 0), new Vec3f(0F, 1.5F, 5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0.74F, 0.55F, 0), new Vec3f(0.74F, 0.55F, 5F), true, 0.2F, 0.05F, textureCable));

            //Pole
            model.add(new RawQuadCube(0.25F, 1, 0.25F, textureConcrete));
			break;
			
		case Pole10kVRight:		
            insulator = Models.render10kVInsulator(textureMetal, textureInsulator);
            model.add(new RawQuadCube(0.15F, 0.1F, 1.6F, textureMetal));
            model.merge(insulator.clone().translateCoord(0, 0.05F, -0.74F));
            model.merge(insulator.clone().translateCoord(0, 0.05F, 0.74F));
            model.merge(insulator.clone().translateCoord(0, 1F, 0));
            model.rotateAroundY(90);
            
            //Insulator
			insulator = Models.render10kVInsulatorSmall(textureMetal, textureInsulator);
			insulator.rotateAroundZ(90);
			model.merge(insulator.clone().translateCoord(-1.4F, -0.2F, 0.65F));
			model.merge(insulator.clone().translateCoord(-1.4F, -0.2F, 0));
			model.merge(insulator.clone().translateCoord(-1.4F, -0.2F, -0.65F));
            
            model.add((new RawQuadCube(1.5F, 0.1F, 0.05F, textureMetal)).translateCoord(-0.625F, -0.25F, 0.15F));
            model.add((new RawQuadCube(1.5F, 0.1F, 0.05F, textureMetal)).translateCoord(-0.625F, -0.25F, -0.15F));
            model.add((new RawQuadCube(0.05F, 0.1F, 1.4F, textureMetal)).translateCoord(-1.4F, -0.25F, 0));
            
            //Cable
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.74F, 0.55F, 0), new Vec3f(-0.74F, 0.55F, -5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0 , 1.5F, 0), new Vec3f(0F, 1.5F, -5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0.74F, 0.55F, 0), new Vec3f(0.74F, 0.55F, -5F), true, 0.2F, 0.05F, textureCable));
            
			//Transformer Connection
			model.merge(Models.render10kVInsulatorSmall(textureMetal, textureInsulator).rotateAroundX(180).translateCoord(-1, -0.2F, -0.15F));
			model.merge(Models.render10kVInsulatorSmall(textureMetal, textureInsulator).rotateAroundX(270).translateCoord(0, -0.2F, -0.125F));
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-1.65F,-0.2F,0.65F), new Vec3f(-0.74F, 0.55F, 0), false, -0.3F, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Object[] {
					new Vec3f(-1.65F,-0.4F, 0),
					0.025F, new Vec3f(-1F,-0.45F, -0.15F),
					0.1F, new Vec3f(0, -0.2F, -0.375F),
					0.1F, new Vec3f(0, 1.4F, -0.6F)}, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-1.65F,-0.4F, -0.65F), new Vec3f(0.74F, 0.45F, -0.8F), false, 0.4F, 0.03F, textureCable));
            
            //Pole
            model.add(new RawQuadCube(0.25F, 1, 0.25F, textureConcrete));
			break;
			
		case Pole415VLeft:
            insulator = Models.render415VInsulator(textureMetal, textureInsulator);
            model.add(new RawQuadCube(0.15F, 0.1F, 1.94F, textureMetal));
            model.merge(insulator.clone().translateCoord(0, 0.05F, -0.9F));
            model.merge(insulator.clone().translateCoord(0, 0.05F, -0.45F));
            model.merge(insulator.clone().translateCoord(0, 0.05F, 0.45F));
            model.merge(insulator.clone().translateCoord(0, 0.05F, 0.9F));
            model.rotateAroundY(90);
            
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.9F, 0.3F, 0), new Vec3f(-0.9F, 0.3F, 5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.45F, 0.3F, 0), new Vec3f(-0.45F, 0.3F, 5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0.45F, 0.3F, 0), new Vec3f(0.45F, 0.3F, 5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0.9F, 0.3F, 0), new Vec3f(0.9F, 0.3F, 5F), true, 0.2F, 0.05F, textureCable));
            
            //Pole
            model.add(new RawQuadCube(0.25F, 1, 0.25F, textureConcrete));
			break;
		case Pole415VRight:
            insulator = Models.render415VInsulator(textureMetal, textureInsulator);
            model.add(new RawQuadCube(0.15F, 0.1F, 1.94F, textureMetal));
            model.merge(insulator.clone().translateCoord(0, 0.05F, -0.9F));
            model.merge(insulator.clone().translateCoord(0, 0.05F, -0.45F));
            model.merge(insulator.clone().translateCoord(0, 0.05F, 0.45F));
            model.merge(insulator.clone().translateCoord(0, 0.05F, 0.9F));
            model.rotateAroundY(90);
            
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.9F, 0.3F, 0), new Vec3f(-0.9F, 0.3F, -5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.45F, 0.3F, 0), new Vec3f(-0.45F, 0.3F, -5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0.45F, 0.3F, 0), new Vec3f(0.45F, 0.3F, -5F), true, 0.2F, 0.05F, textureCable));
            model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0.9F, 0.3F, 0), new Vec3f(0.9F, 0.3F, -5F), true, 0.2F, 0.05F, textureCable));
            
            //Pole
            model.add(new RawQuadCube(0.25F, 1, 0.25F, textureConcrete));
			break;
			
		case TransformerLeft:
			//Support
			model.add((new RawQuadCube(0.05F, 0.1F, 1.5F, textureMetal)).translateCoord(0.15F, 0, -0.25F));
			model.add((new RawQuadCube(0.05F, 0.1F, 1.5F, textureMetal)).translateCoord(-0.15F, 0, -0.25F));
			
			//Body
			model.add((new RawQuadCube(0.8F, 0.9F, 1.8F, new TextureAtlasSprite[] {
					textureTransformerFrontBack, textureTransformerFrontBack, textureTransformerSide, textureTransformerSide, textureTransformerFrontBack, textureTransformerFrontBack
					})).translateCoord(0, 0.1F, 0.5F));
			//Heatsinker(Front)
			model.add((new RawQuadCube(0.2F, 0.7F, 1.4F, new TextureAtlasSprite[] {
					textureTransformerFrontBack, textureTransformerFrontBack, textureTransformerSide, textureTransformerSide, textureHeatSink, textureHeatSink
					})).translateCoord(-0.5F, 0.15F, 0.5F));
			//Heatsinker(Back)
			model.add((new RawQuadCube(0.2F, 0.7F, 1.4F, new TextureAtlasSprite[] {
					textureTransformerFrontBack, textureTransformerFrontBack, textureTransformerSide, textureTransformerSide, textureHeatSink, textureHeatSink
					})).translateCoord(0.5F, 0.15F, 0.5F));
			
			//Insulators
			model.merge(Models.render415VInsulatorTall(textureMetal, textureCeramic).translateCoord(0.25F, 1, 0));
			model.merge(Models.render415VInsulatorTall(textureMetal, textureCeramic).translateCoord(0.25F, 1, 0.333F));
			model.merge(Models.render415VInsulatorTall(textureMetal, textureCeramic).translateCoord(0.25F, 1, 0.666F));
			model.merge(Models.render415VInsulatorTall(textureMetal, textureCeramic).translateCoord(0.25F, 1, 1F));
			
			model.merge(Models.render10kVInsulatorTall(textureMetal, textureCeramic).translateCoord(-0.25F, 1, 0));
			model.merge(Models.render10kVInsulatorTall(textureMetal, textureCeramic).translateCoord(-0.25F, 1, 0.5F));
			model.merge(Models.render10kVInsulatorTall(textureMetal, textureCeramic).translateCoord(-0.25F, 1, 1F));
			
			//Cable
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.25F, 1.4F, 0), new Vec3f(-1.125F, 2.75F, 0), false, 0.3F, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.25F, 1.4F, 0.5F), new Vec3f(-0.775F, 2.75F, 0.5F), false, 0.15F, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(-0.25F, 1.4F, 1), new Vec3f(-0.425F, 2.75F, 1), false, 0.05F, 0.03F, textureCable));
			
			model.merge(PowerCableBakery.renderParabolicCable(new Object[]{new Vec3f(0.25F, 1.325F, 0), 0.05F,new Vec3f(0.3F, 1.8F, -0.2F), -0.05F, new Vec3f(0.5F, 2.4F, -0.25F)}, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0.25F, 1.325F, 0.333F), new Vec3f(0.5F, 2.4F, 0.25F), false, -0.15F, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Vec3f(0.25F, 1.325F, 0.666F), new Vec3f(0.5F, 2.4F, 0.75F), false, -0.15F, 0.03F, textureCable));
			model.merge(PowerCableBakery.renderParabolicCable(new Object[]{new Vec3f(0.25F, 1.325F, 1F), 0.05F,new Vec3f(0.3F, 1.8F, 1.2F), -0.05F, new Vec3f(0.5F, 2.4F, 1.25F)}, 0.03F, textureCable));
			
			break;
		case TransformerRight:
			//Support
			model.add((new RawQuadCube(0.05F, 0.1F, 1.5F, textureMetal)).translateCoord(0.15F, 0, 0.25F));
			model.add((new RawQuadCube(0.05F, 0.1F, 1.5F, textureMetal)).translateCoord(-0.15F, 0, 0.25F));
			break;
			
		default:
			break;
		}
		
		if (facing != null)
			model.rotateAroundY(270-facing.toYRot());
        model.translateCoord(0.5F, 0, 0.5F);
        model.bake(list);
	}
	
	private static class NoBaking extends CodeBasedModel {
	    protected boolean skipLegacyTextureRegistration() {
	    	return true;
	    }
		
		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData extraData) {
			if (instance == null)
				return emptyQuadList;
			return instance.getQuads(state, side, rand, extraData);
		}

		@Override
		public TextureAtlasSprite getParticleIcon() {
			if (instance == null)
				return null;
			return instance.getParticleIcon();
		}

		@Override
		protected void bake(Function<ResourceLocation, TextureAtlasSprite> textureRegistry) {

		}
	}
}
