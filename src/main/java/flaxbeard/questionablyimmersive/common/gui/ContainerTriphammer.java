package flaxbeard.questionablyimmersive.common.gui;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.InventoryTile;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityTriphammer;
import flaxbeard.questionablyimmersive.common.network.GUIUpdatePacket;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class ContainerTriphammer extends ContainerIEBase<TileEntityTriphammer> implements GUIUpdatePacket.IPacketReceiver
{

	private final EntityPlayer player;
	public int maximumCost;
	public int materialCost;

	public ContainerTriphammer(InventoryPlayer inventoryPlayer, TileEntityTriphammer tile)
	{
		super(inventoryPlayer, tile);
		this.inv = new InventoryTile(tile)
		{
			@Override
			public void markDirty()
			{
				super.markDirty();
				ContainerTriphammer.this.onCraftMatrixChanged(this);
			}
		};

		player = inventoryPlayer.player;

		addSlotToContainer(new Slot(this.inv, 0, 27, 47));
		addSlotToContainer(new Slot(this.inv, 1, 76, 47));
		addSlotToContainer(new Slot(this.inv, 2, 134, 47)
		{
			/**
			 * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
			 */
			public boolean isItemValid(ItemStack stack)
			{
				return false;
			}
			/**
			 * Return whether this slot's stack can be taken from this slot.
			 */
			public boolean canTakeStack(EntityPlayer playerIn)
			{
				return (playerIn.capabilities.isCreativeMode || playerIn.experienceLevel >= ContainerTriphammer.this.maximumCost) && ContainerTriphammer.this.maximumCost > 0 && this.getHasStack();
			}
			public ItemStack onTake(EntityPlayer thePlayer, ItemStack stack)
			{
				if (!thePlayer.capabilities.isCreativeMode)
				{
					thePlayer.addExperienceLevel(-ContainerTriphammer.this.maximumCost);
				}

				float breakChance = net.minecraftforge.common.ForgeHooks.onAnvilRepair(thePlayer, stack, ContainerTriphammer.this.inv.getStackInSlot(0), ContainerTriphammer.this.inv.getStackInSlot(1));

				ContainerTriphammer.this.inv.setInventorySlotContents(0, ItemStack.EMPTY);

				if (ContainerTriphammer.this.materialCost > 0)
				{
					ItemStack itemstack = ContainerTriphammer.this.inv.getStackInSlot(1);

					if (!itemstack.isEmpty() && itemstack.getCount() > ContainerTriphammer.this.materialCost)
					{
						itemstack.shrink(ContainerTriphammer.this.materialCost);
						ContainerTriphammer.this.inv.setInventorySlotContents(1, itemstack);
					}
					else
					{
						ContainerTriphammer.this.inv.setInventorySlotContents(1, ItemStack.EMPTY);
					}
				}
				else
				{
					ContainerTriphammer.this.inv.setInventorySlotContents(1, ItemStack.EMPTY);
				}

				ContainerTriphammer.this.maximumCost = 0;
				/*IBlockState iblockstate = player.world.getBlockState(blockPosIn);

				if (!thePlayer.capabilities.isCreativeMode && !worldIn.isRemote && iblockstate.getBlock() == Blocks.ANVIL && thePlayer.getRNG().nextFloat() < breakChance)
				{
					int l = ((Integer)iblockstate.getValue(BlockAnvil.DAMAGE)).intValue();
					++l;

					if (l > 2)
					{
						worldIn.setBlockToAir(blockPosIn);
						worldIn.playEvent(1029, blockPosIn, 0);
					}
					else
					{
						worldIn.setBlockState(blockPosIn, iblockstate.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(l)), 2);
						worldIn.playEvent(1030, blockPosIn, 0);
					}
				}
				else if (!worldIn.isRemote)
				{
					worldIn.playEvent(1030, blockPosIn, 0);
				}*/

				return stack;
			}
		});

		slotCount = 2;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 142));

		updateRepairOutput();
	}

	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn)
	{
		super.onCraftMatrixChanged(inventoryIn);

		if (inventoryIn == inv)
		{
			if (!tile.getWorld().isRemote)
			{
				ItemStack itemstack = inv.getStackInSlot(0);
				if (itemstack.isEmpty())
				{
					updateItemName("");
				}
			}
			this.updateRepairOutput();
		}
	}

	public void updateRepairOutput()
	{
		ItemStack itemstack = inv.getStackInSlot(0);
		this.maximumCost = 1;
		int i = 0;
		int j = 0;
		int k = 0;


		if (itemstack.isEmpty())
		{
			inv.setInventorySlotContents(2, ItemStack.EMPTY);
			this.maximumCost = 0;
		}
		else
		{
			ItemStack outputItem = itemstack.copy();
			ItemStack itemstack2 = inv.getStackInSlot(1);
			Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(outputItem);
			j = j + itemstack.getRepairCost() + (itemstack2.isEmpty() ? 0 : itemstack2.getRepairCost());
			this.materialCost = 0;
			boolean isEnchantedBook = false;

			if (!itemstack2.isEmpty())
			{
				// TODO if (!net.minecraftforge.common.ForgeHooks.onAnvilChange(this, itemstack, itemstack2, inv, repairedItemName, j)) return; // TODO
				isEnchantedBook = itemstack2.getItem() == Items.ENCHANTED_BOOK && !ItemEnchantedBook.getEnchantments(itemstack2).hasNoTags();

				if (outputItem.isItemStackDamageable() && outputItem.getItem().getIsRepairable(itemstack, itemstack2))
				{
					int l2 = Math.min(outputItem.getItemDamage(), outputItem.getMaxDamage() / 4);

					if (l2 <= 0)
					{
						inv.setInventorySlotContents(2, ItemStack.EMPTY);
						this.maximumCost = 0;
						return;
					}

					int i3;

					for (i3 = 0; l2 > 0 && i3 < itemstack2.getCount(); ++i3)
					{
						int j3 = outputItem.getItemDamage() - l2;
						outputItem.setItemDamage(j3);
						++i;
						l2 = Math.min(outputItem.getItemDamage(), outputItem.getMaxDamage() / 4);
					}

					this.materialCost = i3;
				}
				else
				{
					if (!isEnchantedBook && (outputItem.getItem() != itemstack2.getItem() || !outputItem.isItemStackDamageable()))
					{
						inv.setInventorySlotContents(2, ItemStack.EMPTY);
						this.maximumCost = 0;

						return;
					}

					if (outputItem.isItemStackDamageable() && !isEnchantedBook)
					{
						int l = itemstack.getMaxDamage() - itemstack.getItemDamage();
						int i1 = itemstack2.getMaxDamage() - itemstack2.getItemDamage();
						int j1 = i1 + outputItem.getMaxDamage() * 12 / 100;
						int k1 = l + j1;
						int l1 = outputItem.getMaxDamage() - k1;

						if (l1 < 0)
						{
							l1 = 0;
						}

						if (l1 < outputItem.getItemDamage()) // vanilla uses metadata here instead of damage.
						{
							outputItem.setItemDamage(l1);
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
							int i2 = map.containsKey(enchantment1) ? ((Integer)map.get(enchantment1)).intValue() : 0;
							int j2 = ((Integer)map1.get(enchantment1)).intValue();
							j2 = i2 == j2 ? j2 + 1 : Math.max(j2, i2);
							boolean canApplyEnchant = enchantment1.canApply(itemstack);

							if (this.player.capabilities.isCreativeMode || itemstack.getItem() == Items.ENCHANTED_BOOK)
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
						inv.setInventorySlotContents(2, ItemStack.EMPTY);
						this.maximumCost = 0;
						return;
					}
				}
			}

			if (StringUtils.isBlank(tile.repairedItemName))
			{
				if (itemstack.hasDisplayName())
				{
					k = 1;
					i += k;
					outputItem.clearCustomName();
				}
			}
			else if (!tile.repairedItemName.equals(itemstack.getDisplayName()))
			{
				k = 1;
				i += k;
				outputItem.setStackDisplayName(tile.repairedItemName);
			}
			if (isEnchantedBook && !outputItem.getItem().isBookEnchantable(outputItem, itemstack2)) outputItem = ItemStack.EMPTY;

			this.maximumCost = j + i;

			if (i <= 0)
			{
				outputItem = ItemStack.EMPTY;
			}

			if (k == i && k > 0 && this.maximumCost >= 40)
			{
				this.maximumCost = 39;
			}

			if (this.maximumCost >= 40 && !this.player.capabilities.isCreativeMode)
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

			inv.setInventorySlotContents(2, outputItem);

			this.detectAndSendChanges();
		}
	}

	/*@Override
	public void addListener(IContainerListener listener)
	{
		super.addListener(listener);
		listener.sendWindowProperty(this, 0, this.maximumCost);
	}*/

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int id, int data)
	{
		if (id == 0)
		{
			this.maximumCost = data;
		}
	}

	/**
	 * used by the Anvil GUI to update the Item Name being typed by the player
	 */
	public void updateItemName(String newName)
	{
		System.out.println("NEW NAME " + newName);
		tile.repairedItemName = newName;

		if (this.getSlot(2).getHasStack())
		{
			ItemStack itemstack = this.getSlot(2).getStack();

			if (StringUtils.isBlank(newName))
			{
				itemstack.clearCustomName();
			}
			else
			{
				itemstack.setStackDisplayName(tile.repairedItemName);
			}
		}

		this.updateRepairOutput();
	}


	@Override
	public void handlePacket(int messageId, NBTTagCompound data)
	{
		if (messageId == 0)
		{
			updateItemName(data.getString("name"));
		}
	}
}
