package net.flaxbeard.questionablyimmersive.common.data;

import blusunrize.immersiveengineering.common.util.loot.DropInventoryLootEntry;
import blusunrize.immersiveengineering.common.util.loot.MBOriginalBlockLootEntry;
import net.flaxbeard.questionablyimmersive.common.QIContent;
import net.flaxbeard.questionablyimmersive.common.blocks.QIBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.IProperty;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.BlockStateProperty;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class BlockLoot extends LootGenerator
{
	public BlockLoot(DataGenerator gen)
	{
		super(gen);
	}

	@Nonnull
	public String getName()
	{
		return "LootTablesQI";
	}

	private ResourceLocation toTableLoc(ResourceLocation in)
	{
		return new ResourceLocation(in.getNamespace(), "blocks/" + in.getPath());
	}

	@Override
	protected void registerTables()
	{
		registerMultiblocks();
	}

	private void registerMultiblocks()
	{
		registerMultiblock(QIBlocks.Multiblocks.cokeOvenBattery);
		registerMultiblock(QIBlocks.Multiblocks.triphammer);
	}

	private void registerAllRemainingAsDefault()
	{
		for (Block b : QIContent.registeredQIBlocks)
		{
			if (!tables.containsKey(toTableLoc(b.getRegistryName())))
				registerSelfDropping(b);
		}
	}

	private void registerMultiblock(Block b)
	{
		register(b, dropInv(), dropOriginalBlock());
	}

	private LootPool.Builder dropInv()
	{
		return createPoolBuilder()
				.addEntry(DropInventoryLootEntry.builder());
	}

	private LootPool.Builder dropOriginalBlock()
	{
		return createPoolBuilder()
				.addEntry(MBOriginalBlockLootEntry.builder());
	}

	private void register(Block b, LootPool.Builder... pools)
	{
		LootTable.Builder builder = LootTable.builder();
		for (LootPool.Builder pool : pools)
		{
			builder.addLootPool(pool);
		}
		register(b, builder);
	}

	private void register(Block b, LootTable.Builder table)
	{
		register(b.getRegistryName(), table);
	}

	private void register(ResourceLocation name, LootTable.Builder table)
	{
		if (tables.put(toTableLoc(name), table.setParameterSet(LootParameterSets.BLOCK).build()) != null)
			throw new IllegalStateException("Duplicate loot table " + name);
	}

	private void registerSelfDropping(Block b, LootPool.Builder... pool)
	{
		LootPool.Builder[] withSelf = Arrays.copyOf(pool, pool.length + 1);
		withSelf[withSelf.length - 1] = singleItem(b);
		register(b, withSelf);
	}

	private LootTable.Builder dropProvider(IItemProvider in)
	{
		return LootTable
				.builder()
				.addLootPool(singleItem(in)
				);
	}

	private LootPool.Builder singleItem(IItemProvider in)
	{
		return createPoolBuilder()
				.rolls(ConstantRange.of(1))
				.addEntry(ItemLootEntry.builder(in));
	}

	private LootPool.Builder createPoolBuilder()
	{
		return LootPool.builder().acceptCondition(SurvivesExplosion.builder());
	}

	private <T extends Comparable<T>> ILootCondition.IBuilder propertyIs(Block b, IProperty<T> prop, T value)
	{
		return BlockStateProperty.builder(b)
				.with(prop, value);
	}
}