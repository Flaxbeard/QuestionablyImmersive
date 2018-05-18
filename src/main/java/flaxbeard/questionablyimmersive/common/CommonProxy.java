package flaxbeard.questionablyimmersive.common;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces;
import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import flaxbeard.questionablyimmersive.client.gui.GuiCokeOvenBattery;
import flaxbeard.questionablyimmersive.client.gui.GuiMortar;
import flaxbeard.questionablyimmersive.client.gui.GuiTriphammer;
import flaxbeard.questionablyimmersive.client.gui.GuiTuneRadio;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityTriphammer;
import flaxbeard.questionablyimmersive.common.gui.ContainerCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.gui.ContainerMortar;
import flaxbeard.questionablyimmersive.common.gui.ContainerTriphammer;
import flaxbeard.questionablyimmersive.common.items.ItemPortableRadio;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nonnull;


public class CommonProxy implements IGuiHandler
{
	public void preInit() {}

	public void preInitEnd() {}
	public void init() {}
	public void postInit() {}

	public static <T extends TileEntity & IGuiTile> void openGuiForTile(@Nonnull EntityPlayer player, @Nonnull T tile)
	{
		player.openGui(QuestionablyImmersive.INSTANCE, tile.getGuiID(), tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
	}

	public static void openGuiForItem(@Nonnull EntityPlayer player, @Nonnull EntityEquipmentSlot slot)
	{
		ItemStack stack = player.getItemStackFromSlot(slot);
		if (stack.isEmpty() || !(stack.getItem() instanceof IEItemInterfaces.IGuiItem))
			return;
		IEItemInterfaces.IGuiItem gui = (IEItemInterfaces.IGuiItem)stack.getItem();
		player.openGui(QuestionablyImmersive.INSTANCE, 100 * slot.ordinal() + gui.getGuiID(stack), player.world, (int) player.posX, (int) player.posY, (int) player.posZ);
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID >= 64)
		{
			EntityEquipmentSlot slot = EntityEquipmentSlot.values()[ID/100];
			ID %= 64; //Slot determined, get actual ID
			ItemStack item = player.getItemStackFromSlot(slot);
			if (!item.isEmpty() && item.getItem() instanceof IEItemInterfaces.IGuiItem && ((IEItemInterfaces.IGuiItem)item.getItem()).getGuiID(item)==ID)
			{

			}
		}
		else
		{
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if (te instanceof IGuiTile)
			{
				Object gui = null;

				if (ID == 1 && te instanceof TileEntityMortar)
				{
					gui = new ContainerMortar(player.inventory, (TileEntityMortar) te);
				}
				else if (ID == 2 && te instanceof TileEntityCokeOvenBattery)
				{
					gui = new ContainerCokeOvenBattery(player.inventory, (TileEntityCokeOvenBattery) te);
				}
				else if (ID == 3 && te instanceof TileEntityTriphammer)
				{
					gui = new ContainerTriphammer(player.inventory, (TileEntityTriphammer) te);
				}
				if(gui!=null)
					((IGuiTile)te).onGuiOpened(player, false);
				return gui;
			}
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if (ID >= 64)
		{
			EntityEquipmentSlot slot = EntityEquipmentSlot.values()[ID/100];
			ID %= 100; //Slot determined, get actual ID
			ItemStack item = player.getItemStackFromSlot(slot);

			if (!item.isEmpty() && item.getItem() instanceof IEItemInterfaces.IGuiItem && ((IEItemInterfaces.IGuiItem)item.getItem()).getGuiID(item) == ID)
			{
				Object gui = null;
				if (ID == 64 && item.getItem() instanceof ItemPortableRadio)
				{
					gui = new GuiTuneRadio(player, item, slot);
				}

				return gui;
			}
		}
		else
		{
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if (te instanceof IGuiTile)
			{
				Object gui = null;
				if (ID == 0 && te instanceof TileEntityRadio)
				{
					gui = new GuiTuneRadio((TileEntityRadio) te);
				}
				else if (ID == 1 && te instanceof TileEntityMortar)
				{
					gui = new GuiMortar(player.inventory, (TileEntityMortar) te);
				}
				else if (ID == 2 && te instanceof TileEntityCokeOvenBattery)
				{
					gui = new GuiCokeOvenBattery(player.inventory, (TileEntityCokeOvenBattery) te);
				}
				else if (ID == 3 && te instanceof TileEntityTriphammer)
				{
					gui = new GuiTriphammer(player.inventory, (TileEntityTriphammer) te);
				}
				return gui;
			}
		}
		return null;
	}

	public void renderTile(TileEntity te) {}
	public void handleEntitySound(SoundEvent soundEvent, Entity e, boolean active, float volume, float pitch)
	{
	}

	public void drawUpperHalfSlab(ItemStack stack) {
	}
}