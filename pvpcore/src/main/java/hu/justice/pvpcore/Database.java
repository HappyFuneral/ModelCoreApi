package hu.justice.pvpcore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.bukkit.configuration.file.FileConfiguration;

import hu.justice.pvpcore.levels.Level;
import hu.justice.pvpcore.levels.Levels;
import hu.justice.pvpcore.levels.RewardConfiguration;

import java.sql.SQLException;


public class Database {

	private String host;
	private String root;
	private String pass;
	
	public Database(App main) {

		if(main.getConfig().getConfigurationSection("database") == null ) {
			host ="jdbc:mysql://localhost:3306/test";
			root = "root";
			pass = "";
			main.getConfig().set("database.host", host);
			main.getConfig().set("database.user", root);
			main.getConfig().set("database.password", pass);
			main.saveConfig();
		} else {
			host = main.getConfig().getString("database.host");
			root = main.getConfig().getString("database.user");
			pass = main.getConfig().getString("database.password");
		}
		connect();
	}
	public void reset() {
		try {
			Connection conn = DriverManager.getConnection(host,root,pass);
			Statement statement = conn.createStatement();
			statement.execute("drop table levels;");
			statement.execute("drop table rewards;");
			
			statement.execute("create table if not exists levels (player varchar(30),lvl int,xp int,maxXp int);");
			statement.execute("create table if not exists rewards (player varchar(30), reward varchar(100));");
			statement.close();
			conn.close();

			Levels.setLevels(new ArrayList<>());
			
			System.out.println("Sql sikerült!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void connect() {
		try {
			Connection conn = DriverManager.getConnection(host,root,pass);
			Statement statement = conn.createStatement();
			statement.execute("create table if not exists levels (player varchar(30),lvl int,xp int,maxXp int);");
			statement.execute("create table if not exists rewards (player varchar(30), reward varchar(100));");
			statement.close();
			conn.close();
			System.out.println("Sql sikerült!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	

	public void insertRewards(String name, ArrayList<String> rewards) {
		if (rewards.isEmpty())return;
		try {
			Connection conn = DriverManager.getConnection(host,root,pass);
			Statement statement = conn.createStatement();
			for (String line : rewards) {
				statement.execute("insert into rewards values('"+name+"','"+line+"');");

			}
			statement.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void insertLevel(Level level) {
		try {
			Connection conn = DriverManager.getConnection(host,root,pass);
			Statement statement = conn.createStatement();
			statement.execute("insert into levels values('"+level.getName()+"',"+level.getLvl()+","+level.getXp()+","+level.getMaxXp()+");");
			statement.close();
			conn.close();
			insertRewards(level.getName(),level.getRewards());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean levelIsContain(String name) {
		
			Connection conn;
			try {
				conn = DriverManager.getConnection(host,root,pass);
				PreparedStatement statement = conn.prepareStatement("select * from levels where (player like '"+name+"');");
				ResultSet res = statement.executeQuery();
				if(!res.next()) {
					res.close();
					statement.close();
					conn.close();
					return false;
				}
				else {
					res.close();
					statement.close();
					conn.close();
				return true;
				}

				//Level level = new Level(res.getString());
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		
	}
	public boolean characterIsContain(String name) {
		
		Connection conn;
		try {
			conn = DriverManager.getConnection(host,root,pass);
			PreparedStatement statement = conn.prepareStatement("select * from characters where (player like '"+name+"');");
			ResultSet res = statement.executeQuery();
			if(!res.next()) {
				res.close();
				statement.close();
				conn.close();
				return false;
			}
			else {
				res.close();
				statement.close();
				conn.close();
			return true;
			}

			//Level level = new Level(res.getString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	
}
	public void refreshLevel(Level level,ArrayList<String> rewards) {
		Connection conn;
		try {
			conn = DriverManager.getConnection(host,root,pass);
			Statement statement1 = conn.createStatement();
			statement1.execute("update levels set player='"+level.getName()+"',lvl="+level.getLvl()+",xp="+level.getXp()+",maxXp="+level.getMaxXp()+" where player like '"+level.getName()+"';");
			statement1.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public Level readLevel(String name) {
		Connection conn;
		try {
			conn = DriverManager.getConnection(host,root,pass);
			PreparedStatement statement = conn.prepareStatement("select * from levels where (player like '"+name+"');");
			ResultSet res = statement.executeQuery();
			res.next();
			String player = res.getString("player");
			int lvl =res.getInt("lvl");
			int xp =res.getInt("xp");
			int maxXp =res.getInt("maxXp");
			res.close();
			statement.close();
			conn.close();
			Level level = new Level(player,lvl,xp,maxXp,readRewards(name,lvl));
			return level;
			//Level level = new Level(res.getString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	public ArrayList<String> readRewards(String name,int lvl){
		ArrayList<String> list = new ArrayList<>();
		Connection conn;
		try {
			conn = DriverManager.getConnection(host,root,pass);
			PreparedStatement statement = conn.prepareStatement("select * from rewards where (player like '"+name+"');");
			ResultSet res = statement.executeQuery();
			FileConfiguration config = RewardConfiguration.getConfig();

			while(res.next()) {
				list.add(res.getString("reward"));
			}
			
			for (String str : config.getConfigurationSection("rewards").getKeys(false)) {
				if (Integer.parseInt(str)<=lvl) {
					config.getStringList("rewards."+str).forEach(s -> list.add(s));
				}
			}			
			return list;
			//Level level = new Level(res.getString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	
	public String getRoot() {
		return root;
	}
	public String getHost() {
		return host;
	}
 

}
