package flaxbeard.questionablyimmersive.client.gui;

import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import flaxbeard.questionablyimmersive.common.items.ItemPortableRadio;
import flaxbeard.questionablyimmersive.common.network.QIPacketHandler;
import flaxbeard.questionablyimmersive.common.network.TunePortableRadioPacket;
import flaxbeard.questionablyimmersive.common.network.TuneRadioPacket;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiTuneRadio extends GuiScreen
{
	private boolean tile;

	private final TileEntityRadio radio;
	private final ItemStack portableRadio;
	private final EntityPlayer player;
	private final EntityEquipmentSlot slot;

	private int tempStation;
	private int stationDigits = 0;
	private boolean editing = false;

	public GuiTuneRadio(TileEntityRadio radio)
	{
		this.radio = radio;
		this.tempStation = radio.frequency;
		this.portableRadio = null;
		this.tile = true;
		this.player = null;
		this.slot = null;
	}

	public GuiTuneRadio(EntityPlayer player, ItemStack item, EntityEquipmentSlot slot)
	{
		this.portableRadio = item;
		this.tempStation = ItemPortableRadio.getFrequency(item);
		this.radio = null;
		this.tile = false;
		this.player = player;
		this.slot = slot;
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		String stationDigits = Integer.toString(tempStation);

		while (stationDigits.length() < 3)
		{
			stationDigits = "0" + stationDigits;
		}
		this.fontRenderer.drawString(stationDigits + " hz", 0, 0, 0xffffff);

		int signum = (int) Math.signum(Mouse.getDWheel());
		tempStation += (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) ? 10 : 1) * signum;
		tempStation = Math.min(999, tempStation);
		tempStation = Math.max(0, tempStation);
		if (!editing && signum != 0)
		{
			editing = true;
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		if (keyCode == Keyboard.KEY_BACK)
		{
			tempStation = tempStation / 10;
			if (stationDigits > 0)
			{
				stationDigits--;
			}
			if (!editing)
			{
				tempStation = 0;
				stationDigits = 0;
			}
			editing = true;
		}
		else if (stationDigits < 3 && typedChar >= '0' && typedChar <= '9')
		{
			if (!editing)
			{
				tempStation = 0;
				stationDigits = 0;
			}

			int value = typedChar - '0';

			tempStation = tempStation * 10 + value;
			stationDigits++;
			editing = true;
		}
		else if (editing && keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)
		{
			updateStation(tempStation);

			editing = false;
		}
		super.keyTyped(typedChar, keyCode);
	}

	public void initGui()
	{
		super.initGui();
		Keyboard.enableRepeatEvents(true);
	}

	public void onGuiClosed()
	{
		super.onGuiClosed();
		updateStation(tempStation);
		Keyboard.enableRepeatEvents(false);
	}

	public void updateStation(int station)
	{
		if (tile)
		{
			radio.setFrequency(station);
			QIPacketHandler.INSTANCE.sendToServer(new TuneRadioPacket(radio.getPos(), tempStation));
		}
		else
		{
			ItemPortableRadio.setFrequency(portableRadio, station);
			QIPacketHandler.INSTANCE.sendToServer(new TunePortableRadioPacket(slot.getIndex(), tempStation));
		}
	}
}
