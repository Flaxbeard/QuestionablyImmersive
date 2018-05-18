package flaxbeard.questionablyimmersive.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelTriphammer extends ModelBase
{
	public ModelRenderer base;
	public ModelRenderer hammer;
	public ModelRenderer spinboye;

	public boolean mirror;
	private boolean wasSneaking = false;
	public float ticks;

	public ModelTriphammer(boolean mirror)
	{
		this.textureWidth = 16;
		this.textureHeight = 16;
		this.mirror = mirror;

		refresh();
	}
	
	public void refresh()
	{
		this.base = new ModelRenderer(this, 0, 0);
		//this.base.addBox(0, 0, 0, 16 * 3, 8, 16 * 3);

		ModelRenderer axle = new ModelRenderer(this, 0, 0);
		axle.addBox(24, 22, 16, 4, 4, 16);
		this.base.addChild(axle);

		ModelRenderer redman = new ModelRenderer(this, 0, 0);
		redman.addBox(-0.01f, 0, 0, 16, 32, 16);
		this.base.addChild(redman);

		/*ModelRenderer input = new ModelRenderer(this, 0, 0);
		input.addBox(48, 0, 0, 16, 16, 16);
		this.base.addChild(input);



		ModelRenderer output = new ModelRenderer(this, 0, 0);
		output.addBox(48, 0, 32, 16, 16, 16);
		this.base.addChild(output);*/

		this.hammer = new ModelRenderer(this, 0, 0);
		this.hammer.setRotationPoint(26, 24, 24);
		this.hammer.addBox(12 - 24, 21 - 24, 22 - 24, 16 * 4 - 12 -2, 6, 4);
		hammer.rotateAngleZ = (float) Math.toRadians(50);
		ModelRenderer hammerHead = new ModelRenderer(this, 0, 0);
		hammerHead.addBox(50 - 24 - 2, 16 - 24, 20 - 24, 12, 14, 8);
		this.hammer.addChild(hammerHead);

		this.spinboye = new ModelRenderer(this, 0, 0);
		this.spinboye.setRotationPoint(8, 24, 24);
		this.spinboye.addBox(-4, -2, -8, 8, 4, 4);

		ModelRenderer trip = new ModelRenderer(this, 0, 0);
		trip.addBox(4, -2, -8, 4, 4, 9);
		this.spinboye.addChild(trip);

		ModelRenderer trip2 = new ModelRenderer(this, 0, 0);
		trip2.addBox(-8, -2, -8, 4, 4, 9);
		this.spinboye.addChild(trip2);


		/*ModelRenderer leg1 = new ModelRenderer(this, 176, 56);
		leg1.addBox(-2F, -2F, -2F, 4, 18, 4);
		leg1.setRotationPoint(26 - 5.6F, 8F, 16.2F);
		leg1.rotateAngleX = (float) Math.toRadians(10);
		leg1.rotateAngleZ = (float) Math.toRadians(-15);
		leg1.rotateAngleY = (float) Math.toRadians(20);
		this.base.addChild(leg1);

		ModelRenderer leg2 = new ModelRenderer(this, 176, 56);
		leg2.addBox(-2F, -2F, -2F, 4, 18, 4);
		leg2.setRotationPoint(26 + 5.6F, 8F, 16.2F);
		leg2.rotateAngleX = (float) Math.toRadians(10);
		leg2.rotateAngleZ = (float) Math.toRadians(15);
		leg2.rotateAngleY = (float) Math.toRadians(-20);
		this.base.addChild(leg2);

		ModelRenderer leg3 = new ModelRenderer(this, 176, 56);
		leg3.addBox(-2F, -2F, -2F, 4, 18, 4);
		leg3.setRotationPoint(26 - 5.6F, 8F, 30.8F);
		leg3.rotateAngleX = (float) Math.toRadians(-10);
		leg3.rotateAngleZ = (float) Math.toRadians(-15);
		leg3.rotateAngleY = (float) Math.toRadians(-20);
		this.base.addChild(leg3);

		ModelRenderer leg4 = new ModelRenderer(this, 176, 56);
		leg4.addBox(-2F, -2F, -2F, 4, 18, 4);
		leg4.setRotationPoint(26 + 5.6F, 8F, 30.8F);
		leg4.rotateAngleX = (float) Math.toRadians(-10);
		leg4.rotateAngleZ = (float) Math.toRadians(15);
		leg4.rotateAngleY = (float) Math.toRadians(20);
		this.base.addChild(leg4);*/

		ModelRenderer leg1 = new ModelRenderer(this, 176, 56);
		leg1.addBox(-2F, -2F, -2F, 4, 27, 4);
		leg1.setRotationPoint(26 - 8.45F, 0F, 15.2F);
		leg1.rotateAngleX = (float) Math.toRadians(10);
		leg1.rotateAngleZ = (float) Math.toRadians(-15);
		leg1.rotateAngleY = (float) Math.toRadians(20);
		this.base.addChild(leg1);

		ModelRenderer leg2 = new ModelRenderer(this, 176, 56);
		leg2.addBox(-2F, -2F, -2F, 4, 27, 4);
		leg2.setRotationPoint(26 + 8.45F, 0F, 15.2F);
		leg2.rotateAngleX = (float) Math.toRadians(10);
		leg2.rotateAngleZ = (float) Math.toRadians(15);
		leg2.rotateAngleY = (float) Math.toRadians(-20);
		this.base.addChild(leg2);

		ModelRenderer leg3 = new ModelRenderer(this, 176, 56);
		leg3.addBox(-2F, -2F, -2F, 4, 27, 4);
		leg3.setRotationPoint(26 - 8.45F, 0F, 32.8F);
		leg3.rotateAngleX = (float) Math.toRadians(-10);
		leg3.rotateAngleZ = (float) Math.toRadians(-15);
		leg3.rotateAngleY = (float) Math.toRadians(-20);
		this.base.addChild(leg3);

		ModelRenderer leg4 = new ModelRenderer(this, 176, 56);
		leg4.addBox(-2F, -2F, -2F, 4, 27, 4);
		leg4.setRotationPoint(26 + 8.45F, 0F, 32.8F);
		leg4.rotateAngleX = (float) Math.toRadians(-10);
		leg4.rotateAngleZ = (float) Math.toRadians(15);
		leg4.rotateAngleY = (float) Math.toRadians(20);
		this.base.addChild(leg4);
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
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
		this.hammer.render(f5);

		float tks = (ticks % 120) / 2;

		float rot = (float) Math.toRadians((tks + 23.5)  * -6 + 180);
		spinboye.rotateAngleZ = rot;


		tks = tks % 30;
		float totalDeg = 37f;

		if (tks > 0 && tks < 9)
		{
			hammer.rotateAngleZ = (float)  Math.toRadians(totalDeg * tks / 9.);
		}
		else if (tks < 15)
		{
			float ttl = (float)  Math.toRadians(totalDeg);
			hammer.rotateAngleZ = ttl;
		}
		else if (tks < 17)
		{
			float ttl = (float)  Math.toRadians(totalDeg);
			float down = (float) Math.cos((tks - 15) / 2 * Math.PI / 2);
			hammer.rotateAngleZ = down * ttl;
		}
		else
		{
			hammer.rotateAngleZ = 0;
		}

		this.spinboye.render(f5);
	}
}
