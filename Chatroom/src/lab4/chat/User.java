/**********************************************
Lab 4
Course:<BTP 400> - Semester 4
Last Name:<Cao>
First Name:<GuoYu>
ID:<061341145>
Section:<NAA>
This assignment represents my own work in accordance with Seneca Academic Policy.
Signature GuoYu Cao
Date:<2021-April-09>
**********************************************/

package lab4.chat;

/**
 * This is the User class that stores the user's name and ip address
 * 
 * @author GuoYu Cao
 * @version 1.0
 * @since 1.0
 */
public class User {
	private String name;
	private String ip;

	/**.
	 * This is constructor
	 * @param name the name of the user
	 * @param ip the IP of the user
	 */
	public User(String name, String ip) {
		this.name = name;
		this.ip = ip;
	}

	/**
	 * Get the name of the user
	 * @return name the name of the user
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the user
	 *@param name the name of the user
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the IP of the user
	 * @return ip the IP of the user
	 */
	public String getIp() {
		return ip;
	}
	
	/**
	 * Set the IP of the user
	 * @param ip the ip of the user
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}
}