package flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import flaxbeard.questionablyimmersive.api.ICoordinateProvider;
import flaxbeard.questionablyimmersive.common.blocks.multiblocks.MultiblockCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.multiblocks.MultiblockMortar;
import flaxbeard.questionablyimmersive.common.entity.EntityMortarItem;
import flaxbeard.questionablyimmersive.common.items.ItemPortableRadio;
import flaxbeard.questionablyimmersive.common.util.RadioHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
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

public class TileEntityCokeOvenBattery extends TileEntityMultiblockMetal<TileEntityCokeOvenBattery, IMultiblockRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds
{
	public NonNullList<ItemStack> inventory;

	public int[] offsetTotal = {0, 0, 0};
	public int ovenLength;
	public int ovenIndex;
	public int[] recuperationTime;
	public int[] process;
	public int[] processMax;
	public boolean[] active;
	public IEInventoryHandler[] insertionHandlers;

	public static class TileEntityCokeOvenBatteryParent extends TileEntityCokeOvenBattery
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

	public TileEntityCokeOvenBattery()
	{
		super(MultiblockCokeOvenBattery.Slice.instance, new int[]{4, 1, 3}, 16000, true);
		inventory = NonNullList.withSize(ovenLength, ItemStack.EMPTY);
		insertionHandlers = new IEInventoryHandler[ovenLength];
		for (int i = 0; i < ovenLength; i++)
		{
			boolean[] insert = new boolean[ovenLength];
			insert[i] = true;
			insertionHandlers[i] = new IEInventoryHandler(ovenLength, this, 0, insert, new boolean[ovenLength]);
		}
		process = new int[ovenLength];
		recuperationTime = new int[ovenLength];
		processMax = new int[ovenLength];
		active = new boolean[ovenLength];
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		offsetTotal = nbt.getIntArray("offsetTotal");
		ovenLength = nbt.getInteger("ovenLength");
		ovenIndex = nbt.getInteger("ovenIndex");
		process = nbt.getIntArray("process").clone();
		recuperationTime = nbt.getIntArray("recuperationTime").clone();

		processMax = nbt.getIntArray("processMax").clone();
		byte[] activeTemp = nbt.getByteArray("active");
		active = new boolean[activeTemp.length];
		for (int i = 0; i < activeTemp.length; i++)
		{
			active[i] = (activeTemp[i] > 0);
		}

		if (!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), ovenLength);

		insertionHandlers = new IEInventoryHandler[ovenLength];
		for (int i = 0; i < ovenLength; i++)
		{
			boolean[] insert = new boolean[ovenLength];
			insert[i] = true;
			insertionHandlers[i] = new IEInventoryHandler(ovenLength, this, 0, insert, new boolean[ovenLength]);
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);

		nbt.setIntArray("offsetTotal", offsetTotal);
		nbt.setInteger("ovenLength", ovenLength);
		nbt.setInteger("ovenIndex", ovenIndex);
		nbt.setIntArray("process", process);
		nbt.setIntArray("processMax", processMax);
		nbt.setIntArray("recuperationTime", recuperationTime);

		byte[] activeTemp = new byte[active.length];
		for (int i = 0; i < activeTemp.length; i++)
		{
			activeTemp[i] = (byte) (active[i] ? 1 : 0);
		}
		nbt.setByteArray("active", activeTemp);


		if (!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public void update()
	{
		super.update();

		boolean update = false;

		if (world.isRemote && !isDummy())
		{
			for (int i = 0; i < active.length; i++)
			{
				if (active[i]) {
					if (process[i] > 0)
					{
						process[i]--;
						if (process[i] == 0)
						{
							recuperationTime[i] = 20;
						}
					}
					else if (recuperationTime[i] > 0)
					{
						recuperationTime[i]--;
					}
				}
			}
		}

		if (world.isRemote || isDummy())
			return;

		for (int i = 0; i < active.length; i++)
		{
			boolean wasActive = active[i];
			if (process[i] > 0)
			{
				if (inventory.get(i).isEmpty())
				{
					process[i] = 0;
					processMax[i] = 0;
					active[i] = false;
				}
				else
				{
					CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(i));
					if (recipe == null)// || recipe.time != 80)//processMax[i])
					{
						process[i] = 0;
						processMax[i] = 0;
						active[i] = false;
					}
					else
					{
						process[i]--;
					}
				}
			}
			else if (recuperationTime[i] > 0)
			{
				recuperationTime[i]--;
				if (recuperationTime[i] == 0)
				{
					active[i] = false;
				}
			}
			else
			{
				if (active[i])
				{
					CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(i));
					if (recipe != null)
					{
						int processAmount = Math.min(8, inventory.get(i).getCount());
						Utils.modifyInvStackSize(inventory, i, -processAmount);
						for (int j = 0; j < processAmount; j++)
						{
							ItemStack toOutput = recipe.output.copy();
							BlockPos dropPos = getPos().offset(facing, i).offset(facing.rotateY(), mirrored ? -2 : 2).down();
							BlockPos offset = BlockPos.ORIGIN.offset(facing.rotateY(), mirrored ? 1 : -1);
							Vec3d dropLoc = Vec3d.ZERO.addVector(dropPos.getX() + offset.getX() * .3f, dropPos.getY(), dropPos.getZ() + offset.getZ() * .3f);
							if (!toOutput.isEmpty())
							{
								EntityItem ei = new EntityItem(world, dropLoc.x + .5,dropLoc.y + .25f + j * .35f,dropLoc.z + .5, toOutput);
								ei.motionY = 0;
								if (facing != null)
								{
									ei.motionX = 0;
									ei.motionZ = 0;
								}
								world.spawnEntity(ei);
							}
						}
						recuperationTime[i] = 20;
					}
					processMax[i] = 0;
				}
				CokeOvenRecipe recipe = CokeOvenRecipe.findRecipe(inventory.get(i));
				if (recipe != null && recuperationTime[i] == 0)
				{
					process[i] = 600; //recipe.time;
					processMax[i] = process[i];
					recuperationTime[i] = 0;
					active[i] = true;
				}
			}
			if (wasActive != active[i])
			{
				update = true;
			}
		}

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
		return !stack.isEmpty() && CokeOvenRecipe.findRecipe(stack) != null;
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
	public TileEntityCokeOvenBattery getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityCokeOvenBattery ? (TileEntityCokeOvenBattery) tile : null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if ((facing == null || pos == 10) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master() != null;
		return super.hasCapability(capability, facing);
	}


	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if ((facing == null || pos == 10) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityCokeOvenBattery master = master();
			if (master != null && master.insertionHandlers.length > ovenIndex)
				return (T) master.insertionHandlers[ovenIndex];
			return null;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void disassemble()
	{
		if (formed && !world.isRemote)
		{
			BlockPos startPos = getOrigin().offset(facing, -ovenIndex);
			BlockPos masterPos = getPos().add(-offsetTotal[0], -offsetTotal[1], -offsetTotal[2]);
			long time = world.getTotalWorldTime();
			for (int yy = 0; yy < structureDimensions[0]; yy++)
				for (int ll = 0; ll < ovenLength; ll++)
					for (int ww = 0; ww < structureDimensions[2]; ww++)
					{
						int w = mirrored ? -ww : ww;
						BlockPos pos = startPos.offset(facing, ll).offset(facing.rotateY(), w).add(0, yy, 0);
						ItemStack s = ItemStack.EMPTY;

						TileEntity te = world.getTileEntity(pos);
						if (te instanceof TileEntityCokeOvenBattery)
						{
							TileEntityCokeOvenBattery part = (TileEntityCokeOvenBattery) te;
							Vec3i diff = pos.subtract(masterPos);
							if (part.offsetTotal[0] != diff.getX() || part.offsetTotal[1] != diff.getY() || part.offsetTotal[2] != diff.getZ())
								continue;
							else if (time != part.onlyLocalDissassembly)
							{
								s = part.getOriginalBlock();
								part.formed = false;
							}
						}
						if (pos.equals(getPos()))
							s = this.getOriginalBlock();
						IBlockState state = Utils.getStateFromItemStack(s);
						if (state != null)
						{
							if (pos.equals(getPos()))
								world.spawnEntity(new EntityItem(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, s));
							else
								replaceStructureBlock(pos, state, s, yy, ll, ww);
						}
					}
		}
	}

	@Nullable
	@Override
	public TileEntityCokeOvenBattery master()
	{
		if (offsetTotal[0] == 0 && offsetTotal[1] == 0 && offsetTotal[2] == 0)
			return this;
		BlockPos masterPos = getPos().add(-offsetTotal[0], -offsetTotal[1], -offsetTotal[2]);
		TileEntity te = Utils.getExistingTileEntity(world, masterPos);
		return this.getClass().isInstance(te) ? (TileEntityCokeOvenBattery) te : null;
	}

}