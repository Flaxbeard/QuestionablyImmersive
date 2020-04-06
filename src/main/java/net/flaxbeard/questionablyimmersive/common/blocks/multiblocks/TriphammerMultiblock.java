package net.flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlocks;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.TriphammerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriphammerMultiblock extends QITemplateMultiblock
{
	@OnlyIn(Dist.CLIENT)
	private static Object te;

	public TriphammerMultiblock()
	{
		super(new ResourceLocation(QuestionablyImmersive.MODID, "multiblocks/triphammer"), new BlockPos(1, 1, 0), new BlockPos(1, 1, 0), () ->
		{
			return QIBlocks.Multiblocks.triphammer.getDefaultState();
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
			te = new TriphammerTileEntity.Master();
		}

		QuestionablyImmersive.proxy.renderTileForManual((TileEntity) te);
	}

	public float getManualScale()
	{
		return 12.0F;
	}


}
