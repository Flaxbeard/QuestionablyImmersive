package net.flaxbeard.questionablyimmersive.common.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.base.Preconditions;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(
		modid = QuestionablyImmersive.MODID,
		bus = Bus.MOD
)
public class QIGuiHandler
{
	private static final Map<Class<? extends TileEntity>, QIGuiHandler.TileContainer<?, ?>> TILE_CONTAINERS = new HashMap();
	private static final Map<Class<? extends Item>, QIGuiHandler.ItemContainer<?>> ITEM_CONTAINERS = new HashMap();
	private static final Map<ResourceLocation, ContainerType<?>> ALL_TYPES = new HashMap();

	public QIGuiHandler()
	{
	}

	public static void commonInit()
	{
		register(CokeOvenBatteryTileEntity.Master.class, CokeOvenBatteryContainer.ID, CokeOvenBatteryContainer::new);
	}

	public static <T extends TileEntity, C extends QIBaseContainer<? super T>> void register(Class<T> tileClass, ResourceLocation name, QIGuiHandler.TileContainerConstructor<T, C> container)
	{
		ContainerType<C> type = IForgeContainerType.create((windowId, inv, data) ->
		{
			World world = ImmersiveEngineering.proxy.getClientWorld();
			BlockPos pos = data.readBlockPos();
			TileEntity te = world.getTileEntity(pos);
			return container.construct(windowId, inv, (T) te);
		});
		type.setRegistryName(name);
		TILE_CONTAINERS.put(tileClass, new QIGuiHandler.TileContainer(type, container));
		ALL_TYPES.put(name, type);
	}

	public static void useSameContainerTile(Class<? extends TileEntity> existing, Class<? extends TileEntity> toAdd)
	{
		Preconditions.checkArgument(TILE_CONTAINERS.containsKey(existing));
		TILE_CONTAINERS.put(toAdd, TILE_CONTAINERS.get(existing));
	}

	public static <T0 extends Item, T extends Item> void useSameContainerItem(Class<T0> existing, Class<T> toAdd)
	{
		Preconditions.checkArgument(ITEM_CONTAINERS.containsKey(existing));
		ITEM_CONTAINERS.put(toAdd, ITEM_CONTAINERS.get(existing));
	}

	public static <T extends TileEntity> Container createContainer(PlayerInventory inv, T te, int id)
	{
		return ((QIGuiHandler.TileContainer) TILE_CONTAINERS.get(te.getClass())).factory.construct(id, inv, te);
	}

	public static Container createContainer(PlayerInventory inv, World w, EquipmentSlotType slot, ItemStack stack, int id)
	{
		return ((QIGuiHandler.ItemContainer) ITEM_CONTAINERS.get(stack.getItem().getClass())).factory.construct(id, inv, w, slot, stack);
	}

	public static ContainerType<?> getContainerTypeFor(TileEntity te)
	{
		return ((QIGuiHandler.TileContainer) TILE_CONTAINERS.get(te.getClass())).type;
	}

	public static ContainerType<?> getContainerTypeFor(ItemStack stack)
	{
		return ((QIGuiHandler.ItemContainer) ITEM_CONTAINERS.get(stack.getItem().getClass())).type;
	}

	public static ContainerType<?> getContainerType(ResourceLocation name)
	{
		return (ContainerType) ALL_TYPES.get(name);
	}

	@SubscribeEvent
	public static void registerContainers(Register<ContainerType<?>> evt)
	{
		Iterator var1 = (new HashSet(TILE_CONTAINERS.values())).iterator();

		while (var1.hasNext())
		{
			QIGuiHandler.TileContainer<?, ?> tc = (QIGuiHandler.TileContainer) var1.next();
			evt.getRegistry().register(tc.type);
		}

		var1 = (new HashSet(ITEM_CONTAINERS.values())).iterator();

		while (var1.hasNext())
		{
			QIGuiHandler.ItemContainer<?> ic = (QIGuiHandler.ItemContainer) var1.next();
			evt.getRegistry().register(ic.type);
		}

	}

	private static class ItemContainer<C extends Container>
	{
		final ContainerType<C> type;
		final QIGuiHandler.ItemContainerConstructor<C> factory;

		private ItemContainer(ContainerType<C> type, QIGuiHandler.ItemContainerConstructor<C> factory)
		{
			this.type = type;
			this.factory = factory;
		}
	}

	private static class TileContainer<T extends TileEntity, C extends QIBaseContainer<? super T>>
	{
		final ContainerType<C> type;
		final QIGuiHandler.TileContainerConstructor<T, C> factory;

		private TileContainer(ContainerType<C> type, QIGuiHandler.TileContainerConstructor<T, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}
	}

	public interface TileContainerConstructor<T extends TileEntity, C extends QIBaseContainer<? super T>>
	{
		C construct(int var1, PlayerInventory var2, T var3);
	}

	public interface ItemContainerConstructor<C extends Container>
	{
		C construct(int var1, PlayerInventory var2, World var3, EquipmentSlotType var4, ItemStack var5);
	}
}
