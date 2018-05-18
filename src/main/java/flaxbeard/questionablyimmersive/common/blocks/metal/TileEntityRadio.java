package flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.questionablyimmersive.common.QIContent;
import flaxbeard.questionablyimmersive.common.network.GUIUpdatePacket;
import flaxbeard.questionablyimmersive.common.util.RadioHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityRadio extends TileEntityIEBase implements IDirectionalTile, IEBlockInterfaces.IHammerInteraction, IBlockOverlayText, IEBlockInterfaces.IGuiTile, IEBlockInterfaces.ITileDrop, GUIUpdatePacket.IPacketReceiver
{
	public EnumFacing facing = EnumFacing.NORTH;
	public boolean receiveMode = true;
	public int power;
	public int frequency;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{

		if (world != null && !world.isRemote)
		{
			RadioHelper.removeRadio(this);
		}

		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		receiveMode = nbt.getBoolean("receiveMode");
		power = nbt.getInteger("power");
		frequency = nbt.getInteger("frequency");


		if (world != null && !world.isRemote)
		{
			RadioHelper.addRadio(this);
		}

		if (descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setBoolean("receiveMode", receiveMode);
		nbt.setInteger("power", power);
		nbt.setInteger("frequency", frequency);

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
		setReceiveMode(!receiveMode);
		return true;
	}

	private void setReceiveMode(boolean target)
	{
		if (!world.isRemote)
		{
			RadioHelper.removeRadio(this);
		}
		receiveMode = target;
		if (!world.isRemote)
		{
			RadioHelper.addRadio(this);
		}


		markContainingBlockForUpdate(null);
		world.notifyNeighborsOfStateChange(getPos(), QIContent.blockGauge, false);
		markDirty();
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
		if (!world.isRemote)
		{
			RadioHelper.addRadio(this);
		}
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		if (!world.isRemote)
		{
			RadioHelper.removeRadio(this);
		}
	}

	public void setFrequency(int frequency)
	{
		if (!world.isRemote)
		{
			RadioHelper.removeRadio(this);
		}
		this.frequency = frequency;
		if (!world.isRemote)
		{
			RadioHelper.addRadio(this);
		}
		markContainingBlockForUpdate(null);
		world.notifyNeighborsOfStateChange(getPos(), QIContent.blockGauge, false);
		markDirty();
	}

	@Override
	public boolean canOpenGui()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return 0;
	}

	@Nullable
	@Override
	public TileEntity getGuiMaster()
	{
		return this;
	}

	@Override
	public ItemStack getTileDrop(@Nullable EntityPlayer player, IBlockState state)
	{
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));

		if (!receiveMode)
		{
			ItemNBTHelper.setBoolean(stack, "broadcast", true);
		}
		if (frequency != 0)
		{
			ItemNBTHelper.setInt(stack, "frequency", frequency);
		}
		return stack;
	}

	@Override
	public void readOnPlacement(@Nullable EntityLivingBase placer, ItemStack stack)
	{
		if (ItemNBTHelper.hasKey(stack, "frequency"))
		{
			setFrequency(ItemNBTHelper.getInt(stack, "frequency"));
		}
		if (ItemNBTHelper.hasKey(stack, "broadcast"))
		{
			setReceiveMode(false);
		}
	}

	@Override
	public void handlePacket(int messageId, NBTTagCompound data)
	{
		if (messageId == 0)
		{
			setFrequency(data.getInteger("station"));
		}
	}
}