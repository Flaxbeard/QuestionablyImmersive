package net.flaxbeard.questionablyimmersive.common;

import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlocks;
import net.flaxbeard.questionablyimmersive.common.blocks.QIMetalMultiblockBlock;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.TriphammerTileEntity;
import net.flaxbeard.questionablyimmersive.common.blocks.multiblocks.QIMultiblocks;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.reflect.Field;
import java.util.*;

@Mod.EventBusSubscriber(
		modid = QuestionablyImmersive.MODID,
		bus = Mod.EventBusSubscriber.Bus.MOD
)
public class QIContent
{
	public static List<Block> registeredQIBlocks = new ArrayList();
	public static List<Item> registeredQIItems = new ArrayList();
	public static List<Class<? extends TileEntity>> registeredQITiles = new ArrayList();

	public static void modConstruction()
	{
		QuestionablyImmersive.proxy.registerContainersAndScreens();

		QIBlocks.Multiblocks.cokeOvenBattery = new QIMetalMultiblockBlock("coke_oven_battery", () ->
		{
			return CokeOvenBatteryTileEntity.TYPE;
		}, () ->
		{
			return CokeOvenBatteryTileEntity.Rendered.TYPE;
		}, () ->
		{
			return CokeOvenBatteryTileEntity.Master.TYPE;
		});

		QIBlocks.Multiblocks.triphammer = new QIMetalMultiblockBlock("triphammer", () ->
		{
			return TriphammerTileEntity.TYPE;
		}, () ->
		{
			return TriphammerTileEntity.Master.TYPE;
		});
	}

	public static void init()
	{
		QIMultiblocks.init();
		MultiblockHandler.registerMultiblock(QIMultiblocks.COKE_OVEN_BATTERY_SLICE);
		MultiblockHandler.registerMultiblock(QIMultiblocks.COKE_OVEN_BATTERY_DISPLAY);
		MultiblockHandler.registerMultiblock(QIMultiblocks.TRIPHAMMER);
	}

	@SubscribeEvent
	public static void registerTEs(RegistryEvent.Register<TileEntityType<?>> event)
	{
		registerTile(CokeOvenBatteryTileEntity.class, event, QIBlocks.Multiblocks.cokeOvenBattery);
		registerTile(CokeOvenBatteryTileEntity.Rendered.class, event, QIBlocks.Multiblocks.cokeOvenBattery);
		registerTile(CokeOvenBatteryTileEntity.Master.class, event, QIBlocks.Multiblocks.cokeOvenBattery);

		registerTile(TriphammerTileEntity.class, event, QIBlocks.Multiblocks.triphammer);
		registerTile(TriphammerTileEntity.Master.class, event, QIBlocks.Multiblocks.triphammer);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		checkNonNullNames(registeredQIBlocks);
		Iterator var1 = registeredQIBlocks.iterator();

		while (var1.hasNext())
		{
			Block block = (Block) var1.next();
			event.getRegistry().register(block);
		}

	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		checkNonNullNames(registeredQIItems);
		Iterator var1 = registeredQIItems.iterator();

		while (var1.hasNext())
		{
			Item item = (Item) var1.next();
			event.getRegistry().register(item);
		}
	}

	private static <T extends IForgeRegistryEntry<T>> void checkNonNullNames(Collection<T> coll)
	{
		int numNull = 0;
		Iterator var2 = coll.iterator();

		while (var2.hasNext())
		{
			T b = (T) var2.next();
			if (b.getRegistryName() == null)
			{
				++numNull;
			}
		}

		if (numNull > 0)
		{
			System.exit(1);
		}

	}

	public static <T extends TileEntity> void registerTile(Class<T> tile, RegistryEvent.Register<TileEntityType<?>> event, Block... valid)
	{
		String s = tile.getName();
		s = s.substring(s.lastIndexOf(".") + 1, s.length());
		s = s.replace("TileEntity", "");
		s = s.replace("$", ".");
		s = s.toLowerCase(Locale.ENGLISH);
		Set<Block> validSet = new HashSet(Arrays.asList(valid));
		TileEntityType<T> type = new TileEntityType(() ->
		{
			try
			{
				return (TileEntity) tile.newInstance();
			} catch (IllegalAccessException | InstantiationException var2)
			{
				var2.printStackTrace();
				return null;
			}
		}, validSet, null);
		type.setRegistryName(QuestionablyImmersive.MODID, s);
		event.getRegistry().register(type);

		try
		{
			Field typeField = tile.getField("TYPE");
			typeField.set((Object) null, type);
		} catch (IllegalAccessException | NoSuchFieldException var7)
		{
			var7.printStackTrace();
			throw new RuntimeException(var7);
		}

		registeredQITiles.add(tile);
	}

	@SubscribeEvent
	public static void registerEntityTypes(RegistryEvent.Register<EntityType<?>> event)
	{
		event.getRegistry().registerAll(
		);
	}

}
