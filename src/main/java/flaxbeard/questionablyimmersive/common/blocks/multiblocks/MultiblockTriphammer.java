package flaxbeard.questionablyimmersive.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDecoration0;
import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDecoration;
import blusunrize.immersiveengineering.common.util.Utils;
import flaxbeard.questionablyimmersive.common.QIContent;
import flaxbeard.questionablyimmersive.common.blocks.metal.BlockTypes_QIMetalMultiblock;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityTriphammer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockTriphammer implements IMultiblock
{
	public static MultiblockTriphammer instance = new MultiblockTriphammer();
	static ItemStack[][][] structure = new ItemStack[2][4][3];

	static
	{
		for (int h = 0; h < 2; h++)
			for (int l = 0; l < 4; l++)
				for (int w = 0; w < 3; w++)
				{
					if (w == 0 && l == 0)
					{
						structure[h][l][w] = new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta());
					}
					else if (l == 3 && h == 1 && w == 1)
					{
						structure[h][l][w] = new ItemStack(Blocks.IRON_BLOCK);
					}
					else if ((h == 1 && w == 1) || (w != 1 && l == 1))
					{
						structure[h][l][w] = new ItemStack(IEContent.blockWoodenDecoration, 1, BlockTypes_WoodenDecoration.FENCE.getMeta());
					}

					if (structure[h][l][w] == null)
					{
						structure[h][l][w] = ItemStack.EMPTY;
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

		return false;
	}

	@Override
	public IBlockState getBlockstateFromStack(int index, ItemStack stack)
	{
		if (!stack.isEmpty() && stack.getItem() instanceof ItemBlock)
		{
			return ((ItemBlock) stack.getItem()).getBlock().getStateFromMeta(stack.getItemDamage());
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

	}

	@Override
	public String getUniqueName()
	{
		return "QI:Triphammer";
	}

	@Override
	public boolean isBlockTrigger(IBlockState state)
	{
		System.out.println("E");
		return state.getBlock() == IEContent.blockWoodenDecoration && (state.getBlock().getMetaFromState(state) == BlockTypes_WoodenDecoration.FENCE.getMeta());
	}

	@Override
	public boolean createStructure(World world, BlockPos pos, EnumFacing side, EntityPlayer player)
	{
		side = side.getOpposite();
		if (side == EnumFacing.UP || side == EnumFacing.DOWN)
			side = EnumFacing.fromAngle(player.rotationYaw);

		boolean mirror = false;
		boolean b = this.structureCheck(world, pos, side, mirror);
		if (!b)
		{
			mirror = true;
			b = structureCheck(world, pos, side, mirror);
		}
		if (!b)
			return false;
		if (b)
			for (int h = -1; h <= 0; h++)
				for (int l = 0; l <= 3; l++)
					for (int w = -1; w <= 1; w++)
					{
						int ww = mirror ? -w : w;
						BlockPos pos2 = pos.offset(side, l).offset(side.rotateY(), ww).add(0, h, 0);

						if (((h == -1 && ((w != -1 || l != 0) && (l != 1 || w == 0))))
								|| (h == 0 && w != 0 && ((l != 1) && (l != 0 || w != -1))))
						{
							continue;
						}

						if (l == 0 && w == 0 && h == 0)
						{
							world.setBlockState(pos2, QIContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_QIMetalMultiblock.TRIPHAMMER_PARENT.getMeta()));
						}
						else
						{
							world.setBlockState(pos2, QIContent.blockMetalMultiblock.getStateFromMeta(BlockTypes_QIMetalMultiblock.TRIPHAMMER.getMeta()));
						}
						TileEntity curr = world.getTileEntity(pos2);
						if (curr instanceof TileEntityTriphammer)
						{
							TileEntityTriphammer tile = (TileEntityTriphammer) curr;
							tile.facing = side;
							tile.formed = true;
							tile.pos = (h + 1) * 12 + (l) * 3 + (w + 1);
							tile.offset = new int[]{(side == EnumFacing.WEST ? -l : side == EnumFacing.EAST ? l : side == EnumFacing.NORTH ? ww : -ww), h, (side == EnumFacing.NORTH ? -l : side == EnumFacing.SOUTH ? l : side == EnumFacing.EAST ? ww : -ww)};
							tile.mirrored = mirror;
							tile.markDirty();
							world.addBlockEvent(pos2, QIContent.blockMetalMultiblock, 255, 0);
						}
					}

		return false;
	}

	boolean structureCheck(World world, BlockPos startPos, EnumFacing dir, boolean mirror)
	{
		for (int h = -1; h <= 0; h++)
			for (int l = 0; l <= 3; l++)
				for (int w = -1; w <= 1; w++)
				{
					int ww = mirror ? -w : w;
					BlockPos pos = startPos.offset(dir, l).offset(dir.rotateY(), ww).add(0, h, 0);

					if (l == 0 && w == -1)
					{
						if (!Utils.isBlockAt(
								world,
								pos,
								IEContent.blockMetalDecoration0,
								BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta()
						))
						{
							return false;
						}
					}
					else if (h == 0 && l == 3 && w == 0)
					{
						if (!Utils.isOreBlockAt(world, pos, "blockIron"))
							return false;
					}
					else if ((w == 0 && h == 0) || (w != 0 && l == 1))
					{
						if (!Utils.isOreBlockAt(world, pos, "fenceTreatedWood"))
							return false;
					}

				}
		return true;
	}

	static final IngredientStack[] materials = new IngredientStack[]{
			new IngredientStack("scaffoldingSteel", 11),
			new IngredientStack("fenceTreatedWood", 6),
			new IngredientStack(new ItemStack(IEContent.blockMetalDevice1, 4, BlockTypes_MetalDevice1.FLUID_PIPE.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 1, BlockTypes_MetalDecoration0.RS_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.LIGHT_ENGINEERING.getMeta())),
			new IngredientStack(new ItemStack(IEContent.blockMetalDecoration0, 2, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING.getMeta())),
			new IngredientStack("blockSteel", 2),
			new IngredientStack("blockSheetmetalSteel", 4)};

	@Override
	public IngredientStack[] getTotalMaterials()
	{
		return materials;
	}
}