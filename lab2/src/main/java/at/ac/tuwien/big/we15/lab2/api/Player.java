package at.ac.tuwien.big.we15.lab2.api;

public class Player {

	private Avatar avatar;
	private int points;
	
	public Player(){
		setPoints(0);
	}

	public Avatar getAvatar() {
		return avatar;
	}

	public void setAvatar(Avatar avatar) {
		this.avatar = avatar;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

}
