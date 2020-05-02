package net.flaxbeard.questionablyimmersive.common.data;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.ValidationResults;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public abstract class LootGenerator implements IDataProvider
{
	private final DataGenerator dataGenerator;
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	protected final Map<ResourceLocation, LootTable> tables = Maps.newHashMap();

	public LootGenerator(DataGenerator gen)
	{
		this.dataGenerator = gen;
	}

	@Override
	public void act(@Nonnull DirectoryCache outCache)
	{
		this.tables.clear();
		Path outFolder = this.dataGenerator.getOutputFolder();
		this.registerTables();
		ValidationResults validator = new ValidationResults();
		this.tables.forEach((name, table) ->
		{
			LootTableManager.func_215302_a(validator, name, table, this.tables::get);
		});
		Multimap<String, String> problems = validator.getProblems();
		if (!problems.isEmpty())
		{
			problems.forEach((name, table) ->
			{
				IELogger.logger.warn("Found validation problem in " + name + ": " + table);
			});
			throw new IllegalStateException("Failed to validate loot tables, see logs");
		}
		else
		{
			this.tables.forEach((name, table) ->
			{
				Path out = getPath(outFolder, name);

				try
				{
					IDataProvider.save(GSON, outCache, LootTableManager.toJson(table), out);
				} catch (IOException var6)
				{
					IELogger.logger.error("Couldn't save loot table {}", out, var6);
				}

			});
		}
	}

	private static Path getPath(Path path, ResourceLocation rl)
	{
		return path.resolve("data/" + rl.getNamespace() + "/loot_tables/" + rl.getPath() + ".json");
	}

	protected abstract void registerTables();
}
