package flaxbeard.questionablyimmersive.common;

import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Configuration;

public class Config
{

	@net.minecraftforge.common.config.Config(modid= QuestionablyImmersive.MODID)
	public static class IPConfig
	{
		@Comment({"Display chunk border while holding Core Samples, default=true"})
		public static boolean sample_displayBorder = true;
		
		public static Extraction extraction;

		public static class Extraction
		{
			@Comment({"List of reservoir types. Format: name, fluid_name, min_mb_fluid, max_mb_fluid, mb_per_tick_replenish, weight, [dim_blacklist], [dim_whitelist], [biome_dict_blacklist], [biome_dict_whitelist]"})
			public static String[] reservoirs = new String[] {
					"aquifer, water, 5000000, 10000000, 6, 30, [], [0], [], []",
					"oil, oil, 2500000, 15000000, 6, 40, [1], [], [], []",
					"lava, lava, 250000, 1000000, 0, 30, [1], [], [], []"
			};
			
			@Comment({"The chance that a chunk contains a fluid reservoir, default=0.5"})
			public static float reservoir_chance = 0.5F;
			
			
			@Comment({"The Flux the Pumpjack requires each tick to pump, default=1024"})
			public static int pumpjack_consumption = 1024;
			
			@Comment({"The amount of mB of oil a Pumpjack extracts per tick, default=15"})
			public static int pumpjack_speed = 15;
			
			@Comment({"Require a pumpjack to have pipes built down to Bedrock, default=false"})
			public static boolean req_pipes = false;
			
			@Comment({"Number of ticks between checking for pipes below pumpjack if required, default=100 (5 secs)"})
			public static int pipe_check_ticks = 100;
		}

		public static Refining refining;

		public static class Refining
		{
			@Comment({"A modifier to apply to the energy costs of every Distillation Tower recipe, default=1"})
			public static float distillationTower_energyModifier = 1;

			@Comment({"A modifier to apply to the time of every Distillation recipe. Can't be lower than 1, default=1"})
			public static float distillationTower_timeModifier = 1;
			
			@Comment({"Distillation Tower recipes. Format: power_cost, input_name, input_mb -> output1_name, output1_mb, output2_name, output2_mb"})
			public static String[] towerRecipes = new String[] {
				"2048, oil, 75 -> lubricant, 9, diesel, 27, gasoline, 39"
			};
			@Comment({"Distillation Tower byproducts. Need one for each recipe. Multiple solid outputs for a single recipe can be separated by semicolons. Format: item_name, stack_size, metadata, percent_chance"})
			public static String[] towerByproduct = new String[] {
				"questionablyimmersive:material, 1, 0, 7"
			};
		}
		
		public static Generation generation;
		
		public static class Generation
		{
			@Comment({"List of Portable Generator fuels. Format: fluid_name, mb_used_per_tick, flux_produced_per_tick"})
			public static String[] fuels = new String[] {
				"gasoline, 5, 256"
			};
			
		}
				
		public static Miscellaneous misc;
				
		public static class Miscellaneous
		{
			@Comment({"List of Motorboat fuels. Format: fluid_name, mb_used_per_tick"})
			public static String[] boat_fuels = new String[] {
				"gasoline, 1"
			};
		}
		
		public static Tools tools;
		
		public static class Tools
		{

		}
	}

	static Configuration config;


}
