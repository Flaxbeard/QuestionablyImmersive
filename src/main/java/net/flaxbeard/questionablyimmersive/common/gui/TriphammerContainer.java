package net.flaxbeard.questionablyimmersive.common.gui;

import blusunrize.immersiveengineering.common.gui.TileInventory;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.TriphammerTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriphammerContainer extends QIBaseContainer<TriphammerTileEntity>
{
	public static ResourceLocation ID = new ResourceLocation(QuestionablyImmersive.MODID, "triphammer");

	private final PlayerEntity player;

	public TriphammerContainer(int id, PlayerInventory inventoryPlayer, TriphammerTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
		this.inv = new TileInventory(tile)
		{
			@Override
			public void markDirty()
			{
				super.markDirty();
				TriphammerContainer.this.onCraftMatrixChanged(this);
			}
		};

		player = inventoryPlayer.player;

		addSlot(new Slot(this.inv, 0, 27, 47));
		addSlot(new Slot(this.inv, 1, 76, 47));
		addSlot(new Slot(this.inv, 2, 134, 47)
		{
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return false;
			}

			@Override
			public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack)
			{
				return stack;
			}
		});

		slotCount = 2;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for (int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 142));

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
		this.tile.updateRepairOutput();
		this.detectAndSendChanges();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void updateProgressBar(int id, int data)
	{
		if (id == 0)
		{
			this.tile.setMaximumCost(data);
		}
	}

	/**
	 * used by the Anvil GUI to update the Item Name being typed by the player
	 */
	public void updateItemName(String newName)
	{
		tile.updateItemName(newName);

		this.updateRepairOutput();
	}


	// TODO @Override
	public void handlePacket(int messageId, CompoundNBT data)
	{
		if (messageId == 0)
		{
			updateItemName(data.getString("name"));
		}
	}

	public boolean hasOutput()
	{
		return !tile.output.isEmpty();
	}

	public ItemStack getOutput()
	{
		return tile.output;
	}

	public void setOutput(ItemStack stack)
	{
		if (stack == null) {
			stack = ItemStack.EMPTY;
		}
		tile.setOutput(stack);
	}

}
