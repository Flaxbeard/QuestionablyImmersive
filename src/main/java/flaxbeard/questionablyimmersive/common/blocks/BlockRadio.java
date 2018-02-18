package flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import flaxbeard.questionablyimmersive.common.blocks.metal.BlockTypes_Radios;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockRadio extends BlockQETileProvider<BlockTypes_Radios>
{
	public BlockRadio()
	{
		super("radio", Material.IRON, PropertyEnum.create("type", BlockTypes_Radios.class), ItemBlockQEBase.class, IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		this.setNotNormalBlock(BlockTypes_Radios.RADIO.getMeta());

	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}

	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		return null;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		state = super.getActualState(state, world, pos);
		return state;
	}


	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch (BlockTypes_Radios.values()[meta])
		{
			case RADIO:
				return new TileEntityRadio();
		}
		return null;
	}

	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

	@Override
	public boolean canIEBlockBePlaced(World world, BlockPos pos, IBlockState newState, EnumFacing side, float hitX, float hitY, float hitZ, EntityPlayer player, ItemStack stack)
	{
		return true;
	}

	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
		TileEntity te = blockAccess.getTileEntity(pos);
		if (te instanceof TileEntityRadio)
		{
			TileEntityRadio radio = ((TileEntityRadio) te);
			return radio.receiveMode ? radio.power : 0;
		}
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
		return getWeakPower(blockState, blockAccess, pos, side);
	}

	@Override
	public boolean canProvidePower(IBlockState state)
	{
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		updatePower(world, pos);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		super.updateTick(world, pos, state, rand);
		updatePower(world, pos);
	}

	private void updatePower(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityRadio)
		{
			TileEntityRadio radio = ((TileEntityRadio) te);
			if (!radio.receiveMode && radio.network != null)
			{
				radio.network.updatePower(pos);
			}
		}
	}
}