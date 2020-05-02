package net.flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class QIMetalMultiblockBlock extends QIMultiblockBlock
{
	private Supplier<TileEntityType<?>> renderedTileType;
	private Supplier<TileEntityType<?>> masterTileType;
	private Supplier<TileEntityType<?>> tileType;

	public QIMetalMultiblockBlock(String name, Supplier<TileEntityType<?>> te, Supplier<TileEntityType<?>> renderedTe, Supplier<TileEntityType<?>> masterTe, IProperty<?>... additionalProperties)
	{
		super(name, Properties.create(Material.IRON).hardnessAndResistance(3.0F, 15.0F), (IProperty[]) ArrayUtils.addAll(additionalProperties, new IProperty[]{IEProperties.MIRRORED}));
		this.tileType = te;
		this.renderedTileType = renderedTe;
		this.masterTileType = masterTe;
		this.setNotNormalBlock();
		this.lightOpacity = 0;
	}

	public QIMetalMultiblockBlock(String name, Supplier<TileEntityType<?>> te, Supplier<TileEntityType<?>> masterTe, IProperty<?>... additionalProperties)
	{
		this(name, te, te, masterTe, additionalProperties);
	}

	@Nullable
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		MultiblockTileType mtt = (MultiblockTileType) state.get(MULTIBLOCK_TILE_TYPE);

		if (mtt == MultiblockTileType.SLAVE)
		{
			return this.tileType.get().create();
		}
		else if (mtt == MultiblockTileType.RENDERED_SLAVE)
		{
			return this.renderedTileType.get().create();
		}
		else
		{
			return this.masterTileType.get().create();
		}
	}
}
