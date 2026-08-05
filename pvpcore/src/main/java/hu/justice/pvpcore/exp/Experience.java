package hu.justice.pvpcore.exp;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class Experience {

	private EntityType entityType;
	private int xp;
	private Material block;
	
	public Experience(EntityType entityType,int xp) {
		setEntityType(entityType);
		setXp(xp);
	}
	public Experience(Material block, int xp) {
		setBlock(block);
		setXp(xp);
	}

	public void setBlock(Material block) {
		this.block=block;
	}
	public Material getBlock(){
		return block;
	}

	public EntityType getEntityType() {
		return entityType;
	}
	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}
	public int getXp() {
		return xp;
	}
	public void setXp(int xp) {
		this.xp = xp;
	}
	
}
