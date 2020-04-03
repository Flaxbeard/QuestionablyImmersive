package net.flaxbeard.questionablyimmersive.common.items;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.flaxbeard.questionablyimmersive.api.ICoordinateProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class PunchcardItem extends QIBaseItem implements ICoordinateProvider
{

	public PunchcardItem(boolean punched)
	{
		super(punched ? "punchcard_punched" : "punchcard", (new Item.Properties()).maxStackSize(punched ? 1 : 64), !punched);
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
	{
		if (ItemNBTHelper.hasKey(stack, "posX"))
		{
			int x = ItemNBTHelper.getInt(stack, "posX");
			int y = ItemNBTHelper.getInt(stack, "posY");
			int z = ItemNBTHelper.getInt(stack, "posZ");

			String dimNameStr = ItemNBTHelper.getString(stack, "dimension");
			ResourceLocation dimName = new ResourceLocation(dimNameStr);
			DimensionType dimension = DimensionType.byName(dimName);

			String worldName = dimension.getRegistryName().getPath();

			tooltip.add(new TranslationTextComponent("desc.questionablyimmersive.info.punchcard_pos", new Object[]{x, y, z, worldName}));
		}
		else
		{
			tooltip.add(new TranslationTextComponent("desc.questionablyimmersive.info.punchcard"));
		}
	}


	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		Hand hand = context.getHand();
		World world = context.getWorld();
		BlockPos pos = context.getPos();

		ItemStack stack = player.getHeldItem(hand);
		if (player.isSneaking() && !ItemNBTHelper.hasKey(stack, "posX"))
		{
			ItemStack punchedCard = new ItemStack(QIItems.Misc.punchcardPunched, 1);
			punchedCard.setCount(1);

			ItemNBTHelper.putInt(punchedCard, "posX", pos.getX());
			ItemNBTHelper.putInt(punchedCard, "posY", pos.getY());
			ItemNBTHelper.putInt(punchedCard, "posZ", pos.getZ());
			String dimNameStr = world.getDimension().getType().getRegistryName().toString();
			ItemNBTHelper.putString(punchedCard, "dimension", dimNameStr);

			stack.shrink(1);

			if (!player.inventory.addItemStackToInventory(punchedCard))
				player.dropItem(punchedCard, false);

			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}

	@Override
	public Vec3d getCoordinate(World world, ItemStack stack)
	{
		if (ItemNBTHelper.hasKey(stack, "posX"))
		{
			return new Vec3d(
					ItemNBTHelper.getInt(stack, "posX"),
					ItemNBTHelper.getInt(stack, "posY"),
					ItemNBTHelper.getInt(stack, "posZ")
			);
		}
		return null;
	}
}