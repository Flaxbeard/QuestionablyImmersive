package net.flaxbeard.questionablyimmersive;

import blusunrize.immersiveengineering.common.items.IEItems;
import net.flaxbeard.questionablyimmersive.client.ClientProxy;
import net.flaxbeard.questionablyimmersive.common.CommonProxy;
import net.flaxbeard.questionablyimmersive.common.QIConfig;
import net.flaxbeard.questionablyimmersive.common.QIContent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(QuestionablyImmersive.MODID)
public class QuestionablyImmersive
{
	public static final String MODID = "questionablyimmersive";
	public static final String VERSION = "@VERSION@";

	private static final Logger LOGGER = LogManager.getLogger();

	public static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);

	public static final SimpleChannel packetHandler = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(QuestionablyImmersive.MODID, "main")).networkProtocolVersion(() ->
	{
		return "1";
	}).serverAcceptedVersions("1"::equals).clientAcceptedVersions("1"::equals).simpleChannel();

	public static ItemGroup itemGroup = new ItemGroup("questionablyimmersive")
	{
		@Override
		public ItemStack createIcon()
		{
			return new ItemStack(IEItems.Tools.hammer);
		}
	};

	public QuestionablyImmersive()
	{
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);

		MinecraftForge.EVENT_BUS.register(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, QIConfig.ALL);

		QIContent.modConstruction();
	}


	private void preInit(final FMLCommonSetupEvent event)
	{
		QIContent.init();
		proxy.preInit();

		proxy.postInit();
	}

	public void loadComplete(FMLLoadCompleteEvent event)
	{
		proxy.postPostInit();
	}
}
