package net.flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.IEItems;
import com.mojang.blaze3d.platform.GlStateManager;
import net.flaxbeard.questionablyimmersive.client.model.CokeOvenBatteryModel;
import net.flaxbeard.questionablyimmersive.client.model.TriphammerModel;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.TriphammerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;

public class TriphammerRenderer extends TileEntityRenderer<TriphammerTileEntity.Master>
{
	private static TriphammerModel model = new TriphammerModel(false);

	private static String texture = "immersiveengineering:textures/block/metal/storage_steel.png";

	@Override
	public void render(TriphammerTileEntity.Master te, double x, double y, double z, float partialTicks, int destroyStage)
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
			GlStateManager.translated(0, 0, -3);

			if (te.getIsMirrored())
			{
			}

			ClientUtils.bindTexture(texture);

			//float ticks = te.activeTicks + (te.wasActive ? partialTicks : 0);;
			//model.ticks = modelM.ticks = 1.5F * ticks;

			GlStateManager.pushMatrix();

			model.ticks = Minecraft.getInstance().player.ticksExisted /* TODO te.ticks */ + partialTicks;
			model.render(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();

			ItemStack stack = te.inventory.get(0).copy();
			stack = new ItemStack(IEItems.Tools.hammer);
			if (!stack.isEmpty())
			{
				stack.setCount(1);
				GlStateManager.translated(3.5, 1, 1.5);
				GlStateManager.translated(0.0F, (1.0F / 32.0F), 0.0F);
				GlStateManager.rotated(90, 1, 0, 0);
				GlStateManager.scaled(1.99, 1.99, 1.99);
				renderItem(stack);

			}
			GlStateManager.popMatrix();



			GlStateManager.popMatrix();

		}
	}

	private void renderItem(ItemStack itemstack)
	{
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer itemRenderer = mc.getItemRenderer();

		if (!itemstack.isEmpty())
		{
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();

			GlStateManager.scaled(0.5F, 0.5F, 0.5F);
			GlStateManager.pushLightingAttributes();
			RenderHelper.enableStandardItemLighting();
			itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popAttributes();


			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
}