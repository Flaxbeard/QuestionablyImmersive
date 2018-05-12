package flaxbeard.questionablyimmersive.common.blocks.tile;


import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import flaxbeard.questionablyimmersive.api.mechpower.IMechConnector;
import flaxbeard.questionablyimmersive.api.mechpower.MechNetworkHelper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;

public abstract class TileEntityMechConnectorBase extends TileEntityIEBase implements IMechConnector, IRotationAcceptor
{

	@Override
	public void invalidate()
	{
		super.invalidate();
		MechNetworkHelper.removeFromNetwork(world, pos);
	}

	@Override
	public void onLoad()
	{
		super.onLoad();
		MechNetworkHelper.getNetworkData(world, pos);
	}

	@Override
	public void inputRotation(double rotation, @Nonnull EnumFacing side)
	{
		if (doesConnect(side))
		{
			TileEntity te = world.getTileEntity(pos.offset(side.getOpposite()));
			if (te instanceof TileEntityWatermill)
			{
				rotation *= -Math.signum(((TileEntityWatermill) te).perTick);
			}
			if (side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE)
			{
				rotation *= -1;
			}
			MechNetworkHelper.getNetworkData(world, pos).addRotation(rotation);
		}
	}


}
