package com.lighty.tygertraits.utiils;

import com.lighty.tygertraits.TygerTraitsPlugin;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

import com.lighty.tygertraits.objects.Trait;
import com.nftworlds.wallet.api.WalletAPI;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONArray;
import org.json.JSONObject;

public class Methods {
    public static String chatColor(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String getTraitConfigName(String traitName) {
        return traitName.replace(' ', '-');
    }

    public static ItemStack createConfigItem(String config) {
        Material material = Material.getMaterial(Objects.requireNonNull(TygerTraitsPlugin.getPlugin().getConfig().getString(config + ".material")));
        String displayName = chatColor(TygerTraitsPlugin.getPlugin().getConfig().getString(config + ".display-name"));
        int customModelData = TygerTraitsPlugin.getPlugin().getConfig().getInt(config + ".custom-model-data");
        ArrayList<String> lore = (ArrayList<String>) TygerTraitsPlugin.getPlugin().getConfig().getStringList(config + ".lore");

        assert material != null;
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(displayName);
        itemMeta.setCustomModelData(customModelData);
        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static boolean checkTraitConfig(String traitName, String category) {
        FileConfiguration config = TygerTraitsPlugin.getPlugin().getConfig();
        String configPath = "traits." + getTraitConfigName(category).toLowerCase() + "." + getTraitConfigName(traitName) + ".material";

        if(Material.getMaterial(config.getString(configPath)) == null) return false;
        else return true;
    }

    @SneakyThrows
    public static JSONObject getTygerMetadata(Integer tokenID) {
        JSONObject json = new JSONObject(IOUtils.toString(new URL("https://metadata.tygrnft.com/" + tokenID), StandardCharsets.UTF_8));
        return json;
    }

    @SneakyThrows
    public static void loadTraitsFromCollection() {
        Bukkit.getScheduler().runTaskAsynchronously(TygerTraitsPlugin.getPlugin(), new Runnable() {
            @Override @SneakyThrows
            public void run() {
                Bukkit.getLogger().info(Methods.chatColor("&aLoading traits from the Tygers NFT collection..."));
                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://api.opensea.io/api/v1/collection/tygrcuballiance")
                        .get()
                        .build();

                Response response = client.newCall(request).execute();
                JSONObject result = new JSONObject(Objects.requireNonNull(response.body()).string());
                JSONObject traits = result.getJSONObject("collection").getJSONObject("traits");

                TygerTraitsPlugin.getLoadedTraits().clear();
                traits.toMap().forEach((key, value) -> {
                    JSONObject slot = traits.getJSONObject(key);
                    slot.toMap().forEach((trait, count) -> {
                        if(checkTraitConfig(trait, key)) {
                            Trait loadedTrait = new Trait(trait, key);
                            TygerTraitsPlugin.getLoadedTraits().add(loadedTrait);
                            Bukkit.getLogger().info(chatColor(" &7- &aLoaded: &e" + loadedTrait.getTraitName()));
                        }
                    });

                });
                Bukkit.getLogger().info(Methods.chatColor("&aSuccesfully registered " + TygerTraitsPlugin.getLoadedTraits().size() + " traits from the Tygers NFT collection!"));
                loadAllPlayerTraits();
            }
        });
    }

    @SneakyThrows
    public static void loadPlayerTraits(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(TygerTraitsPlugin.getPlugin(), new Runnable() {
            @Override @SneakyThrows
            public void run() {
                WalletAPI walletAPI = TygerTraitsPlugin.getWalletAPI();
                if(!walletAPI.getNFTPlayer(player).isLinked()) return;

                String playerWallet = walletAPI.getPrimaryWallet(player).getAddress();

                OkHttpClient client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("https://deep-index.moralis.io/api/v2/" + playerWallet + "/nft/0x9558fAe06F6d49fAAC337e9443e69f41967c25bC?chain=eth&format=decimal")
                        .get()
                        .addHeader("X-API-KEY", "k4TJtpaAoL3RPFR2XLCAbRw9mmZPMdzCxs9xAgCFrLdeQA7GLacGKURFkTPAzsJt")
                        .build();

                Response response = client.newCall(request).execute();
                JSONObject responseJSON = new JSONObject(Objects.requireNonNull(response.body()).string());
                JSONArray ownedNFTs =responseJSON.getJSONArray("result");

                TygerTraitsPlugin.getPlayerTraits().remove(player);
                TygerTraitsPlugin.getPlayerTraits().put(player, new ArrayList<>());
                TygerTraitsPlugin.getPlayerTraits().get(player).clear();
                ownedNFTs.forEach(ownedNFT -> {
                    JSONObject nft = (JSONObject) ownedNFT;
                    int tokenID = nft.getInt("token_id");
                    JSONArray nftTraits = getTygerMetadata(tokenID).getJSONArray("attributes");

                    nftTraits.forEach(traitMetadata -> {
                        JSONObject traitJSON = (JSONObject) traitMetadata;
                        String traitSlot = traitJSON.getString("trait_type").toLowerCase();
                        String traitName = traitJSON.getString("value").toLowerCase();

                        if(checkTraitConfig(traitName, traitSlot)) {
                            Trait loadedTrait = new Trait(traitName, traitSlot);
                            TygerTraitsPlugin.getPlayerTraits().get(player).add(loadedTrait);
                        }
                    });
                });
                TygerTraitsPlugin.getPlayerTraits().get(player).forEach(trait -> {
                    player.getInventory().addItem(trait.getTraitItem());
                    if(trait.getTraitBonusItem() != null) player.getInventory().addItem(trait.getTraitBonusItem());
                });
            }
        });

    }

    public static void loadAllPlayerTraits() {
        Bukkit.getScheduler().runTaskAsynchronously(TygerTraitsPlugin.getPlugin(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getLogger().info(chatColor("&aLoading and registering traits of players..."));
                for(Player player : Bukkit.getOnlinePlayers()) {
                    loadPlayerTraits(player);
                }
                Bukkit.getLogger().info(chatColor("&aSucessfully loaded and registered traits for players!"));
            }
        });
    }

}
