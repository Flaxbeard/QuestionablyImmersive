package net.flaxbeard.questionablyimmersive.common.gui;

import blusunrize.immersiveengineering.common.gui.IESlot.Ghost;
import blusunrize.immersiveengineering.common.gui.TileInventory;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QIBaseContainer<T extends TileEntity> extends Container
{
	public T tile;
	@Nullable
	public IInventory inv;
	public int slotCount;

	public QIBaseContainer(PlayerInventory inventoryPlayer, T tile, int id)
	{
		super(QIGuiHandler.getContainerTypeFor(tile), id);
		this.tile = tile;
		if (tile instanceof IIEInventory)
		{
			this.inv = new TileInventory(tile);
		}

	}

	public boolean canInteractWith(@Nonnull PlayerEntity player)
	{
		return this.inv != null && this.inv.isUsableByPlayer(player);
	}

	@Nonnull
	public ItemStack slotClick(int id, int button, ClickType clickType, PlayerEntity player)
	{
		Slot slot = id < 0 ? null : (Slot) this.inventorySlots.get(id);
		if (!(slot instanceof Ghost))
		{
			return super.slotClick(id, button, clickType, player);
		}
		else
		{
			ItemStack stack = ItemStack.EMPTY;
			ItemStack stackSlot = slot.getStack();
			if (!stackSlot.isEmpty())
			{
				stack = stackSlot.copy();
			}

			if (button == 2)
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				PlayerInventory playerInv;
				ItemStack stackHeld;
				if (button != 0 && button != 1)
				{
					if (button == 5)
					{
						playerInv = player.inventory;
						stackHeld = playerInv.getItemStack();
						if (!slot.getHasStack())
						{
							slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
						}
					}
				}
				else
				{
					playerInv = player.inventory;
					stackHeld = playerInv.getItemStack();
					if (stackSlot.isEmpty())
					{
						if (!stackHeld.isEmpty() && slot.isItemValid(stackHeld))
						{
							slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
						}
					}
					else if (stackHeld.isEmpty())
					{
						slot.putStack(ItemStack.EMPTY);
					}
					else if (slot.isItemValid(stackHeld))
					{
						slot.putStack(Utils.copyStackWithAmount(stackHeld, 1));
					}
				}
			}

			return stack;
		}
	}

	@Nonnull
	public ItemStack transferStackInSlot(PlayerEntity player, int slot)
	{
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slotObject = (Slot) this.inventorySlots.get(slot);
		if (slotObject != null && slotObject.getHasStack())
		{
			ItemStack itemstack1 = slotObject.getStack();
			itemstack = itemstack1.copy();
			if (slot < this.slotCount)
			{
				if (!this.mergeItemStack(itemstack1, this.slotCount, this.inventorySlots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(itemstack1, 0, this.slotCount, false))
			{
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty())
			{
				slotObject.putStack(ItemStack.EMPTY);
			}
			else
			{
				slotObject.onSlotChanged();
			}
		}

		return itemstack;
	}

	protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection)
	{
		return super.mergeItemStack(stack, startIndex, endIndex, reverseDirection);
	}

	public void onContainerClosed(PlayerEntity playerIn)
	{
		super.onContainerClosed(playerIn);
		if (this.inv != null)
		{
			this.inv.closeInventory(playerIn);
		}

	}
}
