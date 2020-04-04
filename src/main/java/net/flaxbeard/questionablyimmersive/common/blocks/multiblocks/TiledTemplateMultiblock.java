package net.flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import net.flaxbeard.questionablyimmersive.common.blocks.QIMultiblockBlock;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.TiledMultiblockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public abstract class TiledTemplateMultiblock extends QITemplateMultiblock
{

	public final BlockPos masterFromOrigin;

	public TiledTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Map<Block, Tag<Block>> tags, Supplier<BlockState> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, tags, baseState);

		// TODO should be able to get rid of this
		this.masterFromOrigin = masterFromOrigin;
	}

	public TiledTemplateMultiblock(ResourceLocation loc, BlockPos masterFromOrigin, BlockPos triggerFromOrigin, Supplier<BlockState> baseState)
	{
		super(loc, masterFromOrigin, triggerFromOrigin, baseState);
		this.masterFromOrigin = masterFromOrigin;
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, Direction side, PlayerEntity player) {
		if (side.getAxis() == Direction.Axis.Y) {
			side = Direction.fromAngle((double)player.rotationYaw);
		}

		Rotation rot = Utils.getRotationBetweenFacings(Direction.NORTH, side.getOpposite());
		if (rot == null) {
			return false;
		} else {

			try
			{
				Template template = getTemplate();

				ImmutableList mirrorStates;
				if (this.canBeMirrored())
				{
					mirrorStates = ImmutableList.of(Mirror.NONE, Mirror.FRONT_BACK);
				} else
				{
					mirrorStates = ImmutableList.of(Mirror.NONE);
				}

				Iterator var8 = mirrorStates.iterator();

				label34:
				while (var8.hasNext())
				{
					Mirror mirror = (Mirror) var8.next();
					PlacementSettings placeSet = (new PlacementSettings()).setMirror(mirror).setRotation(rot);
					BlockPos origin = pos.subtract(Template.transformedBlockPos(placeSet, this.triggerFromOrigin));

					Vec3i depthOffset = side.getOpposite().getDirectionVec();
					int sliceWidth = template.getSize().getZ();
					depthOffset = new Vec3i(depthOffset.getX() * sliceWidth, 0, depthOffset.getZ() * sliceWidth);

					BlockPos offsetOrigin = origin;

					int width = 0;
					widthLabel:
					while (width < 100)
					{
						for (Template.BlockInfo info : template.blocks.get(0))
						{
							BlockPos realRelPos = Template.transformedBlockPos(placeSet, info.pos);
							BlockPos here = offsetOrigin.add(realRelPos);
							BlockState expected = info.state.mirror(mirror).rotate(rot);
							BlockState inWorld = world.getBlockState(here);

							BlockMatcher.Result matches = BlockMatcher.matches(expected, inWorld, world, here, additionalPredicates);

							// TODO should be able to get rid of this
							Method isAllow = ObfuscationReflectionHelper.findMethod(BlockMatcher.Result.class, "isAllow");
							isAllow.setAccessible(true);
							boolean result = (boolean) isAllow.invoke(matches);

							if (!result)
							{
								break widthLabel;
							}
						}
						width += 1;
						offsetOrigin = offsetOrigin.add(depthOffset);
					}

					if (width < getMinLayers()) {
						continue label34;
					}

					this.form(world, origin, rot, mirror, side, template, width);
					return true;
				}
			} catch (IllegalAccessException e)
			{
				e.printStackTrace();
			} catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}

			return false;
		}
	}

	protected abstract int getMinLayers();

	protected void form(World world, BlockPos pos, Rotation rot, Mirror mirror, Direction sideHit, Template template, int width) {
		BlockPos originalPos = pos;
		BlockPos masterPos = withSettingsAndOffset(pos, masterFromOrigin, mirror, rot);

		Vec3i depthOffset = sideHit.getOpposite().getDirectionVec();
		int sliceWidth = template.getSize().getZ();
		depthOffset = new Vec3i(depthOffset.getX() * sliceWidth, 0, depthOffset.getZ() * sliceWidth);

		for (int i = 0; i < width; i++)
		{
			Iterator var7 = this.getStructure().iterator();
			while (var7.hasNext())
			{
				Template.BlockInfo block = (Template.BlockInfo) var7.next();
				BlockPos actualPos = withSettingsAndOffset(pos, block.pos, mirror, rot);

				BlockPos layerPos = withSettingsAndOffset(originalPos, block.pos, mirror, rot);
				QIMultiblockBlock.MultiblockTileType tileType = getTileType(layerPos.subtract(masterPos).equals(Vec3i.NULL_VECTOR), i);

				this.replaceStructureBlock(block, world, actualPos, mirror != Mirror.NONE, sideHit, actualPos.subtract(masterPos), tileType);
				TileEntity curr = world.getTileEntity(actualPos);
				if (curr instanceof TiledMultiblockTileEntity) {

					TiledMultiblockTileEntity tile = (TiledMultiblockTileEntity) curr;
					tile.numLayers = width;
					tile.layer = i;
					afterReplacement(tile, i);
				}
			}

			pos = pos.add(depthOffset);
		}
	}

	protected QIMultiblockBlock.MultiblockTileType getTileType(boolean equals, int i)
	{
		if (equals && i == 0) {
			return QIMultiblockBlock.MultiblockTileType.MASTER;
		}
		return QIMultiblockBlock.MultiblockTileType.SLAVE;
	}

	protected abstract void afterReplacement(TiledMultiblockTileEntity tile, int layer);
}
