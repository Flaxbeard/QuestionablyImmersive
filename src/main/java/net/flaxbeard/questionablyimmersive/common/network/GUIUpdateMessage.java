package net.flaxbeard.questionablyimmersive.common.network;

import blusunrize.immersiveengineering.common.network.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.function.Supplier;

public class GUIUpdateMessage implements IMessage
{
	public interface IMessageReceiver
	{
		void handleMessage(int messageId, CompoundNBT data);
	}

	private BlockPos pos;
	private int messageID;
	private CompoundNBT data;

	public GUIUpdateMessage(BlockPos pos, int messageID, CompoundNBT data)
	{
		this.pos = pos;
		this.messageID = messageID;
		this.data = data;
	}

	public GUIUpdateMessage(PacketBuffer buf)
	{
		this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		this.messageID = buf.readInt();
		this.data = buf.readCompoundTag();
	}

	public void toBytes(PacketBuffer buf)
	{
		buf.writeInt(this.pos.getX()).writeInt(this.pos.getY()).writeInt(this.pos.getZ());
		buf.writeInt(messageID);
		buf.writeCompoundTag(this.data);
	}

	public void process(Supplier<Context> context)
	{
		Context ctx = (Context) context.get();
		if (ctx.getDirection().getReceptionSide() == LogicalSide.SERVER)
		{
			ctx.enqueueWork(() ->
			{
				ServerPlayerEntity player = ctx.getSender();
				Container container = player.openContainer;

				if (container instanceof IMessageReceiver)
				{
					((IMessageReceiver) container).handleMessage(messageID, data);
				}

				TileEntity te = player.world.getTileEntity(pos);
				if (te instanceof IMessageReceiver)
				{
					((IMessageReceiver) te).handleMessage(messageID, data);
				}
			});
		}
	}
}
