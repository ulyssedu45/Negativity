package com.elikill58.negativity.spigot.protocols;

import java.text.NumberFormat;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.elikill58.negativity.spigot.SpigotNegativity;
import com.elikill58.negativity.spigot.SpigotNegativityPlayer;
import com.elikill58.negativity.spigot.listeners.PlayerPacketsClearEvent;
import com.elikill58.negativity.spigot.packets.PacketType;
import com.elikill58.negativity.spigot.utils.Utils;
import com.elikill58.negativity.universal.Cheat;
import com.elikill58.negativity.universal.CheatKeys;
import com.elikill58.negativity.universal.ReportType;
import com.elikill58.negativity.universal.adapter.Adapter;
import com.elikill58.negativity.universal.utils.UniversalUtils;

@SuppressWarnings("deprecation")
public class ForceFieldProtocol extends Cheat implements Listener {

	private NumberFormat nf = NumberFormat.getInstance();
	
	public ForceFieldProtocol() {
		super(CheatKeys.FORCEFIELD, true, Material.DIAMOND_SWORD, CheatCategory.COMBAT, true, "ff", "killaura");
		nf.setMaximumIntegerDigits(2);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		if (!(e.getDamager() instanceof Player) || e.isCancelled())
			return;
		Player p = (Player) e.getDamager();
		SpigotNegativityPlayer np = SpigotNegativityPlayer.getNegativityPlayer(p);
		if (!np.ACTIVE_CHEAT.contains(this) || e.getEntity() == null)
			return;
		if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.getGameMode().equals(GameMode.ADVENTURE))
			return;
		boolean mayCancel = false;
		if(!p.hasLineOfSight(e.getEntity())) {
			mayCancel = SpigotNegativity.alertMod(ReportType.VIOLATION, p, this, UniversalUtils.parseInPorcent(90 + np.getWarn(this)), "Hit " + e.getEntity().getType().name()
					+ " but cannot see it, ping: " + Utils.getPing(p),
					hoverMsg("line_sight", "%name%", e.getEntity().getType().name().toLowerCase()));
		}
		if(hasThorns(p)) {
			if (isSetBack() && mayCancel)
				e.setCancelled(true);
			return;
		}
		Location tempLoc = e.getEntity().getLocation().clone();
		tempLoc.setY(p.getLocation().getY());
		double dis = tempLoc.distance(p.getLocation());
		if (dis > Adapter.getAdapter().getConfig().getDouble("cheats.forcefield.reach")
				&& !p.getItemInHand().getType().equals(Material.BOW) &&!e.getEntityType().equals(EntityType.ENDER_DRAGON)) {
			mayCancel = SpigotNegativity.alertMod(ReportType.WARNING, p, this,
					UniversalUtils.parseInPorcent(dis * 2 * 10),
					"Big distance with: " + e.getEntity().getType().name().toLowerCase() + ". Exact distance: " + dis + ", without thorns"
							+ ". Ping: " + Utils.getPing(p),
							hoverMsg("distance", "%name%", e.getEntity().getName(), "%distance%", nf.format(dis)));
		}
		final Location loc = p.getLocation().clone();
		Bukkit.getScheduler().runTaskLater(SpigotNegativity.getInstance(), new Runnable() {
			public void run() {
				Location loc1 = p.getLocation();
				int gradeRounded = Math.round(Math.abs(loc.getYaw() - loc1.getYaw()));
				if (gradeRounded > 180.0) {
					SpigotNegativity.alertMod(ReportType.WARNING, p, Cheat.forKey(CheatKeys.FORCEFIELD), UniversalUtils.parseInPorcent(gradeRounded),
							"Player rotate too much (" + gradeRounded + "°) without thorns", hoverMsg("rotate", "%degrees%", gradeRounded));
				}
			}
		}, 1);
		if (isSetBack() && mayCancel)
			e.setCancelled(true);
	}

	private boolean hasThorns(Player p) {
		ItemStack[] armor = p.getInventory().getArmorContents();
		if(armor == null)
			return false;
		for(ItemStack item : armor)
			if(item != null && item.containsEnchantment(Enchantment.THORNS))
				return true;
		return false;
	}
	
	@EventHandler
	public void onPacketClear(PlayerPacketsClearEvent e) {
		int use = e.getPackets().getOrDefault(PacketType.Client.USE_ENTITY, 0);
		if(use > 7) {
			Player p = e.getPlayer();
			int ping = Utils.getPing(p);
			SpigotNegativity.alertMod(ReportType.WARNING, p, this, UniversalUtils.parseInPorcent(use * 15 - ping),
					use + " USE_ENTITY packets sent. Ping: " + ping, (CheatHover) null, use - 7);
		}
	}
	
	public static void manageForcefieldForFakeplayer(Player p, SpigotNegativityPlayer np) {
		if (np.fakePlayerTouched < 5)
			return;
		Cheat forcefield = Cheat.forKey(CheatKeys.FORCEFIELD);
		double timeBehindStart = System.currentTimeMillis() - np.timeStartFakePlayer;
		double rapport = np.fakePlayerTouched / (timeBehindStart / 1000);
		SpigotNegativity.alertMod(rapport > 20 ? ReportType.VIOLATION : ReportType.WARNING, p, forcefield,
				UniversalUtils.parseInPorcent(rapport * 10), "Hitting fake entities. " + np.fakePlayerTouched
						+ " entites touch in " + timeBehindStart + " millisecondes",
						forcefield.hoverMsg("fake_players", "%nb%", np.fakePlayerTouched, "%time%", timeBehindStart));
	}
}
