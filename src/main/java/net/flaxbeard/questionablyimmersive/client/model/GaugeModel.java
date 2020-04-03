package net.flaxbeard.questionablyimmersive.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.Entity;

public class GaugeModel extends Model
{
	public RendererModel base;
	public RendererModel dial;

	public float dialRotation = 0;

	public GaugeModel()
	{
		this.textureWidth = 64;
		this.textureHeight = 64;

		refresh();
	}

	public void refresh()
	{
		this.base = new RendererModel(this, 0, 0);
		this.base.addBox(4, 4, 0, 8, 8, 1);

		RendererModel baseBack = new RendererModel(this, 0, 18);
		baseBack.addBox(4, 4, 0.96f, 8, 8, 0);
		base.addChild(baseBack);

		this.dial = new RendererModel(this, 26, 6);
		this.dial.setRotationPoint(8, 8f, 0.98f);
		this.dial.addBox(-0.5f, -0.5f, 0, 1, 4, 0);

	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSneaking())
		{
			refresh();
		}
		this.dial.rotateAngleZ = (float) Math.toRadians(dialRotation * -270f + 135f);
		this.dial.rotateAngleX = 0;
		this.base.render(f5);
		this.dial.render(f5);
	}
}