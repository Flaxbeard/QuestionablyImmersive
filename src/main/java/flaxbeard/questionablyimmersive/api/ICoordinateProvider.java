package flaxbeard.questionablyimmersive.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ICoordinateProvider
{
	/**
	 * Gets the coordinate stored on the passed ItemStack. Returns null if no coordinate is stored.
	 *
	 * @param world The world that this access is happening
	 * @param stack The coordinate-containing instance of this item
	 */
	Vec3d getCoordinate(World world, ItemStack stack);
}
