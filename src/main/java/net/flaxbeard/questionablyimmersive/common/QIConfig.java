package net.flaxbeard.questionablyimmersive.common;

import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
		modid = QuestionablyImmersive.MODID,
		bus = Mod.EventBusSubscriber.Bus.MOD
)
public class QIConfig
{
	public static final ForgeConfigSpec ALL;
	public static final CokeOvenBattery COKE_OVEN_BATTERY;
	public static final Triphammer TRIPHAMMER;

	public QIConfig()
	{
	}

	static
	{
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		COKE_OVEN_BATTERY = new CokeOvenBattery(builder);
		TRIPHAMMER = new Triphammer(builder);
		ALL = builder.build();
	}


	public static class CokeOvenBattery
	{
		public final ForgeConfigSpec.DoubleValue creosoteLoss;
		public final ForgeConfigSpec.IntValue simultaneousOperations;
		public final ForgeConfigSpec.DoubleValue operationTimeModifier;

		CokeOvenBattery(ForgeConfigSpec.Builder builder)
		{
			builder.push("coke_oven_battery");
			this.creosoteLoss = builder.comment("Percentage of a recipe's Creosote Oil produced by the battery (1 is the same as base Coke Oven, 0.5 is half), Default: 0.8")
					.defineInRange("creosote_loss", 0.8D, 0D, 2.0D);
			this.simultaneousOperations = builder.comment("Simultaneous operations that a single battery's oven can process, Default: 9")
					.defineInRange("simultaneous_operations", 9, 1, 2147483647);
			this.operationTimeModifier = builder.comment("Relative operation time to the base Coke Oven (.6666 = 33% faster per operation), Default: .666666")
					.defineInRange("operation_time_modifier", 2D / 3D, 0.001D, 2.0D);
			builder.pop();
		}
	}

	public static class Triphammer
	{
		public final ForgeConfigSpec.IntValue costPerTick;
		public final ForgeConfigSpec.IntValue ticksPerLevel;
		public final ForgeConfigSpec.IntValue ticksPerHardness;
		public final ForgeConfigSpec.DoubleValue relativeYield;

		Triphammer(ForgeConfigSpec.Builder builder)
		{
			builder.push("triphammer");
			this.costPerTick = builder.comment("The Flux the Triphammer will use per tick, Default: 64")
					.defineInRange("cost_per_tick", 64, 1, 2147483647);
			this.ticksPerLevel = builder.comment("Number of ticks to perform one level's worth of anvil operations, Default: 600")
					.defineInRange("ticks_per_level", 600, 1, 2147483647);
			this.ticksPerHardness = builder.comment("Number of ticks per point of block hardness to destroy a block, Default: 50")
					.defineInRange("ticks_per_hardness", 50, 1, 2147483647);
			this.relativeYield = builder.comment("Triphammer yield relative to Crusher, Default: 0.75 (0.75*2x = 1.5x per ore)")
					.defineInRange("relativeYield", 0.75, 0, 2147483647);
			builder.pop();
		}
	}
}