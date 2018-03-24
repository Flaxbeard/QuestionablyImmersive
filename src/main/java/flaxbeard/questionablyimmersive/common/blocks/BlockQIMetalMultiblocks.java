package flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import flaxbeard.questionablyimmersive.common.blocks.metal.BlockTypes_QIMetalMultiblock;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.Properties;

public class BlockQIMetalMultiblocks extends BlockQIMultiblock<BlockTypes_QIMetalMultiblock>
{
	public BlockQIMetalMultiblocks()
	{
		super("metal_multiblock",Material.IRON, PropertyEnum.create("type", BlockTypes_QIMetalMultiblock.class), ItemBlockQIBase.class, IEProperties.DYNAMICRENDER,IEProperties.BOOLEANS[0],Properties.AnimationProperty,IEProperties.OBJ_TEXTURE_REMAP);
		setHardness(3.0F);
		setResistance(15.0F);
		this.setAllNotNormalBlock();
		lightOpacity = 0;
	}

	@Override
	public boolean useCustomStateMapper()
	{
		return true;
	}
	@Override
	public String getCustomStateMapping(int meta, boolean itemBlock)
	{
		if(BlockTypes_QIMetalMultiblock.values()[meta].needsCustomState())
			return BlockTypes_QIMetalMultiblock.values()[meta].getCustomState();
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		switch(BlockTypes_QIMetalMultiblock.values()[meta])
		{
			case MORTAR:
				return new TileEntityMortar();
			case MORTAR_PARENT:
				return new TileEntityMortar.TileEntityMortarParent();
			case COKE_OVEN_BATTERY:
				return new TileEntityCokeOvenBattery();
			case COKE_OVEN_BATTERY_PARENT:
				return new TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent();
		}
		return null;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileEntityMultiblockPart)
		{
			TileEntityMultiblockPart tile = (TileEntityMultiblockPart)te;
			if(tile instanceof TileEntityMultiblockMetal && ((TileEntityMultiblockMetal) tile).isRedstonePos())
				return true;

		}
		return super.isSideSolid(state, world, pos, side);
	}



	@Override
	public boolean allowHammerHarvest(IBlockState state)
	{
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
	{
		super.onEntityCollidedWithBlock(worldIn, pos, state, entityIn);
		if (entityIn instanceof EntityLivingBase&&!((EntityLivingBase) entityIn).isOnLadder() && isLadder(state, worldIn, pos, (EntityLivingBase)entityIn))
		{
			float f5 = 0.15F;
			if (entityIn.motionX < -f5)
				entityIn.motionX = -f5;
			if (entityIn.motionX > f5)
				entityIn.motionX = f5;
			if (entityIn.motionZ < -f5)
				entityIn.motionZ = -f5;
			if (entityIn.motionZ > f5)
				entityIn.motionZ = f5;

			entityIn.fallDistance = 0.0F;
			if (entityIn.motionY < -0.15D)
				entityIn.motionY = -0.15D;

			if(entityIn.motionY<0 && entityIn instanceof EntityPlayer && entityIn.isSneaking())
			{
				entityIn.motionY=.05;
				return;
			}
			if(entityIn.isCollidedHorizontally)
				entityIn.motionY=.2;
		}
	}

	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}