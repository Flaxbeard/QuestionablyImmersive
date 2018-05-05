package flaxbeard.questionablyimmersive.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelAxle extends ModelBase
{
	public ModelRenderer axle;

	public ModelAxle()
	{
		this.textureWidth = 64;
		this.textureHeight = 32;

		refresh();
	}
	
	public void refresh()
	{		
		this.axle = new ModelRenderer(this, 0, 0);
		this.axle.addBox(6, 6, 0, 4, 4, 16);
	}
	
	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{
		if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isSneaking())
		{
			refresh();
		}
		this.axle.render(f5);
	}
}
