package flaxbeard.questionablyimmersive.common.gui;

import blusunrize.immersiveengineering.common.gui.ContainerIEBase;
import blusunrize.immersiveengineering.common.gui.IESlot;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerMortar extends ContainerIEBase<TileEntityMortar>
{
	public ContainerMortar(InventoryPlayer inventoryPlayer, TileEntityMortar tile)
	{
		super(inventoryPlayer, tile);

		addSlotToContainer(new Slot(this.inv, 0, 0, 0));
		addSlotToContainer(new IESlot(this, this.inv, 1, 18, 0)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				return tile.isStackValid(1, itemStack);
			}
		});

		slotCount = 2;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}
