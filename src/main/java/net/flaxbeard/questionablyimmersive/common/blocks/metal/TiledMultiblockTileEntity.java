package net.flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public abstract class TiledMultiblockTileEntity<T extends TiledMultiblockTileEntity<T>> extends MultiblockPartTileEntity<T>
{
	protected BlockPos posInMultiblockTotal;
	public int layer;
	public int numLayers;

	public TiledMultiblockTileEntity(IETemplateMultiblock multiblockInstance, TileEntityType<? extends T> type, boolean redstoneControl)
	{
		super(multiblockInstance, type, redstoneControl);
		this.posInMultiblockTotal = BlockPos.ZERO;
		this.numLayers = 0;
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		this.posInMultiblockTotal = NBTUtil.readBlockPos(nbt.getCompound("posInMultiblockTotal"));
		this.numLayers = nbt.getInt("numLayers");
		this.layer = nbt.getInt("layer");
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.put("posInMultiblockTotal", NBTUtil.writeBlockPos(new BlockPos(this.posInMultiblockTotal)));
		nbt.putInt("numLayers", numLayers);
		nbt.putInt("layer", layer);
	}

	public void disassemble() {
		if (this.formed && !this.world.isRemote) {
			if (this.isDummy())
			{
				this.master().disassemble();
			}
			else {
				T master = this.master();
				ObfuscationReflectionHelper.setPrivateValue(MultiblockPartTileEntity.class, this, master, "tempMasterTE");

				BlockPos startPos = this.getOrigin();
				for (int i = 0; i < numLayers; i++)
				{
					IETemplateMultiblock multiblockInstance = ObfuscationReflectionHelper.getPrivateValue(MultiblockPartTileEntity.class, this, "multiblockInstance");
					multiblockInstance.disassemble(this.world, startPos.offset(getFacing(), i), this.getIsMirrored(), multiblockInstance.untransformDirection(this.getFacing()));
				}
			}
		}
	}

}
