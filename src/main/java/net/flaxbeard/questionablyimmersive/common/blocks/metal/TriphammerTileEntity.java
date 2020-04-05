package net.flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import com.google.common.collect.ImmutableSet;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlockInterfaces;
import net.flaxbeard.questionablyimmersive.common.blocks.multiblocks.QIMultiblocks;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class TriphammerTileEntity extends PoweredMultiblockTileEntity<TriphammerTileEntity, IMultiblockRecipe> implements QIBlockInterfaces.IInteractionObjectQI
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

	public NonNullList<ItemStack> inventory;
	LazyOptional<IItemHandler> insertionHandler = registerConstantCap(new IEInventoryHandler(3, this, 0, new boolean[]{true, false, false}, new boolean[]{false, false, true})
	{
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

	public boolean active;
	public boolean isAnvilMode;

	public ItemStack output = ItemStack.EMPTY;

	private TriphammerTileEntity(TileEntityType<TriphammerTileEntity> type)
	{
		super(QIMultiblocks.TRIPHAMMER, 0, false, type);
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

		if (!descPacket)
		{
			output = ItemStack.read(nbt.getCompound("output"));
		}
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

		nbt.put("output", output.serializeNBT());
	}

	@Override
	public void tick()
	{
		super.tick();

		if (isDummy()) {
			return;
		}

		boolean update = false;
		if (!world.isRemote)
		{
			boolean wasAnvilMode = isAnvilMode;
			BlockPos targetPos = getBlockPosForPos(new BlockPos(1, 0, 3));

			BlockState targetedBlock = world.getBlockState(targetPos);
			isAnvilMode = targetedBlock.getBlock() instanceof AnvilBlock;
			if (wasAnvilMode && !isAnvilMode)
			{
				setMaximumCost(0);

				progress = 0;

				for (int i = 0; i < inventory.size(); i++)
				{
					InventoryHelper.spawnItemStack(world, targetPos.getX() + .5, targetPos.getY() + .5, targetPos.getZ() + .5, inventory.get(i));
					inventory.set(i, ItemStack.EMPTY);
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
						);
				if (shouldConsume)
				{
					int consumed =  0; // TODO Config.QIConfig.Triphammer.costPerTick;
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
									}
									else
									{
										inventory.set(1, ItemStack.EMPTY);
									}
								}
								else
								{
									inventory.set(1, ItemStack.EMPTY);
								}
								setMaximumCost(0);

								progress = 0;

								update = true;
							}
						}
						else
						{
							if (progress >= 200 && ticks % 60 == 35)
							{
								world.destroyBlock(targetPos, true);
								progress = 0;
								update = true;
							}
						}

					}
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
		}
		else
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
				BlockPos anvilPos = getPos().offset(facing, 3);
				for (int i = 0; i < 10; i++)
				{
					/*world.spawnParticle(EnumParticleTypes.ITEM_CRACK,
							anvilPos.getX() + .5f, anvilPos.getY(), anvilPos.getZ() + .5f,
							(this.world.rand.nextFloat() - .5f) * .5f, this.world.rand.nextFloat() * .2f, (this.world.rand.nextFloat() - .5f) * .5f,
							Item.getIdFromItem(inventory.get(0).getItem())
					); TODO */
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
			} else if (active) {
				BlockPos targetPos = getBlockPosForPos(new BlockPos(1, 0, 3));

				BlockState targetedBlock = world.getBlockState(targetPos);

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
			}
		}
		/*else if (world.isRemote && ticks % 60 == 0)
		{
			world.playSound(getPos().getX(), getPos().getY(), getPos().getZ(), SoundEvents.BLOCK_WOOD_HIT, SoundCategory.BLOCKS, 0.2F, .5f, false);
		}*/

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
		this.maxProgress = /*Config.QIConfig.Triphammer.ticksPerLevel*/ 40 * maximumCost;
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

	public void updateRepairOutput()
	{
		ItemStack itemstack = inventory.get(0);
		setMaximumCost(1);
		int i = 0;
		int j = 0;
		int k = 0;


		if (itemstack.isEmpty())
		{
			setOutput(ItemStack.EMPTY);
			setMaximumCost(0);
		}
		else
		{
			ItemStack outputItem = itemstack.copy();
			ItemStack itemstack2 = inventory.get(1);
			Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(outputItem);
			j = j + itemstack.getRepairCost() + (itemstack2.isEmpty() ? 0 : itemstack2.getRepairCost());
			setMaterialCost(0);
			boolean isEnchantedBook = false;

			if (!itemstack2.isEmpty())
			{
				// TODO if (!net.minecraftforge.common.ForgeHooks.onAnvilChange(this, itemstack, itemstack2, inv, repairedItemName, j)) return; // TODO
				isEnchantedBook = itemstack2.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(itemstack2).isEmpty();

				if (outputItem.isDamageable() && outputItem.getItem().getIsRepairable(itemstack, itemstack2))
				{
					int l2 = Math.min(outputItem.getDamage(), outputItem.getMaxDamage() / 4);

					if (l2 <= 0)
					{
						setOutput(ItemStack.EMPTY);
						setMaximumCost(0);
						return;
					}

					int i3;

					for (i3 = 0; l2 > 0 && i3 < itemstack2.getCount(); ++i3)
					{
						int j3 = outputItem.getDamage() - l2;
						outputItem.setDamage(j3);
						++i;
						l2 = Math.min(outputItem.getDamage(), outputItem.getMaxDamage() / 4);
					}

					setMaterialCost(i3);
				}
				else
				{
					if (!isEnchantedBook && (outputItem.getItem() != itemstack2.getItem() || !outputItem.isDamageable()))
					{
						setOutput(ItemStack.EMPTY);
						setMaximumCost(0);

						return;
					}

					if (outputItem.isDamageable() && !isEnchantedBook)
					{
						int l = itemstack.getMaxDamage() - itemstack.getDamage();
						int i1 = itemstack2.getMaxDamage() - itemstack2.getDamage();
						int j1 = i1 + outputItem.getMaxDamage() * 12 / 100;
						int k1 = l + j1;
						int l1 = outputItem.getMaxDamage() - k1;

						if (l1 < 0)
						{
							l1 = 0;
						}

						if (l1 < outputItem.getDamage()) // vanilla uses metadata here instead of damage.
						{
							outputItem.setDamage(l1);
							i += 2;
						}
					}

					Map<Enchantment, Integer> map1 = EnchantmentHelper.getEnchantments(itemstack2);
					boolean isApplicable = false;
					boolean isntApplicable = false;

					for (Enchantment enchantment1 : map1.keySet())
					{
						if (enchantment1 != null)
						{
							int i2 = map.containsKey(enchantment1) ? ((Integer) map.get(enchantment1)).intValue() : 0;
							int j2 = ((Integer) map1.get(enchantment1)).intValue();
							j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);
							boolean canApplyEnchant = enchantment1.canApply(itemstack);

							if (itemstack.getItem() == Items.ENCHANTED_BOOK)
							{
								canApplyEnchant = true;
							}

							for (Enchantment enchantment : map.keySet())
							{
								if (enchantment != enchantment1 && !enchantment1.isCompatibleWith(enchantment))
								{
									canApplyEnchant = false;
									++i;
								}
							}

							if (!canApplyEnchant)
							{
								isntApplicable = true;
							}
							else
							{
								isApplicable = true;

								if (j2 > enchantment1.getMaxLevel())
								{
									j2 = enchantment1.getMaxLevel();
								}

								map.put(enchantment1, Integer.valueOf(j2));
								int k3 = 0;

								switch (enchantment1.getRarity())
								{
									case COMMON:
										k3 = 1;
										break;
									case UNCOMMON:
										k3 = 2;
										break;
									case RARE:
										k3 = 4;
										break;
									case VERY_RARE:
										k3 = 8;
								}

								if (isEnchantedBook)
								{
									k3 = Math.max(1, k3 / 2);
								}

								i += k3 * j2;

								if (itemstack.getCount() > 1)
								{
									i = 40;
								}
							}
						}
					}

					if (isntApplicable && !isApplicable)
					{
						setOutput(ItemStack.EMPTY);
						setMaximumCost(0);
						return;
					}
				}
			}

			if (StringUtils.isBlank(this.repairedItemName)) {
				if (itemstack.hasDisplayName()) {
					k = 1;
					i += k;
					itemstack.clearCustomName();
				}
			} else if (!this.repairedItemName.equals(itemstack.getDisplayName().getString())) {
				k = 1;
				i += k;
				itemstack.setDisplayName(new StringTextComponent(this.repairedItemName));
			}
			if (isEnchantedBook && !outputItem.getItem().isBookEnchantable(outputItem, itemstack2)) outputItem = ItemStack.EMPTY;

			setMaximumCost(j + i);

			if (i <= 0)
			{
				outputItem = ItemStack.EMPTY;
			}

			if (k == i && k > 0 && getMaximumCost() >= 40)
			{
				setMaximumCost(39);
			}

			if (getMaximumCost() >= 40)
			{
				outputItem = ItemStack.EMPTY;
			}

			if (!outputItem.isEmpty())
			{
				int k2 = outputItem.getRepairCost();

				if (!itemstack2.isEmpty() && k2 < itemstack2.getRepairCost())
				{
					k2 = itemstack2.getRepairCost();
				}

				if (k != i || k == 0)
				{
					k2 = k2 * 2 + 1;
				}

				outputItem.setRepairCost(k2);
				EnchantmentHelper.setEnchantments(map, outputItem);
			}

			setOutput(outputItem);
		}
	}

	public void setOutput(ItemStack output)
	{
		if (output == null)
		{
			output = ItemStack.EMPTY;
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
			}
			else
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