package net.flaxbeard.questionablyimmersive.api;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ICoordinateProvider
{
	Vec3d getCoordinate(World world, ItemStack stack);
}
