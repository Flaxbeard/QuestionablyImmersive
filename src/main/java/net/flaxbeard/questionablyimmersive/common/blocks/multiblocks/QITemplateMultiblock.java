package net.flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.flaxbeard.questionablyimmersive.common.blocks.QIMultiblockBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;

import java.util.Map;
import java.util.function.Supplier;

public abstract class QITemplateMultiblock extends IETemplateMultiblock
{
	private final Supplier<BlockState> baseState;

	public QITemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Map<Block, Tag<Block>> tags, Supplier<BlockState> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, tags, baseState);
		this.baseState = baseState;
	}

	public QITemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Supplier<BlockState> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, baseState);
		this.baseState = baseState;
	}

	protected void replaceStructureBlock(Template.BlockInfo info, World world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster, QIMultiblockBlock.MultiblockTileType tileType)
	{
		BlockState state = this.baseState.get();
		state = state.with(QIMultiblockBlock.MULTIBLOCK_TILE_TYPE, tileType);

		world.setBlockState(actualPos, state);
		TileEntity curr = world.getTileEntity(actualPos);
		if (curr instanceof MultiblockPartTileEntity) {
			MultiblockPartTileEntity tile = (MultiblockPartTileEntity)curr;
			tile.formed = true;
			tile.offsetToMaster = new BlockPos(offsetFromMaster);
			tile.posInMultiblock = info.pos;
			if (state.getProperties().contains(IEProperties.MIRRORED)) {
				tile.setMirrored(mirrored);
			}

			tile.setFacing(this.transformDirection(clickDirection.getOpposite()));
			tile.markDirty();
			world.addBlockEvent(actualPos, world.getBlockState(actualPos).getBlock(), 255, 0);
		} else {
			IELogger.logger.error("Expected MB TE at {} during placement", actualPos);
		}
	}
	@Override
	protected void replaceStructureBlock(Template.BlockInfo info, World world, BlockPos actualPos, boolean mirrored, Direction clickDirection, Vec3i offsetFromMaster) {
		QIMultiblockBlock.MultiblockTileType tileType = QIMultiblockBlock.MultiblockTileType.SLAVE;

		if (offsetFromMaster.equals(Vec3i.NULL_VECTOR)) {
			tileType = QIMultiblockBlock.MultiblockTileType.MASTER;
		}
		replaceStructureBlock(info, world, actualPos, mirrored, clickDirection, offsetFromMaster, tileType);
	}
}
