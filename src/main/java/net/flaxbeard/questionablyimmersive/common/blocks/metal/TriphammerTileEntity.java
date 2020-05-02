package net.flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.shapes.CachedVoxelShapes;
import blusunrize.immersiveengineering.common.util.shapes.MultiblockCacheKey;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import net.flaxbeard.questionablyimmersive.common.QIConfig;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlockInterfaces;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlocks;
import net.flaxbeard.questionablyimmersive.common.blocks.TriphammerAnvilBlock;
import net.flaxbeard.questionablyimmersive.common.blocks.multiblocks.QIMultiblocks;
import net.flaxbeard.questionablyimmersive.common.util.AnvilUtils;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DirectionalPlaceContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class TriphammerTileEntity extends PoweredMultiblockTileEntity<TriphammerTileEntity, IMultiblockRecipe> implements QIBlockInterfaces.IInteractionObjectQI, IEBlockInterfaces.IBlockBounds
{
	public static TileEntityType<TriphammerTileEntity> TYPE;

	@Nullable
	@Override
	public QIBlockInterfaces.IInteractionObjectQI getGuiMaster()
	{
		return this.master();
	}

	@Override
	public boolean canUseGui(PlayerEntity var1)
	{
		return formed && isAnvilMode;
	}

	public static class Master extends TriphammerTileEntity
	{
		public static TileEntityType<TriphammerTileEntity> TYPE;

		public Master()
		{
			super(TYPE);
		}
	}

	private Method getStateForPlacement = ObfuscationReflectionHelper.findMethod(BlockItem.class, "func_195945_b", BlockItemUseContext.class);
	public NonNullList<ItemStack> inventory;
	LazyOptional<IItemHandler> insertionHandlerAbove = registerConstantCap(new IEInventoryHandler(3, this, 0, new boolean[]{true, false, false}, new boolean[]{false, false, true})
	{
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			if (isAnvilMode)
			{
				ItemStack result = super.insertItem(slot, stack, simulate);
				if (slot < 2 && !simulate && !(ItemStack.areItemStacksEqual(stack, result) || ItemStack.areItemStackTagsEqual(stack, result)))
				{
					TriphammerTileEntity.this.updateRepairOutput();
				}
				return result;
			} else
			{
				ItemStack og = stack.copy();
				stack = stack.copy();

				BlockPos targetPos = getBlockPosForPos(new BlockPos(1, 0, 3));
				if (stack.getItem() instanceof BlockItem)
				{
					BlockItem bi = (BlockItem) stack.getItem();
					if (simulate)
					{
						DirectionalPlaceContext context = new DirectionalPlaceContext(world, targetPos, getFacing(), stack, Direction.DOWN);
						try
						{
							BlockState state = (BlockState) getStateForPlacement.invoke(bi, context);
							if (context.canPlace() && state != null && state.isValidPosition(world, targetPos))
							{
								stack.setCount(stack.getCount() - 1);
							}
							return stack;
						} catch (IllegalAccessException | InvocationTargetException e)
						{
							e.printStackTrace();
						}
					}
					ActionResultType result = bi.tryPlace(new DirectionalPlaceContext(world, targetPos, getFacing(), stack, Direction.DOWN));
				}
				return stack;
			}
		}
	});
	LazyOptional<IItemHandler> insertionHandlerBelow = registerConstantCap(new IEInventoryHandler(3, this, 0, new boolean[]{false, true, false}, new boolean[]{false, false, true})
	{
		@Override
		public boolean isItemValid(int slot, @Nonnull ItemStack stack)
		{
			return isAnvilMode;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
		{
			ItemStack result = super.insertItem(slot, stack, simulate);
			if (slot < 2 && !simulate && !(ItemStack.areItemStacksEqual(stack, result) || ItemStack.areItemStackTagsEqual(stack, result)))
			{
				TriphammerTileEntity.this.updateRepairOutput();
			}
			return result;
		}
	});

	public int ticks = 0;
	public int fallingTicks = 0;

	public String repairedItemName = "";

	private int materialCost;
	private int maximumCost;
	private int progress;
	private int maxProgress;

	private int newAnvilCooldown;

	public boolean active;
	public boolean isAnvilMode;

	public ItemStack output = ItemStack.EMPTY;

	private FakePlayer fakePlayer;

	private static final CachedVoxelShapes<MultiblockCacheKey> SHAPES = new CachedVoxelShapes<MultiblockCacheKey>(TriphammerTileEntity::getShape);

	private TriphammerTileEntity(TileEntityType<TriphammerTileEntity> type)
	{
		super(QIMultiblocks.TRIPHAMMER, 16000, false, type);
		this.inventory = NonNullList.withSize(3, ItemStack.EMPTY);
	}

	public TriphammerTileEntity()
	{
		this(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);

		inventory = Utils.readInventory(nbt.getList("inventory", 10), 3);

		repairedItemName = nbt.getString("repairedItemName");
		maximumCost = nbt.getInt("maximumCost");
		materialCost = nbt.getInt("materialCost");
		progress = nbt.getInt("progress");
		maxProgress = nbt.getInt("maxProgress");
		active = nbt.getBoolean("active");
		isAnvilMode = nbt.getBoolean("isAnvilMode");
		if (!active)
		{
			ticks = nbt.getInt("ticks");
		}
		newAnvilCooldown = nbt.getInt("newAnvilCooldown");

		output = ItemStack.read(nbt.getCompound("output")).copy();
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);

		nbt.put("inventory", Utils.writeInventory(inventory));

		nbt.putString("repairedItemName", repairedItemName);
		nbt.putInt("maximumCost", maximumCost);
		nbt.putInt("materialCost", materialCost);
		nbt.putInt("progress", progress);
		nbt.putInt("maxProgress", maxProgress);
		nbt.putBoolean("active", active);
		nbt.putBoolean("isAnvilMode", isAnvilMode);
		nbt.putInt("ticks", ticks);
		nbt.putInt("newAnvilCooldown", newAnvilCooldown);

		nbt.put("output", output.serializeNBT());
	}

	@Override
	public void disassemble()
	{
		removeAnvil();
		super.disassemble();
	}

	public void addAnvil()
	{
		BlockPos targetPos = getBlockPosForPos(new BlockPos(1, 0, 3));
		BlockState targetedBlock = world.getBlockState(targetPos);

		if (targetedBlock.getBlock() == Blocks.ANVIL || targetedBlock.getBlock() == Blocks.DAMAGED_ANVIL || targetedBlock.getBlock() == Blocks.CHIPPED_ANVIL)
		{
			Direction facing = targetedBlock.get(AnvilBlock.FACING);

			BlockState baseState = QIBlocks.Metal.TRIPHAMMER_ANVIL.getDefaultState();
			if (targetedBlock.getBlock() == Blocks.CHIPPED_ANVIL)
			{
				baseState = QIBlocks.Metal.TRIPHAMMER_ANVIL_CHIPPED.getDefaultState();
			} else if (targetedBlock.getBlock() == Blocks.DAMAGED_ANVIL)
			{
				baseState = QIBlocks.Metal.TRIPHAMMER_ANVIL_DAMAGED.getDefaultState();
			}
			world.setBlockState(targetPos, baseState.with(IEProperties.FACING_HORIZONTAL, facing));
		}
	}

	public void removeAnvil()
	{
		BlockPos targetPos = getBlockPosForPos(new BlockPos(1, 0, 3));
		BlockState targetedBlock = world.getBlockState(targetPos);

		if (targetedBlock.getBlock() instanceof TriphammerAnvilBlock)
		{
			world.setBlockState(targetPos, ((TriphammerAnvilBlock) targetedBlock.getBlock()).toOriginal(targetedBlock));
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

		boolean update = false;
		if (!world.isRemote)
		{
			boolean wasAnvilMode = isAnvilMode;
			BlockPos targetPos = getBlockPosForPos(new BlockPos(1, 0, 3));

			BlockState targetedBlock = world.getBlockState(targetPos);

			if (targetedBlock.getBlock() == Blocks.ANVIL || targetedBlock.getBlock() == Blocks.DAMAGED_ANVIL || targetedBlock.getBlock() == Blocks.CHIPPED_ANVIL)
			{
				addAnvil();
			}
			isAnvilMode = targetedBlock.getBlock() instanceof TriphammerAnvilBlock;
			if (wasAnvilMode && !isAnvilMode)
			{
				setMaximumCost(0);

				progress = 0;
			} else if (isAnvilMode && !wasAnvilMode)
			{
				newAnvilCooldown = 1;
			}

			if (!isAnvilMode && newAnvilCooldown > 0)
			{
				newAnvilCooldown--;

				if (newAnvilCooldown == 0)
				{
					for (int i = 0; i < inventory.size(); i++)
					{
						InventoryHelper.spawnItemStack(world, targetPos.getX() + .5, targetPos.getY() + .5, targetPos.getZ() + .5, inventory.get(i));
						inventory.set(i, ItemStack.EMPTY);
					}
				}
			}

			if (isAnvilMode != wasAnvilMode)
			{
				update = true;
			}

			boolean wasActive = active;
			active = false;
			if (!isRSDisabled())
			{
				boolean shouldConsume = isAnvilMode ?
						(inventory.get(2).isEmpty() && progress < maxProgress && !output.isEmpty()) :
						!targetedBlock.getBlock().isAir(
								targetedBlock,
								world,
								targetPos
						) && targetedBlock.getBlockHardness(world, targetPos) != -1;
				if (shouldConsume)
				{
					int consumed = QIConfig.TRIPHAMMER.costPerTick.get();
					int extracted = energyStorage.extractEnergy(consumed, true);

					if (extracted >= consumed)
					{
						energyStorage.extractEnergy(consumed, false);
						active = true;

						progress++;

						if (isAnvilMode)
						{
							if (progress == maxProgress)
							{
								float breakChance = 0.12f;

								inventory.set(2, output);
								output = ItemStack.EMPTY;

								inventory.set(0, ItemStack.EMPTY);

								if (materialCost > 0)
								{
									ItemStack itemstack = inventory.get(1);

									if (!itemstack.isEmpty() && itemstack.getCount() > materialCost)
									{
										itemstack.shrink(materialCost);
										inventory.set(1, itemstack);
									} else
									{
										inventory.set(1, ItemStack.EMPTY);
									}
								} else
								{
									inventory.set(1, ItemStack.EMPTY);
								}
								setMaximumCost(0);

								progress = 0;

								update = true;

								if (world.rand.nextFloat() < breakChance)
								{
									targetedBlock = world.getBlockState(targetPos);
									BlockState damagedStack = TriphammerAnvilBlock.damage(targetedBlock);
									if (damagedStack == null)
									{
										world.removeBlock(targetPos, false);
										world.playEvent(1029, targetPos, 0);
										newAnvilCooldown = 60;
									} else
									{
										world.setBlockState(targetPos, damagedStack, 2);
										world.playEvent(1030, targetPos, 0);
									}
								}
							}
						} else
						{
							maxProgress = (int) Math.ceil(targetedBlock.getBlockHardness(world, targetPos) * QIConfig.TRIPHAMMER.ticksPerHardness.get());
							if (progress >= maxProgress && ticks % 60 == 35)
							{
								ItemStack blockStack = new ItemStack(targetedBlock.getBlock());
								CrusherRecipe recipe = CrusherRecipe.findRecipe(blockStack);
								if (recipe != null)
								{
									world.destroyBlock(targetPos, false);
									NonNullList<ItemStack> outputs = recipe.getActualItemOutputs(this);
									for (ItemStack stack : outputs)
									{
										ItemStack out = stack.copy();
										float size = (float) (out.getCount() * QIConfig.TRIPHAMMER.relativeYield.get());
										out.setCount((int) size);
										if (world.rand.nextFloat() < size % 1)
										{
											out.setCount((int) size + 1);
										}
										Utils.dropStackAtPos(world, targetPos, out);
									}
								} else
								{
									world.destroyBlock(targetPos, true);
								}
								progress = 0;
								update = true;
								world.sendBlockBreakProgress(0, targetPos, -1);
							}
						}

					}
				} else
				{
					progress = 0;
				}
			}

			if (wasActive != active)
			{
				update = true;
			}
		}

		if (active)
		{
			ticks = (ticks + 1) % 120;
			if (ticks == 0)
			{
				fallingTicks = 0;
			}
			fallingTicks = Math.max(fallingTicks, ticks);

			if (world.isRemote)
			{
				progress++;
			}
		} else
		{
			if (fallingTicks % 60 >= 30 && fallingTicks % 60 < 40)
			{
				fallingTicks++;
			}
		}

		Direction facing = getFacing();
		if (world.isRemote && fallingTicks % 60 == 37)
		{
			if (isAnvilMode)
			{
				BlockPos anvilPos = getPos().offset(facing, -3);
				for (int i = 0; i < 10; i++)
				{
					world.addParticle(new ItemParticleData(ParticleTypes.ITEM, inventory.get(0)),
							anvilPos.getX() + .5f, anvilPos.getY(), anvilPos.getZ() + .5f,
							(this.world.rand.nextFloat() - .5f) * .5f, this.world.rand.nextFloat() * .2f, (this.world.rand.nextFloat() - .5f) * .5f);
				}
				world.playSound(
						getPos().getX(),
						getPos().getY(),
						getPos().getZ(),
						SoundEvents.BLOCK_ANVIL_LAND,
						SoundCategory.BLOCKS,
						0.3F,
						this.world.rand.nextFloat() * 0.1F + 0.75F,
						false
				);
			} else if (active)
			{
				BlockPos targetPos = getBlockPosForPos(new BlockPos(1, 0, 3));

				BlockState targetedBlock = world.getBlockState(targetPos);

				world.sendBlockBreakProgress(0, targetPos, (progress * 10) / maxProgress);

				SoundType soundType = targetedBlock.getBlock().getSoundType(targetedBlock, world, targetPos, null);
				world.playSound(
						targetPos.getX(),
						targetPos.getY(),
						targetPos.getZ(),
						soundType.getBreakSound(),
						SoundCategory.BLOCKS,
						(soundType.getVolume() + 1.0F) / 2.0F,
						soundType.getPitch() * 0.8F,
						false
				);

				for (int i = 0; i < 10; i++)
				{
					world.addParticle(new BlockParticleData(ParticleTypes.BLOCK, targetedBlock),
							targetPos.getX() + .5f, targetPos.getY() + 1, targetPos.getZ() + .5f,
							(this.world.rand.nextFloat() - .5f) * .5f, this.world.rand.nextFloat() * .2f, (this.world.rand.nextFloat() - .5f) * .5f);
				}
			}
		}

		if (update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	public int getMaterialCost()
	{
		return materialCost;
	}

	public void setMaterialCost(int materialCost)
	{
		this.materialCost = materialCost;
	}

	public int getMaximumCost()
	{
		return maximumCost;
	}

	public void setMaximumCost(int maximumCost)
	{
		this.maximumCost = maximumCost;
		this.maxProgress = QIConfig.TRIPHAMMER.ticksPerLevel.get() * maximumCost;
	}


	@Override
	public Set<BlockPos> getEnergyPos()
	{
		return ImmutableSet.of(new BlockPos(2, 1, 0));
	}

	@Override
	public Set<BlockPos> getRedstonePos()
	{
		return ImmutableSet.of(new BlockPos(2, 1, 0));
	}

	@Override
	public VoxelShape getBlockBounds()
	{
		return SHAPES.get(new MultiblockCacheKey(this));
	}

	private static List<AxisAlignedBB> getShape(MultiblockCacheKey key)
	{
		BlockPos posInMultiblock = key.posInMultiblock;
		Direction fl = key.facing;
		Direction fw = key.facing.rotateY();
		if (key.mirrored)
		{
			fw = fw.getOpposite();
		}

		ArrayList list = new ArrayList<AxisAlignedBB>();
		float minX;
		float maxX;
		float minZ;
		float maxZ;

		if (posInMultiblock.getX() == 2 && posInMultiblock.getZ() == 0)
		{
			list.add(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
			return list;
		}

		if (posInMultiblock.getY() == 0 && posInMultiblock.getZ() < 3)
		{
			if (posInMultiblock.getX() == 1)
			{
				list.add(new AxisAlignedBB(0, 0, 0, 1, 0.5, 1));
			} else if (posInMultiblock.getX() == 0)
			{
				minX = fw == Direction.EAST ? 0.5F : 0F;
				minZ = fw == Direction.SOUTH ? 0.5F : 0F;
				maxX = fw == Direction.WEST ? 0.5F : 1F;
				maxZ = fw == Direction.NORTH ? 0.5F : 1F;
				list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 0.5, maxZ));
			} else
			{
				minX = fw == Direction.WEST ? 0.5F : 0F;
				minZ = fw == Direction.NORTH ? 0.5F : 0F;
				maxX = fw == Direction.EAST ? 0.5F : 1F;
				maxZ = fw == Direction.SOUTH ? 0.5F : 1F;
				list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 0.5, maxZ));
			}
		}

		if (new BlockPos(1, 1, 0).equals(posInMultiblock))
		{
			minX = fw == Direction.EAST ? 7F / 16F : 0F;
			minZ = fw == Direction.SOUTH ? 7F / 16F : 0F;
			maxX = fw == Direction.WEST ? 7F / 16F : 1F;
			maxZ = fw == Direction.NORTH ? 7F / 16F : 1F;
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ));
			return list;
		}

		if (new BlockPos(1, 0, 1).equals(posInMultiblock))
		{
			minX = fl == Direction.WEST ? 4F / 16F : (fl == Direction.EAST ? 0F / 16F : -2F / 16F);
			minZ = fl == Direction.NORTH ? 4F / 16F : (fl == Direction.SOUTH ? 0F / 16F : -2F / 16F);
			maxX = fl == Direction.WEST ? 16F / 16F : (fl == Direction.EAST ? 12F / 16F : 4F / 16F);
			maxZ = fl == Direction.NORTH ? 16F / 16F : (fl == Direction.SOUTH ? 12F / 16F : 4F / 16F);
			list.add(new AxisAlignedBB(minX, 0.5, minZ, maxX, 1, maxZ));
		}

		if (new BlockPos(1, 1, 1).equals(posInMultiblock))
		{
			minX = fw == Direction.EAST || fw == Direction.WEST ? 6F / 16F : 0;
			minZ = fw == Direction.NORTH || fw == Direction.SOUTH ? 6F / 16F : 0;
			maxX = fw == Direction.EAST || fw == Direction.WEST ? 10F / 16F : 1;
			maxZ = fw == Direction.NORTH || fw == Direction.SOUTH ? 10F / 16F : 1;
			list.add(new AxisAlignedBB(minX, 2F / 16F, minZ, maxX, 14F / 16F, maxZ));

			minX = fl == Direction.WEST ? 8F / 16F : (fl == Direction.EAST ? 4F / 16F : 0);
			minZ = fl == Direction.NORTH ? 8F / 16F : (fl == Direction.SOUTH ? 4F / 16F : 0);
			maxX = fl == Direction.WEST ? 12F / 16F : (fl == Direction.EAST ? 8F / 16F : 1);
			maxZ = fl == Direction.NORTH ? 12F / 16F : (fl == Direction.SOUTH ? 8F / 16F : 1);
			list.add(new AxisAlignedBB(minX, 6F / 16F, minZ, maxX, 10F / 16F, maxZ));

			minX = fl == Direction.WEST ? 6F / 16F : (fl == Direction.EAST ? 2F / 16F : 0F / 16F);
			minZ = fl == Direction.NORTH ? 6F / 16F : (fl == Direction.SOUTH ? 2F / 16F : 0F / 16F);
			maxX = fl == Direction.WEST ? 14F / 16F : (fl == Direction.EAST ? 10F / 16F : 6F / 16F);
			maxZ = fl == Direction.NORTH ? 14F / 16F : (fl == Direction.SOUTH ? 10F / 16F : 6F / 16F);
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 8 / 16F, maxZ));
			return list;
		}
		if (new BlockPos(1, 1, 2).equals(posInMultiblock))
		{
			minX = fw == Direction.EAST || fw == Direction.WEST ? 6F / 16F : 0;
			minZ = fw == Direction.NORTH || fw == Direction.SOUTH ? 6F / 16F : 0;
			maxX = fw == Direction.EAST || fw == Direction.WEST ? 10F / 16F : 1;
			maxZ = fw == Direction.NORTH || fw == Direction.SOUTH ? 10F / 16F : 1;
			list.add(new AxisAlignedBB(minX, 2F / 16F, minZ, maxX, 14F / 16F, maxZ));
			return list;
		}
		if (new BlockPos(1, 1, 3).equals(posInMultiblock))
		{
			minX = fw == Direction.EAST || fw == Direction.WEST ? 4F / 16F : 0;
			minZ = fw == Direction.NORTH || fw == Direction.SOUTH ? 4F / 16F : 0;
			maxX = fw == Direction.EAST || fw == Direction.WEST ? 12F / 16F : 1;
			maxZ = fw == Direction.NORTH || fw == Direction.SOUTH ? 12F / 16F : 1;
			list.add(new AxisAlignedBB(minX, 0F / 16F, minZ, maxX, 16 / 16F, maxZ));
			return list;
		}
		return list;
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
			BlockState state = this.getBlockState();
			return super.getFacing();
		}
		return Direction.NORTH;
	}

	@Nullable
	public TriphammerTileEntity getTileForPos(BlockPos targetPosInMB)
	{
		BlockPos target = this.getBlockPosForPos(targetPosInMB);
		TileEntity tile = Utils.getExistingTileEntity(this.getWorldNonnull(), target);
		return tile instanceof TriphammerTileEntity ? (TriphammerTileEntity) tile : null;
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
		if (
				(
						facing == null
								|| (new BlockPos(1, 1, 3).equals(this.posInMultiblock)
								&& facing != Direction.UP)
				)
						&& capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			TriphammerTileEntity master = (TriphammerTileEntity) this.master();
			if (master != null)
			{
				return master.insertionHandlerAbove.cast();
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

	public void updateRepairOutput()
	{
		if (world.isRemote)
		{
			return;
		}

		if (fakePlayer == null)
		{
			fakePlayer = FakePlayerFactory.get((ServerWorld) this.world, new GameProfile(UUID.randomUUID(), "Anvil Man"));
		}
		AnvilUtils.RepairOutput repairOutput = AnvilUtils.updateRepairOutput(
				fakePlayer,
				repairedItemName,
				inventory.get(0).copy(),
				inventory.get(1).copy()
		);

		this.setMaterialCost(repairOutput.materialCost);
		this.setMaximumCost(repairOutput.maximumCost);

		this.setOutput(repairOutput.output);

		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	public void setOutput(ItemStack output)
	{
		if (output == null)
		{
			output = ItemStack.EMPTY;
		}

		if (output.hasTag() && output.getRepairCost() == 0)
		{
			output.getTag().remove("RepairCost");
			if (output.getTag().isEmpty())
			{
				output.setTag(null);
			} else
			{
				output.setRepairCost(0);
			}
		}

		if (this.output == null || !output.isItemEqual(this.output) || !ItemStack.areItemStackTagsEqual(output, this.output))
		{
			this.progress = 0;
		}
		this.output = output;

		//this.inventory.set(2, output);
	}

	public void updateItemName(String newName)
	{
		if (!newName.equals(repairedItemName))
		{
			this.progress = 0;
		}
		repairedItemName = newName;

		if (!output.isEmpty())
		{
			if (StringUtils.isBlank(newName))
			{
				output.clearCustomName();
			} else
			{
				output.setDisplayName(new StringTextComponent(this.repairedItemName));
			}
		}
	}

	public int getProgress()
	{
		return progress;
	}

	public int getMaxProgress()
	{
		return maxProgress;
	}
}