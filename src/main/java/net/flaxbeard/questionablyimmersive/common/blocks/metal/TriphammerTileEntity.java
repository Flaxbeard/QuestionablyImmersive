package net.flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableSet;
import net.flaxbeard.questionablyimmersive.common.QIConfig;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlockInterfaces;
import net.flaxbeard.questionablyimmersive.common.blocks.multiblocks.QIMultiblocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class TriphammerTileEntity extends PoweredMultiblockTileEntity<TriphammerTileEntity, IMultiblockRecipe>
{
	public static TileEntityType<TriphammerTileEntity> TYPE;

	public static class Master extends TriphammerTileEntity
	{
		public static TileEntityType<TriphammerTileEntity> TYPE;

		public Master()
		{
			super(TYPE);
		}
	}

	public NonNullList<ItemStack> inventory;
	LazyOptional<IItemHandler> insertionHandler;

	private TriphammerTileEntity(TileEntityType<TriphammerTileEntity> type)
	{
		super(QIMultiblocks.TRIPHAMMER, 0, false, type);
		this.inventory = NonNullList.withSize(3, ItemStack.EMPTY);
		this.insertionHandler = registerConstantCap(new IEInventoryHandler(3, this, 0, new boolean[]{true, false, false}, new boolean[3]));
	}

	public TriphammerTileEntity()
	{
		this(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		if (!descPacket)
		{
			inventory = Utils.readInventory(nbt.getList("inventory", 2), 6);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);

		if (!descPacket)
		{
			nbt.put("inventory", Utils.writeInventory(inventory));
		}
	}

	@Override
	public void tick()
	{
		super.tick();

		if (!isDummy()) {
			System.out.println(this.inventory.get(0));
		}

		if (world.isRemote || isRSDisabled())
		{
			return;
		}

		boolean update = false;
		if (update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}


	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(new BlockPos(1, 0, 4));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(new BlockPos(1, 0, 4));
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F};
	}

	@Override
	public void doGraphicalUpdates(int i)
	{
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public Direction getFacing()
	{
		if (this.world != null)
		{
			return super.getFacing();
		}
		return Direction.NORTH;
	}

	@Override
	public boolean getIsMirrored()
	{
		if (this.world != null)
		{
			return super.getIsMirrored();
		}
		return false;
	}

	// Inventory stuff

	@Nullable
	@Override
	public NonNullList<ItemStack> getInventory()
	{
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if ((facing == null || (new BlockPos(0, 0, 0)).equals(this.posInMultiblock)) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TriphammerTileEntity master = (TriphammerTileEntity) this.master();
			if (master != null)
			{
				return master.insertionHandler.cast();
			}
		}

		return super.getCapability(capability, facing);
	}


	// Useless tank stuff

	@Nullable
	@Override
	public IFluidTank[] getInternalTanks()
	{
		return new IFluidTank[0];
	}

	@Nullable
	@Override
	public int[] getOutputTanks()
	{
		return new int[0];
	}

	@Nonnull
	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction direction)
	{
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int i, Direction direction, FluidStack fluidStack)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int i, Direction direction)
	{
		return false;
	}


	// Useless recipe stuff beyond this point

	@Nullable
	@Override
	protected IMultiblockRecipe readRecipeFromNBT(CompoundNBT compoundNBT)
	{
		return null;
	}

	@Nullable
	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack itemStack)
	{
		return null;
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> multiblockProcess)
	{
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack itemStack)
	{
	}

	@Override
	public void doProcessFluidOutput(FluidStack fluidStack)
	{
	}

	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> multiblockProcess)
	{
	}

	@Override
	public int getMaxProcessPerTick()
	{
		return 0;
	}

	@Override
	public int getProcessQueueMaxLength()
	{
		return 0;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> multiblockProcess)
	{
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine()
	{
		return false;
	}

	@Nullable
	@Override
	public int[] getOutputSlots()
	{
		return null;
	}
}