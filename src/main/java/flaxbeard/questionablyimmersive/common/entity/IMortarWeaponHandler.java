package flaxbeard.questionablyimmersive.common.entity;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IMortarWeaponHandler
{
	void handleItem(ItemStack item, World world, double xPos, double yPos, double zPos);
}
