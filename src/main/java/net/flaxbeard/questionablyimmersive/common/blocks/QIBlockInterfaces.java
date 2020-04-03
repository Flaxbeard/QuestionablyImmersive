package net.flaxbeard.questionablyimmersive.common.blocks;

import com.google.common.base.Preconditions;
import net.flaxbeard.questionablyimmersive.common.gui.QIGuiHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class QIBlockInterfaces
{
	public interface IInteractionObjectQI extends INamedContainerProvider
	{
		@Nullable
		IInteractionObjectQI getGuiMaster();

		boolean canUseGui(PlayerEntity var1);

		default boolean isValid() {
			return this.getGuiMaster() != null;
		}

		@Nonnull
		default Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
			IInteractionObjectQI master = this.getGuiMaster();
			Preconditions.checkState(master instanceof TileEntity);
			return QIGuiHandler.createContainer(playerInventory, (TileEntity)master, id);
		}

		default ITextComponent getDisplayName() {
			return new StringTextComponent("");
		}
	}
}
