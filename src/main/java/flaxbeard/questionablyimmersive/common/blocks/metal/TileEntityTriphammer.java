package flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import flaxbeard.questionablyimmersive.api.ICoordinateProvider;
import flaxbeard.questionablyimmersive.common.blocks.multiblocks.MultiblockTriphammer;
import flaxbeard.questionablyimmersive.common.network.GUIUpdatePacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEntityTriphammer extends TileEntityMultiblockMetal<TileEntityTriphammer, IMultiblockRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IEBlockInterfaces.IPlayerInteraction, IGuiTile
{
	public float ticks;
	public String repairedItemName = "";

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		//System.out.println(isDummy() + " " + pos + " " + getOrigin());
		return false;
	}

	public NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);

	@Override
	public boolean canOpenGui()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return 3;
	}

	@Nullable
	@Override
	public TileEntity getGuiMaster()
	{
		return master();
	}



	public static class TileEntityTriphammerParent extends TileEntityTriphammer
	{
		@SideOnly(Side.CLIENT)
		@Override
		public AxisAlignedBB getRenderBoundingBox()
		{
			BlockPos nullPos = this.getPos();
			return new AxisAlignedBB(nullPos.offset(facing, -2).offset(mirrored ? facing.rotateYCCW() : facing.rotateY(), -1).down(1), nullPos.offset(facing, 5).offset(mirrored ? facing.rotateYCCW() : facing.rotateY(), 2).up(3));
		}

		@Override
		public boolean isDummy()
		{
			return false;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared()
		{
			return super.getMaxRenderDistanceSquared() * IEConfig.increasedTileRenderdistance;
		}
	}

	public TileEntityTriphammer()
	{
		super(MultiblockTriphammer.instance, new int[]{2, 4, 3}, 16000, true);
	}


	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		repairedItemName = nbt.getString("repairedItemName");
		if (!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 6);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setString("repairedItemName", repairedItemName);
		if (!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}


	@Override
	public void update()
	{
		super.update();

		ticks++;

		boolean update = false;
		if (update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0, 0, 0, 0, 0, 0};
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds()
	{
		List list = new ArrayList<AxisAlignedBB>();

		list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(getPos().getX(), getPos().getY(), getPos().getZ()));
		return list;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list)
	{
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds()
	{
		List list = new ArrayList<AxisAlignedBB>();
		return getAdvancedSelectionBounds();
	}

	@Override
	public int[] getEnergyPos()
	{
		return new int[]{20};
	}

	@Override
	public int[] getRedstonePos()
	{
		return new int[]{18};
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output)
	{
	}

	@Override
	public void doProcessFluidOutput(FluidStack output)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process)
	{
		return 0;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if (slot == 1) {
			return stack.getItem() instanceof ICoordinateProvider;
		}
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public int[] getOutputSlots()
	{
		return null;
	}

	@Override
	public int[] getOutputTanks()
	{
		return null;
	}


	@Override
	public void doGraphicalUpdates(int slot)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting)
	{
		return null;
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag)
	{
		return null;
	}

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public IFluidTank[] getInternalTanks()
	{
		return null;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process)
	{
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side)
	{
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side)
	{
		return false;
	}

	@Override
	public boolean isDummy()
	{
		return true;
	}

	@Override
	public TileEntityTriphammer master()
	{
		if (offset[0] == 0 && offset[1] == 0 && offset[2] == 0)
		{
			return this;
		}
		TileEntity te = world.getTileEntity(getPos().add(-offset[0], -offset[1], -offset[2]));
		return this.getClass().isInstance(te) ? (TileEntityTriphammer) te : null;
	}

	@Override
	public TileEntityTriphammer getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityTriphammer ? (TileEntityTriphammer) tile : null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if ((facing == null || pos == 3) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master() != null;
		return super.hasCapability(capability, facing);
	}

	IItemHandler insertionHandler = new IEInventoryHandler(3, this, 0, new boolean[]{true, false}, new boolean[3]);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if ((facing == null || pos == 3) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityTriphammer master = master();
			if (master != null)
				return (T) master.insertionHandler;
			return null;
		}
		return super.getCapability(capability, facing);
	}
}