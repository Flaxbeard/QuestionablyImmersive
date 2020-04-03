package net.flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import net.flaxbeard.questionablyimmersive.client.model.MortarModel;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.RailgunMortarTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.Direction;

public class RailgunMortarRenderer extends TileEntityRenderer<RailgunMortarTileEntity.Master>
{

	private static MortarModel model = new MortarModel(false);
	private static MortarModel modelM = new MortarModel(true);

	private static String texture = "immersiveengineering:textures/block/metal/storage_steel.png";


	@Override
	public void render(RailgunMortarTileEntity.Master te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translated(x, y - 1, z);

			Direction rotation = te.getFacing();

			float rotationMod = 0;
			if (rotation == Direction.SOUTH)
			{
				rotationMod = 90f;
				GlStateManager.rotated(90F, 0, 1, 0);
				GlStateManager.translated(-1, 0, 1);
			}
			else if (rotation == Direction.EAST)
			{
				rotationMod = 180f;
				GlStateManager.rotated(180F, 0, 1, 0);
				GlStateManager.translated(-1, 0, 0);
			}
			else if (rotation == Direction.NORTH)
			{
				rotationMod = 270f;
				GlStateManager.rotated(270F, 0, 1, 0);
			}
			else
			{
				GlStateManager.translated(0, 0, 1);
			}
			GlStateManager.translated(0, 0, -2);

			if (te.getIsMirrored())
			{
			}

			ClientUtils.bindTexture(texture);

			//\\float ticks = te.activeTicks + (te.wasActive ? partialTicks : 0);;
			//model.ticks = modelM.ticks = 1.5F * ticks;


			model.render(null, 0, 0, 0, 0, 0, 0.0625F);


			GlStateManager.translated(24 / 16f, 8 / 16f, 24 / 16f);
			GlStateManager.rotated(te.rotation - rotationMod, 0, 1, 0);
			model.renderCannonBase(null, 0, 0, 0, 0, 0, 0.0625F);

			GlStateManager.translated(-8 / 16f, 18 / 16f, 0F);
			GlStateManager.rotated(80, 0, 0, 1);

			float recoilTicks = (20 - te.reloadTicks);
			if (recoilTicks < 20)
			{
				recoilTicks += Minecraft.getInstance().getRenderPartialTicks();
			}
			float recoilAmount;

			if (recoilTicks <= 1F)
			{
				recoilAmount = (1F - recoilTicks) / 1F;
			}
			else
			{
				recoilTicks -= 1F;
				recoilAmount = recoilTicks / 19F;
			}


			recoilAmount = 1F - (float) Math.cos(recoilAmount * Math.PI / 2F);
			GlStateManager.translated(-1 / 16F + recoilAmount * 1 / 16F, 0, 0);
			model.renderCannon(null, 0, 0, 0, 0, 0, 0.0625F);

			GlStateManager.translated(MortarModel.BASE_HEIGHT / 16F, 0, 0);
			GlStateManager.translated(-3 / 16F + recoilAmount * 3 / 16F, 0, 0);
			model.renderCannonTop(null, 0, 0, 0, 0, 0, 0.0625F);

			GlStateManager.popMatrix();

		}
	}

}
