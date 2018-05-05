package flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.questionablyimmersive.api.mechpower.MechNetworkHelper;
import flaxbeard.questionablyimmersive.client.model.ModelAxle;
import flaxbeard.questionablyimmersive.client.model.ModelGauge;
import flaxbeard.questionablyimmersive.common.blocks.tile.TileEntityAxle;
import flaxbeard.questionablyimmersive.common.blocks.tile.TileEntityGauge;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileAxleRenderer extends TileEntitySpecialRenderer<TileEntityAxle>
{

	private static ModelAxle model = new ModelAxle();
	private static String texture = "questionablyimmersive:textures/models/axle.png";

	@Override
	public void render(TileEntityAxle te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + .5, y + .5, z + .5);
			BlockPos pos = te.getPos().offset(te.facing);

			if (te.facing == EnumFacing.SOUTH)
			{
				GlStateManager.rotate(180, 0, 1, 0);
			}
			else if (te.facing == EnumFacing.WEST)
			{
				GlStateManager.rotate(90, 0, 1, 0);
			}
			else if (te.facing == EnumFacing.EAST)
			{
				GlStateManager.rotate(270, 0, 1, 0);
			}
			else if (te.facing == EnumFacing.UP)
			{
				GlStateManager.rotate(90, 1, 0, 0);
			}
			else if (te.facing == EnumFacing.DOWN)
			{
				GlStateManager.rotate(270, 1, 0, 0);
			}

			GlStateManager.rotate(360f * MechNetworkHelper.getNetworkData(te.getWorld(), te.getPos()).getRotation(), 0, 0, 1);

			GlStateManager.translate(-.5, -.5, -.5);
			ClientUtils.bindTexture(texture);
			model.render(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();
		}
	}
}