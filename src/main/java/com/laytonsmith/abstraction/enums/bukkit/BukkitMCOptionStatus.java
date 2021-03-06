package com.laytonsmith.abstraction.enums.bukkit;

import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.laytonsmith.abstraction.Implementation;
import com.laytonsmith.abstraction.enums.EnumConvertor;
import com.laytonsmith.abstraction.enums.MCOptionStatus;
import com.laytonsmith.annotations.abstractionenum;

@abstractionenum(
		implementation = Implementation.Type.BUKKIT,
		forAbstractEnum = MCOptionStatus.class,
		forConcreteEnum = Team.OptionStatus.class
)
public class BukkitMCOptionStatus extends EnumConvertor<MCOptionStatus, OptionStatus> {

	private static BukkitMCOptionStatus instance;

	public static BukkitMCOptionStatus getConvertor() {
		if(instance == null) {
			instance = new BukkitMCOptionStatus();
		}
		return instance;
	}
}
