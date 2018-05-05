package flaxbeard.questionablyimmersive.common.blocks.tile;

import blusunrize.immersiveengineering.api.energy.IRotationAcceptor;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import flaxbeard.questionablyimmersive.api.mechpower.MechNetworkHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityAxle extends TileEntityMechConnectorBase implements IDirectionalTile, IEBlockInterfaces.IPlayerInteraction
{
	public EnumFacing facing = EnumFacing.NORTH;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public EnumFacing getFacingForPlacement(EntityLivingBase placer, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		EnumFacing def = IDirectionalTile.super.getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
		if (def.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE)
		{
			def = def.getOpposite();
		}
		return def;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		if (facing.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE)
		{
			facing = facing.getOpposite();
		}
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}


	@Override
	public boolean doesConnect(@Nullable EnumFacing f)
	{
		if (f == null)
		{
			return true;
		}
		return f == facing || f == facing.getOpposite();
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		System.out.print(MechNetworkHelper.getNetworkData(world, pos).getContainedBlocks().size() + " " );
		System.out.println(MechNetworkHelper.getNetworkData(world, pos).getContainedAcceptors().size());
		return true;
	}

}