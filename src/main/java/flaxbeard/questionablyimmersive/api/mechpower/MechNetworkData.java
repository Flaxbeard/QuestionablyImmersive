package flaxbeard.questionablyimmersive.api.mechpower;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class MechNetworkData
{

	private boolean valid;
	private Set<BlockPos> containedBlocks;
	private Set<BlockPos> containedAcceptors;

	private float rotation;

	public MechNetworkData(World world, Set<BlockPos> containedBlocks, Set<BlockPos> containedAcceptors)
	{
		this.valid = true;
		this.containedBlocks = containedBlocks;
		this.containedAcceptors = containedAcceptors;
		this.rotation = 0f;

	}

	public boolean isValid()
	{
		return valid;
	}

	public void invalidate()
	{
		valid = false;
	}

	public Set<BlockPos> getContainedBlocks()
	{
		return containedBlocks;
	}

	public void addRotation(double rotation)
	{
		this.rotation += rotation;
	}

	public Set<BlockPos>  getContainedAcceptors()
	{
		return containedAcceptors;
	}

	public float getRotation()
	{
		return rotation / 800f;
	}
}
