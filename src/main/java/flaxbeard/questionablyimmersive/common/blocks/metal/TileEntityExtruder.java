package flaxbeard.questionablyimmersive.common.blocks.metal;

import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class TileEntityExtruder extends TileEntityIEBase implements IDirectionalTile, ITickable, IEBlockInterfaces.IPlayerInteraction
{
	public EnumFacing facing = EnumFacing.NORTH;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = EnumFacing.getFront(nbt.getInteger("facing"));

		if (descPacket)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("facing", facing.ordinal());
	}

	@Override
	public EnumFacing getFacing()
	{
		return facing;
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		this.facing = facing;
	}

	@Override
	public int getFacingLimitation()
	{
		return 1;
	}

	@Override
	public boolean mirrorFacingOnPlacement(EntityLivingBase placer)
	{
		return false;
	}

	@Override
	public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean canRotate(EnumFacing axis)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared() * IEConfig.increasedTileRenderdistance;
	}

	@Override
	public void update()
	{

	}

	private boolean doMove(boolean extending)
	{
		if (!extending)
		{
			world.setBlockToAir(pos.offset(facing));
		}

		BlockPistonStructureHelper structureHelper = new BlockPistonStructureHelper(world, pos, facing, extending);

		if (!structureHelper.canMove())
		{
			return false;
		}
		else
		{
			List<BlockPos> list = structureHelper.getBlocksToMove();
			List<IBlockState> list1 = Lists.<IBlockState>newArrayList();

			for (int i = 0; i < list.size(); ++i)
			{
				BlockPos blockpos = list.get(i);
				list1.add(world.getBlockState(blockpos).getActualState(world, blockpos));
			}

			List<BlockPos> list2 = structureHelper.getBlocksToDestroy();
			int k = list.size() + list2.size();
			IBlockState[] aiblockstate = new IBlockState[k];
			EnumFacing enumfacing = extending ? facing : facing.getOpposite();

			for (int j = list2.size() - 1; j >= 0; --j)
			{
				BlockPos blockpos1 = list2.get(j);
				IBlockState iblockstate = world.getBlockState(blockpos1);
				// Forge: With our change to how snowballs are dropped this needs to disallow to mimic vanilla behavior.
				float chance = iblockstate.getBlock() instanceof BlockSnow ? -1.0f : 1.0f;
				iblockstate.getBlock().dropBlockAsItemWithChance(world, blockpos1, iblockstate, chance, 0);
				world.setBlockState(blockpos1, Blocks.AIR.getDefaultState(), 4);
				--k;
				aiblockstate[k] = iblockstate;
			}

			for (int l = list.size() - 1; l >= 0; --l)
			{
				BlockPos blockpos3 = list.get(l);
				IBlockState iblockstate2 = world.getBlockState(blockpos3);
				world.setBlockState(blockpos3, Blocks.AIR.getDefaultState(), 2);
				blockpos3 = blockpos3.offset(enumfacing);
				world.setBlockState(blockpos3, Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockDirectional.FACING, facing), 4);
				world.setTileEntity(blockpos3, BlockPistonMoving.createTilePiston(list1.get(l), facing, extending, false));
				--k;
				aiblockstate[k] = iblockstate2;
			}

			BlockPos pushPos = pos.offset(facing);

			if (extending)
			{
				BlockPistonExtension.EnumPistonType blockpistonextension$enumpistontype = BlockPistonExtension.EnumPistonType.DEFAULT;
				IBlockState iblockstate3 = Blocks.AIR.getDefaultState();
				IBlockState iblockstate1 = Blocks.PISTON_EXTENSION.getDefaultState().withProperty(BlockPistonMoving.FACING, facing);
				world.setBlockState(pushPos, iblockstate1, 4);
				world.setTileEntity(pushPos, BlockPistonMoving.createTilePiston(iblockstate3, facing, true, false));
			}

			for (int i1 = list2.size() - 1; i1 >= 0; --i1)
			{
				world.notifyNeighborsOfStateChange(list2.get(i1), aiblockstate[k++].getBlock(), false);
			}

			for (int j1 = list.size() - 1; j1 >= 0; --j1)
			{
				world.notifyNeighborsOfStateChange(list.get(j1), aiblockstate[k++].getBlock(), false);
			}

			if (extending)
			{
				world.notifyNeighborsOfStateChange(pushPos, Blocks.PISTON_HEAD, false);
			}

			return true;
		}
	}

	@Override
	public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if (!world.isRemote)
		{
			FakePlayer fakePlayer = QuestionablyImmersive.proxy.getFakePlayer(world);
			ItemStack stack =  new ItemStack(Item.getItemFromBlock(Blocks.PISTON));
			fakePlayer.setHeldItem(EnumHand.MAIN_HAND, stack);
			EnumActionResult result = ForgeHooks.onPlaceItemIntoWorld(stack, fakePlayer, world, getPos().offset(facing), facing.getOpposite(), 0, 0, 0, EnumHand.MAIN_HAND);
			return true;
		}
		/*if (!Utils.isHammer(player.getHeldItem(hand)))
		{
			doMove(true);
			return true;
		}*/
		return false;
	}
}