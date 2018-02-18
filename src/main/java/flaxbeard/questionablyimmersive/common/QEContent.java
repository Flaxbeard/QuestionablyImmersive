package flaxbeard.questionablyimmersive.common;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import flaxbeard.questionablyimmersive.common.blocks.BlockGauge;
import flaxbeard.questionablyimmersive.common.blocks.BlockQEBase;
import flaxbeard.questionablyimmersive.common.blocks.BlockQEMetalMultiblocks;
import flaxbeard.questionablyimmersive.common.blocks.BlockRadio;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityGauge;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import flaxbeard.questionablyimmersive.common.blocks.multiblocks.MultiblockMortar;
import flaxbeard.questionablyimmersive.common.entity.EntityMortarItem;
import flaxbeard.questionablyimmersive.common.items.ItemPunchcard;
import net.minecraft.block.Block;
import net.minecraft.entity.projectile.*;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid= QuestionablyImmersive.MODID)
public class QEContent
{
	public static ArrayList<Block> registeredIPBlocks = new ArrayList<Block>();

	public static BlockQEBase blockMetalMultiblock;

	public static BlockQEBase blockGauge;
	public static BlockQEBase blockRadio;

	
	public static ArrayList<Item> registeredIPItems = new ArrayList<Item>();


	public static Item itemPunchcard;

	public static void preInit()
	{
		EntityRegistry.registerModEntity(new ResourceLocation(QuestionablyImmersive.MODID, "mortar_item"), EntityMortarItem.class, "mortar_item", 1, QuestionablyImmersive.INSTANCE, 180, 1, true);


		blockMetalMultiblock = new BlockQEMetalMultiblocks();

		blockGauge = new BlockGauge();
		blockRadio = new BlockRadio();


		itemPunchcard = new ItemPunchcard("punchcard");

		EntityMortarItem.registerHandler(Items.ARROW, -1, (item, world, xPos, yPos, zPos) -> {
			EntityArrow arrow = new EntityTippedArrow(world, xPos + (world.rand.nextFloat() * 4f) - 2f, yPos, zPos + (world.rand.nextFloat() * 4f) - 2f);
			arrow.setThrowableHeading(0, -6, 0, 3f, 0);
			arrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
			world.spawnEntity(arrow);
		});
		EntityMortarItem.registerHandler(Items.TIPPED_ARROW, -1, (item, world, xPos, yPos, zPos) -> {
			EntityTippedArrow arrow = new EntityTippedArrow(world, xPos + (world.rand.nextFloat() * 4f) - 2f, yPos, zPos + (world.rand.nextFloat() * 4f) - 2f);
			arrow.setThrowableHeading(0, -6, 0, 3f, 0);
			arrow.setPotionEffect(item);
			arrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
			world.spawnEntity(arrow);
		});
		EntityMortarItem.registerHandler(Items.SPECTRAL_ARROW, -1, (item, world, xPos, yPos, zPos) -> {
			EntityArrow arrow = new EntitySpectralArrow(world, xPos + (world.rand.nextFloat() * 4f) - 2f, yPos, zPos + (world.rand.nextFloat() * 4f) - 2f);
			arrow.setThrowableHeading(0, -6, 0, 3f, 0);
			arrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
			world.spawnEntity(arrow);
		});
		EntityMortarItem.registerHandler(Items.SNOWBALL, -1, (item, world, xPos, yPos, zPos) -> {
			EntitySnowball snowball = new EntitySnowball(world, xPos + (world.rand.nextFloat() * 4f) - 2f, yPos, zPos + (world.rand.nextFloat() * 4f) - 2f);
			snowball.setThrowableHeading(0, -6, 0, 3f, 0);
			world.spawnEntity(snowball);
		});
		EntityMortarItem.registerHandler(Items.FIRE_CHARGE, -1, (item, world, xPos, yPos, zPos) -> {
			EntityFireball fireball = new EntitySmallFireball(world, xPos + (world.rand.nextFloat() * 4f) - 2f, yPos, zPos + (world.rand.nextFloat() * 4f) - 2f, 0, -6, 0);
			fireball.motionY = -8;
			world.spawnEntity(fireball);
		});
		EntityMortarItem.registerHandler(Items.SPLASH_POTION, -1, (item, world, xPos, yPos, zPos) -> {
			EntityPotion potion = new EntityPotion(world, xPos + (world.rand.nextFloat() * 4f) - 2f, yPos, zPos + (world.rand.nextFloat() * 4f) - 2f, item.copy());
			potion.setThrowableHeading(0, -6, 0, 3f, 0);
			world.spawnEntity(potion);
		});
	}

	public static void init()
	{

		registerTile(TileEntityMortar.class);
		registerTile(TileEntityMortar.TileEntityMortarParent.class);
		registerTile(TileEntityGauge.class);
		registerTile(TileEntityRadio.class);

		MultiblockHandler.registerMultiblock(MultiblockMortar.instance);

	}
	
	public static void registerTile(Class<? extends TileEntity> tile)
	{
		String s = tile.getSimpleName();
		s = s.substring(s.indexOf("TileEntity")+"TileEntity".length());
		GameRegistry.registerTileEntity(tile, QuestionablyImmersive.MODID+":"+ s);
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		for(Block block : registeredIPBlocks)
			event.getRegistry().register(block.setRegistryName(createRegistryName(block.getUnlocalizedName())));
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		for(Item item : registeredIPItems)
			event.getRegistry().register(item.setRegistryName(createRegistryName(item.getUnlocalizedName())));
	}

	private static ResourceLocation createRegistryName(String unlocalized)
	{
		unlocalized = unlocalized.substring(unlocalized.indexOf("questionably"));
		unlocalized = unlocalized.replaceFirst("\\.", ":");
		return new ResourceLocation(unlocalized);
	}
	
}
