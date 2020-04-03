package net.flaxbeard.questionablyimmersive.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.QIContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QIBaseItem extends Item implements IColouredItem
{
	public String itemName;
	private int burnTime;
	private boolean isHidden;

	public QIBaseItem(String name, boolean inGroup)
	{
		this(name, new Properties(), inGroup);
	}

	public QIBaseItem(String name, Properties props, boolean inGroup)
	{
		super(inGroup ? props.group(QuestionablyImmersive.itemGroup) : props);
		this.burnTime = -1;
		this.isHidden = false;
		this.itemName = name;
		this.setRegistryName(QuestionablyImmersive.MODID, name);
		QIContent.registeredQIItems.add(this);
	}

	public QIBaseItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public int getBurnTime(ItemStack itemStack)
	{
		return this.burnTime;
	}

	public boolean isHidden()
	{
		return this.isHidden;
	}

	public void hide()
	{
		this.isHidden = true;
	}

	public void unhide()
	{
		this.isHidden = false;
	}

	protected void openGui(PlayerEntity player, final EquipmentSlotType slot)
	{
		final ItemStack stack = player.getItemStackFromSlot(slot);
		NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider()
		{
			@Nonnull
			public ITextComponent getDisplayName()
			{
				return new StringTextComponent("");
			}

			@Nullable
			public Container createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity)
			{
				return GuiHandler.createContainer(playerInventory, playerEntity.world, slot, stack, i);
			}
		}, (buffer) ->
		{
			buffer.writeInt(slot.ordinal());
		});
	}

	@Override
	public boolean hasCustomProperties()
	{
		return true;
	}

	public static Properties withIEOBJRender()
	{
		return ImmersiveEngineering.proxy.useIEOBJRenderer(new Properties());
	}
}
