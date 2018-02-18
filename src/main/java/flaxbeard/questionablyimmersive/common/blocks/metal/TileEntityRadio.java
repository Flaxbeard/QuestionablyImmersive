package flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import flaxbeard.questionablyimmersive.common.QEContent;
import flaxbeard.questionablyimmersive.common.util.RadioHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityRadio extends TileEntityIEBase implements IDirectionalTile, IEBlockInterfaces.IHammerInteraction, IBlockOverlayText
{
	public EnumFacing facing = EnumFacing.NORTH;
	public boolean receiveMode = true;
	public int power;
	public RadioHelper.RadioNetwork network;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		receiveMode = nbt.getBoolean("receiveMode");
		power = nbt.getInteger("power");

		if (descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("receiveMode", receiveMode);
		nbt.setInteger("power", power);

	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 2;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		RadioHelper.removeRadio(this);
		receiveMode = !receiveMode;
		network = RadioHelper.addRadio(this);


		markContainingBlockForUpdate(null);
		world.notifyNeighborsOfStateChange(getPos(), QEContent.blockRadio, false);
		markDirty();
		return true;
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		return new String[]{receiveMode ? "Receive Mode" : "Broadcast Mode"};
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}

	static Map<Integer, List<BlockPos>> radios = new HashMap<>();

	@Override
	public void onLoad()
	{
		super.onLoad();
		network = RadioHelper.addRadio(this);
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		RadioHelper.removeRadio(this);
	}
}