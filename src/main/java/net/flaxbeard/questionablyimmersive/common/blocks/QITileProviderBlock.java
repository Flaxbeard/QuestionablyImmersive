//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.DimensionBlockPos;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile.PlacementLimitation;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import static net.flaxbeard.questionablyimmersive.common.blocks.QIMultiblockBlock.MULTIBLOCK_TILE_TYPE;

@EventBusSubscriber
public abstract class QITileProviderBlock extends QIBaseBlock implements IColouredBlock
{
	private boolean hasColours = false;
	private static final Map<DimensionBlockPos, TileEntity> tempTile = new HashMap();

	public QITileProviderBlock(String name, Properties blockProps, @Nullable Class<? extends QIBlockItem> itemBlock, IProperty... stateProps)
	{
		super(name, blockProps, itemBlock, stateProps);
	}

	@SubscribeEvent
	public static void onTick(ServerTickEvent ev)
	{
		if (ev.phase == Phase.END)
		{
			tempTile.clear();
		}

	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	protected BlockState getInitDefaultState()
	{
		BlockState ret = super.getInitDefaultState();
		if (ret.getProperties().contains(IEProperties.FACING_ALL))
		{
			ret = (BlockState) ret.with(IEProperties.FACING_ALL, this.getDefaultFacing());
		} else if (ret.getProperties().contains(IEProperties.FACING_HORIZONTAL))
		{
			ret = (BlockState) ret.with(IEProperties.FACING_HORIZONTAL, this.getDefaultFacing());
		}

		if (ret.getProperties().contains(MULTIBLOCK_TILE_TYPE))
		{
			ret = ret.with(MULTIBLOCK_TILE_TYPE, QIMultiblockBlock.MultiblockTileType.SLAVE);
		}

		return ret;
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		TileEntity tile = world.getTileEntity(pos);
		if (state.getBlock() != newState.getBlock())
		{
			if (tile instanceof QIBaseTileEntity)
			{
				((QIBaseTileEntity) tile).setOverrideState(state);
			}

			if (tile instanceof IHasDummyBlocks)
			{
				((IHasDummyBlocks) tile).breakDummies(pos, state);
			}

			Consumer dropHandler;
			if (world.getGameRules().getBoolean(GameRules.DO_TILE_DROPS))
			{
				dropHandler = (cc) ->
				{
					Connection c = (Connection) cc;
					if (!c.isInternal())
					{
						BlockPos end = c.getOtherEnd(c.getEndFor(pos)).getPosition();
						double dx = (double) pos.getX() + 0.5D + (double) Math.signum((float) (end.getX() - pos.getX()));
						double dy = (double) pos.getY() + 0.5D + (double) Math.signum((float) (end.getY() - pos.getY()));
						double dz = (double) pos.getZ() + 0.5D + (double) Math.signum((float) (end.getZ() - pos.getZ()));
						world.addEntity(new ItemEntity(world, dx, dy, dz, c.type.getWireCoil(c)));
					}

				};
			} else
			{
				dropHandler = (c) ->
				{
				};
			}

			if (tile instanceof IImmersiveConnectable && !world.isRemote)
			{
				Iterator var10 = ((IImmersiveConnectable) tile).getConnectionPoints().iterator();

				while (var10.hasNext())
				{
					ConnectionPoint cp = (ConnectionPoint) var10.next();
					GlobalWireNetwork.getNetwork(world).removeAllConnectionsAt(cp, dropHandler);
				}
			}
		}

		tempTile.put(new DimensionBlockPos(pos, world.getDimension().getType()), tile);
		super.onReplaced(state, world, pos, newState, isMoving);
	}

	@Override
	public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity tile, ItemStack stack)
	{
		if (tile instanceof IAdditionalDrops)
		{
			Collection<ItemStack> stacks = ((IAdditionalDrops) tile).getExtraDrops(player, state);
			if (stacks != null && !stacks.isEmpty())
			{
				Iterator var8 = stacks.iterator();

				while (var8.hasNext())
				{
					ItemStack s = (ItemStack) var8.next();
					if (!s.isEmpty())
					{
						spawnAsEntity(world, pos, s);
					}
				}
			}
		}

		super.harvestBlock(world, player, pos, state, tile, stack);
	}

	@Override
	public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity)
	{
		TileEntity tile = world.getTileEntity(pos);
		return tile instanceof IEntityProof ? ((IEntityProof) tile).canEntityDestroy(entity) : super.canEntityDestroy(state, world, pos, entity);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof ITileDrop && target instanceof BlockRayTraceResult)
		{
			ItemStack s = ((ITileDrop) tile).getPickBlock(player, world.getBlockState(pos), target);
			if (!s.isEmpty())
			{
				return s;
			}
		}

		Item item = this.asItem();
		return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, 1);
	}

	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
	{
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity != null && tileentity.receiveClientEvent(eventID, eventParam);
	}

	protected Direction getDefaultFacing()
	{
		return Direction.NORTH;
	}

	@Override
	public void onIEBlockPlacedBy(BlockItemUseContext context, BlockState state)
	{
		World world = context.getWorld();
		BlockPos pos = context.getPos();
		TileEntity tile = world.getTileEntity(pos);
		PlayerEntity placer = context.getPlayer();
		Direction side = context.getFace();
		float hitX = (float) context.getHitVec().x - (float) pos.getX();
		float hitY = (float) context.getHitVec().y - (float) pos.getY();
		float hitZ = (float) context.getHitVec().z - (float) pos.getZ();
		ItemStack stack = context.getItem();
		if (tile instanceof IDirectionalTile)
		{
			Direction f = ((IDirectionalTile) tile).getFacingForPlacement(placer, pos, side, hitX, hitY, hitZ);
			((IDirectionalTile) tile).setFacing(f);
			if (tile instanceof IAdvancedDirectionalTile)
			{
				((IAdvancedDirectionalTile) tile).onDirectionalPlacement(side, hitX, hitY, hitZ, placer);
			}
		}

		if (tile instanceof ITileDrop)
		{
			((ITileDrop) tile).readOnPlacement(placer, stack);
		}

		if (tile instanceof IHasDummyBlocks)
		{
			((IHasDummyBlocks) tile).placeDummies(context, state);
		}

		if (tile instanceof IPlacementInteraction)
		{
			((IPlacementInteraction) tile).onTilePlaced(world, pos, state, side, hitX, hitY, hitZ, placer, stack);
		}

	}

	@Override
	public boolean hammerUseSide(Direction side, PlayerEntity player, World w, BlockPos pos, BlockRayTraceResult hit)
	{
		TileEntity tile = w.getTileEntity(pos);
		if (tile instanceof IHammerInteraction)
		{
			boolean b = ((IHammerInteraction) tile).hammerUseSide(side, player, hit.getHitVec());
			if (b)
			{
				return true;
			}
		}

		return super.hammerUseSide(side, player, w, pos, hit);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit)
	{
		Direction side = hit.getFace();
		float hitX = (float) hit.getHitVec().x - (float) pos.getX();
		float hitY = (float) hit.getHitVec().y - (float) pos.getY();
		float hitZ = (float) hit.getHitVec().z - (float) pos.getZ();
		ItemStack heldItem = player.getHeldItem(hand);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof IDirectionalTile && Utils.isHammer(heldItem) && ((IDirectionalTile) tile).canHammerRotate(side, hit.getHitVec().subtract(new Vec3d(pos)), player) && !world.isRemote)
		{
			Direction f = ((IDirectionalTile) tile).getFacing();
			Direction oldF = f;
			PlacementLimitation limit = ((IDirectionalTile) tile).getFacingLimitation();
			switch (limit)
			{
				case SIDE_CLICKED:
					f = Direction.values()[(f.ordinal() + 1) % Direction.values().length];
					break;
				case PISTON_LIKE:
					f = player.isSneaking() ? f.rotateAround(side.getAxis()).getOpposite() : f.rotateAround(side.getAxis());
					break;
				case HORIZONTAL:
				case HORIZONTAL_PREFER_SIDE:
				case HORIZONTAL_QUADRANT:
				case HORIZONTAL_AXIS:
					f = player.isSneaking() ? f.rotateYCCW() : f.rotateY();
			}

			((IDirectionalTile) tile).setFacing(f);
			((IDirectionalTile) tile).afterRotation(oldF, f);
			tile.markDirty();
			world.notifyBlockUpdate(pos, state, state, 3);
			world.addBlockEvent(tile.getPos(), tile.getBlockState().getBlock(), 255, 0);
			return true;
		} else
		{
			if (tile instanceof IPlayerInteraction)
			{
				boolean b = ((IPlayerInteraction) tile).interact(side, player, hand, heldItem, hitX, hitY, hitZ);
				if (b)
				{
					return b;
				}
			}

			if (tile instanceof QIBlockInterfaces.IInteractionObjectQI && hand == Hand.MAIN_HAND && !player.isSneaking())
			{
				QIBlockInterfaces.IInteractionObjectQI interaction = (QIBlockInterfaces.IInteractionObjectQI) tile;
				interaction = interaction.getGuiMaster();
				if (interaction != null && interaction.canUseGui(player) && !world.isRemote)
				{
					NetworkHooks.openGui((ServerPlayerEntity) player, interaction, ((TileEntity) interaction).getPos());
				}

				return true;
			} else
			{
				return super.onBlockActivated(state, world, pos, player, hand, hit);
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		if (!world.isRemote)
		{
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof INeighbourChangeTile && !tile.getWorld().isRemote)
			{
				((INeighbourChangeTile) tile).onNeighborBlockChange(fromPos);
			}
		}

	}

	public QITileProviderBlock setHasColours()
	{
		this.hasColours = true;
		return this;
	}

	@Override
	public boolean hasCustomBlockColours()
	{
		return this.hasColours;
	}

	public int getRenderColour(BlockState state, @Nullable IBlockReader worldIn, @Nullable BlockPos pos, int tintIndex)
	{
		if (worldIn != null && pos != null)
		{
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof IColouredTile)
			{
				return ((IColouredTile) tile).getRenderColour(tintIndex);
			}
		}

		return 16777215;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context)
	{
		if (state.getBlock() == this)
		{
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IAdvancedCollisionBounds)
			{
				List<AxisAlignedBB> bounds = ((IAdvancedCollisionBounds) te).getAdvancedCollisionBounds();
				if (bounds != null && !bounds.isEmpty())
				{
					VoxelShape ret = VoxelShapes.empty();
					Iterator var8 = bounds.iterator();

					while (var8.hasNext())
					{
						AxisAlignedBB aabb = (AxisAlignedBB) var8.next();
						if (aabb != null)
						{
							ret = VoxelShapes.combineAndSimplify(ret, VoxelShapes.create(aabb), IBooleanFunction.OR);
						}
					}

					return ret;
				}
			}

			if (te instanceof IBlockBounds)
			{
				float[] bounds = ((IBlockBounds) te).getBlockBounds();
				if (bounds != null)
				{
					AxisAlignedBB aabb = new AxisAlignedBB((double) bounds[0], (double) bounds[1], (double) bounds[2], (double) bounds[3], (double) bounds[4], (double) bounds[5]);
					return VoxelShapes.create(aabb);
				}
			}
		}

		return super.getShape(state, world, pos, context);
	}

	@Override
	public VoxelShape getRaytraceShape(BlockState state, IBlockReader world, BlockPos pos)
	{
		if (world.getBlockState(pos).getBlock() == this)
		{
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof IAdvancedSelectionBounds)
			{
				List<AxisAlignedBB> bounds = ((IAdvancedSelectionBounds) te).getAdvancedSelectionBounds();
				if (bounds != null && !bounds.isEmpty())
				{
					VoxelShape ret = VoxelShapes.empty();
					Iterator var7 = bounds.iterator();

					while (var7.hasNext())
					{
						AxisAlignedBB aabb = (AxisAlignedBB) var7.next();
						if (aabb != null)
						{
							ret = VoxelShapes.combineAndSimplify(ret, VoxelShapes.create(aabb), IBooleanFunction.OR);
						}
					}

					return ret;
				}
			}
		}

		return super.getRaytraceShape(state, world, pos);
	}

	@Nullable
	@Override
	public RayTraceResult getRayTraceResult(BlockState state, World world, BlockPos pos, Vec3d start, Vec3d end, RayTraceResult original)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof IAdvancedSelectionBounds)
		{
			List<AxisAlignedBB> list = ((IAdvancedSelectionBounds) te).getAdvancedSelectionBounds();
			if (list != null && !list.isEmpty())
			{
				RayTraceResult min = null;
				double minDist = 1.0D / 0.0;
				Iterator var12 = list.iterator();

				while (var12.hasNext())
				{
					AxisAlignedBB aabb = (AxisAlignedBB) var12.next();
					BlockRayTraceResult mop = VoxelShapes.create(aabb.offset((double) (-pos.getX()), (double) (-pos.getY()), (double) (-pos.getZ()))).rayTrace(start, end, pos);
					if (mop != null)
					{
						double dist = mop.getHitVec().squareDistanceTo(start);
						if (dist < minDist)
						{
							min = mop;
							minDist = dist;
						}
					}
				}

				return min;
			}
		}

		return original;
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state)
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return te instanceof IComparatorOverride ? ((IComparatorOverride) te).getComparatorInputOverride() : 0;
	}

	@Override
	public int getWeakPower(BlockState blockState, IBlockReader world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		return te instanceof IRedstoneOutput ? ((IRedstoneOutput) te).getWeakRSOutput(blockState, side) : 0;
	}

	@Override
	public int getStrongPower(BlockState blockState, IBlockReader world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		return te instanceof IRedstoneOutput ? ((IRedstoneOutput) te).getStrongRSOutput(blockState, side) : 0;
	}

	@Override
	public boolean canProvidePower(BlockState state)
	{
		return true;
	}

	@Override
	public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side)
	{
		TileEntity te = world.getTileEntity(pos);
		return te instanceof IRedstoneOutput ? ((IRedstoneOutput) te).canConnectRedstone(state, side) : false;
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof QIBaseTileEntity)
		{
			((QIBaseTileEntity) te).onEntityCollision(world, entity);
		}

	}

	@Deprecated
	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
