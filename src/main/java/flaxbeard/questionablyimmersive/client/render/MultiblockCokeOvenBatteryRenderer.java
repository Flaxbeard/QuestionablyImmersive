package flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.questionablyimmersive.client.model.ModelCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.tile.TileEntityCokeOvenBattery;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MultiblockCokeOvenBatteryRenderer extends TileEntitySpecialRenderer<TileEntityCokeOvenBattery.TileEntityCokeOvenRenderedPart>
{
	private static ModelCokeOvenBattery model = new ModelCokeOvenBattery(false);
	private static ModelCokeOvenBattery modelM = new ModelCokeOvenBattery(true);

	private static String texture = "questionablyimmersive:textures/models/coke_oven_battery.png";
	private static String textureOff = "questionablyimmersive:textures/models/coke_oven_battery_off.png";

	@Override
	public void render(TileEntityCokeOvenBattery.TileEntityCokeOvenRenderedPart te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
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
				GlStateManager.translate(te.ovenLength, 0, 3);
				GlStateManager.rotate(180, 0, 1, 0);
			}

			if (te.ovenLength == 0)
			{
				GlStateManager.translate(-3, 0, 1);
				ClientUtils.bindTexture(texture);
				for (int i = 0; i < 6; i++)
				{
					GlStateManager.pushMatrix();
					GlStateManager.translate(i, 0 , 0 );
					model.render(null, i, 0.0625F);
					GlStateManager.popMatrix();
				}
			}
			else
			{
				for (int i = te.ovenIndex; i < Math.min(te.ovenIndex + 5, te.ovenLength); i++)
				{
					GlStateManager.pushMatrix();
					int ovenTest = i - te.ovenIndex;
					GlStateManager.translate(te.mirrored ? (te.ovenLength - ovenTest - 1) : ovenTest, 0 , 0 );

					TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent parent = (TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent) te.master();
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
