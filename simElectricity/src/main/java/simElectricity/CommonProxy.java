package simElectricity;

import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import simElectricity.Blocks.*;

public class CommonProxy implements IGuiHandler {
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public void registerTileEntitySpecialRenderer() {
    }

    public World getClientWorld() {
        return null;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);

        if (te instanceof TileQuantumGenerator)
            return new ContainerQuantumGenerator(player.inventory, te);
        if (te instanceof TileVoltageMeter)
            return new ContainerVoltageMeter(player.inventory, te);
        if (te instanceof TileElectricFurnace)
            return new ContainerElectricFurnace(player.inventory, te);
        if (te instanceof TileSimpleGenerator)
            return new ContainerSimpleGenerator(player.inventory, te);
        if (te instanceof TileAdjustableResistor)
            return new ContainerAdjustableResistor(player.inventory, te);
        if (te instanceof TileAdjustableTransformer)
            return new ContainerAdjustableTransformer(player.inventory, te);
        if (te instanceof TileSwitch)
        	return new ContainerSwitch(player.inventory, te);

        return null;
    }
}
