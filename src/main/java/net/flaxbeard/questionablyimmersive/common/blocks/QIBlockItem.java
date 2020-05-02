//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.flaxbeard.questionablyimmersive.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class QIBlockItem extends BlockItem
{
	private int burnTime;

	public QIBlockItem(Block b, Properties props)
	{
		super(b, props);
	}

	public QIBlockItem(Block b)
	{
		this(b, (new Properties()).group(ImmersiveEngineering.itemGroup));
		this.setRegistryName(b.getRegistryName());
	}

	public String getTranslationKey(ItemStack stack)
	{
		return this.getBlock().getTranslationKey();
	}

	@OnlyIn(Dist.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced)
	{
		if (this.getBlock() instanceof QIBaseBlock)
		{
			QIBaseBlock ieBlock = (QIBaseBlock) this.getBlock();
			if (ieBlock.hasFlavour())
			{
				String flavourKey = "desc.immersiveengineering.flavour." + ieBlock.name;
				tooltip.add(new TranslationTextComponent(I18n.format(flavourKey, new Object[0]), new Object[0]));
			}
		}

		super.addInformation(stack, world, tooltip, advanced);
		if (ItemNBTHelper.hasKey(stack, "energyStorage"))
		{
			tooltip.add(new TranslationTextComponent("desc.immersiveengineering.info.energyStored", new Object[]{ItemNBTHelper.getInt(stack, "energyStorage")}));
		}

		if (ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if (fs != null)
			{
				tooltip.add(new TranslationTextComponent("desc.immersiveengineering.info.fluidStored", new Object[]{fs.getDisplayName(), fs.getAmount()}));
			}
		}

	}

	public QIBlockItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	public int getBurnTime(ItemStack itemStack)
	{
		return this.burnTime;
	}

	protected boolean placeBlock(BlockItemUseContext context, BlockState newState)
	{
		Block b = this.getBlock();
		if (b instanceof QIBaseBlock)
		{
			QIBaseBlock ieBlock = (QIBaseBlock) b;
			if (!ieBlock.canIEBlockBePlaced(newState, context))
			{
				return false;
			}
			else
			{
				boolean ret = super.placeBlock(context, newState);
				if (ret)
				{
					ieBlock.onIEBlockPlacedBy(context, newState);
				}

				return ret;
			}
		}
		else
		{
			return super.placeBlock(context, newState);
		}
	}

	public static class QIBlockItemNoInventory extends QIBlockItem
	{
		public QIBlockItemNoInventory(Block b, Properties props)
		{
			super(b, props);
		}

		@Nullable
		public CompoundNBT getShareTag(ItemStack stack)
		{
			CompoundNBT ret = super.getShareTag(stack);
			if (ret != null)
			{
				ret = ret.copy();
				ret.remove("inventory");
			}

			return ret;
		}
	}
}
