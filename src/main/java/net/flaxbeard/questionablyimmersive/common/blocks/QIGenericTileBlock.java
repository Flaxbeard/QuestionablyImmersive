package net.flaxbeard.questionablyimmersive.common.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

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
