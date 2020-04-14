package net.flaxbeard.questionablyimmersive.common.blocks.metal;

import net.flaxbeard.questionablyimmersive.common.blocks.QIBaseTileEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TriphammerAnvilTileEntity extends QIBaseTileEntity
{
	public static TileEntityType<TriphammerAnvilTileEntity> TYPE;

	public TriphammerAnvilTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT var1, boolean var2)
	{

	}

	@Override
	public void writeCustomNBT(CompoundNBT var1, boolean var2)
	{

	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing)
	{
		TileEntity triphammer = world.getTileEntity(getPos().add(0, 1, 0));
		if (triphammer instanceof TriphammerTileEntity)
		{
			return ((TriphammerTileEntity) triphammer).master().insertionHandlerBelow.cast();
		}
		return super.getCapability(cap, facing);
	}
}
