package net.flaxbeard.questionablyimmersive.client.model;

import com.mojang.blaze3d.platform.GlStateManager;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;

public class CokeOvenBatteryModel extends Model
{
	public RendererModel base;
	public RendererModel oven0;
	public RendererModel oven1;
	public RendererModel oven2;


	public RendererModel door;
	public RendererModel piston;
	public RendererModel cokeBlock;

	public boolean mirror;
	private boolean wasSneaking = false;


	public CokeOvenBatteryModel(boolean mirror)
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
		base = new RendererModel(this, 0, 0);

		oven0 = new RendererModel(this, 0, 0);
		oven0.addBox(0, 0, 0, 16, 64, 16 * 3 - 2);
		oven1 = new RendererModel(this, 0, 110);
		oven1.addBox(0, 0, 0, 16, 64, 16 * 3 - 2);
		oven2 = new RendererModel(this, 124, 110);
		oven2.addBox(0, 0, 0, 16, 64, 16 * 3 - 2);

		RendererModel hinge1 = new RendererModel(this, 0, 0);
		hinge1.setRotationPoint(2, 3, 46);
		hinge1.addBox(0, 0, 0, 2, 1, 1);
		base.addChild(hinge1);

		RendererModel hinge2 = new RendererModel(this, 0, 0);
		hinge2.setRotationPoint(2, 60, 46);
		hinge2.addBox(0, 0, 0, 2, 1, 1);
		base.addChild(hinge2);

		RendererModel wall1 = new RendererModel(this, 132, 0);
		wall1.setRotationPoint(0, 0, 46);
		wall1.addBox(0, 0, 0, 2, 64, 2);
		base.addChild(wall1);

		RendererModel wall2 = new RendererModel(this, 124, 0);
		wall2.setRotationPoint(14, 0, 46);
		wall2.addBox(0, 0, 0, 2, 64, 2);
		base.addChild(wall2);


		door = new RendererModel(this, 140, 0);
		door.setRotationPoint(3, 4, 46);
		door.addBox(0, 0, -1, 10, 56, 2);
		piston = new RendererModel(this, 6, 0);
		door.addChild(piston);
		piston.setRotationPoint(9, 57, 0);
		piston.addBox(-1, -1, -1, 2, 2, 2);
		RendererModel pistonSmall = new RendererModel(this, 0, 0);
		pistonSmall.setRotationPoint(0, 0, 2);
		pistonSmall.addBox(-.5f, -.5f, -17, 1, 1, 14);
		piston.addChild(pistonSmall);

		cokeBlock = new RendererModel(this, 124, 66);
		cokeBlock.setRotationPoint(4, 6, 37.99f);
		cokeBlock.addBox(0, 0, 0, 8, 50, 8);
	}

	public void render(CokeOvenBatteryTileEntity.Master battery, int index, float f5)
	{


		if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSneaking()) {
			wasSneaking = true;
		} else {
			wasSneaking = false;
		}

		if (index == 0)
		{
			this.oven1.render(f5);
		}
		else if ((battery != null && index == battery.numLayers - 1) || (battery == null && index == 5))
		{
			this.oven2.render(f5);
		}
		else
		{
			this.oven0.render(f5);
		}

		this.base.render(f5);
		cokeBlock.setRotationPoint(4, 4, 37.99f);
		if (Minecraft.getInstance().player != null)
		{
			float t = 0;
			if (battery != null && battery.active.length > index && battery.active[index] && battery.hasWorld())
			{
				if (battery.processMax[index] > 0 && battery.process[index] < 50)
				{
					t = (50 - battery.process[index]) + Minecraft.getInstance().getRenderPartialTicks();
				}
				else
				{
					t = 50 + (20 - battery.recuperationTime[index]) + Minecraft.getInstance().getRenderPartialTicks();
				}
			}
			float progress = t;

			double doorOpenPercent = 0;
			float cokeMovePercent = (Math.min(35, progress - 15f) / 35f) * 6f;

			if (progress <= 15f)
			{
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.color4f(1, 1, 1, progress / 15f);
				cokeBlock.setRotationPoint(4, 4, 38.01f);
				cokeBlock.render(f5);
				GlStateManager.color4f(1, 1, 1, 1);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			else if (progress <= 30f)
			{
				cokeBlock.setRotationPoint(4, 4, 38.01f + cokeMovePercent);
				cokeBlock.render(f5);
				doorOpenPercent = 1f - Math.cos((progress - 15f) * Math.PI / 30f);
			}
			else if (progress <= 50f)
			{
				doorOpenPercent = 1;
				cokeBlock.setRotationPoint(4, 4, 38.01f + cokeMovePercent);
				cokeBlock.render(f5);
			}
			else if (progress <= 55f)
			{
				if (progress <= 51f)
				{
					cokeBlock.setRotationPoint(4, 4, 38.01f + cokeMovePercent);
					cokeBlock.render(f5);
				}
				doorOpenPercent = 1;
			}
			else if (progress <= 70f)
			{
				doorOpenPercent = Math.cos((progress - 55) * Math.PI / 30f);
			}

			if (battery == null) {
				double ticks = Minecraft.getInstance().player.ticksExisted +  Minecraft.getInstance().getRenderPartialTicks();

				if (Math.floor((ticks / 45)) % 6 == index) {
					ticks = ticks - Math.floor((ticks / 45)) * 45;

					if (ticks < 15)
					{
						doorOpenPercent = (float) 1 - Math.cos((ticks * Math.PI / 30f));
					} else if (ticks < 30) {
						doorOpenPercent = 1;
					} else {
						doorOpenPercent = (float) Math.cos(((ticks - 30) * Math.PI / 30f));
					}
				}
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