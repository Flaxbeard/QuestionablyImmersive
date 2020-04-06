package net.flaxbeard.questionablyimmersive.common.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

public class AnvilUtils
{
	public static class RepairOutput
	{
		public int maximumCost;
		public int materialCost;
		public ItemStack output;

		public RepairOutput(int maximumCost, int materialCost, ItemStack output)
		{
			this.maximumCost = maximumCost;
			this.materialCost = materialCost;
			this.output = output;
		}
	}

	public static RepairOutput updateRepairOutput(ServerPlayerEntity fakePlayer, String repairedItemName, ItemStack input1, ItemStack input2)
	{
		RepairContainer dummyContainer = new RepairContainer(0, fakePlayer.inventory, IWorldPosCallable.DUMMY);
		dummyContainer.putStackInSlot(0, input1);
		dummyContainer.putStackInSlot(1, input2);

		dummyContainer.updateItemName(repairedItemName);

		dummyContainer.updateRepairOutput();

		return new RepairOutput(dummyContainer.getMaximumCost(), dummyContainer.materialCost, dummyContainer.getInventory().get(2));
	}
}
