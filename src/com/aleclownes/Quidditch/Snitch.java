package com.aleclownes.Quidditch;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**This is the snitch, which randomly changes direction and ends the game when it is caught
 * @author lownes
 *
 */
public class Snitch extends Ball {
	
	int countdown = 0;
	int speed = 4;

	public Snitch(Quidditch p, Player referee) {
		super(Material.getMaterial(p.getConfiguration().getString("snitchBlock")), referee.getEyeLocation());
		setLocation(getLocation().add(0, 50, 0));
		changeDirection();
	}

	@Override
	public void action(Field f) {
		countdown--;
		if (countdown <= 0){
			countdown = (int) (Math.random()*100);
			changeDirection();
		}
	}
	
	private void changeDirection(){
		setVector(Vector.getRandom().normalize().multiply(Math.random()*3));
	}

}
