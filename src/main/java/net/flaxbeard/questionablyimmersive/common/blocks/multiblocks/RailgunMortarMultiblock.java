package net.flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlocks;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.RailgunMortarTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RailgunMortarMultiblock extends IETemplateMultiblock
{
	@OnlyIn(Dist.CLIENT)
	private static Object te;

	public RailgunMortarMultiblock()
	{
		super(new ResourceLocation("questionablyimmersive", "multiblocks/mortar"), new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), () ->
		{
			return QIBlocks.Multiblocks.railgunMortar.getDefaultState();
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
			te = new RailgunMortarTileEntity.Master();
		}

		QuestionablyImmersive.proxy.renderTileForManual((TileEntity) te);
	}

	public float getManualScale()
	{
		return 12.0F;
	}
}
