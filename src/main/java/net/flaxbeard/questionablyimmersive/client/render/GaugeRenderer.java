package net.flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import net.flaxbeard.questionablyimmersive.client.model.GaugeModel;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.GaugeTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class GaugeRenderer extends TileEntityRenderer<GaugeTileEntity>
{

	private static GaugeModel model = new GaugeModel();
	private static String texture = "questionablyimmersive:textures/models/gauge.png";

	@Override
	public void render(GaugeTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translated(x + .5, y + .5, z + .5);
			Direction facing = te.getFacing();

			BlockPos pos = te.getPos().offset(facing);
			BlockState facingState = te.getWorld().getBlockState(pos);

			if (facing == Direction.SOUTH)
			{
				GlStateManager.rotated(180, 0, 1, 0);
			}
			else if (facing == Direction.WEST)
			{
				//double offset = 1 - facingState.getBlock().getBoundingBox(facingState, te.getWorld(), pos).maxX;
				//GlStateManager.translate(-offset, 0, 0);

				GlStateManager.rotated(90, 0, 1, 0);
			}
			else if (facing == Direction.EAST)
			{
				//double offset = facingState.getBlock().getBoundingBox(facingState, te.getWorld(), pos).minX;
				//GlStateManager.translate(offset, 0, 0);

				GlStateManager.rotated(270, 0, 1, 0);
			}
			else if (facing == Direction.UP)
			{
				GlStateManager.rotated(90, 1, 0, 0);
			}
			else if (facing == Direction.DOWN)
			{
				GlStateManager.rotated(270, 1, 0, 0);
			}
			GlStateManager.translated(-.5, -.5, -.5);
			ClientUtils.bindTexture(texture);
			model.dialRotation = te.getDisplayRotation();
			model.render(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();
		}
	}
}