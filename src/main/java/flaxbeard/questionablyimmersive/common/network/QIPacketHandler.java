package flaxbeard.questionablyimmersive.common.network;

import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class QEPacketHandler
{
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(QuestionablyImmersive.MODID);
	
	public static void preInit()
	{
	}
}
