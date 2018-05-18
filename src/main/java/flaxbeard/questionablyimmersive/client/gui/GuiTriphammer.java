package flaxbeard.questionablyimmersive.client.gui;

import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityTriphammer;
import flaxbeard.questionablyimmersive.common.gui.ContainerTriphammer;
import flaxbeard.questionablyimmersive.common.network.GUIUpdatePacket;
import flaxbeard.questionablyimmersive.common.network.QIPacketHandler;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class GuiTriphammer extends GuiContainer implements IContainerListener
{
	private static final ResourceLocation ANVIL_RESOURCE = new ResourceLocation("textures/gui/container/anvil.png");
	private final ContainerTriphammer triphammer;
	private GuiTextField nameField;
	private final InventoryPlayer playerInventory;
	private boolean ready = false;

	public GuiTriphammer(InventoryPlayer inventoryIn, TileEntityTriphammer hammer)
	{
		super(new ContainerTriphammer(inventoryIn, hammer));
		this.playerInventory = inventoryIn;
		this.triphammer = (ContainerTriphammer) this.inventorySlots;
	}

	/**
	 * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
	 * window resizes, the buttonList is cleared beforehand.
	 */
	public void initGui()
	{
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.nameField = new GuiTextField(0, this.fontRenderer, i + 62, j + 24, 103, 12);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);
		this.inventorySlots.removeListener(this);
		this.inventorySlots.addListener(this);
		ready = true;
		this.nameField.setText(triphammer.tile.repairedItemName);

		Slot slot = this.triphammer.getSlot(0);
		if (slot != null && slot.getHasStack() && !slot.getStack().hasDisplayName() && triphammer.tile.repairedItemName.equals(""))
		{
			this.nameField.setText(slot.getStack().getDisplayName());
		}
	}

	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
		this.inventorySlots.removeListener(this);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		this.fontRenderer.drawString(I18n.format("container.repair"), 60, 6, 4210752);

		if (this.triphammer.maximumCost > 0)
		{
			int i = 8453920;
			boolean flag = true;
			String s = I18n.format("container.repair.cost", this.triphammer.maximumCost);

			if (this.triphammer.maximumCost >= 40 && !this.mc.player.capabilities.isCreativeMode)
			{
				s = I18n.format("container.repair.expensive");
				i = 16736352;
			}
			else if (!this.triphammer.getSlot(2).getHasStack())
			{
				flag = false;
			}
			else if (!this.triphammer.getSlot(2).canTakeStack(this.playerInventory.player))
			{
				i = 16736352;
			}

			if (flag)
			{
				int j = -16777216 | (i & 16579836) >> 2 | i & -16777216;
				int k = this.xSize - 8 - this.fontRenderer.getStringWidth(s);
				int l = 67;

				if (this.fontRenderer.getUnicodeFlag())
				{
					drawRect(k - 3, 65, this.xSize - 7, 77, -16777216);
					drawRect(k - 2, 66, this.xSize - 8, 76, -12895429);
				}
				else
				{
					this.fontRenderer.drawString(s, k, 68, j);
					this.fontRenderer.drawString(s, k + 1, 67, j);
					this.fontRenderer.drawString(s, k + 1, 68, j);
				}

				this.fontRenderer.drawString(s, k, 67, i);
			}
		}

		GlStateManager.enableLighting();
	}

	/**
	 * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
	 */
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		if (this.nameField.textboxKeyTyped(typedChar, keyCode))
		{
			this.renameItem();
		}
		else
		{
			super.keyTyped(typedChar, keyCode);
		}
	}

	private void renameItem()
	{
		String s = this.nameField.getText();
		Slot slot = this.triphammer.getSlot(0);

		if (slot != null && !slot.getHasStack())
		{
			s = "";
		}
		this.nameField.setText(s);


		if (slot != null && slot.getHasStack() && !slot.getStack().hasDisplayName() && s.equals(slot.getStack().getDisplayName()))
		{
			s = "";
		}

		this.triphammer.updateItemName(s);
		NBTTagCompound data = new NBTTagCompound();
		data.setString("name", s);
		QIPacketHandler.INSTANCE.sendToServer(new GUIUpdatePacket(triphammer.tile.getPos(), 0, data));
	}

	/**
	 * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
	 */
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		this.nameField.drawTextBox();
	}

	/**
	 * Draws the background layer of this container (behind the items).
	 */
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(ANVIL_RESOURCE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
		this.drawTexturedModalRect(i + 59, j + 20, 0, this.ySize + (this.triphammer.getSlot(0).getHasStack() ? 0 : 16), 110, 16);

		if ((this.triphammer.getSlot(0).getHasStack() || this.triphammer.getSlot(1).getHasStack()) && !this.triphammer.getSlot(2).getHasStack())
		{
			this.drawTexturedModalRect(i + 99, j + 45, this.xSize, 0, 28, 21);
		}
	}

	/**
	 * update the crafting window inventory with the items in the list
	 */
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList)
	{
		this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
	}

	/**
	 * Sends the contents of an inventory slot to the client-side Container. This doesn't have to match the actual
	 * contents of that slot.
	 */
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack)
	{
		if (ready && slotInd == 0)
		{
			this.nameField.setText(stack.isEmpty() ? "" : stack.getDisplayName());
			this.nameField.setEnabled(!stack.isEmpty());

			if (!stack.isEmpty())
			{
				this.renameItem();
			}
		}
	}

	/**
	 * Sends two ints to the client-side Container. Used for furnace burning time, smelting progress, brewing progress,
	 * and enchanting level. Normally the first int identifies which variable to update, and the second contains the new
	 * value. Both are truncated to shorts in non-local SMP.
	 */
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue)
	{
	}

	public void sendAllWindowProperties(Container containerIn, IInventory inventory)
	{
	}
}