package com.aleclownes.Quidditch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**The is the bludger, which chases players and knocks them around
 * @author lownes
 *
 */
public class Bludger extends Ball {
	
	private Player lastHit;
	//this is the player the bludger is currently targeting.
	private Player target;
	private Quidditch p;
	private Bludger otherBludger;

	public Bludger(Quidditch p, Player referee) {
		super(Material.getMaterial(p.getConfiguration().getString("bludgerBlock")), referee.getEyeLocation());
		lastHit = referee;
		this.p = p;
	}

	@Override
	public void action(Field f) {
		autoTarget(f);
		Vector addVec = target.getLocation().toVector().subtract(getLocation().toVector()).normalize().multiply(0.05);
		setVector(getVector().add(addVec));
		if (getVector().length() > 1){
			setVector(getVector().normalize());
		}
		if (target.getLocation().distance(getLocation()) <= 1){
			target.setVelocity(target.getVelocity().add(getVector()));
			target.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 0));
			lastHit = target;
			Team team = f.getTeam(target);
			Announcer.announce(p, team.getColor(), Announcer.BLUDGER_IMPACT, target.getDisplayName());
			autoTarget(f);
		}
	}
	
	/**Hits the bludger. Only a beater should use this method.
	 * @param ply - the beater
	 */
	public void hit(Player ply){
		setVector(ply.getEyeLocation().getDirection());
		lastHit = ply;
	}
	
	/**Automatically finds the best target which satisfies the bludger's rules.
	 * @param f - Field
	 */
	public void autoTarget(Field f){
		target = null;
		List<Player> candidates = new ArrayList<Player>();
		for (Player ply : f.getQuidditchPlayers()){
			if (target == null){
				target = ply;
			}
			if (!ply.equals(otherBludger.target) && !ply.equals(otherBludger.lastHit) && !ply.equals(lastHit)){
				candidates.add(ply);
			}
		}
		if (candidates.size() > 0){
			Collections.sort(candidates, new Comparator<Player>() {

				public int compare(Player a, Player b) {
					//we want to sort in ascending order
					double distanceA = a.getLocation().distance(getLocation());
					double distanceB = b.getLocation().distance(getLocation());
					if (distanceA > distanceB){
						return 1;
					}
					else if (distanceA == distanceB){
						return 0;
					}
					else{
						return -1;
					}
				}
			});
			target = candidates.get(0);
		}
	}
	
	/**Set's the otherBludger field to be the opposite bludger, and also mutates the parameter by setting it's otherBludger as well to this.
	 * This method should be called before setting the field's bludgers.
	 * @param bludger - Bludger to be set as otherBludger and mutated by setting it's otherBludger to this.
	 */
	public void setOtherBludger(Bludger bludger){
		otherBludger = bludger;
		bludger.otherBludger = this;
	}
	
	public Bludger getOtherBludger(){
		return otherBludger;
	}
	
	public Player getTarget(){
		return target;
	}
	
	public Player lastHit(){
		return lastHit;
	}

}
