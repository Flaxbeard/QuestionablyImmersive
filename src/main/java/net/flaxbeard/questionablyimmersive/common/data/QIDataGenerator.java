package net.flaxbeard.questionablyimmersive.common.data;

import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = QuestionablyImmersive.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QIDataGenerator
{
	@SubscribeEvent
	public static void gatherData(GatherDataEvent event)
	{
		DataGenerator gen = event.getGenerator();
		if (event.includeServer())
		{
			gen.addProvider(new BlockLoot(gen));
		}
	}
}
