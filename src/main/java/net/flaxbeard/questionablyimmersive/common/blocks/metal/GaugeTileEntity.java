package net.flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBaseTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class GaugeTileEntity extends QIBaseTileEntity implements IEBlockInterfaces.IStateBasedDirectional, IEBlockInterfaces.IHammerInteraction, IBlockOverlayText, IEBlockInterfaces.IBlockBounds
{
	public static TileEntityType<GaugeTileEntity> TYPE;

	public int dataType = 0;

	public GaugeTileEntity()
	{
		super(TYPE);
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		dataType = nbt.getInt("dataType");

		if (descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("dataType", dataType);
	}

	@Override
	public String[] getOverlayText(PlayerEntity player, RayTraceResult mop, boolean hammer)
	{
		Tuple<String[], Float> data = getData(dataType);
		return data.getA();
	}

	public Tuple<String[], Float> getData(int dataType)
	{
		Direction facing = getFacing();

		BlockPos offsetPos = pos.offset(facing);
		BlockState state = world.getBlockState(offsetPos);
		TileEntity te = getProperTE(offsetPos);

		switch (dataType)
		{
			case 0:
				int power = world.getRedstonePower(offsetPos, facing);
				return new Tuple<>(
						new String[]{
								"Redstone Power",
								Math.round(100 * power / 15f) + "% (" + power + "/15)"
						},
						power / 15f
				);
			case 1:
				int comparatorPower = 0;
				if (state.hasComparatorInputOverride())
				{
					comparatorPower = state.getComparatorInputOverride(world, offsetPos);
				}
				return new Tuple<>(
						new String[]{
								"Comparator Power",
								Math.round(100 * comparatorPower / 15f) + "% (" + comparatorPower + "/15)"
						},
						comparatorPower / 15f
				);
			case 2:
				float waterLevel = 0;
				float capacity = 0;
				float fill = 0;

				if (te != null)
				{
					if (te instanceof PoweredMultiblockTileEntity)
					{
						IFluidTank[] tanks = ((PoweredMultiblockTileEntity) te).getInternalTanks();
						if (tanks != null)
						{
							for (IFluidTank tank : tanks)
							{
								capacity += tank.getCapacity();
								fill += tank.getFluidAmount();
							}
							waterLevel = fill / capacity;
						}
					}
					else
					{
						final IFluidHandler[] cap = {null};
						te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing).ifPresent(value -> cap[0] = value);
						if (cap[0] == null)
						{
							te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).ifPresent(value -> cap[0] = value);
						}
						IFluidHandler handler = cap[0];

						if (handler != null)
						{
							for (int i = 0; i < handler.getTanks(); i++)
							{
								capacity += handler.getTankCapacity(i);
								fill += handler.getFluidInTank(i).getAmount();
							}
							waterLevel = fill / capacity;
						}
					}
				}
				return new Tuple<>(
						new String[]{
								"Stored Fluid",
								Math.round(100 * waterLevel) + "% (" + Math.round(fill) + "/" + Math.round(capacity) + " mB)"
						},
						waterLevel
				);
			case 3:
				float powerLevel = 0;
				capacity = 0;
				fill = 0;

				if (te != null)
				{
					if (te instanceof PoweredMultiblockTileEntity)
					{
						FluxStorage fluxStorage = ((PoweredMultiblockTileEntity) te).getFluxStorage();
						capacity = fluxStorage.getMaxEnergyStored();
						fill = fluxStorage.getEnergyStored();
						powerLevel = fill / capacity;
					}
					else
					{
						final IEnergyStorage[] cap = {null};
						te.getCapability(CapabilityEnergy.ENERGY, facing).ifPresent(value -> cap[0] = value);
						if (cap[0] == null)
						{
							te.getCapability(CapabilityEnergy.ENERGY, null).ifPresent(value -> cap[0] = value);
						}
						IEnergyStorage handler = cap[0];

						if (handler != null)
						{
							capacity = handler.getMaxEnergyStored();
							fill = handler.getEnergyStored();
							powerLevel = fill / capacity;
						}
					}
				}
				return new Tuple<>(
						new String[]{
								"Stored Flux",
								Math.round(100 * powerLevel) + "% (" + Math.round(fill) + "/" + Math.round(capacity) + " Flux)"
						},
						powerLevel
				);
		}
		return new Tuple<>(new String[0], 0f);
	}

	private TileEntity getProperTE(BlockPos offsetPos)
	{
		TileEntity te = world.getTileEntity(offsetPos);

		if (te instanceof PoweredMultiblockTileEntity)
		{
			PoweredMultiblockTileEntity mb = ((PoweredMultiblockTileEntity) te);
			if (mb.master() != null)
			{
				return mb.master();
			}
		}
		return te;
	}

	@Override
	public boolean useNixieFont(PlayerEntity player, RayTraceResult mop)
	{
		return false;
	}

    /*@Override
    public boolean hammerUseSide(Direction side, PlayerEntity player, float hitX, float hitY, float hitZ)
    {
        dataType = (dataType + 1) % 4;
        markContainingBlockForUpdate(null);
        markDirty();
        return true;
    }*/


	public float getRotation()
	{
		return getData(dataType).getB();
	}


	private float lastRotation;
	private int lastRotationTick = 0;

	@OnlyIn(Dist.CLIENT)
	public float getDisplayRotation()
	{
		float actualRotation = getRotation();

		if (Minecraft.getInstance().player != null)
		{
			int tick = Minecraft.getInstance().player.ticksExisted;
			if (tick != lastRotationTick)
			{
				lastRotationTick = tick;
				lastRotation = lastRotation + (actualRotation - lastRotation) / 5F;
			}
		}

		return lastRotation + ((actualRotation - lastRotation) / 5F) * Minecraft.getInstance().getRenderPartialTicks();
	}


	@Override
	public EnumProperty<Direction> getFacingProperty()
	{
		return IEProperties.FACING_ALL;
	}

	@Override
	public PlacementLimitation getFacingLimitation()
	{
		return PlacementLimitation.SIDE_CLICKED;
	}

	@Override
	public boolean mirrorFacingOnPlacement(LivingEntity livingEntity)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(Direction direction, Vec3d vec3d, LivingEntity livingEntity)
	{
		return false;
	}

	@Override
	public boolean canRotate(Direction direction)
	{
		return true;
	}

	@Override
	public boolean hammerUseSide(Direction direction, PlayerEntity playerEntity, Vec3d vec3d)
	{
		dataType = (dataType + 1) % 4;
		markContainingBlockForUpdate(null);
		markDirty();
		return true;
	}

	@Override
	public VoxelShape getBlockBounds()
	{
		Direction dir = getFacing();
		switch (dir)
		{
			case DOWN:
				return VoxelShapes.create(0.25f, 0, 0.25f, 0.75f, 0.0625f, 0.75f);
			case UP:
				return VoxelShapes.create(0.25f, 0.9375f, 0.25f, 0.75f, 1, 0.75f);
			case EAST:
				return VoxelShapes.create(0.9375f, 0.25f, 0.25f, 1, 0.75f, 0.75f);
			case WEST:
				return VoxelShapes.create(0, 0.25f, 0.25f, 0.0625f, 0.75f, 0.75f);
			case SOUTH:
				return VoxelShapes.create(0.25f, 0.25f, 0.9375f, 0.75f, 0.75f, 1);
			case NORTH:
			default:
				return VoxelShapes.create(0.25f, 0.25f, 0, 0.75f, 0.75f, 0.0625f);
		}
	}
}