package flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsAll;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration1;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import flaxbeard.questionablyimmersive.QuestionablyImmersive;
import flaxbeard.questionablyimmersive.common.QIContent;
import flaxbeard.questionablyimmersive.common.blocks.metal.BlockTypes_QIMetalMultiblock;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityCokeOvenBattery;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityMortar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockCokeOvenBattery implements IMultiblock
{
    public static class Slice extends MultiblockCokeOvenBattery {
        static ItemStack[][][] structure = new ItemStack[4][1][3];

        static{
            for(int h=0;h<4;h++)
                for(int w=0;w<3;w++)
                {
					if (w == 2)
					{
						structure[h][0][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
					}
					else if (w == 1 && h == 0)
					{
						structure[h][0][w] = new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
					}
					else if (w == 1 && h == 3)
					{
						structure[h][0][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if (w == 1)
					{
						structure[h][0][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}
					else if (h == 0 || h == 3)
					{
						structure[h][0][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else
					{
						structure[h][0][w] = new ItemStack(Blocks.PISTON);
					}
                }
        }

        @Override
        public ItemStack[][][] getStructureManual()
        {
            return structure;
        }
    }

	public static MultiblockCokeOvenBattery instance = new MultiblockCokeOvenBattery();
	static ItemStack[][][] structure = new ItemStack[4][6][3];

	static{
		for(int h=0;h<4;h++)
			for(int l=0;l<6;l++)
				for(int w=0;w<3;w++)
				{
					if (w == 2)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockSheetmetal,1,BlockTypes_MetalsAll.STEEL.getMeta());
					}
					else if (w == 1 && h == 0)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDevice1,1,BlockTypes_MetalDevice1.FLUID_PIPE.getMeta());
					}
					else if (w == 1 && h == 3)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else if (w == 1)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0,1,BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}
					else if (h == 0 || h == 3)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration1,1,BlockTypes_MetalDecoration1.STEEL_SCAFFOLDING_0.getMeta());
					}
					else
					{
						structure[h][l][w] = new ItemStack(Blocks.PISTON);
					}
				}
	}
	
	@Override
	public ItemStack[][][] getStructureManual()
	{
		return structure;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		if (iterator == 1)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("002100");
			return true;
		}
		else if (iterator == 16)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("001200");
			return true;
		}
		else if (iterator % 3 == 1 && iterator < 18)
		{
			ImmersiveEngineering.proxy.drawSpecificFluidPipe("001100");
			return true;
		}
		return false;
	}

	@Override
	public IBlockState getBlockstateFromStack(int index, ItemStack stack)
	{
		if(!stack.isEmpty() && stack.getItem() instanceof ItemBlock)
		{
			return ((ItemBlock)stack.getItem()).getBlock().getStateFromMeta(stack.getItemDamage());
		}
		return null;
	}

	@Override
	public float getManualScale()
	{
		return 12;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}
	
	Object te;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		if (te == null)
		{
			te = new TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent();
		}

		QuestionablyImmersive.proxy.renderTile((TileEntity) te);
	}

	@Override
	public String getUniqueName()
	{
		return "QI:CokeOvenBattery";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		return state.getBlock()==IEContent.blockMetalDecoration0 && (state.getBlock().getMetaFromState(state)==BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		side = side.getOpposite();
		if(side==EnumFacing.UP||side==EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		boolean mirror = false;
		int b = this.structureCheck(world, pos, side, mirror);
		if (b == -1)
		{
			mirror = true;
			b = structureCheck(world, pos, side, mirror);
		}
		if (b == -1)
			return false;

		for (int h = -1; h <= 2; h++)
            for (int w = -1; w <= 1; w++)
                for (int l = 0; l < b; l++)
            {
				int ww = mirror?-w:w;
				BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

				if (w == 0 && h == 0 && l == 0)
				{
					world.setBlockState(pos2, QIContent.blockMetalMultiblock.getStateFromMeta(
							BlockTypes_QIMetalMultiblock.COKE_OVEN_BATTERY_PARENT.getMeta()));
				}
				else if (w == 0 && h == 0 && l % 5 == 0)
				{
					world.setBlockState(pos2, QIContent.blockMetalMultiblock.getStateFromMeta(
							BlockTypes_QIMetalMultiblock.COKE_OVEN_BATTERY_RENDERED.getMeta()));
				}
				else
				{
					world.setBlockState(pos2, QIContent.blockMetalMultiblock.getStateFromMeta(
							BlockTypes_QIMetalMultiblock.COKE_OVEN_BATTERY.getMeta()));
				}
				TileEntity curr = world.getTileEntity(pos2);

				if (curr instanceof TileEntityCokeOvenBattery)
				{
					TileEntityCokeOvenBattery tile = (TileEntityCokeOvenBattery) curr;
					tile.facing = side;
					tile.formed = true;
					tile.pos = (h+1)*3 + (w+1);
					tile.offset = new int[] {
							(side == EnumFacing.WEST || side == EnumFacing.EAST ? 0 : side == EnumFacing.NORTH ? ww: -ww),
							h,
							(side == EnumFacing.NORTH || side == EnumFacing.SOUTH ? 0 : side == EnumFacing.EAST ? ww : -ww)
					};
					tile.offsetTotal = new int[] {
							(side == EnumFacing.WEST ? -l : side == EnumFacing.EAST ? l : side == EnumFacing.NORTH ? ww: -ww),
							h,
							(side == EnumFacing.NORTH ? -l : side == EnumFacing.SOUTH ? l : side == EnumFacing.EAST ? ww : -ww)
					};
					tile.ovenLength = b;
					tile.tank = new FluidTank(b * 6000);
					tile.ovenIndex = l;
					tile.mirrored = mirror;
					tile.markDirty();
					world.addBlockEvent(pos2, QIContent.blockMetalMultiblock, 255, 0);
				}
				if (curr instanceof TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent)
				{
					TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent tile = (TileEntityCokeOvenBattery.TileEntityCokeOvenBatteryParent) curr;
					tile.inventory = NonNullList.withSize(b + 2, ItemStack.EMPTY);
					tile.insertionHandlers = new IEInventoryHandler[b];
					for (int i = 0; i < b; i++)
					{
						boolean[] insert = new boolean[b + 2];
						insert[i + 2] = true;
						tile.insertionHandlers[i] = new IEInventoryHandler(b + 2, tile, 0, insert, new boolean[b]);
					}
					tile.process = new int[b];
					tile.processMax = new int[b];
					tile.active = new boolean[b];
					tile.recuperationTime = new int[b];

				}

			}

		return false;
	}

	private int structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
        for (int l = 0; l < 100; l++)
            for (int h = -1; h <= 2; h++)
                for (int w = -1; w <= 1; w++)
				{
					int ww = mirror?w:-w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);

					if (w == -1)
					{
						if (!Utils.isOreBlockAt(world, pos, "blockSheetmetalSteel"))
							return l >= 6 ? l : -1;
					}
					else if (w == 0 && h == -1)
					{
						if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDevice1, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()))
							return l >= 6 ? l : -1;
					}
					else if (w == 0 && h == 2)
					{
						if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
							return l >= 6 ? l : -1;
					}
					else if (w == 0)
					{
						if (!Utils.isBlockAt(world, pos, IEContent.blockMetalDecoration0, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()))
							return l >= 6 ? l : -1;
					}
					else if (h == 2 || h == -1)
					{
						if (!Utils.isOreBlockAt(world, pos, "scaffoldingSteel"))
							return l >= 6 ? l : -1;
					}
					else
					{
						if (!Utils.isBlockAt(world, pos, Blocks.PISTON, -1))
							return l >= 6 ? l : -1;
					}
				}
		return 100;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 18),
			new IngredientStack(new ItemStack(
					IEContent.blockMetalDevice1,
					6,
					BlockTypes_MetalDevice1.FLUID_PIPE.getMeta()
			)),
			new IngredientStack("blockSheetmetalSteel", 24),
			new IngredientStack(new ItemStack(
					IEContent.blockMetalDecoration0,
					12,
					BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()
			)),
			new IngredientStack(new ItemStack(
					Blocks.PISTON,
					12
			))
	};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}