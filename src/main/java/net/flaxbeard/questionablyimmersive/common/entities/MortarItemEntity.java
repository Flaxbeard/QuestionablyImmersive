package net.flaxbeard.questionablyimmersive.common.entities;

import net.flaxbeard.questionablyimmersive.QuestionablyImmersive;
import net.flaxbeard.questionablyimmersive.api.IMortarWeaponHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class MortarItemEntity extends ItemEntity implements IRendersAsItem
{
	public static final EntityType<MortarItemEntity> TYPE = EntityType.Builder
			.<MortarItemEntity>create(MortarItemEntity::new, EntityClassification.MISC)
			.size(.25F, .25F)
			.build(QuestionablyImmersive.MODID + ":mortar_item");

	static
	{
		TYPE.setRegistryName(QuestionablyImmersive.MODID, "mortar_item");
	}

	private boolean goingUp;
	private double xTarget;
	private double zTarget;
	private double lastY;
	private float momentum;
	private boolean goingDown;
	private double motionXConst;
	private double motionZConst;
	private boolean weapon;

	public MortarItemEntity(EntityType<MortarItemEntity> type, World worldIn)
	{
		super(type, worldIn);
		setRenderDistanceWeight(2);
	}

	public MortarItemEntity(World worldIn, double x, double y, double z, ItemStack stack, double xTarget, double zTarget)
	{
		this(worldIn, x, y, z, stack, xTarget, zTarget, 0, 0);
		setRenderDistanceWeight(2);
	}

	public MortarItemEntity(World worldIn, double x, double y, double z, ItemStack stack, double xTarget, double zTarget, double motionX, double motionZ)
	{
		this(worldIn, x, y, z, stack, xTarget, zTarget, motionX, motionZ, false);
	}

	public MortarItemEntity(World worldIn, double x, double y, double z, ItemStack stack, double xTarget, double zTarget, double motionX, double motionZ, boolean weapon)
	{
		this(TYPE, worldIn);
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

		this.setPosition(x, y, z);
		this.rotationYaw = this.rand.nextFloat() * 360.0F;
		this.setMotion(this.rand.nextDouble() * 0.2D - 0.1D, 0.2D, this.rand.nextDouble() * 0.2D - 0.1D);
		this.setItem(stack);
		this.lifespan = (stack.getItem() == null ? 6000 : stack.getEntityLifespan(worldIn));
	}

	@Override
	public void writeAdditional(CompoundNBT compound)
	{
		super.writeAdditional(compound);

		compound.putDouble("xTarget", xTarget);
		compound.putDouble("zTarget", zTarget);
		compound.putBoolean("goingUp", goingUp);
		compound.putBoolean("goingDown", goingDown);
		compound.putFloat("momentum", momentum);
		compound.putDouble("lastY", lastY);
		compound.putDouble("motionXConst", motionXConst);
		compound.putDouble("motionZConst", motionZConst);
		compound.putBoolean("weapon", weapon);
	}

	@Override
	public void readAdditional(CompoundNBT compound)
	{
		super.readAdditional(compound);

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
				remove();
			}
			else
			{
				setPosition(xTarget + (world.rand.nextFloat() * 4f) - 2f, world.getHeight(), zTarget + (world.rand.nextFloat() * 4f) - 2f);

				Vec3d motion = new Vec3d(0, -3F, 0);
				this.setMotion(motion);

				momentum = 4f;
				goingDown = true;
				goingUp = false;
			}
		}
		else
		{
			setPosition(xTarget, world.getHeight(), zTarget);

			Vec3d motion = new Vec3d(0, -3F, 0);
			this.setMotion(motion);

			momentum = 4f;
			goingDown = true;
			goingUp = false;
		}
	}

	@Override
	public void tick()
	{
		super.tick();

		if (goingUp)
		{
			if (posY <= lastY)
			{
				goingUp = false;
			}
			Vec3d motion = new Vec3d(motionXConst, 6F, motionZConst);
			this.setMotion(motion);

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

			Vec3d motion = getMotion();
			motion = new Vec3d(motion.x, Math.min(-3, motion.y), motion.z);
			this.setMotion(motion);
		}

		if (!world.isRemote && momentum > 0)
		{
			breakBlocks();
		}
		lastY = posY;
	}

	private static Map<Item, IMortarWeaponHandler> map = new HashMap<>();

	public static void registerHandler(Item item, IMortarWeaponHandler handler)
	{
		map.put(item, handler);
	}

	private IMortarWeaponHandler getHandler(ItemStack stack)
	{
		Item item = stack.getItem();
		if (map.containsKey(item))
		{
			return map.get(item);
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
					double motionY = getMotion().y;

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

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}