package flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.questionablyimmersive.client.model.ModelMortar;
import flaxbeard.questionablyimmersive.common.blocks.tile.TileEntityMortar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MultiblockMortarRenderer extends TileEntitySpecialRenderer<TileEntityMortar.TileEntityMortarParent>
{
	private static ModelMortar model = new ModelMortar(false);
	private static ModelMortar modelM = new ModelMortar(true);

	private static String texture = "immersiveengineering:textures/blocks/storage_steel.png";

	@Override
	public void render(TileEntityMortar.TileEntityMortarParent te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if (te != null)
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y - 1, z);
	
			EnumFacing rotation = te.facing;

			float rotationMod = 0;
			if (rotation == EnumFacing.NORTH)
			{
				rotationMod = 90f;
				GlStateManager.rotate(90F, 0, 1, 0);
				GlStateManager.translate(-1, 0, 1);
			}
			else if (rotation == EnumFacing.WEST)
			{
				rotationMod = 180f;
				GlStateManager.rotate(180F, 0, 1, 0);
				GlStateManager.translate(-1, 0, 0);
			}
			else if (rotation == EnumFacing.SOUTH)
			{
				rotationMod = 270f;
				GlStateManager.rotate(270F, 0, 1, 0);
			}
			else
			{
				GlStateManager.translate(0, 0, 1);
			}
			GlStateManager.translate(0, 0, -2);

			if (te.mirrored)
			{
			}
			
			ClientUtils.bindTexture(texture);
			
			//float ticks = te.activeTicks + (te.wasActive ? partialTicks : 0);;
			//model.ticks = modelM.ticks = 1.5F * ticks;


			model.render(null, 0, 0, 0, 0, 0, 0.0625F);



			GlStateManager.translate(24/16f, 8/16f, 24/16f);
			GlStateManager.rotate(te.rotation - rotationMod, 0, 1, 0);
			model.renderCannonBase(null, 0,0, 0 , 0, 0, 0.0625F);

			GlStateManager.translate(-8/16f, 18/16f, 0F);
			GlStateManager.rotate(80, 0, 0, 1);

			float recoilTicks = (20 - te.reloadTicks);
			if (recoilTicks < 20)
			{
				recoilTicks += Minecraft.getMinecraft().getRenderPartialTicks();
			}
			float recoilAmount;

			if (recoilTicks <= 1F) {
				recoilAmount = (1F - recoilTicks) / 1F;
			} else {
				recoilTicks -= 1F;
				recoilAmount = recoilTicks / 19F;
			}


			recoilAmount = 1F - (float) Math.cos(recoilAmount * Math.PI / 2F);
			GlStateManager.translate(-1/16F + recoilAmount * 1/16F, 0, 0 );
			model.renderCannon(null, 0,0, 0 , 0, 0, 0.0625F);

			GlStateManager.translate(ModelMortar.BASE_HEIGHT / 16F, 0, 0);
			GlStateManager.translate(-3/16F + recoilAmount * 3/16F, 0, 0 );
			model.renderCannonTop(null, 0,0, 0 , 0, 0, 0.0625F);

			GlStateManager.popMatrix();
			
		}
	}

}
