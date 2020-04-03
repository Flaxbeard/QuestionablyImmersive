package net.flaxbeard.questionablyimmersive.common;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.common.network.IMessage;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlocks;
import net.flaxbeard.questionablyimmersive.common.blocks.QIGenericTileBlock;
import net.flaxbeard.questionablyimmersive.common.blocks.QIMetalMultiblockBlock;
import net.flaxbeard.questionablyimmersive.common.blocks.TriphammerAnvilBlock;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.*;
import net.flaxbeard.questionablyimmersive.common.blocks.multiblocks.QIMultiblocks;
import net.flaxbeard.questionablyimmersive.common.entities.MortarItemEntity;
import net.flaxbeard.questionablyimmersive.common.items.PunchcardItem;
import net.flaxbeard.questionablyimmersive.common.items.QIItems;
import net.flaxbeard.questionablyimmersive.common.network.GUIUpdateMessage;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.network.PacketBuffer;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

@Mod.EventBusSubscriber(
		modid = QuestionablyImmersive.MODID,
		bus = Mod.EventBusSubscriber.Bus.MOD
)
public class QIContent
{
	private static int packetId = 0;

	public static List<Block> registeredQIBlocks = new ArrayList();
	public static List<Item> registeredQIItems = new ArrayList();
	public static List<Class<? extends TileEntity>> registeredQITiles = new ArrayList();

	public static void modConstruction()
	{
		QuestionablyImmersive.proxy.registerContainersAndScreens();

		QIBlocks.Multiblocks.railgunMortar = new QIMetalMultiblockBlock("railgun_mortar", () ->
		{
			return RailgunMortarTileEntity.TYPE;
		}, () ->
		{
			return RailgunMortarTileEntity.Master.TYPE;
		});

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

		QIBlocks.Metal.TRIPHAMMER_ANVIL = new TriphammerAnvilBlock("triphammer_anvil", () ->
		{
			return TriphammerAnvilTileEntity.TYPE;
		}, Blocks.ANVIL);
		QIBlocks.Metal.TRIPHAMMER_ANVIL_CHIPPED = new TriphammerAnvilBlock("triphammer_anvil_chipped", () ->
		{
			return TriphammerAnvilTileEntity.TYPE;
		}, Blocks.CHIPPED_ANVIL);
		QIBlocks.Metal.TRIPHAMMER_ANVIL_DAMAGED = new TriphammerAnvilBlock("triphammer_anvil_damaged", () ->
		{
			return TriphammerAnvilTileEntity.TYPE;
		}, Blocks.DAMAGED_ANVIL);

		Block.Properties defaultMetalProperties = Block.Properties.create(Material.IRON).hardnessAndResistance(3.0F, 15.0F);
		QIBlocks.MetalDevices.gauge = new QIGenericTileBlock("gauge", () ->
		{
			return GaugeTileEntity.TYPE;
		}, defaultMetalProperties, new IProperty[]{IEProperties.FACING_ALL});

		QIItems.Misc.punchcardBlank = new PunchcardItem(false);
		QIItems.Misc.punchcardPunched = new PunchcardItem(true);
	}

	public static void init()
	{
		QIMultiblocks.init();
		MultiblockHandler.registerMultiblock(QIMultiblocks.COKE_OVEN_BATTERY_SLICE);
		MultiblockHandler.registerMultiblock(QIMultiblocks.COKE_OVEN_BATTERY_DISPLAY);
		MultiblockHandler.registerMultiblock(QIMultiblocks.TRIPHAMMER);
		MultiblockHandler.registerMultiblock(QIMultiblocks.RAILGUN_MORTAR);

		registerMessage(GUIUpdateMessage.class, GUIUpdateMessage::new);
	}

	@SubscribeEvent
	public static void registerTEs(RegistryEvent.Register<TileEntityType<?>> event)
	{
		registerTile(CokeOvenBatteryTileEntity.class, event, QIBlocks.Multiblocks.cokeOvenBattery);
		registerTile(CokeOvenBatteryTileEntity.Rendered.class, event, QIBlocks.Multiblocks.cokeOvenBattery);
		registerTile(CokeOvenBatteryTileEntity.Master.class, event, QIBlocks.Multiblocks.cokeOvenBattery);

		registerTile(TriphammerTileEntity.class, event, QIBlocks.Multiblocks.triphammer);
		registerTile(TriphammerTileEntity.Master.class, event, QIBlocks.Multiblocks.triphammer);

		registerTile(TriphammerAnvilTileEntity.class, event, QIBlocks.Metal.TRIPHAMMER_ANVIL, QIBlocks.Metal.TRIPHAMMER_ANVIL_CHIPPED, QIBlocks.Metal.TRIPHAMMER_ANVIL_DAMAGED);

		registerTile(GaugeTileEntity.class, event, QIBlocks.MetalDevices.gauge);
		registerTile(RailgunMortarTileEntity.class, event, QIBlocks.Multiblocks.railgunMortar);
		registerTile(RailgunMortarTileEntity.Master.class, event, QIBlocks.Multiblocks.railgunMortar);
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

	private static <T extends IMessage> void registerMessage(Class<T> packetType, Function<PacketBuffer, T> decoder)
	{
		QuestionablyImmersive.packetHandler.registerMessage(packetId++, packetType, IMessage::toBytes, decoder, (t, ctx) ->
		{
			t.process(ctx);
			ctx.get().setPacketHandled(true);
		});
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
				IELogger.logger.info("Null name for {} (class {})", b, b.getClass());
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
				MortarItemEntity.TYPE
		);
	}

}
