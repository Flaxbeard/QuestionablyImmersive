package flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.questionablyimmersive.client.model.ModelGauge;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityGauge;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileGaugeRenderer extends TileEntitySpecialRenderer<TileEntityGauge>
{

	private static ModelGauge model = new ModelGauge();
	private static String texture = "questionablyimmersive:textures/models/gauge.png";

	@Override
	public boolean isGlobalRenderer(TileEntityGauge te)
    {
        return true;
    }
	
	@Override
	public void render(TileEntityGauge te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + .5, y + .5, z + .5);
			BlockPos pos = te.getPos().offset(te.facing);
			IBlockState facingState = te.getWorld().getBlockState(pos);
			if (te.facing == EnumFacing.SOUTH)
			{
				GlStateManager.rotate(180, 0, 1, 0);
			}
			else if (te.facing == EnumFacing.WEST)
			{
				//double offset = 1 - facingState.getBlock().getBoundingBox(facingState, te.getWorld(), pos).maxX;
				//GlStateManager.translate(-offset, 0, 0);

				GlStateManager.rotate(90, 0, 1, 0);
			}
			else if (te.facing == EnumFacing.EAST)
			{
				//double offset = facingState.getBlock().getBoundingBox(facingState, te.getWorld(), pos).minX;
				//GlStateManager.translate(offset, 0, 0);

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
			GlStateManager.translate(-.5, -.5, -.5);
			ClientUtils.bindTexture(texture);
			model.dialRotation = te.getDisplayRotation();
			model.render(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();
		}
	}
}