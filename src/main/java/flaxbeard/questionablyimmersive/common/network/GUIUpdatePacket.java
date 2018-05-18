package flaxbeard.questionablyimmersive.common.network;

import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GUIUpdatePacket implements IMessage
{
	public interface IPacketReceiver
	{
		void handlePacket(int messageId, NBTTagCompound data);
	}

	public GUIUpdatePacket() {}

	public BlockPos pos;
	public int messageID;
	public NBTTagCompound data;

	public GUIUpdatePacket( BlockPos pos, int messageID, NBTTagCompound data)
	{
		this.pos = pos;
		this.messageID = messageID;
		this.data = data;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(messageID);
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		ByteBufUtils.writeTag(buf, data);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		messageID = buf.readInt();
		pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		data = ByteBufUtils.readTag(buf);
	}

	public static class Handler implements IMessageHandler<GUIUpdatePacket, IMessage>
	{

		@Override
		public IMessage onMessage(GUIUpdatePacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(() -> {
				Container container = player.openContainer;
				if (container instanceof IPacketReceiver)
				{
					((IPacketReceiver) container).handlePacket(message.messageID, message.data);
				}

				TileEntity te = player.world.getTileEntity(message.pos);
				if (te instanceof IPacketReceiver)
				{
					((IPacketReceiver) te).handlePacket(message.messageID, message.data);
				}
			});

			return null;
		}

	}
}