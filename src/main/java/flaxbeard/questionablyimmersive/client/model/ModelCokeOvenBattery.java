package flaxbeard.questionablyimmersive.client.model;

import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityCokeOvenBattery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelCokeOvenBattery extends ModelBase
{
	public ModelRenderer base;
	public ModelRenderer door;
	public ModelRenderer piston;
	public ModelRenderer cokeBlock;

	public boolean mirror;
	private boolean wasSneaking = false;


	public ModelCokeOvenBattery(boolean mirror)
	{
		this.textureWidth = 256;
		this.textureHeight = 256;
		this.mirror = mirror;
		
		refresh();
	}
	
	public void refresh()
	{
		this.textureWidth = 256;
		this.textureHeight = 256;
		base = new ModelRenderer(this, 0, 0);
		base.addBox(0, 0, 0, 16, 64, 16 * 3 - 2);

		ModelRenderer hinge1 = new ModelRenderer(this, 0, 0);
		hinge1.setRotationPoint(2, 3, 46);
		hinge1.addBox(0, 0, 0, 2, 1, 1);
		base.addChild(hinge1);

		ModelRenderer hinge2 = new ModelRenderer(this, 0, 0);
		hinge2.setRotationPoint(2, 60, 46);
		hinge2.addBox(0, 0, 0, 2, 1, 1);
		base.addChild(hinge2);

		ModelRenderer wall1 = new ModelRenderer(this, 124, 0);
		wall1.setRotationPoint(0, 0, 46);
		wall1.addBox(0, 0, 0, 2, 64, 2);
		base.addChild(wall1);

		ModelRenderer wall2 = new ModelRenderer(this, 132, 0);
		wall2.setRotationPoint(14, 0, 46);
		wall2.addBox(0, 0, 0, 2, 64, 2);
		base.addChild(wall2);


		door = new ModelRenderer(this, 140, 0);
		door.setRotationPoint(3, 4, 46);
		door.addBox(0, 0, -1, 10, 56, 2);
		piston = new ModelRenderer(this, 6, 0);
		door.addChild(piston);
		piston.setRotationPoint(9, 57, 0);
		piston.addBox(-1, -1, -1, 2, 2, 2);
		ModelRenderer pistonSmall = new ModelRenderer(this, 0, 0);
		pistonSmall.setRotationPoint(0, 0, 2);
		pistonSmall.addBox(-.5f, -.5f, -17, 1, 1, 14);
		piston.addChild(pistonSmall);

		cokeBlock = new ModelRenderer(this, 124, 66);
		cokeBlock.setRotationPoint(4, 6, 37.99f);
		cokeBlock.addBox(0, 0, 0, 8, 50, 8);
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{

	}

	public void render(TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent battery, int index, float f5)
	{
		if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isSneaking()) {
			if (!wasSneaking) {
				refresh();
			}
			wasSneaking = true;
		} else {
			wasSneaking = false;
		}
		this.base.render(f5);
		cokeBlock.setRotationPoint(4, 4, 37.99f);
		if (Minecraft.getMinecraft().player != null)
		{
			float t = 0;
			if (battery.active[index])
			{
				if (battery.processMax[index] > 0 && battery.process[index] < 50)
				{
					t = (50 - battery.process[index]) + Minecraft.getMinecraft().getRenderPartialTicks();
				}
				else
				{
					t = 50 + (20 - battery.recuperationTime[index]) + Minecraft.getMinecraft().getRenderPartialTicks();
				}
			}
			float progress = t;

			double doorOpenPercent = 0;
			float cokeMovePercent = Math.min(35, progress - 15f) / 35f;

			if (progress <= 15f)
			{
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.color(1, 1, 1, progress / 15f);
				cokeBlock.setRotationPoint(4, 4, 38.01f);
				cokeBlock.render(f5);
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			else if (progress <= 30f)
			{
				cokeBlock.setRotationPoint(4, 4, 38.01f + cokeMovePercent * 6f);
				cokeBlock.render(f5);
				doorOpenPercent = 1f - Math.cos((progress - 15f) * Math.PI / 30f);
			}
			else if (progress <= 50f)
			{
				doorOpenPercent = 1;
				cokeBlock.setRotationPoint(4, 4, 38.01f + cokeMovePercent * 6f);
				cokeBlock.render(f5);
			}
			else if (progress <= 55f)
			{
				if (progress <= 51f)
				{
					cokeBlock.setRotationPoint(4, 4, 38.01f + cokeMovePercent * 6f);
					cokeBlock.render(f5);
				}
				doorOpenPercent = 1;
			}
			else if (progress <= 70f)
			{
				doorOpenPercent = Math.cos((progress - 55) * Math.PI / 30f);
			}

			if (doorOpenPercent > 0)
			{
				door.rotateAngleY = (float) -Math.toRadians(doorOpenPercent * 105);
				float b = 9;
				float c = 9;
				double a = Math.sqrt(b*b + c*c - 2*b*c*Math.cos(-door.rotateAngleY));
				double angle = Math.asin(b * Math.sin(-door.rotateAngleY) / a);
				piston.rotateAngleY = (float) (Math.toRadians(90) - angle);
			}
			else
			{
				door.rotateAngleY = 0;
				piston.rotateAngleY = (float) (Math.toRadians(0));
			}
		}
		this.door.render(f5);
	}
}
