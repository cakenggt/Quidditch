package com.aleclownes.Quidditch;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

/**Represents a ball in the game of Quidditch
 * @author lownes
 *
 */
public abstract class Ball {
	
	private Vector vector = new Vector(0, 0, 0);
	private Location location;
	private Material flairMaterial;

	public Ball(Material mat, Location loc) {
		flairMaterial = mat;
		location = loc;
	}
	
	/**Moves the ball in the direction of the vector
	 * 
	 */
	@SuppressWarnings("deprecation")
	public void move(Field f){
		if (!f.isStarted()){
			return;
		}
		if (location.clone().add(vector).getBlock().getType() != Material.AIR){
			setVector(vector.multiply(-0.5));
		}
		else if (!f.isInside(location)){
			Vector movDir = f.getLocation().toVector().subtract(location.toVector());
			movDir.setY(vector.getY());
			double horizMagnitude = Math.sqrt((vector.getX()*vector.getX())+(vector.getZ()*vector.getZ()));
			double newHorizMagnitude = Math.sqrt((movDir.getX()*movDir.getX())+(movDir.getZ()*movDir.getZ()));
			double ratio = horizMagnitude/newHorizMagnitude;
			movDir.setX(movDir.getX()*ratio);
			movDir.setZ(movDir.getZ()*ratio);
			setVector(movDir);
		}
		location.add(vector);
		location.getWorld().playEffect(location, Effect.STEP_SOUND, flairMaterial.getId());
	}
	
	public void setLocation(Location loc){
		location = loc;
	}
	
	public void setVector(Vector vec){
		vector = vec;
	}
	
	public Location getLocation(){
		return location;
	}
	
	public Vector getVector(){
		return vector;
	}
	
	/**The action the ball takes
	 * 
	 */
	public abstract void action(Field f);

}
