package net.flaxbeard.questionablyimmersive.client.gui;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import com.mojang.blaze3d.platform.GlStateManager;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.common.gui.TriphammerContainer;
import net.flaxbeard.questionablyimmersive.common.network.GUIUpdateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class TriphammerScreen extends IEContainerScreen<TriphammerContainer> implements IContainerListener
{
	private static final ResourceLocation ANVIL_RESOURCE = new ResourceLocation("textures/gui/container/anvil.png");
	private static final ResourceLocation FURNACE_RESOURCE = new ResourceLocation("textures/gui/container/furnace.png");

	private final TriphammerContainer triphammer;
	private TextFieldWidget nameField;

	private final PlayerInventory playerInventory;
	private boolean ready = false;

	public TriphammerScreen(TriphammerContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.playerInventory = inventoryPlayer;
		this.triphammer = container;
	}

	@Override
	public void init()
	{
		super.init();

		this.minecraft.keyboardListener.enableRepeatEvents(true);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.nameField = new TextFieldWidget(this.font, i + 62, j + 24, 103, 12, I18n.format("container.repair"));
		this.nameField.setCanLoseFocus(false);
		this.nameField.changeFocus(true);
		this.nameField.setTextColor(-1);
		this.nameField.setDisabledTextColour(-1);
		this.nameField.setEnableBackgroundDrawing(false);
		this.nameField.setMaxStringLength(35);
		this.nameField.setResponder(this::renameItem);
		this.children.add(this.nameField);
		this.container.addListener(this);
		this.setFocusedDefault(this.nameField);

		ready = true;
		this.nameField.setText(triphammer.tile.repairedItemName);

		Slot slot = this.triphammer.getSlot(0);
		if (slot != null && slot.getHasStack() && !slot.getStack().hasDisplayName() && triphammer.tile.repairedItemName.equals(""))
		{
			this.nameField.setText(slot.getStack().getDisplayName().getFormattedText());
		}
	}

	@Override
	public void resize(Minecraft p_resize_1_, int p_resize_2_, int p_resize_3_)
	{
		String s = this.nameField.getText();
		this.init(p_resize_1_, p_resize_2_, p_resize_3_);
		this.nameField.setText(s);
	}

	@Override
	public void removed()
	{
		super.removed();
		this.minecraft.keyboardListener.enableRepeatEvents(false);
		this.container.removeListener(this);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		this.font.drawString(I18n.format("container.repair"), 60, 6, 4210752);

		if (this.triphammer.tile.getMaximumCost() > 0)
		{
			int i = 8453920;
			boolean flag = true;
			String s = I18n.format("container.repair.cost", this.triphammer.tile.getMaximumCost());

			if (this.triphammer.tile.getMaximumCost() >= 40)
			{
				s = I18n.format("container.repair.expensive");
				i = 16736352;
			}
			else if (!this.triphammer.hasOutput())
			{
				flag = false;
			}
			else if (!this.triphammer.getSlot(2).canTakeStack(this.playerInventory.player))
			{
				i = 16736352;
			}

			if (flag)
			{
				int k = this.xSize - 8 - this.font.getStringWidth(s) - 2;
				fill(k - 2, 67, this.xSize - 8, 79, 1325400064);
				this.font.drawStringWithShadow(s, (float) k, 69.0F, i);
			}
		}

		GlStateManager.enableLighting();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(ANVIL_RESOURCE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(i, j, 0, 0, this.xSize, this.ySize);
		this.blit(i + 59, j + 20, 0, this.ySize + (this.container.getSlot(0).getHasStack() ? 0 : 16), 110, 16);
		if ((this.container.getSlot(0).getHasStack() || this.container.getSlot(1).getHasStack()) && !this.triphammer.hasOutput())
		{
			this.blit(i + 99, j + 45, this.xSize, 0, 28, 21);
		}

		if (this.triphammer.hasOutput())
		{
			this.minecraft.getTextureManager().bindTexture(FURNACE_RESOURCE);

			int l = getProgressScaled(24);
			this.blit(i + 101, j + 47, 176, 14, l + 1, 16);
		}
	}

	private int getProgressScaled(int pixels)
	{
		int i = triphammer.tile.getProgress();
		int j = triphammer.tile.getMaxProgress();
		return j != 0 && i != 0 ? (int) Math.ceil(i * pixels * 1.0 / j) : 0;
	}

	@Override
	public void render(int p_render_1_, int p_render_2_, float p_render_3_)
	{
		this.renderBackground();
		super.render(p_render_1_, p_render_2_, p_render_3_);
		this.renderHoveredToolTip(p_render_1_, p_render_2_);
		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
		this.nameField.render(p_render_1_, p_render_2_, p_render_3_);
	}


	@Override
	public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList)
	{
		this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
	}

	@Override
	public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack)
	{
		if (ready && slotInd == 0)
		{
			this.nameField.setText(stack.isEmpty() ? "" : stack.getDisplayName().getFormattedText());
			this.nameField.setEnabled(!stack.isEmpty());

			if (!stack.isEmpty())
			{
				this.renameItem();
			}
		}
	}

	@Override
	public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue)
	{

	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (keyCode == 256)
		{
			this.minecraft.player.closeScreen();
		}

		return !this.nameField.keyPressed(keyCode, scanCode, modifiers)
				&& !this.nameField.func_212955_f() ? super.keyPressed(keyCode, scanCode, modifiers) : true;
	}

	private void renameItem(String s)
	{
		renameItem();
	}

	private void renameItem()
	{
		String s = this.nameField.getText();
		Slot slot = this.triphammer.getSlot(0);

		if (!slot.getHasStack())
		{
			s = "";
		}

		if (!this.nameField.getText().equals(s))
		{
			this.nameField.setText(s);
		}

		if (slot.getHasStack() && !slot.getStack().hasDisplayName() && s.equals(slot.getStack().getDisplayName().getFormattedText()))
		{
			s = "";
		}

		this.triphammer.updateItemName(s);

		CompoundNBT data = new CompoundNBT();
		data.putString("name", s);
		QuestionablyImmersive.packetHandler.sendToServer(new GUIUpdateMessage(triphammer.tile.getPos(), 0, data));
	}

}
