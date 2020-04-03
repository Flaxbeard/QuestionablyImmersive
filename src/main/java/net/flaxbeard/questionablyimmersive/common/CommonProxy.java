package net.flaxbeard.questionablyimmersive.common;

import net.flaxbeard.questionablyimmersive.common.gui.QIGuiHandler;
import net.minecraft.tileentity.TileEntity;

public class CommonProxy
{
	public void preInit()
	{
	}

	public void postInit()
	{
	}

	public void postPostInit()
	{
	}

	public void renderTileForManual(TileEntity te)
	{
	}

	public void registerContainersAndScreens()
	{
		QIGuiHandler.commonInit();
	}
}
