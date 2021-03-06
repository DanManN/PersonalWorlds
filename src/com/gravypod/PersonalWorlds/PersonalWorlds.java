package com.gravypod.PersonalWorlds;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.gravypod.PersonalWorlds.CommandHandlers.CommandHandler;
import com.gravypod.PersonalWorlds.Listener.PlayerListener;
import com.gravypod.PersonalWorlds.utils.PluginUtil;

public class PersonalWorlds extends JavaPlugin {
	
	Logger log = Logger.getLogger("Minecraft");
	
	private List<String> generators = null;
	
	private List<String> commands = null;
	
	private int borderSize;
	
	private FileConfiguration configFile;
	
	private List<String> ownerPerms;
	
	private List<String> guestPerms;
	
	private I18n messageDB;
	
	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		
		final File config = new File(getDataFolder() + System.getProperty("file.separator") + "config.yml");
		
		if (!config.exists()) {
			
			configFile = getConfig();
			
			configFile.options().copyDefaults(true);
			
			try {
				
				configFile.save(config);
				
			} catch (final IOException e) {
				e.printStackTrace();
			}
			
		} else {
			
			try {
				
				getConfig().load(config);
				configFile = getConfig();
				
			} catch (final Exception e) {
				
				throw new IllegalStateException("The config could not load!", e);
				
			}
			
		}
		
		Object list;
		
		list = configFile.getList("Permission.Guest");
		
		if (list instanceof List) {
			
			guestPerms = new ArrayList<String>((List<String>) configFile.getList("Permission.Guest"));
			
		} else if (list instanceof String) {
			
			guestPerms = new ArrayList<String>();
			guestPerms.add((String) list);
			
		} else {
			
			throw new IllegalStateException("Unknown entry for guest permissions!");
			
		}
		
		list = configFile.getList("Permission.Owner");
		if (list instanceof List) {
			
			ownerPerms = new ArrayList<String>((List<String>) configFile.getList("Permission.Owner"));
			
		} else if (list instanceof String) {
			
			ownerPerms = new ArrayList<String>();
			ownerPerms.add((String) list);
			
		} else {
			throw new IllegalStateException("Unknown entry for owner permissions!");
		}
		
		setBorderSize(configFile.getInt("Borders"));
		
		PersonalPerms.initialize(this);
		
		generators = new ArrayList<String>();
		
		commands = ListClasses.getClasseNamesInPackage(getFile().getAbsolutePath(), "com.gravypod.PersonalWorlds.commands.");
		
		log.info("Enabling PersonalWorlds. Made by Gravypod");
		
		getCommand(getPluginName()).setExecutor(new CommandHandler(this));
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		PluginUtil.init(this);
		
		final File worldsFolder = new File(getPluginName());
		
		if (!worldsFolder.exists()) {
			
			worldsFolder.mkdir();
			
		} else if (!worldsFolder.canRead() || !worldsFolder.canWrite()) {
			
			throw new IllegalStateException("You do not have Read/Write access for the server root folder!");
			
		}
		
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			
			@Override
			public void run() {
			
				final Plugin[] plugins = getServer().getPluginManager().getPlugins();
				log.warning("Ignore the message: 'Plugin {Plugin} does not contain any generators'");
				
				for (final Plugin p : plugins) {
					
					// Collect a list of generators
					if (p.isEnabled() && p.getDefaultWorldGenerator("world", "") != null) {
						generators.add(p.getDescription().getName());
					}
					
				}
				
				String enviromentName;
				
				for (final Environment t : Environment.values()) {
					
					enviromentName = t.name();
					
					if (!generators.contains(enviromentName)) {
						generators.add(enviromentName);
					}
					
				}
				
			}
			
		});
		
		messageDB = new I18n(this);
		
	}
	
	@Override
	public void onDisable() {
	
		generators = null;
		commands = null;
		
		log.info("Disabling PersonalWorlds. Made by gravypod");
		
	}
	
	/**
	 * Gets the plugin's name from the plugin.yml
	 * 
	 * @return String of the plugin's name
	 */
	public String getPluginName() {
	
		return getName();
		
	}
	
	/**
	 * Tests if a string is a world generator we know about.
	 * 
	 * @param name
	 * @return String of a world generators name.
	 * 
	 */
	public String getGenerator(final String name) {
	
		for (final String genName : generators) {
			
			if (genName.equalsIgnoreCase(name)) {
				return genName;
			}
			
		}
		
		return null;
		
	}
	
	/**
	 * This returns a joined list of worlds. Separated by ", "
	 * 
	 * @return String of all the generators we know about.
	 */
	public String joinedGenList() {
	
		String generatorsList = "";
		
		for (final String gen : generators) {
			generatorsList += gen.toLowerCase() + ", ";
		}
		
		return generatorsList.length() >= 1 ? generatorsList.substring(0, generatorsList.length() - 2) : "";
		
	}
	
	/**
	 * Returns a list of arguments we can use.
	 * 
	 * @return Classes
	 * 
	 */
	public List<String> getCommands() {
	
		return commands;
		
	}
	
	/**
	 * Gets the world border limit size.
	 * 
	 * @return int of border size
	 * 
	 */
	public int getBorderSize() {
	
		return borderSize;
		
	}
	
	/**
	 * Set and int for the border size
	 * 
	 * @param borderSize
	 *            (int)
	 * 
	 */
	public void setBorderSize(final int borderSize) {
	
		this.borderSize = borderSize;
		
	}
	
	public List<String> getOwnerPerms() {
	
		return ownerPerms;
	}
	
	public void setOwnerPerms(final List<String> ownerPerms) {
	
		this.ownerPerms = ownerPerms;
	}
	
	public List<String> getGuestPerms() {
	
		return guestPerms;
	}
	
	public void setGuestPerms(final List<String> guestPerms) {
	
		this.guestPerms = guestPerms;
	}
	
	public String getMessage(String key) {
		return messageDB.getColoredMessage(key);
	}
	
}
