//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.client.utils.CombinedModelData;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.BlockstateProvider;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPropertyPassthrough;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IEForgeEnergyWrapper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxConnector;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class QIBaseTileEntity extends TileEntity implements BlockstateProvider
{
	private BlockState overrideBlockState = null;
	private final Set<LazyOptional<?>> caps = new HashSet();
	private final Map<Direction, LazyOptional<IEnergyStorage>> energyCaps = new HashMap();

	public QIBaseTileEntity(TileEntityType<? extends TileEntity> type)
	{
		super(type);
	}

	public void read(CompoundNBT nbt)
	{
		super.read(nbt);
		this.readCustomNBT(nbt, false);
	}

	public abstract void readCustomNBT(CompoundNBT var1, boolean var2);

	public CompoundNBT write(CompoundNBT nbt)
	{
		super.write(nbt);
		this.writeCustomNBT(nbt, false);
		return nbt;
	}

	public abstract void writeCustomNBT(CompoundNBT var1, boolean var2);

	public SUpdateTileEntityPacket getUpdatePacket()
	{
		CompoundNBT nbttagcompound = new CompoundNBT();
		this.writeCustomNBT(nbttagcompound, true);
		return new SUpdateTileEntityPacket(this.pos, 3, nbttagcompound);
	}

	public CompoundNBT getUpdateTag()
	{
		CompoundNBT nbt = super.getUpdateTag();
		this.writeCustomNBT(nbt, true);
		return nbt;
	}

	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		this.readCustomNBT(pkt.getNbtCompound(), true);
	}

	public void rotate(Rotation rot)
	{
		if (rot != Rotation.NONE && this instanceof IDirectionalTile && ((IDirectionalTile) this).canRotate(Direction.UP))
		{
			Direction f = ((IDirectionalTile) this).getFacing();
			switch (rot)
			{
				case CLOCKWISE_90:
					f = f.rotateY();
					break;
				case CLOCKWISE_180:
					f = f.getOpposite();
					break;
				case COUNTERCLOCKWISE_90:
					f = f.rotateYCCW();
			}

			((IDirectionalTile) this).setFacing(f);
			this.markDirty();
			if (this.pos != null)
			{
				this.markBlockForUpdate(this.pos, (BlockState) null);
			}
		}

	}

	public void mirror(Mirror mirrorIn)
	{
		if (mirrorIn == Mirror.FRONT_BACK && this instanceof IDirectionalTile)
		{
			((IDirectionalTile) this).setFacing(((IDirectionalTile) this).getFacing());
			this.markDirty();
			if (this.pos != null)
			{
				this.markBlockForUpdate(this.pos, (BlockState) null);
			}
		}

	}

	public void receiveMessageFromClient(CompoundNBT message)
	{
	}

	public void receiveMessageFromServer(CompoundNBT message)
	{
	}

	public void onEntityCollision(World world, Entity entity)
	{
	}

	public boolean receiveClientEvent(int id, int type)
	{
		if (id != 0 && id != 255)
		{
			if (id == 254)
			{
				BlockState state = this.world.getBlockState(this.pos);
				this.world.notifyBlockUpdate(this.pos, state, state, 3);
				return true;
			} else
			{
				return super.receiveClientEvent(id, type);
			}
		} else
		{
			this.markContainingBlockForUpdate((BlockState) null);
			return true;
		}
	}

	public void markContainingBlockForUpdate(@Nullable BlockState newState)
	{
		this.markBlockForUpdate(this.getPos(), newState);
	}

	public void markBlockForUpdate(BlockPos pos, @Nullable BlockState newState)
	{
		BlockState state = this.world.getBlockState(pos);
		if (newState == null)
		{
			newState = state;
		}

		this.world.notifyBlockUpdate(pos, state, newState, 3);
		this.world.notifyNeighborsOfStateChange(pos, newState.getBlock());
	}

	protected <T> LazyOptional<T> registerConstantCap(T val)
	{
		return this.registerCap(() ->
		{
			return val;
		});
	}

	protected <T> LazyOptional<T> registerCap(NonNullSupplier<T> cap)
	{
		return this.registerCap(LazyOptional.of(cap));
	}

	protected <T> LazyOptional<T> registerCap(LazyOptional<T> cap)
	{
		this.caps.add(cap);
		return cap;
	}

	protected <T> void unregisterCap(LazyOptional<T> cap)
	{
		cap.invalidate();
		this.caps.remove(cap);
	}

	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityEnergy.ENERGY && this instanceof IIEInternalFluxConnector)
		{
			if (!this.energyCaps.containsKey(side))
			{
				IEForgeEnergyWrapper wrapper = ((IIEInternalFluxConnector) this).getCapabilityWrapper(side);
				if (wrapper != null)
				{
					this.energyCaps.put(side, this.registerConstantCap(wrapper));
				} else
				{
					this.energyCaps.put(side, LazyOptional.empty());
				}
			}

			return ((LazyOptional) this.energyCaps.get(side)).cast();
		} else
		{
			return super.getCapability(cap, side);
		}
	}

	public double getMaxRenderDistanceSquared()
	{
		double increase = (Double) IEConfig.GENERAL.increasedTileRenderdistance.get();
		return super.getMaxRenderDistanceSquared() * increase * increase;
	}

	public void remove()
	{
		super.remove();
		Iterator var1 = this.caps.iterator();

		while (var1.hasNext())
		{
			LazyOptional<?> cap = (LazyOptional) var1.next();
			if (cap.isPresent())
			{
				cap.invalidate();
			}
		}

	}

	@Nonnull
	public World getWorldNonnull()
	{
		return (World) Objects.requireNonNull(super.getWorld());
	}

	protected void checkLight()
	{
		this.checkLight(this.pos);
	}

	protected void checkLight(BlockPos pos)
	{
		this.getWorldNonnull().getPendingBlockTicks().scheduleTick(pos, this.getBlockState().getBlock(), 4);
	}

	public void setOverrideState(BlockState state)
	{
		this.overrideBlockState = state;
	}

	public BlockState getBlockState()
	{
		return this.overrideBlockState != null ? this.overrideBlockState : super.getBlockState();
	}

	public void updateContainingBlockInfo()
	{
		BlockState old = this.getBlockState();
		super.updateContainingBlockInfo();
		BlockState newState = this.getBlockState();
		if (old != null && newState != null && this.getType().isValidBlock(old.getBlock()) && !this.getType().isValidBlock(newState.getBlock()))
		{
			this.setOverrideState(old);
		}

	}

	public void setState(BlockState state)
	{
		this.getWorldNonnull().setBlockState(this.pos, state);
	}

	public BlockState getState()
	{
		return this.getBlockState();
	}

	@Nonnull
	public IModelData getModelData()
	{
		IModelData base = super.getModelData();
		return (IModelData) (this instanceof IPropertyPassthrough ? new CombinedModelData(new IModelData[]{base, new SinglePropertyModelData(this, Model.TILEENTITY_PASSTHROUGH)}) : base);
	}
}
