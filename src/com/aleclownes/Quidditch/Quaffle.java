package com.aleclownes.Quidditch;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**This is the quaffle, which can be thrown and is affected by half gravity due to it's slow-falling charm
 * @author lownes
 *
 */
public class Quaffle extends Ball {

	private boolean isCarried;
	private Player lastTouched;
	public static final Vector halfGravity = new Vector(0, -9.8/160, 0);
	private Quidditch p;

	public Quaffle(Quidditch p, Player referee) {
		super(Material.getMaterial(p.getConfiguration().getString("quaffleBlock")), referee.getLocation().add(0, 0.5, 0));
		isCarried = true;
		lastTouched = referee;
		this.p = p;
	}

	@Override
	public void action(Field f) {
		if (isCarried){
			setVector(new Vector(0, 0, 0));
			setLocation(lastTouched.getLocation().add(0, 0.5, 0));
		}
		else{
			setVector(getVector().add(halfGravity));
			if (f.getTeamA().getGoal().contains(getLocation().getBlock())){
				f.getTeamB().setScore(f.getTeamB().getScore()+10);
				Announcer.announce(p, f.getTeamB().getColor(), Announcer.QUAFFLE_SCORE, lastTouched.getDisplayName());
				isCarried = true;
				lastTouched = f.getTeamA().getKeeper();
			}
			else if (f.getTeamB().getGoal().contains(getLocation().getBlock())){
				f.getTeamA().setScore(f.getTeamA().getScore()+10);
				Announcer.announce(p, f.getTeamA().getColor(), Announcer.QUAFFLE_SCORE, lastTouched.getDisplayName());
				isCarried = true;
				lastTouched = f.getTeamB().getKeeper();
			}
		}
	}

	public boolean isCarried(){
		return isCarried;
	}

	public Player getLastTouched(){
		return lastTouched;
	}

	public void setCarried(boolean carried){
		isCarried = carried;
	}

	public void setLastTouched(Player ply){
		lastTouched = ply;
	}

	/**The player tosses the quaffle. Only a chaser or keeper should use this method.
	 *
	 */
	public void toss(){
		setVector(lastTouched.getEyeLocation().getDirection().add(lastTouched.getVelocity()));
		setLocation(lastTouched.getEyeLocation());
		setCarried(false);
	}

}
