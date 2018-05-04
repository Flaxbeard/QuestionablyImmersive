package flaxbeard.questionablyimmersive.common.gui;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.InventoryTile;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerCokeOvenBattery extends ContainerIEBase<TileEntityCokeOvenBattery>
{
	public IESlot[] slots;
	public ContainerCokeOvenBattery(InventoryPlayer inventoryPlayer, TileEntityCokeOvenBattery tile)
	{
		super(inventoryPlayer, tile);
		this.inv = new InventoryTile(tile) {
			@Override
			public boolean isUsableByPlayer(EntityPlayer player)
			{
				return true;
			}
		};


		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 0, 148,19, 0));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 148,55));


		slots = new IESlot[tile.ovenLength];
		for (int i = 0; i < tile.ovenLength; i++)
		{
			slots[i] = new IESlot(this, this.inv, i + 2, 30, 35)
			{
				@Override
				public boolean isItemValid(ItemStack itemStack)
				{
					return CokeOvenRecipe.findRecipe(itemStack) != null;
				}
			};
			this.addSlotToContainer(slots[i]);
		}

		slotCount = tile.ovenLength + 2;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}
