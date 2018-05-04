package flaxbeard.questionablyimmersive.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.IESlot;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import flaxbeard.questionablyimmersive.common.gui.ContainerCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.gui.ContainerMortar;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class GuiCokeOvenBattery extends GuiContainer
{
	private final TileEntityCokeOvenBattery tile;
	private int scroll;
	private int maxScroll;
	private ContainerCokeOvenBattery container;
	private boolean drawSlots = true;
	private boolean wasDown = false;

	public GuiCokeOvenBattery(InventoryPlayer inventoryPlayer, TileEntityCokeOvenBattery tile)
	{
		super(new ContainerCokeOvenBattery(inventoryPlayer, tile));
		this.tile = tile;
		this.container = (ContainerCokeOvenBattery) this.inventorySlots;

		maxScroll = (int) ((tile.ovenLength - 5) / 5. * 110);
		scroll = 0;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		this.drawDefaultBackground();

		GlStateManager.pushMatrix();

		GL11.glEnable(GL11.GL_SCISSOR_TEST);

		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;

		scissor(i + 10, j + 14, 110, 38);
		drawSlots = true;
		super.drawScreen(mx, my, partial);
		GlStateManager.popMatrix();

		drawSlots = false;

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		scissor(0, j + 14, i + 10, 38);
		super.drawScreen(mx, my, partial);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		scissor(i + 120, j + 14, this.width - this.xSize + 120, 38);
		super.drawScreen(mx, my, partial);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		scissor(0, 0, this.width, j + 14);
		super.drawScreen(mx, my, partial);
		GlStateManager.popMatrix();

		drawSlots = true;
		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		scissor(0, j + 14 + 38, this.width, this.height - (j + 14 + 38));
		super.drawScreen(mx, my, partial);
		GlStateManager.popMatrix();

		GL11.glDisable(GL11.GL_SCISSOR_TEST);

		this.renderHoveredToolTip(mx, my);

		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft + 125,guiTop + 21, 16,47, 176,31,20,51, mx,my, "questionablyimmersive:textures/gui/cokeoven.png", tooltip);
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize,-1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	private void scissor(int x, int y, int xSize, int ySize)
	{
		ScaledResolution res = new ScaledResolution(mc);
		x = x * res.getScaleFactor();
		ySize = ySize * res.getScaleFactor();
		y = mc.displayHeight - (y * res.getScaleFactor()) - ySize;
		xSize = xSize * res.getScaleFactor();
		GL11.glScissor(x, y, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int mouseChange = Mouse.getDWheel();
		scroll -= Integer.signum(mouseChange) * 40;

		if ((isPointInRegion(10, 56, 110, 12, mx, my) || wasDown) && Mouse.isButtonDown(0))
		{
			scroll = (int) ((mx - (15./2) - guiLeft - 10.) * maxScroll) / 95;
			wasDown = true;
		}
		else
		{
			wasDown = false;
		}

		scroll = Math.min(scroll, maxScroll);
		scroll = Math.max(0, scroll);

		ClientUtils.bindTexture("questionablyimmersive:textures/gui/cokeoven.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		this.drawTexturedModalRect(guiLeft + 10 + (int) (95 * (scroll * 1. / maxScroll)), guiTop + 56, 177, 0, 15, 12);

		for (int j = 0; j < container.slots.length; j++)
		{
			int i = tile.mirrored ? container.slots.length - j - 1 : j;
			IESlot slot = container.slots[i];
			int x = j * 22 - scroll;
			int y = 0;

			if ((x < -20 || x > 110) || (!drawSlots))
			{
				slot.xPos = -100;
				slot.yPos = -100;
			}
			else
			{
				slot.xPos = 13 + x;
				slot.yPos = 17 + y;

				int h = tile.active[i] ? (int)(12 * (tile.process[i] / (float) tile.processMax[i]) + .99) : 0;

				this.drawTexturedModalRect(guiLeft + 11 + x, guiTop + 15, 200, 0, 20, 36);
				this.drawTexturedModalRect(guiLeft + 11 + x, guiTop + 38 + (12 - h), 225, (12 - h), 20, h);
			}

		}
		ClientUtils.handleGuiTank(tile.tank, guiLeft + 125,guiTop + 21, 16,47, 176,31,20,51, mx,my, "questionablyimmersive:textures/gui/cokeoven.png", null);
	}
}
