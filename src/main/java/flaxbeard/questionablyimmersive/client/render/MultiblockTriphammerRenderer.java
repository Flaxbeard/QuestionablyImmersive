package flaxbeard.questionablyimmersive.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import flaxbeard.questionablyimmersive.client.model.ModelMortar;
import flaxbeard.questionablyimmersive.client.model.ModelTriphammer;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityTriphammer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.collection.parallel.ParIterableLike;

@SideOnly(Side.CLIENT)
public class MultiblockTriphammerRenderer extends TileEntitySpecialRenderer<TileEntityTriphammer.TileEntityTriphammerParent>
{
	private static ModelTriphammer model = new ModelTriphammer(false);

	private static String texture = "immersiveengineering:textures/blocks/storage_steel.png";

	@Override
	public void render(TileEntityTriphammer.TileEntityTriphammerParent te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
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

			GlStateManager.pushMatrix();

			model.ticks = te.ticks + partialTicks;
			model.render(null, 0, 0, 0, 0, 0, 0.0625F);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();

			ItemStack stack = te.inventory.get(0).copy();
			if (!stack.isEmpty())
			{

				stack.setCount(1);
				GlStateManager.translate(3.5, 1, 1.5);
				GlStateManager.translate(0.0F, (1.0F / 32.0F), 0.0F);
				GlStateManager.rotate(90, 1, 0, 0);
				GlStateManager.scale(1.99, 1.99, 1.99);
				renderItem(stack);

			}
			GlStateManager.popMatrix();



			GlStateManager.popMatrix();
			
		}
	}

	private void renderItem(ItemStack itemstack)
	{
		Minecraft mc = Minecraft.getMinecraft();
		RenderItem itemRenderer = mc.getRenderItem();

		if (!itemstack.isEmpty())
		{
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();

			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.pushAttrib();
			RenderHelper.enableStandardItemLighting();
			itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popAttrib();


			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
}
