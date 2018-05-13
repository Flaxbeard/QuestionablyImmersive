package flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import flaxbeard.questionablyimmersive.common.blocks.metal.BlockTypes_QIMetalDevice;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityExtruder;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityGauge;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import flaxbeard.questionablyimmersive.common.util.RadioHelper;
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

public class BlockQIMetalDevice extends BlockQITileProvider<BlockTypes_QIMetalDevice>
{
	public BlockQIMetalDevice()
	{
		super("metal_device", Material.IRON, PropertyEnum.create("type", BlockTypes_QIMetalDevice.class), ItemBlockMetalBlock.class, IEProperties.FACING_ALL);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		this.setNotNormalBlock(BlockTypes_QIMetalDevice.GAUGE.getMeta());

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
		switch(BlockTypes_QIMetalDevice.values()[meta])
		{
			case GAUGE:
				return new TileEntityGauge();
			case RADIO:
				return new TileEntityRadio();
			case EXTRUDER:
				return new TileEntityExtruder();
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
		int meta = this.getMetaFromState(state);
		switch(BlockTypes_QIMetalDevice.values()[meta])
		{
			case GAUGE:
				return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
			case RADIO:
				return EnumBlockRenderType.MODEL;
			case EXTRUDER:
				return EnumBlockRenderType.MODEL;
		}
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
		int meta = this.getMetaFromState(state);
		switch(BlockTypes_QIMetalDevice.values()[meta])
		{
			case GAUGE:
				TileEntity te = world.getTileEntity(pos);
				if (te != null && te instanceof TileEntityGauge)
				{
					switch (((TileEntityGauge) te).facing)
					{
						case DOWN:
							return new AxisAlignedBB(0.25f, 0, 0.25f, 0.75f, 0.0625f, 0.75f);
						case UP:
							return new AxisAlignedBB(0.25f, 0.9375f, 0.25f, 0.75f, 1, 0.75f);
						case EAST:
							return new AxisAlignedBB(0.9375f, 0.25f, 0.25f, 1, 0.75f, 0.75f);
						case WEST:
							return new AxisAlignedBB(0, 0.25f, 0.25f, 0.0625f, 0.75f, 0.75f);
						case SOUTH:
							return new AxisAlignedBB(0.25f, 0.25f, 0.9375f, 0.75f, 0.75f, 1);
						case NORTH:
							return new AxisAlignedBB(0.25f, 0.25f, 0, 0.75f, 0.75f, 0.0625f);
					}
				}
		}
		return new AxisAlignedBB(0,0,0,1,1,1);
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
	{
		if (this.getMetaFromState(blockState) == BlockTypes_QIMetalDevice.RADIO.getMeta())
		{
			TileEntity te = blockAccess.getTileEntity(pos);
			if (te instanceof TileEntityRadio)
			{
				TileEntityRadio radio = ((TileEntityRadio) te);
				return radio.receiveMode ? radio.power : 0;
			}
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
		if (this.getMetaFromState(state) == BlockTypes_QIMetalDevice.RADIO.getMeta())
		{
			return true;
		}
		return false;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		if (this.getMetaFromState(state) == BlockTypes_QIMetalDevice.RADIO.getMeta())
		{
			updatePower(world, pos);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		super.updateTick(world, pos, state, rand);
		if (this.getMetaFromState(state) == BlockTypes_QIMetalDevice.RADIO.getMeta())
		{
			updatePower(world, pos);
		}
	}

	private void updatePower(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		if (!world.isRemote && te instanceof TileEntityRadio)
		{
			TileEntityRadio radio = ((TileEntityRadio) te);
			if (!radio.receiveMode)
			{
				RadioHelper.getNetwork(world.provider.getDimension(), radio.frequency).updatePower(pos);
				System.out.println(RadioHelper.getNetwork(world.provider.getDimension(), radio.frequency));
			}
		}
	}

}