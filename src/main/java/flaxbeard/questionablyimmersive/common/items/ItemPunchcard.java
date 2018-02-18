package flaxbeard.questionablyimmersive.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPunchcard extends ItemQIBase
{

	public ItemPunchcard(String name)
	{
		super(name, 64, "unpunched", "punched");
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (ItemNBTHelper.hasKey(stack, "posX"))
		{
			tooltip.add(TextFormatting.GRAY
					+ "x: " + ItemNBTHelper.getInt(stack, "posX")
					+ " y: " + ItemNBTHelper.getInt(stack, "posY")
					+ " z: " + ItemNBTHelper.getInt(stack, "posZ")
					+ " " + ItemNBTHelper.getString(stack, "worldName")
			);
		}
		else
			tooltip.add(I18n.format(Lib.DESC_FLAVOUR + "drill.empty"));
	}


	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (player.isSneaking() && !ItemNBTHelper.hasKey(stack, "posX"))
		{
			ItemStack punchedCard = new ItemStack(this, 1, 1);
			punchedCard.setCount(1);

			ItemNBTHelper.setInt(punchedCard, "posX", pos.getX());
			ItemNBTHelper.setInt(punchedCard, "posY", pos.getY());
			ItemNBTHelper.setInt(punchedCard, "posZ", pos.getZ());
			ItemNBTHelper.setInt(punchedCard, "world", world.provider.getDimension());
			String[] words = world.provider.getDimensionType().getName().split(" ");
			StringBuilder name = new StringBuilder();
			for (int i = 0; i < words.length; i++)
			{
				String word = words[i];
				name.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
				if (i != words.length - 1)
				{
					name.append(" ");
				}
			}
			ItemNBTHelper.setString(punchedCard, "worldName", name.toString());

			stack.shrink(1);

			if (!player.inventory.addItemStackToInventory(punchedCard))
				player.dropItem(punchedCard, false);

			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}
}
