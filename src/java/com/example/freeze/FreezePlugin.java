package com.example.freeze;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public final class FreezePlugin extends JavaPlugin implements Listener, CommandExecutor {

    public static UUID taggerUUID = null;

    @Override
    public void onEnable() {
        // Регистрируем слушатель событий (Events)
        getServer().getPluginManager().registerEvents(this, this);
        
        // Регистрируем команду /tagset
        if (getCommand("tagset") != null) {
            getCommand("tagset").setExecutor(this);
        }

        getLogger().info("FreezePlugin успешно запущен!");
    }

    // --- 1. ОБРАБОТКА УДАРА ПАЛОЧКОЙ ---
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Проверяем, что и атакующий, и жертва — игроки
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof Player target) {

            // Если атакующий — текущий водящий и бьет палочкой (STICK)
            if (attacker.getUniqueId().equals(taggerUUID) && attacker.getInventory().getItemInMainHand().getType() == Material.STICK) {
                
                // Передаем роль
                transferTag(attacker, target);
                
                // Отменяем урон от палки
                event.setCancelled(true);
            }
        }
    }

    private void transferTag(Player oldTagger, Player newTagger) {
        taggerUUID = newTagger.getUniqueId();

        // 1. Снимаем свечение со старого водящего и забираем 1 палочку
        oldTagger.setGlowing(false);
        oldTagger.getInventory().removeItem(new ItemStack(Material.STICK, 1));

        // 2. Включаем свечение новому и выдаем палочку
        newTagger.setGlowing(true);
        newTagger.getInventory().addItem(new ItemStack(Material.STICK, 1));

        // 3. Заморозка на 10 секунд (в Paper время указывается в тиках: 1 сек = 20 тиков, 10 сек = 200 тиков)
        newTagger.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 255, false, false));
        newTagger.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 255, false, false));

        // 4. Уведомления в чат
        oldTagger.sendMessage(ChatColor.GREEN + "Вы передали роль водящего!");
        newTagger.sendMessage(ChatColor.RED + "Теперь вы водите! Заморозка на 10 секунд.");
    }

    // --- 2. КОМАНДА /tagset <player> ---
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Использование: /tagset <ник_игрока>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Игрок не найден или оффлайн!");
            return true;
        }

        taggerUUID = target.getUniqueId();
        target.setGlowing(true);

        if (!target.getInventory().contains(Material.STICK)) {
            target.getInventory().addItem(new ItemStack(Material.STICK, 1));
        }

        sender.sendMessage(ChatColor.GOLD + "Игрок " + ChatColor.WHITE + target.getName() + ChatColor.GOLD + " назначен водящим!");
        target.sendMessage(ChatColor.RED + "Вы были назначены водящим!");

        return true;
    }
}