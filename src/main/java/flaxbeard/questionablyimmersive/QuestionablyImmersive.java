package flaxbeard.questionablyimmersive;

import flaxbeard.questionablyimmersive.common.CommonProxy;
import flaxbeard.questionablyimmersive.common.QEContent;
import flaxbeard.questionablyimmersive.common.network.QEPacketHandler;
import flaxbeard.questionablyimmersive.common.util.CommandHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = QuestionablyImmersive.MODID, version = QuestionablyImmersive.VERSION, dependencies = "required-after:immersiveengineering@[0.12,);")
public class QuestionablyImmersive
{
	public static final String MODID = "questionablyimmersive";
	public static final String VERSION = "@VERSION@";
	
	@SidedProxy(clientSide="flaxbeard.questionablyimmersive.client.ClientProxy", serverSide="flaxbeard.questionablyimmersive.common.CommonProxy")
	public static CommonProxy proxy;
	
	@Instance(MODID)
	public static QuestionablyImmersive INSTANCE;
	
	static
	{
		FluidRegistry.enableUniversalBucket();
	}
		
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		QEContent.preInit();
		proxy.preInit();
		proxy.preInitEnd();
		
		QEPacketHandler.preInit();
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent event)
	{
		QEContent.init();
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, proxy);
		proxy.init();
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.postInit();
	}

	
	public static CreativeTabs creativeTab = new CreativeTabs(MODID)
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(Items.APPLE);
		}
		
		@Override
		public ItemStack getIconItemStack()
		{
			return new ItemStack(Items.APPLE);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public void displayAllRelevantItems(NonNullList<ItemStack> list)
		{
			super.displayAllRelevantItems(list);
		}
	};
	
	@Mod.EventHandler
	public void serverStarted(FMLServerStartedEvent event)
	{

	}
	
	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new CommandHandler());
	}

}
