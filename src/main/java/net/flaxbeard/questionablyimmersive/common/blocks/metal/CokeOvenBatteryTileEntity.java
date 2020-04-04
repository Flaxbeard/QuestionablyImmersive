package net.flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
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
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
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

public class CokeOvenBatteryTileEntity extends TiledMultiblockTileEntity<CokeOvenBatteryTileEntity> implements IIEInventory, IEBlockInterfaces.IActiveState, QIBlockInterfaces.IInteractionObjectQI, IEBlockInterfaces.IProcessTile
{
	public static TileEntityType<CokeOvenBatteryTileEntity> TYPE;

	public static class Rendered extends CokeOvenBatteryTileEntity
	{
		public static TileEntityType<CokeOvenBatteryTileEntity> TYPE;

		public Rendered(TileEntityType<CokeOvenBatteryTileEntity> type)
		{
			super(type);
		}

		public Rendered()
		{
			super(TYPE);
		}

		@OnlyIn(Dist.CLIENT)
		@Override
		public AxisAlignedBB getRenderBoundingBox()
		{
			BlockPos nullPos = this.getPos();
			Direction facing = getFacing();
			boolean mirrored = getIsMirrored();
			return new AxisAlignedBB(nullPos.offset(facing, -1).offset(mirrored ? facing.rotateYCCW() : facing.rotateY(), -2).down(1), nullPos.offset(facing, 5 + 1).offset(mirrored ? facing.rotateYCCW() : facing.rotateY(), 3).up(2));
		}
	}

	public static class Master extends Rendered
	{
		public static TileEntityType<CokeOvenBatteryTileEntity> TYPE;

		public Master()
		{
			super(TYPE);
		}
	}

	public FluidTank tank = new FluidTank(12000);
	public NonNullList<ItemStack> inventory;

	public int[] recuperationTime;
	public int[] process;
	public int[] processMax;
	public boolean[] active;
	public LazyOptional<IItemHandler>[] invHandlers;
	public CokeOvenBatteryTileEntity.Data guiData;

	private CokeOvenBatteryTileEntity(TileEntityType<CokeOvenBatteryTileEntity> type)
	{
		super(QIMultiblocks.COKE_OVEN_BATTERY_SLICE, type, false);
		this.inventory = NonNullList.withSize(0, ItemStack.EMPTY);
		this.guiData = new Data();
		this.process = new int[0];
		this.processMax = new int[0];
		this.active = new boolean[0];
		this.active = new boolean[0];
		this.invHandlers = new LazyOptional[0];
	}

	public CokeOvenBatteryTileEntity()
	{
		this(TYPE);
	}

	@Override
	public <T> LazyOptional<T> registerConstantCap(T val)
	{
		return super.registerConstantCap(val);
	}

	@Override
	public boolean canUseGui(PlayerEntity player)
	{
		return this.formed;
	}

	@Override
	public QIBlockInterfaces.IInteractionObjectQI getGuiMaster()
	{
		return (QIBlockInterfaces.IInteractionObjectQI) this.master();
	}

	private boolean isActive;

	@Override
	public void tick()
	{
		ApiUtils.checkForNeedlessTicking(this);
		Direction facing = getFacing();
		boolean mirrored = getIsMirrored();

		boolean update = false;

		if (world.isRemote && !isDummy())
		{
			for (int i = 0; i < active.length; i++)
			{
				if (active[i])
				{
					if (process[i] > 0)
					{
						process[i]--;
						if (process[i] < 35 && !canDoorOpen(i))
						{
							process[i] = 35;
						}

						if (process[i] == 0)
						{
							recuperationTime[i] = 20;
							processMax[i] = 0;

							// Drop particles when items are dropped (clientside)
							for (int j = 0; j <= 20; j++)
							{
								BlockPos dropPos = getPos().offset(facing, i).offset(facing.rotateY(), mirrored ? 2 : -2).down();
								BlockPos offset = BlockPos.ZERO.offset(facing.rotateY(), mirrored ? -1 : 1);
								Vec3d dropLoc = Vec3d.ZERO.add(dropPos.getX() + offset.getX() * .3f, dropPos.getY(), dropPos.getZ() + offset.getZ() * .3f);
								double randomZOffset = (world.rand.nextDouble() - .5) * .4;
								double randomXOffset = (world.rand.nextDouble() - .5) * .4;
								BlockState state = IEBlocks.StoneDecoration.coke.getDefaultState();
								world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, state), dropLoc.x + .5f + randomZOffset, dropLoc.y + .25 + 3 * (j / 20.), dropLoc.z + .5f + randomXOffset, 0, 0, 0);

								randomZOffset = (world.rand.nextDouble() - .5) * .4;
								randomXOffset = (world.rand.nextDouble() - .5) * .4;
								state = Blocks.LAVA.getDefaultState();
								world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, state), dropLoc.x + .5f + randomZOffset, dropLoc.y + .25 + 3 * (j / 20.), dropLoc.z + .5f + randomXOffset, 0, 0, 0);

							}

						}
					} else if (recuperationTime[i] > 0)
					{
						recuperationTime[i]--;
					}
				}
			}
		}

		if (world.isRemote() || isDummy())
		{
			return;
		}

		for (int i = 0; i < active.length; i++)
		{
			boolean wasActive = active[i];
			if (process[i] > 0)
			{
				if (inventory.get(i + 2).isEmpty())
				{
					process[i] = 0;
					processMax[i] = 0;
					active[i] = false;
				} else
				{
					CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(i + 2));
					if (recipe == null || getRecipeTime(recipe) != processMax[i])
					{
						process[i] = 0;
						processMax[i] = 0;
						active[i] = false;
					} else
					{
						process[i]--;
						if (process[i] < 35 && !canDoorOpen(i))
						{
							process[i] = 35;
						}
					}
				}
			} else if (recuperationTime[i] > 0)
			{
				recuperationTime[i]--;
				if (recuperationTime[i] == 0)
				{
					active[i] = false;
				}
			}
			if (process[i] == 0 && recuperationTime[i] == 0)
			{
				if (active[i])
				{
					CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(i + 2));
					if (recipe != null)
					{
						// Drop items, fill creosote tank up to 9 times
						int processAmount = Math.min(getProcessAmount(recipe), inventory.get(i + 2).getCount());
						Utils.modifyInvStackSize(inventory, i + 2, -processAmount);
						for (int j = 0; j < processAmount; j++)
						{

							ItemStack toOutput = recipe.output.copy();
							BlockPos dropPos = getPos().offset(facing, i).offset(facing.rotateY(), mirrored ? 2 : -2).down();
							BlockPos offset = BlockPos.ZERO.offset(facing.rotateY(), mirrored ? 1 : -1);
							Vec3d dropLoc = Vec3d.ZERO.add(dropPos.getX() + offset.getX() * .3f, dropPos.getY(), dropPos.getZ() + offset.getZ() * .3f);
							if (!toOutput.isEmpty())
							{
								ItemEntity ei = new ItemEntity(world, dropLoc.x + .5, dropLoc.y + .25f + j * .35f, dropLoc.z + .5, toOutput);
								ei.setMotion(new Vec3d(0, 0, 0));
								world.addEntity(ei);
							}
						}
						// TODO
						int creosoteLoss = 1;
						this.tank.fill(new FluidStack(IEContent.fluidCreosote, (int) (recipe.creosoteOutput * processAmount * creosoteLoss)), IFluidHandler.FluidAction.EXECUTE);
						recuperationTime[i] = 20;
					} else
					{
						active[i] = false;
					}
					processMax[i] = 0;
				}

				// Find a new recipe
				CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(i + 2));
				if (recipe != null && recuperationTime[i] == 0)
				{
					int recipeTime = getRecipeTime(recipe);
					process[i] = recipeTime;
					processMax[i] = process[i];
					recuperationTime[i] = 0;
					update = true;
					active[i] = true;
				}
			}

			if (wasActive != active[i])
			{
				update = true;
			}
		}

		// Output fluid to pipes
		if (tank.getFluidAmount() > 0)
		{
			ItemStack inFullSlot = (ItemStack) this.inventory.get(1);
			if (inFullSlot.isEmpty() || inFullSlot.getCount() + 1 <= inFullSlot.getMaxStackSize())
			{
				ItemStack filledContainer = Utils.fillFluidContainer(this.tank, this.inventory.get(0).copy(), inFullSlot, (PlayerEntity) null);
				if (!filledContainer.isEmpty())
				{
					if (this.inventory.get(0).getCount() == 1 && !Utils.isFluidContainerFull(filledContainer))
					{
						this.inventory.set(2, filledContainer.copy());
					} else
					{
						if (!inFullSlot.isEmpty() && ItemHandlerHelper.canItemStacksStack(inFullSlot, filledContainer))
						{
							inFullSlot.grow(filledContainer.getCount());
						} else if (inFullSlot.isEmpty())
						{
							this.inventory.set(1, filledContainer.copy());
						}

						Utils.modifyInvStackSize(this.inventory, 0, -filledContainer.getCount());
					}

					this.markDirty();
				}
			}

			FluidStack out = Utils.copyFluidStackWithAmount(this.tank.getFluid(), Math.min(this.tank.getFluidAmount(), 80), false);
			BlockPos outputPos = this.getPos().offset(facing, -1).offset(Direction.DOWN, 1);
			LazyOptional<IFluidHandler> optOutput = FluidUtil.getFluidHandler(world, outputPos, facing);
			if (optOutput.isPresent())
			{
				IFluidHandler output = optOutput.orElse(null);
				int accepted = output.fill(out, IFluidHandler.FluidAction.SIMULATE);
				if (accepted > 0)
				{
					int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), IFluidHandler.FluidAction.EXECUTE);
					this.tank.drain(drained, IFluidHandler.FluidAction.EXECUTE);
					out = Utils.copyFluidStackWithAmount(out, out.getAmount() - drained, false);
					this.markDirty();
				}
			}

			outputPos = this.getPos().offset(facing, numLayers).offset(Direction.DOWN, 1);
			optOutput = FluidUtil.getFluidHandler(world, outputPos, facing);
			if (optOutput.isPresent())
			{
				IFluidHandler output = optOutput.orElse(null);
				int accepted = output.fill(out, IFluidHandler.FluidAction.SIMULATE);
				if (accepted > 0)
				{
					int drained = output.fill(Utils.copyFluidStackWithAmount(out, Math.min(out.getAmount(), accepted), false), IFluidHandler.FluidAction.EXECUTE);
					this.tank.drain(drained, IFluidHandler.FluidAction.EXECUTE);
					this.markDirty();
					;
				}
			}
		}

		if (update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}


	private boolean canDoorOpen(int j)
	{
		Direction facing = getFacing();
		boolean mirrored = getIsMirrored();
		BlockPos pos = getPos().offset(facing, j).offset(facing.rotateY(), mirrored ? 2 : -2).down();
		for (int i = 0; i < 4; i++)
		{
			BlockState block = world.getBlockState(pos.up(i));
			if (block.isOpaqueCube(world, pos.up(i)))
			{
				return false;
			}
		}
		return true;
	}

	private int getRecipeTime(CokeOvenRecipe recipe)
	{
		// TODO
		int simultaneousOperations = 9;
		float operationTimeModifier = 2f / 3f;

		int recipeTime = (int) (recipe.time * simultaneousOperations * operationTimeModifier);
		if (recipe.output.getItem() == Item.getItemFromBlock(IEBlocks.StoneDecoration.coke))
		{
			recipeTime = (int) (recipe.time * operationTimeModifier);
		}
		return 100;//recipeTime;
	}

	private int getProcessAmount(CokeOvenRecipe recipe)
	{
		int processAmount = 9;//Config.QIConfig.cokeOvenBattery.simultaneousOperations;
		if (recipe.output.getItem() == Item.getItemFromBlock(IEBlocks.StoneDecoration.coke))
		{
			processAmount = 1;
		}
		return processAmount;
	}

	@Nullable
	public CokeOvenRecipe getRecipe()
	{
		if (((ItemStack) this.inventory.get(0)).isEmpty())
		{
			return null;
		} else
		{
			CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe((ItemStack) this.inventory.get(0));
			if (recipe == null)
			{
				return null;
			} else
			{
				return (((ItemStack) this.inventory.get(1)).isEmpty() || ItemStack.areItemsEqual((ItemStack) this.inventory.get(1), recipe.output) && ((ItemStack) this.inventory.get(1)).getCount() + recipe.output.getCount() <= this.getSlotLimit(1)) && this.tank.getFluidAmount() + recipe.creosoteOutput <= this.tank.getCapacity() ? recipe : null;
			}
		}
	}

	@Override
	public int[] getCurrentProcessesStep()
	{
		CokeOvenBatteryTileEntity master = (CokeOvenBatteryTileEntity) this.master();

		if (master != this && master != null)
		{
			return master.getCurrentProcessesStep();
		}

		int[] out = new int[this.process.length];
		for (int i = 0; i < this.process.length; i++)
		{
			out[i] = this.processMax[i] - this.process[i];
		}
		return out;
	}

	@Override
	public int[] getCurrentProcessesMax()
	{
		CokeOvenBatteryTileEntity master = (CokeOvenBatteryTileEntity) this.master();
		return master != this && master != null ? master.getCurrentProcessesMax() : this.processMax;
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if (id == 0)
		{
			this.formed = arg == 1;
		}

		this.markDirty();
		this.markContainingBlockForUpdate((BlockState) null);
		return true;
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return super.getUpdatePacket();
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		tank = new FluidTank(6000 * numLayers);
		this.tank.readFromNBT(nbt.getCompound("tank"));

		this.isActive = nbt.getBoolean("isActive");

		this.process = nbt.getIntArray("process").clone();

		if (!descPacket || this.process.length + 2 != this.inventory.size())
		{
			this.inventory = NonNullList.withSize(this.process.length + 2, ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(nbt, this.inventory);
		}

		this.recuperationTime = nbt.getIntArray("recuperationTime").clone();
		this.processMax = nbt.getIntArray("processMax").clone();

		byte[] activeTemp = nbt.getByteArray("active").clone();
		active = new boolean[activeTemp.length];
		for (int i = 0; i < activeTemp.length; i++)
		{
			active[i] = (activeTemp[i] > 0);
		}

		int numInvHandlers = this.process.length;
		invHandlers = new LazyOptional[numInvHandlers];
		for (int i = 0; i < numInvHandlers; i++)
		{
			boolean[] insert = new boolean[numInvHandlers + 2];
			insert[i + 2] = true;
			IEInventoryHandler handler = new IEInventoryHandler(numInvHandlers + 2, this, 0, insert, new boolean[numInvHandlers]);
			invHandlers[i] = registerConstantCap(handler);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		CompoundNBT tankTag = this.tank.writeToNBT(new CompoundNBT());
		nbt.put("tank", tankTag);
		nbt.putBoolean("isActive", isActive);
		if (!descPacket)
		{
			ItemStackHelper.saveAllItems(nbt, this.inventory);
		}
		nbt.putIntArray("process", this.process);
		nbt.putIntArray("processMax", this.processMax);
		nbt.putIntArray("recuperationTime", this.recuperationTime);

		byte[] activeTemp = new byte[active.length];
		for (int i = 0; i < activeTemp.length; i++)
		{
			activeTemp[i] = (byte) (active[i] ? 1 : 0);
		}
		nbt.putByteArray("active", activeTemp);
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F};
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

	// Tank stuff

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(Direction side)
	{
		CokeOvenBatteryTileEntity master = (CokeOvenBatteryTileEntity) this.master();
		return master != null ? new FluidTank[]{master.tank} : new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, Direction side, FluidStack resource)
	{
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, Direction side)
	{
		return true;
	}


	// Inventory stuff

	@Override
	public NonNullList<ItemStack> getInventory()
	{
		CokeOvenBatteryTileEntity master = (CokeOvenBatteryTileEntity) this.master();
		return master != null && master.formed && this.formed ? master.inventory : this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack)
	{
		if (slot == 0)
		{
			return Utils.isFluidRelatedItemStack(stack);
		}

		return !stack.isEmpty() && CokeOvenRecipe.findRecipe(stack) != null;
	}

	@Override
	public int getSlotLimit(int slot)
	{
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot)
	{
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			CokeOvenBatteryTileEntity master = (CokeOvenBatteryTileEntity) this.master();
			return master == null ? null : master.invHandlers[layer].cast();
		} else
		{
			return super.getCapability(capability, facing);
		}
	}

	public class Data implements IIntArray
	{
		public static final int FLUID_AMOUNT = 0;

		public Data()
		{
		}

		public int get(int index)
		{
			switch (index)
			{
				case 0:
					return CokeOvenBatteryTileEntity.this.tank.getFluidAmount();
				default:
					throw new IllegalArgumentException("Unknown index " + index);
			}
		}

		public void set(int index, int value)
		{
			switch (index)
			{
				case 0:
					CokeOvenBatteryTileEntity.this.tank.setFluid(new FluidStack(IEContent.fluidCreosote, value));
					break;
				default:
					throw new IllegalArgumentException("Unknown index " + index);
			}

		}

		public int size()
		{
			return 1;
		}
	}
}