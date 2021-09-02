package com.windanesz.mospells.entity;

import com.bobmowzie.mowziesmobs.server.config.ConfigHandler;
import com.bobmowzie.mowziesmobs.server.entity.frostmaw.EntityFrostmaw;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.UUID;

public class EntityFrostmawMinion extends EntityFrostmaw implements ISummonedCreature {

	// Field implementations
	private int lifetime = -1;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override
	public int getLifetime() { return lifetime; }

	@Override
	public void setLifetime(int lifetime) { this.lifetime = lifetime; }

	@Override
	public UUID getOwnerId() { return casterUUID; }

	@Override
	public void setOwnerId(UUID uuid) { this.casterUUID = uuid; }

	public EntityFrostmawMinion(World world) {
		super(world);
		this.experienceValue = 0;
	}

	// This particular override is pretty standard: let the superclass handle basic AI like swimming, but replace its
	// targeting system with one that targets hostile mobs and takes the AllyDesignationSystem into account.
	@Override
	protected void initEntityAI() {
		super.initEntityAI();
		this.targetTasks.taskEntries.clear();
		// Spiders use a custom AI type specific to spiders which I can't access, but it's just an extension of
		// EntityAINearestAttackableTarget which takes daylight into account. Since I want spider minions to attack
		// regardless of daylight, I can just use EntityAINearestAttackableTarget.
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
		this.targetTasks.addTask(1, new EntityAINearestAttackableTarget<>(this, EntityLivingBase.class, 0, true, false, this.getTargetSelector()));

	}

	@Override
	public void setRevengeTarget(EntityLivingBase entity) {
		if (this.shouldRevengeTarget(entity)) { super.setRevengeTarget(entity); }
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.updateDelegate();
	}

	@Override
	public void onSpawn() {
		this.spawnParticleEffect();
	}

	@Override
	public void onDespawn() {
		this.spawnParticleEffect();
	}

	private void spawnParticleEffect() {
		if (this.world.isRemote) {
			for (int i = 0; i < 15; i++) {
				ParticleBuilder.create(ParticleBuilder.Type.DARK_MAGIC)
						.pos(this.posX + this.rand.nextFloat(), this.posY + this.rand.nextFloat(), this.posZ + this.rand.nextFloat())
						.clr(0.1f, 0.2f, 0.0f)
						.spawn(world);
			}
		}
	}

	@Override
	public boolean hasParticleEffect() {
		return true;
	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand) {
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's short-circuiting method of evaluating OR statements.
		return this.interactDelegate(player, hand) || super.processInteract(player, hand);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		this.writeNBTDelegate(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		this.readNBTDelegate(nbttagcompound);
	}

	// Recommended overrides

	@Override
	protected int getExperiencePoints(EntityPlayer player) { return 0; }

	@Override
	protected boolean canDropLoot() { return false; }

	@Override
	protected Item getDropItem() { return null; }

	@Override
	protected ResourceLocation getLootTable() { return null; }

	@Override
	public boolean canPickUpLoot() { return false; }

	// This vanilla method has nothing to do with the custom despawn() method.
	@Override
	protected boolean canDespawn() {
		return getCaster() == null && getOwnerId() == null;
	}

	@Override
	public boolean getCanSpawnHere() {
		return this.world.getDifficulty() != EnumDifficulty.PEACEFUL;
	}

	@Override
	public ITextComponent getDisplayName() {
		if (getCaster() != null) {
			return new TextComponentTranslation(NAMEPLATE_TRANSLATION_KEY, getCaster().getName(),
					new TextComponentTranslation("entity." + this.getEntityString() + ".name"));
		} else {
			return super.getDisplayName();
		}
	}

	@Override
	protected boolean hasBossBar() {
		return false;
	}

	@Override
	public boolean hasCustomName() {
		// If this returns true, the renderer will show the nameplate when looking directly at the entity
		return Wizardry.settings.summonedCreatureNames && getCaster() != null;
	}
}