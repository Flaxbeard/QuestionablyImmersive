package net.flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlocks;
import net.flaxbeard.questionablyimmersive.common.blocks.QIMultiblockBlock;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.TiledMultiblockTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class CokeOvenBatterySliceMultiblock extends TiledTemplateMultiblock
{
	@OnlyIn(Dist.CLIENT)
	private static Object te;

	public CokeOvenBatterySliceMultiblock()
	{
		super(new ResourceLocation("questionablyimmersive", "multiblocks/coke_oven_battery_slice"), new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), () ->
		{
			return QIBlocks.Multiblocks.cokeOvenBattery.getDefaultState();
		});
	}

	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
	}

	public float getManualScale()
	{
		return 12.0F;
	}

	@Override
	protected void afterReplacement(TiledMultiblockTileEntity tile, int layer)
	{
		if (tile instanceof CokeOvenBatteryTileEntity)
		{
			CokeOvenBatteryTileEntity oven = (CokeOvenBatteryTileEntity) tile;

			oven.tank = new FluidTank(6000 * oven.numLayers);
			if (!oven.isDummy())
			{
				int layers = oven.numLayers;
				oven.process = new int[layers];
				oven.processMax = new int[layers];
				oven.active = new boolean[layers];
				oven.recuperationTime = new int[layers];
				oven.invHandlers = new LazyOptional[layers];
				for (int i = 0; i < layers; i++)
				{
					boolean[] insert = new boolean[layers + 2];
					insert[i + 2] = true;
					IEInventoryHandler handler = new IEInventoryHandler(layers + 2, oven, 0, insert, new boolean[layers]);
					oven.invHandlers[i] = oven.registerConstantCap(handler);
				}
				oven.inventory = NonNullList.withSize(layers + 2, ItemStack.EMPTY);

				oven.markDirty();
				oven.markContainingBlockForUpdate((BlockState) null);
			} else
			{
				oven.process = new int[0];
				oven.processMax = new int[0];
				oven.active = new boolean[0];
				oven.active = new boolean[0];
				oven.recuperationTime = new int[0];
				oven.invHandlers = new LazyOptional[0];
				oven.inventory = NonNullList.withSize(0, ItemStack.EMPTY);
			}
		}
	}

	@Override
	protected int getMinLayers()
	{
		return 6;
	}

	@Override
	protected QIMultiblockBlock.MultiblockTileType getTileType(boolean equals, int i)
	{
		if (equals && i == 0)
		{
			return QIMultiblockBlock.MultiblockTileType.MASTER;
		} else if (equals && i % 5 == 0)
		{
			return QIMultiblockBlock.MultiblockTileType.RENDERED_SLAVE;
		}
		return QIMultiblockBlock.MultiblockTileType.SLAVE;
	}
}
