package flaxbeard.questionablyimmersive.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMortar extends ModelBase
{
	public ModelRenderer base;
	public ModelRenderer cannonBase;
	public ModelRenderer cannon;
	public ModelRenderer cannonTop;

	public boolean mirror;
	private boolean wasSneaking = false;
	
	public static int BASE_HEIGHT = 38;
	public static int BARREL_HEIGHT = 12;

	public ModelMortar(boolean mirror)
	{
		this.textureWidth = 16;
		this.textureHeight = 16;
		this.mirror = mirror;
		
		refresh();
	}
	
	public void refresh()
	{		
		this.base = new ModelRenderer(this, 0, 0);
		this.base.addBox(0, 0, 0, 16 * 5, 8, 16 * 3);

		BASE_HEIGHT = 38;
		int baseLipTop = 1;
		int baseLipBottom = 5;
		int baseLipLeftRight = 4;

		int underbarrelBoxHeight = 16;
		int underbarrelBoxWidth = 4;

		int lowerHeight = 32;
		int cannonHeight = 16 * 4;
		int barrelWidth = 8;
		BARREL_HEIGHT = 12;
		int barrelGap = 4;
		int prongHeight = (BARREL_HEIGHT - 2 - barrelGap) / 2;

		ModelRenderer bigBoxBottom = new ModelRenderer(this, 0, 0);
		bigBoxBottom.setRotationPoint(24, 8, 24);
		bigBoxBottom.addBox(-16, 0, -16, 16 * 2, 8, 16 * 2);
		this.base.addChild(bigBoxBottom);

		cannonBase = new ModelRenderer(this, 0, 0);
		cannonBase.addBox(-16, 8, -16, 16 * 2, 8, 16 * 2);

		ModelRenderer holderLeft = new ModelRenderer(this, 0, 0);
		holderLeft.addBox(-8, 16, -(barrelWidth + baseLipLeftRight)/2 - 4, 12, 28, 4);
		cannonBase.addChild(holderLeft);

		ModelRenderer holderRight = new ModelRenderer(this, 0, 0);
		holderRight.addBox(-8, 16, (barrelWidth + baseLipLeftRight)/2, 12, 28, 4);
		cannonBase.addChild(holderRight);

		cannon = new ModelRenderer(this, 0, 0);
		cannon.addBox(0, -(BARREL_HEIGHT + 2)/2 - baseLipBottom, -(barrelWidth + baseLipLeftRight)/2, BASE_HEIGHT, BARREL_HEIGHT + baseLipTop + baseLipBottom + 2, barrelWidth + baseLipLeftRight);
		cannon.rotateAngleZ = 0;

		ModelRenderer baseBox = new ModelRenderer(this, 0, 0);
		baseBox.addBox(BASE_HEIGHT, -(BARREL_HEIGHT + 2)/2 - baseLipBottom + 1, -(underbarrelBoxWidth)/2, underbarrelBoxHeight,  baseLipBottom - 1, underbarrelBoxWidth);
		cannon.addChild(baseBox);


		cannonTop = new ModelRenderer(this, 0, 0);
		cannonTop.setRotationPoint(0, (BARREL_HEIGHT / 2) - 1, 0);
		cannonTop.addBox(0, 0, -(barrelWidth / 2), cannonHeight, 2, barrelWidth);

		ModelRenderer barrelTS1 = new ModelRenderer(this, 0, 0);
		barrelTS1.addBox(0, -prongHeight, -(barrelWidth / 2), cannonHeight, prongHeight, 2);
		cannonTop.addChild(barrelTS1);

		ModelRenderer barrelTS2 = new ModelRenderer(this, 0, 0);
		barrelTS2.addBox(0, -prongHeight, (barrelWidth / 2) - 2, cannonHeight, prongHeight, 2);
		cannonTop.addChild(barrelTS2);

		ModelRenderer barrelTS1L = new ModelRenderer(this, 0, 0);
		barrelTS1L.addBox(0, -prongHeight - 1, -(barrelWidth / 2), lowerHeight, 1, 2);
		cannonTop.addChild(barrelTS1L);

		ModelRenderer barrelTS2L = new ModelRenderer(this, 0, 0);
		barrelTS2L.addBox(0, -prongHeight - 1, (barrelWidth / 2) - 2, lowerHeight, 1, 2);
		cannonTop.addChild(barrelTS2L);


		int numLoops = 3;

		for (int i = 0; i < numLoops; i++)
		{

			ModelRenderer barrelFrontS1 = new ModelRenderer(this, 0, 0);
			barrelFrontS1.setRotationPoint(cannonHeight - 2 - 4 * i, -BARREL_HEIGHT + 4, -(barrelWidth / 2) - 2.75f);
			barrelFrontS1.addBox(0, 0, 0, 2, BARREL_HEIGHT - 6, 2);
			cannonTop.addChild(barrelFrontS1);

			ModelRenderer barrelFrontS1Arm1 = new ModelRenderer(this, 0, 0);
			barrelFrontS1Arm1.setRotationPoint(0, 0, 0);
			barrelFrontS1Arm1.addBox(-0.001f, -4, 0, 2, 4, 2);
			barrelFrontS1Arm1.rotateAngleX = (float) Math.toRadians(-45);
			barrelFrontS1.addChild(barrelFrontS1Arm1);

			ModelRenderer barrelFrontS1Arm2 = new ModelRenderer(this, 0, 0);
			barrelFrontS1Arm2.setRotationPoint(0, BARREL_HEIGHT - 6, 0);
			barrelFrontS1Arm2.addBox(-0.001f, 0, 0, 2, 4, 2);
			barrelFrontS1Arm2.rotateAngleX = (float) Math.toRadians(45);
			barrelFrontS1.addChild(barrelFrontS1Arm2);

			ModelRenderer barrelFrontS2 = new ModelRenderer(this, 0, 0);
			barrelFrontS2.setRotationPoint(cannonHeight - 2 - 4 * i, -BARREL_HEIGHT + 4, (barrelWidth / 2) + 2.75f);
			barrelFrontS2.addBox(0, 0, -2, 2, BARREL_HEIGHT - 6, 2);
			cannonTop.addChild(barrelFrontS2);

			ModelRenderer barrelFrontS2Arm1 = new ModelRenderer(this, 0, 0);
			barrelFrontS2Arm1.setRotationPoint(0, 0, 0);
			barrelFrontS2Arm1.addBox(-0.001f, -4, -2, 2, 4, 2);
			barrelFrontS2Arm1.rotateAngleX = (float) Math.toRadians(45);
			barrelFrontS2.addChild(barrelFrontS2Arm1);

			ModelRenderer barrelFrontS2Arm2 = new ModelRenderer(this, 0, 0);
			barrelFrontS2Arm2.setRotationPoint(0, BARREL_HEIGHT - 6, 0);
			barrelFrontS2Arm2.addBox(-0.001f, 0, -2, 2, 4, 2);
			barrelFrontS2Arm2.rotateAngleX = (float) Math.toRadians(-45);
			barrelFrontS2.addChild(barrelFrontS2Arm2);
		}

		ModelRenderer barrelBottom = new ModelRenderer(this, 0, 0);
		barrelBottom.addBox(0, -BARREL_HEIGHT, -(barrelWidth / 2), cannonHeight, 2, barrelWidth);
		cannonTop.addChild(barrelBottom);

		ModelRenderer barrelBS1 = new ModelRenderer(this, 0, 0);
		barrelBS1.addBox(0, -BARREL_HEIGHT + 2, -(barrelWidth / 2), cannonHeight, prongHeight, 2);
		cannonTop.addChild(barrelBS1);

		ModelRenderer barrelBS2 = new ModelRenderer(this, 0, 0);
		barrelBS2.addBox(0, -BARREL_HEIGHT + 2, (barrelWidth / 2) - 2, cannonHeight, prongHeight, 2);
		cannonTop.addChild(barrelBS2);


		ModelRenderer barrelBS1L = new ModelRenderer(this, 0, 0);
		barrelBS1L.addBox(0, -BARREL_HEIGHT + 2 + prongHeight, -(barrelWidth / 2), lowerHeight, 1, 2);
		cannonTop.addChild(barrelBS1L);

		ModelRenderer barrelBS2L = new ModelRenderer(this, 0, 0);
		barrelBS2L.addBox(0, -BARREL_HEIGHT + 2 + prongHeight, (barrelWidth / 2) - 2, lowerHeight, 1, 2);
		cannonTop.addChild(barrelBS2L);

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
	}

	public void renderCannonBase(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		this.cannonBase.render(f5);
	}

	public void renderCannon(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		this.cannon.render(f5);
	}

	public void renderCannonTop(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		this.cannonTop.render(f5);
	}
}
