package net.flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import net.flaxbeard.questionablyimmersive.client.model.CokeOvenBatteryModel;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;

public class CokeOvenBatteryRenderer extends TileEntityRenderer<CokeOvenBatteryTileEntity.Rendered>
{
	private static CokeOvenBatteryModel model = new CokeOvenBatteryModel(false);
	private static CokeOvenBatteryModel modelM = new CokeOvenBatteryModel(true);

	private static String texture = "questionablyimmersive:textures/models/coke_oven_battery.png";
	private static String textureOff = "questionablyimmersive:textures/models/coke_oven_battery_off.png";

	@Override
	public void render(CokeOvenBatteryTileEntity.Rendered te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translated(x, y - 1, z);

			Direction rotation = te.getFacing();

			float rotationMod = 0;
			if (rotation == Direction.NORTH)
			{
				rotationMod = 90f;
				GlStateManager.rotated(90F, 0, 1, 0);
				GlStateManager.translated(-1, 0, 1);
			}
			else if (rotation == Direction.WEST)
			{
				rotationMod = 180f;
				GlStateManager.rotated(180F, 0, 1, 0);
				GlStateManager.translated(-1, 0, 0);
			}
			else if (rotation == Direction.SOUTH)
			{
				rotationMod = 270f;
				GlStateManager.rotated(270F, 0, 1, 0);
			}
			else
			{
				GlStateManager.translated(0, 0, 1);
			}
			GlStateManager.translated(0, 0, -2);


			if (!te.getIsMirrored())
			{
				GlStateManager.translated(te.numLayers, 0, 3);
				GlStateManager.rotated(180, 0, 1, 0);
			}

			if (te.numLayers == 0)
			{
				GlStateManager.translated(-1, 0, 0);
				ClientUtils.bindTexture(texture);
				for (int i = 0; i < 6; i++)
				{
					GlStateManager.pushMatrix();
					GlStateManager.translated(i, 0, 0);
					model.render(null, i, 0.0625F);
					GlStateManager.popMatrix();
				}
			}
			else
			{
				// Render 5 ovens per Render-tagged TE
				for (int i = te.layer; i < Math.min(te.layer + 5, te.numLayers); i++)
				{
					GlStateManager.pushMatrix();
					int ovenTest = i - te.layer;
					GlStateManager.translated(!te.getIsMirrored() ? (te.numLayers - ovenTest - 1) : ovenTest, 0, 0);

					CokeOvenBatteryTileEntity.Master parent = (CokeOvenBatteryTileEntity.Master) te.master();
					if (parent != null && i < parent.active.length && parent.active[i])
					{
						ClientUtils.bindTexture(texture);
					}
					else
					{
						ClientUtils.bindTexture(textureOff);
					}
					model.render(parent, i, 0.0625F);
					GlStateManager.popMatrix();
				}
			}


			GlStateManager.popMatrix();

		}
	}

}