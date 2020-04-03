package net.flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlocks;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CokeOvenBatteryDisplayMultiblock extends IETemplateMultiblock
{
	@OnlyIn(Dist.CLIENT)
	private static Object te;

	public CokeOvenBatteryDisplayMultiblock()
	{
		super(new ResourceLocation("questionablyimmersive", "multiblocks/coke_oven_battery_display"), new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), () ->
		{
			return QIBlocks.Multiblocks.cokeOvenBattery.getDefaultState();
		});
	}

	@OnlyIn(Dist.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void renderFormedStructure()
	{
		if (te == null)
		{
			te = new CokeOvenBatteryTileEntity.Master();
		}

		QuestionablyImmersive.proxy.renderTileForManual((TileEntity) te);
	}

	public float getManualScale()
	{
		return 12.0F;
	}

	@Override
	public boolean isBlockTrigger(BlockState state)
	{
		// Shouldn't be formable
		return false;
	}
}
