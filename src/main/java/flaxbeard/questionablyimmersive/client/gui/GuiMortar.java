package flaxbeard.questionablyimmersive.client.gui;

import flaxbeard.questionablyimmersive.common.blocks.tile.TileEntityMortar;
import flaxbeard.questionablyimmersive.common.gui.ContainerMortar;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiMortar extends GuiContainer
{
	private final TileEntityMortar tile;

	public GuiMortar(InventoryPlayer inventoryPlayer, TileEntityMortar tile)
	{
		super(new ContainerMortar(inventoryPlayer, tile));
		this.tile = tile;
	}

	@Override
	public void drawScreen(int mx, int my, float partial)
	{
		this.drawDefaultBackground();
		super.drawScreen(mx, my, partial);
		this.renderHoveredToolTip(mx, my);
	}
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{

	}
}
