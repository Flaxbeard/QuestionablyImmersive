package flaxbeard.questionablyimmersive.common.network;

import flaxbeard.questionablyimmersive.common.items.ItemPortableRadio;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TunePortableRadioPacket implements IMessage
{
	public TunePortableRadioPacket() {}

	public int slot;
	public int newStation;

	public TunePortableRadioPacket(int slot, int newStation)
	{
		this.slot = slot;
		this.newStation = newStation;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(newStation);
		buf.writeInt(slot);

	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		newStation = buf.readInt();
		slot = buf.readInt();
	}

	public static class Handler implements IMessageHandler<TunePortableRadioPacket, IMessage>
	{

		@Override
		public IMessage onMessage(TunePortableRadioPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(() -> {
				EntityEquipmentSlot slot = EntityEquipmentSlot.values()[message.slot];
				ItemStack radio = player.getItemStackFromSlot(slot);
				ItemPortableRadio.setFrequency(radio, message.newStation);
			});

			return null;
		}

	}
}