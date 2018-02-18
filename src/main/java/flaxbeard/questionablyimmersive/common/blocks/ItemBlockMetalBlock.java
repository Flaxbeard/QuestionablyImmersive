package flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.questionablyimmersive.common.blocks.metal.TileEntityRadio;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemBlockMetalBlock extends ItemBlockQIBase
{
	public ItemBlockMetalBlock(Block b)
	{
		super(b);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		TileEntity te = world.getTileEntity(pos);
		if (player.isSneaking() && te instanceof TileEntityRadio)
		{
			int frequency = ((TileEntityRadio) te).frequency;
			if (frequency == 0)
			{
				ItemNBTHelper.remove(stack, "frequency");
			}
			else
			{
				ItemNBTHelper.setInt(stack, "frequency", frequency);
			}
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag advInfo)
	{
		super.addInformation(stack, worldIn, list, advInfo);

		if (stack.getMetadata() == 1)
		{
			if (ItemNBTHelper.hasKey(stack, "broadcast"))
			{
				list.add("Broadcast Mode");
			}
			else
			{
				list.add("Receiver Mode");
			}
			int frequency = 0;
			if (ItemNBTHelper.hasKey(stack, "frequency"))
			{
				frequency = ItemNBTHelper.getInt(stack, "frequency");
			}
			list.add("Tuned to " + frequency + "MHz");
		}
	}
}
