package flaxbeard.questionablyimmersive.common.items;

import blusunrize.immersiveengineering.common.items.IEItemInterfaces;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.questionablyimmersive.api.ICoordinateProvider;
import flaxbeard.questionablyimmersive.common.CommonProxy;
import flaxbeard.questionablyimmersive.common.blocks.tile.TileEntityRadio;
import flaxbeard.questionablyimmersive.common.util.RadioHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemPortableRadio extends ItemQIBase implements IEItemInterfaces.IGuiItem, ICoordinateProvider
{

	public ItemPortableRadio(String name)
	{
		super(name, 1);
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add("Tuned to " + getFrequency(stack) + " MHz");
		if (worldIn != null)
		{
			tooltip.add("Current power " + RadioHelper.getPower(worldIn.provider.getDimension(), getFrequency(stack)) + "/15");
			RadioHelper.RadioNetwork net = RadioHelper.getNetwork(worldIn.provider.getDimension(), getFrequency(stack));

			Vec3d targetLocation = net.getTargetLocation();

			if (targetLocation != null)
			{
				tooltip.add("Aimed at "
						+ "x: " + Math.round(targetLocation.x)
						+ " y: " + Math.round(targetLocation.y)
						+ " z: " + Math.round(targetLocation.z)
				);
			}
		}
	}


	@Override
	public int getGuiID(ItemStack stack)
	{
		return 64;
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
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);

		if (player.isSneaking())
		{
			if (world.isRemote)
				CommonProxy.openGuiForItem(player, hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND);
		}
		else
		{
			if (!world.isRemote)
			{
				Vec3d look = player.getPositionEyes(1F);
				RayTraceResult traceResult = world.rayTraceBlocks(
						look,
						look.add(player.getLookVec().scale(100))
				);
				if (traceResult != null && traceResult.typeOfHit != RayTraceResult.Type.MISS)
				{
					//System.out.println(traceResult.hitVec);
					RadioHelper.setTargetLocation(world, getFrequency(stack), traceResult.hitVec);
				}


				RadioHelper.togglePortableRadio(world, getFrequency(stack));
			}
		}

		return new ActionResult(EnumActionResult.SUCCESS, stack);
	}

	public static int getFrequency(ItemStack stack)
	{
		if (stack.getItem() instanceof ItemPortableRadio)
		{
			if (ItemNBTHelper.hasKey(stack, "frequency"))
			{
				return ItemNBTHelper.getInt(stack,"frequency");
			}
		}
		return 0;
	}

	public static void setFrequency(ItemStack stack, int station)
	{
		if (stack.getItem() instanceof ItemPortableRadio)
		{
			ItemNBTHelper.setInt(stack, "frequency", station);
		}
	}

	@Override
	public Vec3d getCoordinate(World world, ItemStack stack)
	{
		RadioHelper.RadioNetwork net = RadioHelper.getNetwork(world.provider.getDimension(), getFrequency(stack));

		return net.getTargetLocation();
	}
}
