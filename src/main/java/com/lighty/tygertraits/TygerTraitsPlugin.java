package com.lighty.tygertraits;

import com.lighty.tygertraits.objects.Trait;
import com.lighty.tygertraits.utiils.Methods;
import com.nftworlds.wallet.api.WalletAPI;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;

public final class TygerTraitsPlugin extends JavaPlugin {

    @Getter private static TygerTraitsPlugin plugin;
    @Getter private static WalletAPI walletAPI;
    @Getter private static BukkitAPIHelper mmAPI;
    @Getter private static ArrayList<Trait> loadedTraits;
    @Getter private static HashMap<Player, ArrayList<Trait>> playerTraits;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        walletAPI = new WalletAPI();
        mmAPI = new BukkitAPIHelper();

        plugin.saveDefaultConfig();

        loadedTraits = new ArrayList<>();
        playerTraits = new HashMap<>();

        Methods.loadTraitsFromCollection();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
