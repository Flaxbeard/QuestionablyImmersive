package flaxbeard.questionablyimmersive.common.network;

import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class TuneRadioPacket implements IMessage
{
	public TuneRadioPacket() {}

	public BlockPos pos;
	public int newStation;

	public TuneRadioPacket(BlockPos pos, int newStation)
	{
		this.pos = pos;
		this.newStation = newStation;
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(newStation);
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());

	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		newStation = buf.readInt();
		pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
	}

	public static class Handler implements IMessageHandler<TuneRadioPacket, IMessage>
	{

		@Override
		public IMessage onMessage(TuneRadioPacket message, MessageContext ctx)
		{
			EntityPlayerMP player = ctx.getServerHandler().player;
			DimensionManager.getWorld(player.world.provider.getDimension()).addScheduledTask(() -> {
				TileEntity te = player.world.getTileEntity(message.pos);
				if (te instanceof TileEntityRadio)
				{
					((TileEntityRadio) te).setFrequency(message.newStation);
				}
			});

			return null;
		}

	}
}