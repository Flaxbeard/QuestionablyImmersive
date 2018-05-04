package flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import flaxbeard.questionablyimmersive.common.util.Pair;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityGauge extends TileEntityIEBase implements IDirectionalTile, IEBlockInterfaces.IHammerInteraction, IBlockOverlayText
{
	public EnumFacing facing = EnumFacing.NORTH;
	public int dataType = 0;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));
		dataType = nbt.getInteger("dataType");

		if (descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
		nbt.setInteger("dataType", dataType);
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 0;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return true;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return false;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared() * IEConfig.increasedTileRenderdistance;
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer)
	{
		Pair<String[], Float> data = getData(dataType);
		return data.getFirst();
	}

	public Pair<String[], Float> getData(int dataType)
	{
		BlockPos offsetPos = pos.offset(facing);
		IBlockState state = world.getBlockState(offsetPos);
		TileEntity te = getProperTE(offsetPos);

		switch (dataType)
		{
			case 0:
				int power = world.getRedstonePower(offsetPos, facing);
				return new Pair<>(
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
				return new Pair<>(
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
					if (te instanceof TileEntityMultiblockMetal)
					{
						for (IFluidTank tank : ((TileEntityMultiblockMetal) te).getInternalTanks())
						{
							capacity += tank.getCapacity();
							fill += tank.getFluidAmount();
						}
						waterLevel = fill / capacity;
					}
					else
					{
						IFluidHandler cap = null;

						if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing))
						{
							cap = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
						}
						else if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
						{
							cap = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
						}

						if (cap != null)
						{
							for (IFluidTankProperties prop : cap.getTankProperties())
							{
								capacity += prop.getCapacity();
								if (prop.getContents() != null)
								{
									fill += prop.getContents().amount;
								}
							}
							waterLevel = fill / capacity;
						}
					}
				}
				return new Pair<>(
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
					if (te instanceof TileEntityMultiblockMetal)
					{
						FluxStorage fluxStorage = ((TileEntityMultiblockMetal) te).getFluxStorage();
						capacity = fluxStorage.getMaxEnergyStored();
						fill = fluxStorage.getEnergyStored();
						powerLevel = fill / capacity;
					}
					else
					{
						IEnergyStorage energyCap = null;

						if (te.hasCapability(CapabilityEnergy.ENERGY, facing))
						{
							energyCap = te.getCapability(CapabilityEnergy.ENERGY, facing);
						}
						else if (te.hasCapability(CapabilityEnergy.ENERGY, null))
						{
							energyCap = te.getCapability(CapabilityEnergy.ENERGY, null);
						}

						if (energyCap != null)
						{
							capacity = energyCap.getMaxEnergyStored();
							fill = energyCap.getEnergyStored();
							powerLevel = fill / capacity;
						}
					}
				}
				return new Pair<>(
						new String[]{
								"Stored Flux",
								Math.round(100 * powerLevel) + "% (" + Math.round(fill) + "/" + Math.round(capacity) + " Flux)"
						},
						powerLevel
				);
		}
		return new Pair<>(new String[0], 0f);
	}

	private TileEntity getProperTE(BlockPos offsetPos)
	{
		TileEntity te = world.getTileEntity(offsetPos);

		if (te instanceof TileEntityMultiblockMetal)
		{
			TileEntityMultiblockMetal mb = ((TileEntityMultiblockMetal) te);
			if (mb.master() != null)
			{
				return mb.master();
			}
		}
		return te;
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop)
	{
		return false;
	}

	@Override
	public boolean hammerUseSide(EnumFacing side, EntityPlayer player, float hitX, float hitY, float hitZ)
	{
		dataType = (dataType + 1) % 4;
		markContainingBlockForUpdate(null);
		markDirty();
		return true;
	}


	public float getRotation()
	{
		return getData(dataType).getSecond();
	}


	private float lastRotation;
	private int lastRotationTick = 0;

	@SideOnly(Side.CLIENT)
	public float getDisplayRotation()
	{
		float actualRotation = getRotation();

		if (Minecraft.getMinecraft().player != null)
		{
			int tick = Minecraft.getMinecraft().player.ticksExisted;
			if (tick != lastRotationTick)
			{
				lastRotationTick = tick;
				lastRotation = lastRotation + (actualRotation - lastRotation) / 5F;
			}
		}

		return lastRotation + ((actualRotation - lastRotation) / 5F) * Minecraft.getMinecraft().getRenderPartialTicks();
	}


}