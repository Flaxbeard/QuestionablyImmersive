package net.flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Block.Properties;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class QIGenericTileBlock extends QITileProviderBlock
{
	private final Supplier<TileEntityType<?>> tileType;

	public QIGenericTileBlock(String name, Supplier<TileEntityType<?>> tileType, Properties blockProps, IProperty<?>... stateProps)
	{
		this(name, tileType, blockProps, QIBlockItem.class, stateProps);
		this.setNotNormalBlock();
	}

	public QIGenericTileBlock(String name, Supplier<TileEntityType<?>> tileType, Properties blockProps, @Nullable Class<? extends QIBlockItem> itemBlock, IProperty<?>... stateProps)
	{
		super(name, blockProps, itemBlock, stateProps);
		this.tileType = tileType;
	}

	@Nullable
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return ((TileEntityType) this.tileType.get()).create();
	}
}
