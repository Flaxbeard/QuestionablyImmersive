package flaxbeard.questionablyimmersive.common;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySampleDrill;
import blusunrize.immersiveengineering.common.items.ItemCoresample;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import flaxbeard.questionablyimmersive.common.Config.IPConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid= QuestionablyImmersive.MODID)
public class EventHandler
{

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void renderLast(RenderWorldLastEvent event)
	{
		GlStateManager.pushMatrix();
		Minecraft mc = Minecraft.getMinecraft();
		if (IPConfig.sample_displayBorder && mc.player != null)
		{
			ItemStack mainItem = mc.player.getHeldItemMainhand();
			ItemStack secondItem = mc.player.getHeldItemOffhand();

			boolean main = !mainItem.isEmpty() && mainItem.getItem() instanceof ItemCoresample && ItemNBTHelper.hasKey(mainItem, "coords");
			boolean off = !secondItem.isEmpty() && secondItem.getItem() instanceof ItemCoresample && ItemNBTHelper.hasKey(secondItem, "coords");

			boolean chunkBorders = false;
			for(EnumHand hand : EnumHand.values())
				if(OreDictionary.itemMatches(new ItemStack(IEContent.blockMetalDevice1,1, BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta()), ClientUtils.mc().player.getHeldItem(hand),true))
				{
					chunkBorders = true;
					break;
				}
			if(!chunkBorders && ClientUtils.mc().objectMouseOver!=null && ClientUtils.mc().objectMouseOver.typeOfHit==Type.BLOCK && ClientUtils.mc().world.getTileEntity(ClientUtils.mc().objectMouseOver.getBlockPos()) instanceof TileEntitySampleDrill)
				chunkBorders = true;

			ItemStack target = main ? mainItem : secondItem;

			if (!chunkBorders && (main || off))
			{

				int[] coords = ItemNBTHelper.getIntArray(target, "coords");

				//World world = DimensionManager.getWorld(coords[0]);
				//if (world.provider.getDimension() == mc.player.worldObj.provider.getDimension())
				//{
					EntityPlayer player = mc.player;
					renderChunkBorder(coords[1] << 4, coords[2] << 4);
				//}
			}
		}
		GlStateManager.popMatrix();

	}

	@SideOnly(Side.CLIENT)
	public static void renderChunkBorder(int chunkX, int chunkZ)
	{
		EntityPlayer player = ClientUtils.mc().player;

		double px = TileEntityRendererDispatcher.staticPlayerX;
		double py = TileEntityRendererDispatcher.staticPlayerY;
		double pz = TileEntityRendererDispatcher.staticPlayerZ;
		int y = Math.min((int)player.posY-2,player.getEntityWorld().getChunkFromBlockCoords(new BlockPos(chunkX, 0, chunkZ)).getLowestHeight());
		float h = (float)Math.max(32, player.posY-y+4);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder vertexbuffer = tessellator.getBuffer();

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		float r = Lib.COLOUR_F_ImmersiveOrange[0];
		float g = Lib.COLOUR_F_ImmersiveOrange[1];
		float b = Lib.COLOUR_F_ImmersiveOrange[2];
		vertexbuffer.setTranslation(chunkX-px, y+2-py, chunkZ-pz);
		GlStateManager.glLineWidth(5f);
		vertexbuffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
		vertexbuffer.pos( 0,0, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,h, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,0, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,h, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,0,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,h,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,0,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,h,16).color(r,g,b,.375f).endVertex();

		vertexbuffer.pos( 0,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos( 0,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2,16).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2, 0).color(r,g,b,.375f).endVertex();
		vertexbuffer.pos(16,2,16).color(r,g,b,.375f).endVertex();
		tessellator.draw();
		vertexbuffer.setTranslation(0, 0, 0);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
	}
	
	@SubscribeEvent
	public static void onEntityJoiningWorld(EntityJoinWorldEvent event)
	{
		if (event.getEntity() instanceof EntityPlayer)
		{
			if (event.getEntity() instanceof FakePlayer) {
				return;
			}
			List<IRecipe> l = new ArrayList<IRecipe>();
			for (IRecipe recipe : CraftingManager.REGISTRY)
			{
				String name = recipe.getRegistryName().toString();
				if (name.length() > QuestionablyImmersive.MODID.length() && name.substring(0, QuestionablyImmersive.MODID.length()).equals(QuestionablyImmersive.MODID))
				{
					l.add(recipe);
				}
			}
			((EntityPlayer) event.getEntity()).unlockRecipes(l);
			
		}
	}

}
