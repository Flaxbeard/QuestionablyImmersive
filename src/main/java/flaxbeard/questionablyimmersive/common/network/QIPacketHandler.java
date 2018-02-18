package flaxbeard.questionablyimmersive.common.network;

import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import scala.collection.script.Update;

public class QIPacketHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(QuestionablyImmersive.MODID);
	
	public static void preInit()
	{
		INSTANCE.registerMessage(TuneRadioPacket.Handler.class, TuneRadioPacket.class, 0, Side.SERVER);
		INSTANCE.registerMessage(TunePortableRadioPacket.Handler.class, TunePortableRadioPacket.class, 1, Side.SERVER);
		INSTANCE.registerMessage(UpdateRadioNetworkPacket.Handler.class, UpdateRadioNetworkPacket.class, 2, Side.CLIENT);

	}
}
