package flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.questionablyimmersive.client.model.ModelCokeOvenBattery;
import flaxbeard.questionablyimmersive.client.model.ModelMortar;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MultiblockCokeOvenBatteryRenderer extends TileEntitySpecialRenderer<TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent>
{
	private static ModelCokeOvenBattery model = new ModelCokeOvenBattery(false);
	private static ModelCokeOvenBattery modelM = new ModelCokeOvenBattery(true);

	private static String texture = "questionablyimmersive:textures/models/coke_oven_battery.png";

	@Override
	public void render(TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
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

			
			ClientUtils.bindTexture(texture);

			if (te.mirrored)
			{
				GlStateManager.translate(te.ovenLength, 0, 3);
				GlStateManager.rotate(180, 0, 1, 0);
			}

			for (int i = 0; i < te.ovenLength; i++) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(te.mirrored ? (te.ovenLength - i - 1) : i, 0 , 0 );
				model.render(te, i, 0.0625F);
				GlStateManager.popMatrix();;
			}

			GlStateManager.popMatrix();
			
		}
	}

}
