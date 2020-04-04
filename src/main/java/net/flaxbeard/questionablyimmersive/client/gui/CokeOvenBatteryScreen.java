package net.flaxbeard.questionablyimmersive.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.common.gui.IESlot;
import com.mojang.blaze3d.platform.GlStateManager;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.flaxbeard.questionablyimmersive.common.gui.CokeOvenBatteryContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class CokeOvenBatteryScreen extends IEContainerScreen<CokeOvenBatteryContainer>
{
	private CokeOvenBatteryTileEntity tile;
	private int scroll;
	private int maxScroll;
	private CokeOvenBatteryContainer container;
	private boolean drawSlots = true;

	private boolean leftMouseWasDown = false;
	private boolean leftMouseDown = false;

	public CokeOvenBatteryScreen(CokeOvenBatteryContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.container = container;

		maxScroll = (int) ((tile.numLayers - 5) / 5. * 110);
		scroll = 0;
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		GlStateManager.pushMatrix();

		GL11.glEnable(GL11.GL_SCISSOR_TEST);

		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;

		scissor(i + 10, j + 14, 110, 38);
		drawSlots = true;
		super.render(mx, my, partial);
		GlStateManager.popMatrix();

		drawSlots = false;

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		scissor(0, j + 14, i + 10, 38);
		super.render(mx, my, partial);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		scissor(i + 120, j + 14, this.width - this.xSize + 120, 38);
		super.render(mx, my, partial);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		scissor(0, 0, this.width, j + 14);
		super.render(mx, my, partial);
		GlStateManager.popMatrix();

		drawSlots = true;
		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		scissor(0, j + 14 + 38, this.width, this.height - (j + 14 + 38));
		super.render(mx, my, partial);
		GlStateManager.popMatrix();

		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		this.renderHoveredToolTip(mx, my);

		List<ITextComponent> tooltip = new ArrayList<>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft + 125, guiTop + 21, 16, 47, 176, 31, 20, 51, mx, my, "questionablyimmersive:textures/gui/cokeoven.png", tooltip);
		if (!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, font, guiLeft + xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	private void scissor(int x, int y, int xSize, int ySize)
	{
		int scaleFactor = (int) Minecraft.getInstance().mainWindow.getGuiScaleFactor();
		int height = Minecraft.getInstance().mainWindow.getHeight();
		x = x * scaleFactor;
		ySize = ySize * scaleFactor;
		y = height - (y * scaleFactor) - ySize;
		xSize = xSize * scaleFactor;
		GL11.glScissor(x, y, xSize, ySize);
	}

	@Override
	public boolean mouseClicked(double p_mouseClicked_1_, double p_mouseClicked_3_, int p_mouseClicked_5_)
	{
		if (p_mouseClicked_5_ == 0)
		{
			this.leftMouseDown = true;
		}
		return super.mouseClicked(p_mouseClicked_1_, p_mouseClicked_3_, p_mouseClicked_5_);
	}

	@Override
	public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_)
	{
		if (p_mouseReleased_5_ == 0)
		{
			this.leftMouseDown = false;
		}
		return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double p_mouseScrolled_5_)
	{
		scroll -= p_mouseScrolled_5_ * 40;

		return super.mouseScrolled(p_mouseScrolled_1_, p_mouseScrolled_3_, p_mouseScrolled_5_);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int mouseChange = 0; // TODO

		if ((isPointInRegion(10, 56, 110, 12, mx, my) || leftMouseWasDown) && this.leftMouseDown)
		{
			scroll = (int) ((mx - (15. / 2) - guiLeft - 10.) * maxScroll) / 95;
			leftMouseWasDown = true;
		} else
		{
			leftMouseWasDown = false;
		}

		scroll = Math.min(scroll, maxScroll);
		scroll = Math.max(0, scroll);

		ClientUtils.bindTexture("questionablyimmersive:textures/guis/coke_oven_battery.png");
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);

		this.blit(guiLeft + 10 + (int) (95 * (scroll * 1. / maxScroll)), guiTop + 56, 177, 0, 15, 12);

		for (int j = 0; j < container.slots.length; j++)
		{
			int i = !tile.getIsMirrored() ? container.slots.length - j - 1 : j;
			IESlot slot = container.slots[i];
			int x = j * 22 - scroll;
			int y = 0;

			if ((x < -20 || x > 110) || (!drawSlots))
			{
				slot.xPos = -Integer.MAX_VALUE;
				slot.yPos = -Integer.MAX_VALUE;
			} else
			{
				slot.xPos = 13 + x;
				slot.yPos = 17 + y;

				int h = tile.active[i] ? (int) (12 * (tile.process[i] / (float) tile.processMax[i]) + .99) : 0;

				this.blit(guiLeft + 11 + x, guiTop + 15, 200, 0, 20, 36);
				this.blit(guiLeft + 11 + x, guiTop + 38 + (12 - h), 225, (12 - h), 20, h);
			}

		}
		ClientUtils.handleGuiTank(tile.tank, guiLeft + 125, guiTop + 21, 16, 47, 176, 31, 20, 51, mx, my, "questionablyimmersive:textures/guis/coke_oven_battery.png", null);
	}
}
