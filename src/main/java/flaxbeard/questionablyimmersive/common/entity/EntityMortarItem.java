package flaxbeard.questionablyimmersive.common.entity;

import flaxbeard.questionablyimmersive.common.util.Pair;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class EntityMortarItem extends EntityItem
{
	private boolean goingUp;
	private double xTarget;
	private double zTarget;
	private double lastY;
	private float momentum;
	private boolean goingDown;
	private double motionXConst;
	private double motionZConst;
	private boolean weapon;

	public EntityMortarItem(World worldIn)
	{
		super(worldIn);
		setRenderDistanceWeight(2);
	}

	public EntityMortarItem(World worldIn, double x, double y, double z, ItemStack stack, double xTarget, double zTarget)
	{
		this(worldIn, x, y, z, stack, xTarget, zTarget, 0, 0);
		setRenderDistanceWeight(2);
	}

	public EntityMortarItem(World worldIn, double x, double y, double z, ItemStack stack, double xTarget, double zTarget, double motionX, double motionZ)
	{
		this(worldIn, x, y, z, stack, xTarget, zTarget, motionX, motionZ, false);
	}

	public EntityMortarItem(World worldIn, double x, double y, double z, ItemStack stack, double xTarget, double zTarget, double motionX, double motionZ, boolean weapon)
	{
		super(worldIn, x, y, z, stack);
		this.xTarget = xTarget;
		this.zTarget = zTarget;
		this.lastY = 0;
		this.goingUp = true;
		this.goingDown = false;
		this.momentum = 4;
		this.motionXConst = motionX;
		this.motionZConst = motionZ;
		this.weapon = weapon;
		setPickupDelay(20);
	}


	@Override
	public void writeEntityToNBT(NBTTagCompound compound)
	{
		super.writeEntityToNBT(compound);
		compound.setDouble("xTarget", xTarget);
		compound.setDouble("zTarget", zTarget);
		compound.setBoolean("goingUp", goingUp);
		compound.setBoolean("goingDown", goingDown);
		compound.setFloat("momentum", momentum);
		compound.setDouble("lastY", lastY);
		compound.setDouble("motionXConst", motionXConst);
		compound.setDouble("motionZConst", motionZConst);
		compound.setBoolean("weapon", weapon);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound)
	{
		super.readEntityFromNBT(compound);
		xTarget = compound.getDouble("xTarget");
		zTarget = compound.getDouble("zTarget");
		goingUp = compound.getBoolean("goingUp");
		goingDown = compound.getBoolean("goingDown");
		lastY = compound.getDouble("lastY");
		momentum = compound.getFloat("momentum");
		motionXConst = compound.getDouble("motionXConst");
		motionZConst = compound.getDouble("motionZConst");
		weapon = compound.getBoolean("weapon");
	}

	public void setGoingDown()
	{
		if (weapon)
		{
			IMortarWeaponHandler handler = getHandler(this.getItem());
			if (handler != null)
			{
				handler.handleItem(getItem(), world, xTarget, world.getHeight(), zTarget);
				setDead();
			}
			else
			{
				setPosition(xTarget + (world.rand.nextFloat() * 4f) - 2f, world.getHeight(), zTarget + (world.rand.nextFloat() * 4f) - 2f);
				motionX = motionY = motionZ = 0;
				motionY = -3F;
				momentum = 4f;
				goingDown = true;
				goingUp = false;
			}
		}
		else
		{
			setPosition(xTarget, world.getHeight(), zTarget);
			motionX = motionY = motionZ = 0;
			motionY = -3F;
			momentum = 4f;
			goingDown = true;
			goingUp = false;
		}
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if (goingUp)
		{
			if (posY <= lastY)
			{
				goingUp = false;
			}
			motionY = 6;
			motionX = motionXConst;
			motionZ = motionZConst;

			if (!world.isRemote)
			{
				if (posY > world.getHeight() + 10)
				{
					setGoingDown();
				}
			}
		}
		else if (goingDown)
		{
			if (posY >= lastY)
			{
				goingDown = false;
			}
			motionY = Math.min(-3, motionY);
		}

		if (!world.isRemote && momentum > 0)
		{
			breakBlocks();
		}
		lastY = posY;
	}

	private static Map<Pair<Item, Integer>, IMortarWeaponHandler> map = new HashMap<>();

	public static void registerHandler(Item item, int meta, IMortarWeaponHandler handler)
	{
		map.put(new Pair<>(item, meta), handler);
	}

	private IMortarWeaponHandler getHandler(ItemStack item)
	{
		Pair Pair = new Pair<>(item.getItem(), item.getItemDamage());
		if (map.containsKey(Pair))
		{
			return map.get(Pair);
		}


		Pair generic = new Pair<>(item.getItem(), -1);
		if (map.containsKey(generic))
		{
			return map.get(generic);
		}

		return null;
	}

	private void breakBlocks()
	{
		for (int i = 0; i < 2; i++)
		{
			float momentumLoss = 0;
			for (int xO = -1; xO <= 1; xO++)
			{
				for (int zO = -1; zO <= 1; zO++)
				{
					double yPos = motionY > 0 ? Math.ceil(posY + i) : Math.floor(posY - i);
					BlockPos glassPos = new BlockPos(Math.floor(posX + xO * .3f), yPos, Math.floor(posZ + zO * .3f));
					if (momentum >= 1F && world.getBlockState(glassPos).getMaterial() == Material.GLASS)
					{
						world.destroyBlock(glassPos, true);
						momentumLoss = Math.max(momentumLoss, 1F);
					}
					else if (momentum >= .5F && world.getBlockState(glassPos).getMaterial() == Material.LEAVES)
					{
						world.destroyBlock(glassPos, true);
						momentumLoss = Math.max(momentumLoss, .5F);
					}
				}
			}
			momentum -= momentumLoss;
			if (momentum <= 0)
			{
				return;
			}
		}
	}
}
