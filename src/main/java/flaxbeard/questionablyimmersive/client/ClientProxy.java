package flaxbeard.questionablyimmersive.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.client.IECustomStateMapper;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import blusunrize.lib.manual.ManualPages;
import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import flaxbeard.questionablyimmersive.client.render.MultiblockCokeOvenBatteryRenderer;
import flaxbeard.questionablyimmersive.client.render.MultiblockMortarRenderer;
import flaxbeard.questionablyimmersive.client.render.MultiblockTriphammerRenderer;
import flaxbeard.questionablyimmersive.client.render.TileGaugeRenderer;
import flaxbeard.questionablyimmersive.common.CommonProxy;
import flaxbeard.questionablyimmersive.common.QIContent;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityGauge;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityTriphammer;
import flaxbeard.questionablyimmersive.common.blocks.multiblocks.MultiblockCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.multiblocks.MultiblockTriphammer;
import flaxbeard.questionablyimmersive.common.entity.EntityMortarItem;
import flaxbeard.questionablyimmersive.common.items.ItemQIBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.Locale;

@SuppressWarnings("deprecation")
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = QuestionablyImmersive.MODID)
public class ClientProxy extends CommonProxy
{
	public static final String CAT_QI = "qi";
	
	@Override
	public void preInit()
	{
		RenderingRegistry.registerEntityRenderingHandler(EntityMortarItem.class, (RenderManager renderManagerIn) -> new RenderEntityItem(renderManagerIn, Minecraft.getMinecraft().getRenderItem()) {
			@Override
			public boolean shouldSpreadItems()
			{
				return false;
			}

			@Override
			public boolean shouldBob()
			{
				return false;
			}
		});
	}
	
	@Override
	public void preInitEnd()
	{
		
	}
		
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt)
	{
		//Going through registered stuff at the end of preInit, because of compat modules possibly adding items
		for (Block block : QIContent.registeredIPBlocks)
		{
			Item blockItem = Item.getItemFromBlock(block);
			final ResourceLocation loc = Block.REGISTRY.getNameForObject(block);
			if (loc != null)
				if (block instanceof IIEMetaBlock) {
					IIEMetaBlock ieMetaBlock = (IIEMetaBlock) block;
					if (ieMetaBlock.useCustomStateMapper())
						ModelLoader.setCustomStateMapper(block, IECustomStateMapper.getStateMapper(ieMetaBlock));
					ModelLoader.setCustomMeshDefinition(blockItem, new ItemMeshDefinition() {
						@Override
						public ModelResourceLocation getModelLocation(ItemStack stack) {
							return new ModelResourceLocation(loc, "inventory");
						}
					});
					boolean isMD = false;
					for (int meta = isMD ? 1 : 0; meta < ieMetaBlock.getMetaEnums().length; meta++) {
						String location = loc.toString();
						String prop = ieMetaBlock.appendPropertiesToState() ? ("inventory," + ieMetaBlock.getMetaProperty().getName() + "=" + ieMetaBlock.getMetaEnums()[meta].toString().toLowerCase(Locale.US)) : null;
						if (ieMetaBlock.useCustomStateMapper()) {
							String custom = ieMetaBlock.getCustomStateMapping(meta, true);
							if (custom != null)
								location += "_" + custom;
						}
						try {
							ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
						} catch (NullPointerException npe) {
							throw new RuntimeException("WELP! apparently " + ieMetaBlock + " lacks an item!", npe);
						}
					}
					if (isMD) {
						ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(new ResourceLocation("questionablyimmersive", "auto_lube"), "inventory"));
					}
				}
				else
					ModelLoader.setCustomModelResourceLocation(blockItem, 0, new ModelResourceLocation(loc, "inventory"));
		}

		for(Item item : QIContent.registeredIPItems)
		{
			if(item instanceof ItemQIBase)
			{
				ItemQIBase ipMetaItem = (ItemQIBase) item;
				if(ipMetaItem.registerSubModels && ipMetaItem.getSubNames() != null && ipMetaItem.getSubNames().length > 0)
				{
					for(int meta = 0; meta < ipMetaItem.getSubNames().length; meta++)
					{
						ResourceLocation loc = new ResourceLocation("questionablyimmersive", ipMetaItem.itemName + "/" + ipMetaItem.getSubNames()[meta]);

						ModelBakery.registerItemVariants(ipMetaItem, loc);
						ModelLoader.setCustomModelResourceLocation(ipMetaItem, meta, new ModelResourceLocation(loc, "inventory"));
					}
				}
				else
				{
					final ResourceLocation loc = new ResourceLocation("questionablyimmersive", ipMetaItem.itemName);
					ModelBakery.registerItemVariants(ipMetaItem, loc);
					ModelLoader.setCustomMeshDefinition(ipMetaItem, new ItemMeshDefinition()
					{
						@Override
						public ModelResourceLocation getModelLocation(ItemStack stack)
						{
							return new ModelResourceLocation(loc, "inventory");
						}
					});
				}
			} 
			else
			{
				final ResourceLocation loc = Item.REGISTRY.getNameForObject(item);
				ModelBakery.registerItemVariants(item, loc);
				ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition()
				{
					@Override
					public ModelResourceLocation getModelLocation(ItemStack stack)
					{
						return new ModelResourceLocation(loc, "inventory");
					}
				});
			}
		}
	}

	@Override
	public void init()
	{
		ShaderUtil.init();
	
	}
	
	@SubscribeEvent(priority=EventPriority.LOWEST)
	public static void onModelBakeEvent(ModelBakeEvent event)
	{
		

	}

	@Override
	public void postInit()
	{


		ManualHelper.addEntry("cokeOvenBattery", CAT_QI,
				new ManualPages.Text(ManualHelper.getManual(), "cokeOvenBattery0"),
				new ManualPageMultiblock(ManualHelper.getManual(), "cokeOvenBattery1", MultiblockCokeOvenBattery.instance),
				new ManualPages.Text(ManualHelper.getManual(), "cokeOvenBattery2")
		);
		ManualHelper.addEntry("tripha", CAT_QI,
				new ManualPageMultiblock(ManualHelper.getManual(), "tripha", MultiblockTriphammer.instance)
		);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGauge.class, new TileGaugeRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMortar.TileEntityMortarParent.class, new MultiblockMortarRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCokeOvenBattery.TileEntityCokeOvenRenderedPart.class, new MultiblockCokeOvenBatteryRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTriphammer.TileEntityTriphammerParent.class, new MultiblockTriphammerRenderer());

	}

	private static void mapFluidState(Block block, Fluid fluid)
	{
		Item item = Item.getItemFromBlock(block);
		FluidStateMapper mapper = new FluidStateMapper(fluid);
		if(item != null)
		{
			ModelLoader.registerItemVariants(item);
			ModelLoader.setCustomMeshDefinition(item, mapper);
		}
		ModelLoader.setCustomStateMapper(block, mapper);
	}

	static class FluidStateMapper extends StateMapperBase implements ItemMeshDefinition
	{
		public final ModelResourceLocation location;

		public FluidStateMapper(Fluid fluid)
		{
			this.location = new ModelResourceLocation(QuestionablyImmersive.MODID + ":fluid_block", fluid.getName());
		}

		@Nonnull
		@Override
		protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state)
		{
			return location;
		}

		@Nonnull
		@Override
		public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack)
		{
			return location;
		}
	}
	
	public void renderTile(TileEntity te)
	{
		GlStateManager.pushMatrix();
		GlStateManager.rotate(-90, 0, 1, 0);
		GlStateManager.translate(0, 1, -4);


		TileEntitySpecialRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer((TileEntity) te);
		tesr.render(te, 0, 0, 0, 0, 0, 0);
		GlStateManager.popMatrix();
	}

	@Override
	public void drawUpperHalfSlab(ItemStack stack) {
		// Render slabs on top half
		BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBlockState state = IEContent.blockMetalDecorationSlabs1.getStateFromMeta(stack.getMetadata());
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState((IBlockState)state);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.5F, 1.0F);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(7425);
		} else {
			GlStateManager.shadeModel(7424);
		}

		blockRenderer.getBlockModelRenderer().renderModelBrightness(model, (IBlockState)state, 0.75F, false);
		GlStateManager.popMatrix();
	}
}