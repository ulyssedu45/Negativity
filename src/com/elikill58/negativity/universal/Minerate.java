package com.elikill58.negativity.universal;

import java.text.NumberFormat;
import java.util.HashMap;

import com.elikill58.negativity.universal.adapter.Adapter;

public class Minerate {

	private final NumberFormat nf;
	private HashMap<MinerateType, Integer> mined = new HashMap<>();
	private int fullMined = 0;

	public Minerate() {
		for(MinerateType type : MinerateType.values())
			mined.put(type, 0);
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
	}

	public Minerate(HashMap<MinerateType, Integer> mined, int fullMined) {
		this.mined = mined;
		this.fullMined = fullMined;
		// For old version, to don't produce NPE
		for(MinerateType type : MinerateType.values())
			mined.putIfAbsent(type, 0);
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
	}

	public void setMine(MinerateType type, int value) {
		mined.put(type, value);
	}

	public void addMine(MinerateType type, Object player) {
		fullMined++;
		if(type == null)
			return;
		mined.put(type, mined.getOrDefault(type, 0) + 1);
		int minedType = 0;
		for(int i : mined.values())
			minedType += i;
		int relia = minedType / fullMined;
		Cheat xray = Cheat.forKey(CheatKeys.XRAY);
		Adapter.getAdapter().alertMod(relia > 80 ? ReportType.VIOLATION : ReportType.WARNING, player, xray,
				relia, type.getOreName() + " mined. Full mined: " + fullMined + ". Mined by type: " + toString(),
				xray.hoverMsg("main", "%name%", type.getName(), "%nb%", mined.get(type)));
	}

	public Integer getMinerateType(MinerateType type) {
		return mined.get(type);
	}

	public int getFullMined() {
		return fullMined;
	}

	public String[] getInventoryLoreString() {
		String[] s = new String[MinerateType.values().length + 1];
		s[0] = "&r&7" + "Full Mined: " + fullMined;
		int i = 1;
		for(MinerateType type : MinerateType.values())
			s[i++] = "&r&7" + type.getName() + ": " + nf.format((mined.get(type) * 100) / (double) (fullMined == 0 ? 1 : fullMined)) + "% (" + mined.get(type) + ")";
		return s;
	}
	
	public String toString() {
		String s = "";
		for(MinerateType m : mined.keySet()) {
			if(s.equalsIgnoreCase(""))
				s = m.getName() + " (" + m.getOreName() + ") : " + mined.get(m);
			else
				s = s + ", " + m.getName() + " (" + m.getOreName() + ") : " + mined.get(m);
		}
		return s;
	}

	public enum MinerateType {
		DIAMOND("Diamond", "DIAMOND_ORE", "minecraft:diamond_ore"),
		GOLD("Gold", "GOLD_ORE", "minecraft:gold_ore"),
		IRON("Iron", "IRON_ORE", "minecraft:iron_ore"),
		COAL("Coal", "COAL_ORE", "minecraft:coal_ore");

		private final String name;
		private final String oreName;
		private final String mcId;

		MinerateType(String name, String oreName, String mcId) {
			this.name = name;
			this.oreName = oreName;
			this.mcId = mcId;
		}

		public String getName() {
			return name;
		}

		public String getOreName() {
			return oreName;
		}

		public String getMcId() {
			return mcId;
		}

		public static MinerateType getMinerateType(String s) {
			for(MinerateType type : MinerateType.values())
				if(type.name().equalsIgnoreCase(s) || type.getOreName().equalsIgnoreCase(s) || type.getMcId().equalsIgnoreCase(s))
					return type;
			return null;
		}

		public static MinerateType fromId(String id) {
			for (MinerateType type : MinerateType.values()) {
				if (type.getMcId().equalsIgnoreCase(id)) {
					return type;
				}
			}
			return null;
		}
	}
}
