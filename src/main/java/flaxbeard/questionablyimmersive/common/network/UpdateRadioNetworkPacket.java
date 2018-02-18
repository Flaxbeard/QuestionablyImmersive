package flaxbeard.questionablyimmersive.common.network;

import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import flaxbeard.questionablyimmersive.common.util.RadioHelper;
import io.netty.buffer.ByteBuf;
import javafx.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class UpdateRadioNetworkPacket implements IMessage
{
	public UpdateRadioNetworkPacket() {}

	public int dimension;
	public int frequency;
	public NBTTagCompound comp;

	public UpdateRadioNetworkPacket(int dimension, int frequency, NBTTagCompound comp)
	{
		this.dimension = dimension;
		this.frequency = frequency;
		this.comp = comp;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(dimension);
		buf.writeInt(frequency);
		ByteBufUtils.writeTag(buf, comp);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		dimension = buf.readInt();
		frequency = buf.readInt();
		comp = ByteBufUtils.readTag(buf);
	}

	public static class Handler implements IMessageHandler<UpdateRadioNetworkPacket, IMessage>
	{

		@Override
		public IMessage onMessage(UpdateRadioNetworkPacket message, MessageContext ctx)
		{
			Minecraft.getMinecraft().addScheduledTask(() -> {
				RadioHelper.getNetwork(message.dimension, message.frequency).readFromNBT(message.comp);
			});

			return null;
		}

	}
}