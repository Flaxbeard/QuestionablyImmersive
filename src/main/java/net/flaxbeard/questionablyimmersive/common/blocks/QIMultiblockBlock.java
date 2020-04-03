package net.flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameters;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class QIMultiblockBlock extends QITileProviderBlock
{
	public enum MultiblockTileType implements IStringSerializable
	{
		MASTER,
		SLAVE,
		RENDERED_SLAVE;

		@Override
		public String getName()
		{
			return this.name().toLowerCase();
		}
	}

	public static final EnumProperty<MultiblockTileType> MULTIBLOCK_TILE_TYPE = EnumProperty.create("multiblock_tile_type", MultiblockTileType.class);

	public QIMultiblockBlock(String name, Block.Properties props, IProperty<?>... additionalProperties)
	{
		super(name, props, QIBlockItem.class, (IProperty[]) ArrayUtils.addAll(additionalProperties, new IProperty[]{IEProperties.FACING_HORIZONTAL, MULTIBLOCK_TILE_TYPE}));
		this.setMobility(PushReaction.BLOCK);
		this.setNotNormalBlock();
	}

	@Override
	protected BlockState getInitDefaultState()
	{
		return super.getInitDefaultState();
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if (tileEntity instanceof QIBaseTileEntity)
			{
				((QIBaseTileEntity) tileEntity).setOverrideState(state);
			}

			if (tileEntity instanceof MultiblockPartTileEntity)
			{
				((MultiblockPartTileEntity) tileEntity).disassemble();
			}
		}

		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		TileEntity te = world.getTileEntity(pos);
		return te instanceof MultiblockPartTileEntity ? Utils.getPickBlock(((MultiblockPartTileEntity) te).getOriginalBlock(), target, player) : ItemStack.EMPTY;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
	{
	}
}
