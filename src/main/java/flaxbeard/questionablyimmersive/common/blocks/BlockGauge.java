package flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import flaxbeard.questionablyimmersive.common.blocks.metal.*;
import flaxbeard.questionablyimmersive.common.blocks.metal.BlockTypes_Gauges;
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

public class BlockGauge extends BlockQETileProvider<BlockTypes_Gauges>
{
	public BlockGauge()
	{
		super("gauge",Material.IRON, PropertyEnum.create("type", BlockTypes_Gauges.class), ItemBlockQEBase.class, IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE);
		setHardness(3.0F);
		setResistance(15.0F);
		lightOpacity = 0;
		this.setNotNormalBlock(BlockTypes_Gauges.GAUGE.getMeta());

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
		switch(BlockTypes_Gauges.values()[meta])
		{
			case GAUGE:
				return new TileEntityGauge();
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
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
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
		return new AxisAlignedBB(0,0,0,1,1,1);
	}

}