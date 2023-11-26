package de.iplexy.marktheenderman.listeners;

import de.iplexy.marktheenderman.MarkTheEnderman;
import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NPCRightClick implements Listener {

    private static List<OfflinePlayer> openedGuis = new ArrayList<>();

    @EventHandler
    public void onNpcRightClick(NPCRightClickEvent event) {
        if (event.getNPC().getRawName().equals(MarkTheEnderman.MARK_NAME)) {
            Gui gui = Gui.gui()
                    .title(MiniMessage.miniMessage().deserialize(MarkTheEnderman.MARK_NAME))
                    .rows(5)
                    .create();
            GuiItem leaderboard = ItemBuilder.from(Material.END_CRYSTAL)
                    .name(Component.text("§7» §5Leaderboard"))
                    .lore(Arrays.asList(
                            Component.text("§eiPlexy §8- §b5 Level")
                    ))
                    .asGuiItem();
            GuiItem votes = ItemBuilder.from(Material.BELL)
                    .name(Component.text("§7» §6Votes"))
                    .lore(Arrays.asList(
                            Component.text("§7Du musst noch §33x §7voten!")
                    ))
                    .asGuiItem();
            gui.setItem(10, getDailyBoniItem(event.getClicker()));
            gui.setItem(16, getMonthlyBoniItem(event.getClicker()));
            gui.setItem(30, leaderboard);
            gui.setItem(32, votes);
            gui.setDefaultClickAction(e -> e.setCancelled(true));
            gui.setOpenGuiAction(e -> {
                openedGuis.add(event.getClicker());
                updateInventory(gui, event.getClicker());
            });
            gui.setCloseGuiAction(e -> openedGuis.remove(event.getClicker()));
            gui.open(event.getClicker());
            gui.setUpdating(true);
        }
    }

    private String formatTime(long millisekunden) {
        long tage = TimeUnit.MILLISECONDS.toDays(millisekunden);
        long stunden = TimeUnit.MILLISECONDS.toHours(millisekunden) % 24;
        long minuten = TimeUnit.MILLISECONDS.toMinutes(millisekunden) % 60;
        long sekunden = TimeUnit.MILLISECONDS.toSeconds(millisekunden) % 60;

        if (tage > 0) {
            return String.format("%d Tage und %02d Stunden", tage, stunden);
        } else if (stunden > 0) {
            return String.format("%02d Stunden und %02d Minuten", stunden, minuten);
        } else if (minuten > 0) {
            return String.format("%02d Minuten und %02d Sekunden", minuten, sekunden);
        } else {
            return String.format("%02d Sekunden", sekunden);
        }
    }

    private void updateInventory(Gui gui, Player player) {
        new BukkitRunnable() {
            public void run() {
                if (openedGuis.contains(player)) {
                    gui.setItem(10, getDailyBoniItem(player));
                    gui.setItem(16, getMonthlyBoniItem(player));
                    gui.update();
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(MarkTheEnderman.getPlugin(), 0, 10);
    }

    private GuiItem getDailyBoniItem(HumanEntity player) {
        return ItemBuilder.from(Material.LANTERN)
                .name(Component.text("§7» §eTäglicher Bonus"))
                .lore(getDailyBoniLore(player))
                .asGuiItem(e -> {
                    if (MarkTheEnderman.getDailyBoniPlayers().contains((Player) player)) {
                        player.sendMessage("Bereits abgeholt");
                    } else {
                        MarkTheEnderman.getDailyBoniPlayers().add((Player) player);
                        player.sendMessage("Du hast deinen täglichen Bonus abgeholt!");
                    }
                });
    }

    private GuiItem getMonthlyBoniItem(HumanEntity player) {
        return ItemBuilder.from(Material.SOUL_LANTERN)
                .name(Component.text("§7» §3Monatlicher Bonus"))
                .lore(getMonthlyBoniLore(player))
                .asGuiItem(e -> {
                    if(!player.hasPermission("mte.monthly")){
                        player.sendMessage("§cDu hast keine Berechtigung für diesen Bonus!");
                    }else if (MarkTheEnderman.getMonthlyBoniPlayers().contains((Player) player)) {
                        player.sendMessage("Bereits abgeholt");
                    } else {
                        MarkTheEnderman.getMonthlyBoniPlayers().add((Player) player);
                        player.sendMessage("Du hast deinen monatlichen Bonus abgeholt!");
                    }
                });
    }

    private List<Component> getDailyBoniLore(HumanEntity p) {
        LocalDateTime nextDay = LocalDate.now().plusDays(1).atStartOfDay();
        Duration nextDayDuration = Duration.between(LocalDateTime.now(), nextDay);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Komme jeden Tag und hole dir deinen täglichen Bonus!"));
        lore.add(Component.text(""));
        if (MarkTheEnderman.getDailyBoniPlayers().contains(p)) {
            lore.add(Component.text("§7Du hast deinen täglichen Bonus bereits abgeholt!"));
            lore.add(Component.text("§7Wieder Verfügbar in: §e" + formatTime(nextDayDuration.toMillis())));
        } else {
            lore.add(Component.text("§7Du hast deinen täglichen Bonus noch nicht abgeholt!"));
            lore.add(Component.text("§7Klicke, um den Bonus abzuholen!"));
        }
        return lore;
    }

    private List<Component> getMonthlyBoniLore(HumanEntity p) {
        LocalDateTime nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();
        Duration nextMonthDuration = Duration.between(LocalDateTime.now(), nextMonth);
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("§7Komme jeden Monat und hole dir deinen Monatlichen Bonus!"));
        lore.add(Component.text(""));
        if (!p.hasPermission("mte.monthly")) {
            lore.add(Component.text("§7Der Monatliche Bonus ist nur für §ePremium §7Spieler verfügbar!"));
            lore.add(Component.text("§7Klicke, um Premium zu kaufen!"));
        } else if (MarkTheEnderman.getMonthlyBoniPlayers().contains(p)) {
            lore.add(Component.text("§7Du hast deinen monatlichen Bonus bereits abgeholt!"));
            lore.add(Component.text("§7Wieder Verfügbar in: §e" + formatTime(nextMonthDuration.toMillis())));
        } else {
            lore.add(Component.text("§7Du hast deinen monatlichen Bonus noch nicht abgeholt!"));
            lore.add(Component.text("§7Klicke, um den Bonus abzuholen!"));
        }
        return lore;
    }

}
