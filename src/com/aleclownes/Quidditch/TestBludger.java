package com.aleclownes.Quidditch;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class TestBludger extends Bludger {
	
	LivingEntity target;
	LivingEntity lastHit;
	int id;

	public TestBludger(Quidditch p, Player referee) {
		super(p, referee);
		lastHit = referee;
		id = (int) (Math.random()*100);
	}
	
	@Override
	public void action(Field f){
		TestField tf = (TestField)f;
		TestBludger otherBludger = (TestBludger) getOtherBludger();
		for (LivingEntity live : getLocation().getWorld().getEntitiesByClass(LivingEntity.class)){
			if (target == null || (!live.equals(otherBludger.target) && !live.equals(otherBludger.lastHit)&& !live.equals(lastHit) && tf.isInside(live.getLocation()) && live.getLocation().distance(getLocation()) < target.getLocation().distance(getLocation()))){
				target = live;
			}
		}
		Vector addVec = target.getLocation().toVector().subtract(getLocation().toVector()).normalize().multiply(0.05);
		setVector(getVector().add(addVec));
		if (getVector().length() > 1){
			setVector(getVector().normalize());
		}
		if (target.getLocation().distance(getLocation()) <= 1){
			target.setVelocity(target.getVelocity().add(getVector()));
			target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 0));
			lastHit = target;
			System.out.println("Bludger " + id + " hit " + target + " " + target.getEntityId());
			target = null;
		}
	}

}
