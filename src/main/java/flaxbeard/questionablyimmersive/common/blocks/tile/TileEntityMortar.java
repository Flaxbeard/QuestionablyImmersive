package flaxbeard.questionablyimmersive.common.blocks.tile;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import flaxbeard.questionablyimmersive.api.ICoordinateProvider;
import flaxbeard.questionablyimmersive.common.blocks.multiblocks.MultiblockMortar;
import flaxbeard.questionablyimmersive.common.entity.EntityMortarItem;
import flaxbeard.questionablyimmersive.common.items.ItemPortableRadio;
import flaxbeard.questionablyimmersive.common.util.RadioHelper;
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

import java.util.ArrayList;
import java.util.List;

public class TileEntityMortar extends TileEntityMultiblockMetal<TileEntityMortar, IMultiblockRecipe> implements IAdvancedSelectionBounds, IAdvancedCollisionBounds, IGuiTile, IEBlockInterfaces.IPlayerInteraction, RadioHelper.IRadioSubscriber
{
	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		System.out.println(isDummy() + " " + pos + " " + getOrigin());

		TileEntityMortar master = master();
		if (master != null && master.isStackValid(1, heldItem))
		{
			ItemStack toDrop = master.inventory.get(1).copy();

			ItemStack toAdd = heldItem.copy();
			toAdd.setCount(1);

			master.inventory.set(1, toAdd);
			heldItem.shrink(1);

			if (!toDrop.isEmpty())
			{
				if (!player.addItemStackToInventory(toDrop))
				{
					player.dropItem(toDrop, false);
				}
			}
			return true;
		}
		return false;
	}

	public NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);


	public static class TileEntityMortarParent extends TileEntityMortar
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

	public TileEntityMortar()
	{
		super(MultiblockMortar.instance, new int[]{8, 5, 3}, 16000, true);
	}

	public int reloadTicks = 0;
	private boolean constantlyFiring = false;
	public Vec3d targetPos = null;
	public float prevRotation;
	public float rotation;
	private boolean weaponMode = true;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		reloadTicks = nbt.getInteger("reloadTicks");
		constantlyFiring = nbt.getBoolean("consistentlyFiring");

		if (nbt.hasKey("posX"))
		{
			targetPos = new Vec3d(
					nbt.getDouble("posX"),
					nbt.getDouble("posY"),
					nbt.getDouble("posZ")
			);
		}
		else
		{
			targetPos = null;
		}

		rotation = nbt.getFloat("rotation");
		if (!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 6);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("reloadTicks", reloadTicks);
		nbt.setBoolean("consistentlyFiring", constantlyFiring);
		nbt.setFloat("rotation", rotation);
		if (targetPos != null) {
			nbt.setDouble("posX", targetPos.x);
			nbt.setDouble("posY", targetPos.y);
			nbt.setDouble("posZ", targetPos.z);
		}


		if (!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	private int subscribedFrequency = -1;
	private boolean shouldShoot = false;

	@Override
	public void notifyTargetChange()
	{
		shouldShoot = true;
	}

	@Override
	public void update()
	{
		super.update();

		int lastSubscribedFrequency = subscribedFrequency;
		subscribedFrequency = -1;
		targetPos = null;
		if (!inventory.get(1).isEmpty())
		{
			if (inventory.get(1).getItem() instanceof ICoordinateProvider)
			{
				targetPos = ((ICoordinateProvider) inventory.get(1).getItem()).getCoordinate(world, inventory.get(1));
				if (inventory.get(1).getItem() instanceof ItemPortableRadio)
				{
					this.subscribedFrequency = ItemPortableRadio.getFrequency(inventory.get(1));
				}
			}
		}

		if (subscribedFrequency != lastSubscribedFrequency)
		{
			if (subscribedFrequency == -1)
			{
				RadioHelper.unsubscribeTile(world, lastSubscribedFrequency, getPos());
			}
			else
			{
				RadioHelper.subscribeTile(world, subscribedFrequency, getPos());
			}
		}


		if (targetPos != null)
		{
			rotation = (float) Math.toDegrees(Math.atan2(targetPos.x - getPos().getX(), targetPos.z - getPos().getZ())) - 90f;
		}

		if (world.isRemote && constantlyFiring && reloadTicks == 0)
		{
			reloadTicks = 20;
		}
		if (reloadTicks == 20)
		{
			world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), IESounds.chargeFast, SoundCategory.BLOCKS, 2.0f, 1f);
			world.playSound(null, getPos().getX(), getPos().getY(), getPos().getZ(), IESounds.railgunFire, SoundCategory.BLOCKS, 1.75f, .5f + (.5f * world.rand.nextFloat()));
		}
		if (reloadTicks > 0)
		{
			reloadTicks--;
		}

		if (world.isRemote || isDummy() || isRSDisabled())
			return;

		boolean update = false;

		if (reloadTicks == 0)
		{
			if (targetPos != null && (!(inventory.get(1).getItem() instanceof ItemPortableRadio) || shouldShoot))
			{
				shouldShoot = false;
				if (!inventory.get(0).isEmpty())
				{
					int fireCount = Math.min(weaponMode ? 1 : 32, inventory.get(0).getCount());
					ItemStack toFire = inventory.get(0).copy();
					toFire.setCount(fireCount);
					inventory.get(0).shrink(fireCount);

					Vec3i basePositionBlock = getPos().offset(facing);
					Vec3d basePosition = new Vec3d(basePositionBlock.getX() + .5f, basePositionBlock.getY() + 10F/16f, basePositionBlock.getZ() + .5f);

					double rot = Math.toRadians(80);
					Vec3d facing2 = new Vec3d(
							-Math.cos(rot) * -Math.cos(Math.toRadians(rotation)),
							Math.sin(rot),
							-Math.cos(rot) * Math.sin(Math.toRadians(rotation))
					);
					facing2.normalize();
					Vec3d facing = new Vec3d(-Math.cos(Math.toRadians(rotation)), 0, Math.sin(Math.toRadians(rotation)));
					facing.normalize();

					Vec3d barrelPosition = basePosition.add(facing2.scale(6.25f).add(facing.scale(.5f)));

					EntityItem itemEntity = new EntityMortarItem(
							world,
							barrelPosition.x, barrelPosition.y, barrelPosition.z,
							toFire,
							targetPos.x + .5f, targetPos.z + .5f,
							facing2.scale(6 / facing2.y).x,
							facing2.scale(6 / facing2.y).z,
							weaponMode
					);
					world.spawnEntity(itemEntity);
					reloadTicks = 20;

					if (!constantlyFiring)
					{
						constantlyFiring = true;
						update = true;
					}
				}
				else if (constantlyFiring)
				{
					constantlyFiring = false;
					update = true;
				}
			}
			else if (constantlyFiring)
			{
				constantlyFiring = false;
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
	public boolean canOpenGui()
	{
		return true;
	}

	@Override
	public int getGuiID()
	{
		return 1;
	}

	@Override
	public TileEntity getGuiMaster()
	{
		return master();
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
	public TileEntityMortar master()
	{
		if (offset[0] == 0 && offset[1] == 0 && offset[2] == 0)
		{
			return this;
		}
		TileEntity te = world.getTileEntity(getPos().add(-offset[0], -offset[1], -offset[2]));
		return this.getClass().isInstance(te) ? (TileEntityMortar) te : null;
	}

	@Override
	public TileEntityMortar getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityMortar ? (TileEntityMortar) tile : null;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if ((facing == null || pos == 3) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return master() != null;
		return super.hasCapability(capability, facing);
	}

	IItemHandler insertionHandler = new IEInventoryHandler(2, this, 0, new boolean[]{true, false}, new boolean[2]);

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if ((facing == null || pos == 3) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TileEntityMortar master = master();
			if (master != null)
				return (T) master.insertionHandler;
			return null;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		if (subscribedFrequency != -1)
		{
			RadioHelper.unsubscribeTile(world, subscribedFrequency, getPos());
		}
	}
}