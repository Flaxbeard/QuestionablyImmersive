package flaxbeard.questionablyimmersive.common.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class QIFakePlayer extends FakePlayer
{
	private static final UUID uuid = UUID.fromString("12f12434-1332-47a8-84b1-a528017235e8");
	private static final GameProfile profile = new GameProfile(uuid, "[QuestionablyImmersive]");

	public QIFakePlayer(World world)
	{
		super((WorldServer) world, profile);
	}

	@Override
	protected void playEquipSound(ItemStack stack)
	{

	}
}
