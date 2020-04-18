package net.flaxbeard.questionablyimmersive.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.client.manual.IEManualInstance;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.Tree;
import com.mojang.blaze3d.platform.GlStateManager;
import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.client.gui.CokeOvenBatteryScreen;
import net.flaxbeard.questionablyimmersive.client.gui.TriphammerScreen;
import net.flaxbeard.questionablyimmersive.client.render.CokeOvenBatteryRenderer;
import net.flaxbeard.questionablyimmersive.client.render.TriphammerRenderer;
import net.flaxbeard.questionablyimmersive.common.CommonProxy;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.CokeOvenBatteryTileEntity;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.TriphammerTileEntity;
import net.flaxbeard.questionablyimmersive.common.gui.CokeOvenBatteryContainer;
import net.flaxbeard.questionablyimmersive.common.gui.QIGuiHandler;
import net.flaxbeard.questionablyimmersive.common.gui.TriphammerContainer;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit()
	{
		super.preInit();
	}

	@Override
	public void postInit()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(CokeOvenBatteryTileEntity.Rendered.class, new CokeOvenBatteryRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TriphammerTileEntity.Master.class, new TriphammerRenderer());
	}

	@Override
	public void postPostInit()
	{
		IEManualInstance ieMan = ManualHelper.getManual();
		Tree.InnerNode<ResourceLocation, ManualEntry> toolsCat = ieMan.getRoot().getOrCreateSubnode(new ResourceLocation(QuestionablyImmersive.MODID, "questionable"));
		ieMan.addEntry(toolsCat, new ResourceLocation(QuestionablyImmersive.MODID, "coke_oven_battery"));
		ieMan.addEntry(toolsCat, new ResourceLocation(QuestionablyImmersive.MODID, "triphammer"));
	}

	@Override
	public void renderTileForManual(TileEntity te)
	{
		GlStateManager.pushMatrix();
		GlStateManager.rotated(-0, 0, 1, 0);
		GlStateManager.translated(1, 1, 0);


		TileEntityRenderer<TileEntity> tesr = TileEntityRendererDispatcher.instance.getRenderer((TileEntity) te);
		tesr.render(te, 0, 0, 0, 0, 0);
		GlStateManager.popMatrix();
	}

	@Override
	public void registerContainersAndScreens()
	{
		super.registerContainersAndScreens();
		this.registerScreen(CokeOvenBatteryContainer.ID, CokeOvenBatteryScreen::new);
		this.registerScreen(TriphammerContainer.ID, TriphammerScreen::new);
	}

	public <C extends Container, S extends Screen & IHasContainer<C>> void registerScreen(ResourceLocation containerName, ScreenManager.IScreenFactory<C, S> factory)
	{
		ContainerType<C> type = (ContainerType<C>) QIGuiHandler.getContainerType(containerName);
		ScreenManager.registerFactory(type, factory);
	}
}