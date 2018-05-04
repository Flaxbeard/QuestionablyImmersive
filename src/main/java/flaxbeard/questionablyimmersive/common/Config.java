package flaxbeard.questionablyimmersive.common;

import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Configuration;

public class Config
{

	@net.minecraftforge.common.config.Config(modid = QuestionablyImmersive.MODID)
	public static class QIConfig
	{
		public static CokeOvenBattery cokeOvenBattery;

		public static class CokeOvenBattery
		{
			@Comment({"Percentage of a recipe's Creosote Oil produced by the battery (1 is the same as base Coke Oven, 0.5 is half), default=0.8"})
			public static float creosoteLoss = 0.8f;

			@Comment({"Simultaneous operations that a single battery's oven can process, default=9"})
			public static int simultaneousOperations = 9;

			@Comment({"Relative operation time to the base Coke Oven (.6666 = 33% faster per operation), default=.666666"})
			public static float operationTimeModifier = .666666f;
		}

	}

	static Configuration config;


}
