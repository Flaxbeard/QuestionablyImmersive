package net.flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import net.flaxbeard.questionablyimmersive.common.blocks.metal.TriphammerTileEntity;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class TriphammerAnvilBlock extends QIGenericTileBlock
{
	private final Block original;

	public TriphammerAnvilBlock(String name, Supplier<TileEntityType<?>> tileType, Block original)
	{
		super(name, tileType, Properties.create(Material.ANVIL, MaterialColor.IRON).hardnessAndResistance(3.0F, 1200.F).sound(SoundType.ANVIL), null, new IProperty[]{IEProperties.FACING_HORIZONTAL});
		this.original = original;
	}

	@Nullable
	public static BlockState damage(BlockState state) {
		Block block = state.getBlock();
		if (block == QIBlocks.Metal.TRIPHAMMER_ANVIL) {
			return QIBlocks.Metal.TRIPHAMMER_ANVIL_CHIPPED.getDefaultState().with(IEProperties.FACING_HORIZONTAL, state.get(IEProperties.FACING_HORIZONTAL));
		} else if (block == QIBlocks.Metal.TRIPHAMMER_ANVIL_CHIPPED) {
			return QIBlocks.Metal.TRIPHAMMER_ANVIL_DAMAGED.getDefaultState().with(IEProperties.FACING_HORIZONTAL, state.get(IEProperties.FACING_HORIZONTAL));
		} else {
			return null;
		}
	}

	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, this.tickRate(worldIn));
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if (!worldIn.isRemote) {
			this.checkFallable(worldIn, pos);
		}
	}

	private void checkFallable(World worldIn, BlockPos pos) {
		if (worldIn.isAirBlock(pos.down()) || canFallThrough(worldIn.getBlockState(pos.down())) && pos.getY() >= 0) {
			if (!worldIn.isRemote) {
				FallingBlockEntity fallingblockentity = new FallingBlockEntity(worldIn, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, toOriginal(worldIn.getBlockState(pos)));
				this.onStartFalling(fallingblockentity);
				worldIn.addEntity(fallingblockentity);
				worldIn.setBlockState(pos, toOriginal(worldIn.getBlockState(pos)));
			}

		}
	}

	public BlockState toOriginal(BlockState state) {
		Direction facing = state.get(IEProperties.FACING_HORIZONTAL);
		return this.original.getDefaultState().with(AnvilBlock.FACING, facing);
	}

	@Override
	public int tickRate(IWorldReader worldIn) {
		return 2;
	}

	public static boolean canFallThrough(BlockState state) {
		Block block = state.getBlock();
		Material material = state.getMaterial();
		return state.isAir() || block == Blocks.FIRE || material.isLiquid() || material.isReplaceable();
	}

	protected void onStartFalling(FallingBlockEntity fallingEntity) {
		fallingEntity.setHurtEntities(true);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(IEProperties.FACING_HORIZONTAL, context.getPlacementHorizontalFacing().rotateY());
	}

	private static final VoxelShape PART_BASE = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 4.0D, 14.0D);
	private static final VoxelShape PART_LOWER_X = Block.makeCuboidShape(3.0D, 4.0D, 4.0D, 13.0D, 5.0D, 12.0D);
	private static final VoxelShape PART_MID_X = Block.makeCuboidShape(4.0D, 5.0D, 6.0D, 12.0D, 10.0D, 10.0D);
	private static final VoxelShape PART_UPPER_X = Block.makeCuboidShape(0.0D, 10.0D, 3.0D, 16.0D, 16.0D, 13.0D);
	private static final VoxelShape PART_LOWER_Z = Block.makeCuboidShape(4.0D, 4.0D, 3.0D, 12.0D, 5.0D, 13.0D);
	private static final VoxelShape PART_MID_Z = Block.makeCuboidShape(6.0D, 5.0D, 4.0D, 10.0D, 10.0D, 12.0D);
	private static final VoxelShape PART_UPPER_Z = Block.makeCuboidShape(3.0D, 10.0D, 0.0D, 13.0D, 16.0D, 16.0D);
	private static final VoxelShape X_AXIS_AABB = VoxelShapes.or(PART_BASE, PART_LOWER_X, PART_MID_X, PART_UPPER_X);
	private static final VoxelShape Z_AXIS_AABB = VoxelShapes.or(PART_BASE, PART_LOWER_Z, PART_MID_Z, PART_UPPER_Z);

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		Direction direction = state.get(IEProperties.FACING_HORIZONTAL);
		return direction.getAxis() == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(IEProperties.FACING_HORIZONTAL, rot.rotate(state.get(IEProperties.FACING_HORIZONTAL)));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(IEProperties.FACING_HORIZONTAL);
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		TileEntity tile = world.getTileEntity(pos.add(0, 1, 0));
		if (tile instanceof TriphammerTileEntity && hand == Hand.MAIN_HAND && !player.isSneaking()) {
			QIBlockInterfaces.IInteractionObjectQI interaction = (QIBlockInterfaces.IInteractionObjectQI) tile;

			interaction = interaction.getGuiMaster();
			if (interaction != null && interaction.canUseGui(player) && !world.isRemote)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, interaction, ((TileEntity) interaction).getPos());
			}
		}
		return false;
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		return new ItemStack(original);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.MODEL;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		return toOriginal(state).getDrops(builder);
	}
}
