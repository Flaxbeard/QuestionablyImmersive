package net.flaxbeard.questionablyimmersive.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.Entity;

public class TriphammerModel extends Model
{
	public RendererModel base;
	public RendererModel hammer;
	public RendererModel spinboye;

	public boolean mirror;
	private boolean wasSneaking = false;
	public float ticks;
	public float fallingTicks;

	public TriphammerModel(boolean mirror)
	{
		this.textureWidth = 256;
		this.textureHeight = 128;
		this.mirror = mirror;

		refresh();
	}

	public void refresh()
	{
		//this.base = new RendererModel(this, 64, 0);
		//this.base.addBox(0, 0, 0, 16 * 3, 8, 16 * 3);
		this.base = new RendererModel(this, 64, 0);
		this.base.addBox(-0.001f, 0, 8, 16 * 3, 8, 16 * 2);

		RendererModel axle = new RendererModel(this, 16, 79);
		axle.addBox(24, 22, 16, 4, 4, 16);
		this.base.addChild(axle);

		if (mirror)
		{
			RendererModel redman = new RendererModel(this, 0, 0);
			redman.addBox(-0.01f, 0.005f, 32, 16, 32, 16);
			this.base.addChild(redman);
		} else
		{
			RendererModel redman = new RendererModel(this, 0, 0);
			redman.addBox(-0.01f, 0.005f, 0, 16, 32, 16);
			this.base.addChild(redman);
		}

		/*RendererModel input = new RendererModel(this, 0, 0);
		input.addBox(48, 0, 0, 16, 16, 16);
		this.base.addChild(input);



		RendererModel output = new RendererModel(this, 0, 0);
		output.addBox(48, 0, 32, 16, 16, 16);
		this.base.addChild(output);*/

		this.hammer = new RendererModel(this, 0, 69);
		this.hammer.setRotationPoint(26, 24, 24);
		this.hammer.addBox(12 - 24, 21 - 24, 22 - 24, 16 * 4 - 12 - 2, 6, 4);
		hammer.rotateAngleZ = (float) Math.toRadians(50);
		RendererModel hammerHead = new RendererModel(this, 56, 79);
		hammerHead.addBox(50 - 24 - 2, 16 - 23, 20 - 24, 12, 14, 8);
		this.hammer.addChild(hammerHead);

		if (mirror)
		{
			this.spinboye = new RendererModel(this, 0, 48);
			this.spinboye.setRotationPoint(8, 24, 36);
			this.spinboye.addBox(-4, -2, -8, 8, 4, 4);

			RendererModel trip = new RendererModel(this, 24, 48);
			trip.addBox(4, -2, -13, 4, 4, 9);
			this.spinboye.addChild(trip);

			RendererModel trip2 = new RendererModel(this, 0, 56);
			trip2.addBox(-8, -2, -13, 4, 4, 9);
			this.spinboye.addChild(trip2);
		} else
		{
			this.spinboye = new RendererModel(this, 0, 48);
			this.spinboye.setRotationPoint(8, 24, 24);
			this.spinboye.addBox(-4, -2, -8, 8, 4, 4);

			RendererModel trip = new RendererModel(this, 24, 48);
			trip.addBox(4, -2, -8, 4, 4, 9);
			this.spinboye.addChild(trip);

			RendererModel trip2 = new RendererModel(this, 0, 56);
			trip2.addBox(-8, -2, -8, 4, 4, 9);
			this.spinboye.addChild(trip2);
		}

		RendererModel leg1 = new RendererModel(this, 0, 79);
		leg1.addBox(-2F, 1F, -2F, 4, 24, 4);
		leg1.setRotationPoint(26 - 8.45F, 0F, 15.2F);
		leg1.rotateAngleX = (float) Math.toRadians(10);
		leg1.rotateAngleZ = (float) Math.toRadians(-15);
		leg1.rotateAngleY = (float) Math.toRadians(20);
		this.base.addChild(leg1);

		RendererModel leg2 = new RendererModel(this, 0, 79);
		leg2.addBox(-2F, 1F, -2F, 4, 24, 4);
		leg2.setRotationPoint(26 + 8.45F, 0F, 15.2F);
		leg2.rotateAngleX = (float) Math.toRadians(10);
		leg2.rotateAngleZ = (float) Math.toRadians(15);
		leg2.rotateAngleY = (float) Math.toRadians(-20);
		this.base.addChild(leg2);

		RendererModel leg3 = new RendererModel(this, 0, 79);
		leg3.addBox(-2F, 1F, -2F, 4, 24, 4);
		leg3.setRotationPoint(26 - 8.45F, 0F, 32.8F);
		leg3.rotateAngleX = (float) Math.toRadians(-10);
		leg3.rotateAngleZ = (float) Math.toRadians(-15);
		leg3.rotateAngleY = (float) Math.toRadians(-20);
		this.base.addChild(leg3);

		RendererModel leg4 = new RendererModel(this, 0, 79);
		leg4.addBox(-2F, 1F, -2F, 4, 24, 4);
		leg4.setRotationPoint(26 + 8.45F, 0F, 32.8F);
		leg4.rotateAngleX = (float) Math.toRadians(-10);
		leg4.rotateAngleZ = (float) Math.toRadians(15);
		leg4.rotateAngleY = (float) Math.toRadians(20);
		this.base.addChild(leg4);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isSneaking())
		{
			if (!wasSneaking)
			{
				refresh();
			}
			wasSneaking = true;
		} else
		{
			wasSneaking = false;
		}
		this.base.render(f5);

		float tks = (ticks % 120) / 2;
		float fTks = (fallingTicks % 120) / 2;

		float rot = (float) Math.toRadians((tks + 23.5) * -6 + 180);
		spinboye.rotateAngleZ = rot;


		fTks = fTks % 30;
		float totalDeg = 37f;

		float offset = (float) Math.toRadians(-2);

		if (fTks >= 0 && fTks < 9)
		{
			hammer.rotateAngleZ = (float) Math.toRadians(totalDeg * fTks / 9.);
		} else if (fTks < 15)
		{
			float ttl = (float) Math.toRadians(totalDeg);
			hammer.rotateAngleZ = ttl;
		} else if (fTks < 17)
		{
			float ttl = (float) Math.toRadians(totalDeg) - offset;
			float down = (float) Math.cos((fTks - 15) / 2 * Math.PI / 2);
			hammer.rotateAngleZ = down * ttl + offset;
		} else
		{
			hammer.rotateAngleZ = 0 + offset;
		}

		this.spinboye.render(f5);
		this.hammer.render(f5);

	}
}