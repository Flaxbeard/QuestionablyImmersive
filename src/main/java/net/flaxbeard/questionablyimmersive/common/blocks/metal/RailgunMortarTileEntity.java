package net.flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.ImmutableSet;
import net.flaxbeard.questionablyimmersive.api.ICoordinateProvider;
import net.flaxbeard.questionablyimmersive.common.blocks.multiblocks.QIMultiblocks;
import net.flaxbeard.questionablyimmersive.common.entities.MortarItemEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

public class RailgunMortarTileEntity extends PoweredMultiblockTileEntity<RailgunMortarTileEntity, IMultiblockRecipe> implements IEBlockInterfaces.IPlayerInteraction
{
	public static TileEntityType<RailgunMortarTileEntity> TYPE;

	public static class Master extends RailgunMortarTileEntity
	{
		public static TileEntityType<RailgunMortarTileEntity> TYPE;

		public Master()
		{
			super(TYPE);
		}
	}

	private final LazyOptional<IEInventoryHandler> insertionHandler;
	public NonNullList<ItemStack> inventory;
	public int reloadTicks = 0;
	private boolean constantlyFiring = false;
	public Vec3d targetPos = null;
	public float prevRotation;
	public float rotation;
	private boolean weaponMode = false;
	private boolean shouldShoot = false;

	private RailgunMortarTileEntity(TileEntityType<RailgunMortarTileEntity> type)
	{
		super(QIMultiblocks.RAILGUN_MORTAR, 16000, true, type);
		this.inventory = NonNullList.withSize(2, ItemStack.EMPTY);
		this.insertionHandler = registerConstantCap(new IEInventoryHandler(2, this, 0, new boolean[]{true, false}, new boolean[2]));
	}

	public RailgunMortarTileEntity()
	{
		this(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		reloadTicks = nbt.getInt("reloadTicks");
		constantlyFiring = nbt.getBoolean("constantlyFiring");

		if (nbt.contains("posX"))
		{
			targetPos = new Vec3d(nbt.getDouble("posX"), nbt.getDouble("posY"), nbt.getDouble("posZ"));
		}
		else
		{
			targetPos = null;
		}

		rotation = nbt.getFloat("rotation");

		if (!descPacket)
		{
			inventory = Utils.readInventory(nbt.getList("inventory", 10), 6);
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);

		nbt.putInt("reloadTicks", reloadTicks);
		nbt.putBoolean("constantlyFiring", constantlyFiring);

		if (targetPos != null)
		{
			nbt.putDouble("posX", targetPos.getX());
			nbt.putDouble("posY", targetPos.getY());
			nbt.putDouble("posZ", targetPos.getZ());
		}

		nbt.putFloat("rotation", rotation);

		if (!descPacket)
		{
			nbt.put("inventory", Utils.writeInventory(inventory));
		}
	}

	@Override
	public void tick()
	{
		super.tick();

		if (isDummy())
		{
			return;
		}

		targetPos = null;
		if (!inventory.get(1).isEmpty())
		{
			if (inventory.get(1).getItem() instanceof ICoordinateProvider)
			{
				targetPos = ((ICoordinateProvider) inventory.get(1).getItem()).getCoordinate(world, inventory.get(1));
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

		if (world.isRemote || isRSDisabled())
		{
			return;
		}

		boolean update = false;

		Direction dir = getFacing();
		if (reloadTicks == 0)
		{
			if (targetPos != null)
			{
				if (!inventory.get(0).isEmpty())
				{
					int fireCount = Math.min(weaponMode ? 1 : 32, inventory.get(0).getCount());
					ItemStack toFire = inventory.get(0).copy();
					toFire.setCount(fireCount);
					inventory.get(0).shrink(fireCount);

					Vec3i basePositionBlock = getPos().offset(dir);
					Vec3d basePosition = new Vec3d(basePositionBlock.getX() + .5f, basePositionBlock.getY() + 10F / 16f, basePositionBlock.getZ() + .5f);

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

					//ItemEntity itemEntity = new MortarItemEntity(world, barrelPosition.x, barrelPosition.y, barrelPosition.z, toFire);
					ItemEntity itemEntity = new MortarItemEntity(
							world,
							barrelPosition.x, barrelPosition.y, barrelPosition.z,
							toFire,
							targetPos.x + .5f, targetPos.z + .5f,
							facing2.scale(6 / facing2.y).x,
							facing2.scale(6 / facing2.y).z,
							true
					);
					world.addEntity(itemEntity);

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
	public boolean interact(Direction direction, PlayerEntity player, Hand hand, ItemStack heldItem, float v, float v1, float v2)
	{
		System.out.println(this.posInMultiblock);

		RailgunMortarTileEntity master = master();
		if (master != null && master.isStackValid(1, heldItem) && !heldItem.isEmpty())
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
		return true;
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
		if (slot == 1)
		{
			return stack.getItem() instanceof ICoordinateProvider;
		}
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
		if ((facing == null || (new BlockPos(0, 0, 1)).equals(this.posInMultiblock)) && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			RailgunMortarTileEntity master = (RailgunMortarTileEntity) this.master();
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