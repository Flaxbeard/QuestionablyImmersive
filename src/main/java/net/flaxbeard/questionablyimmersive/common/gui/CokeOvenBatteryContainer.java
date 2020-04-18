package net.flaxbeard.questionablyimmersive.common.gui;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.TileInventory;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CokeOvenBatteryContainer extends QIBaseContainer<CokeOvenBatteryTileEntity>
{
	public static ResourceLocation ID = new ResourceLocation(QuestionablyImmersive.MODID, "coke_oven_battery");

	public IESlot[] slots;
	public CokeOvenBatteryTileEntity.Data data;

	public CokeOvenBatteryContainer(int id, PlayerInventory inventoryPlayer, CokeOvenBatteryTileEntity tile)
	{
		super(inventoryPlayer, tile, id);
		this.inv = new TileInventory(tile)
		{
			@Override
			public boolean isUsableByPlayer(PlayerEntity player)
			{
				return true;
			}
		};

		// Fluid IO
		this.addSlot(new IESlot.FluidContainer(this, this.inv, 0, 148, 19, 0));
		this.addSlot(new IESlot.Output(this, this.inv, 1, 148, 55));

		// Add oven slots
		slots = new IESlot[tile.numLayers];
		for (int i = 0; i < tile.numLayers; i++)
		{
			slots[i] = new IESlot(this, this.inv, i + 2, 30, 35)
			{
				@Override
				public boolean isItemValid(ItemStack itemStack)
				{
					return CokeOvenRecipe.findRecipe(itemStack) != null;
				}
			};
			this.addSlot(slots[i]);
		}

		slotCount = tile.numLayers + 2;

		// Add player inv slots
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlot(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 85 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++)
		{
			addSlot(new Slot(inventoryPlayer, i, 8 + i * 18, 143));
		}

		// Track change in creosote fill
		this.data = tile.guiData;
		this.trackIntArray(this.data);
	}
}
