package com.lighty.tygertraits.objects;

import com.lighty.tygertraits.utiils.Methods;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class Trait {

    @Getter public  ItemStack traitItem;
    @Getter public  ItemStack traitBonusItem = null;
    @Getter public  String traitConfig;
    @Getter public  String traitName;
    @Getter public  String traitCategory;
    @Getter public  String traitSkill;


    public Trait(String name, String slot) {
        traitName = name;
        traitCategory = slot;
        traitConfig = "traits." + slot.toLowerCase() + "." + Methods.getTraitConfigName(traitName).toLowerCase();
        traitItem = Methods.createConfigItem(traitConfig);
        traitSkill = Methods.getTraitConfigName(traitCategory).toLowerCase() + "-" + Methods.getTraitConfigName(traitName).toLowerCase();
        if(traitCategory.equalsIgnoreCase("suit")) {
            traitBonusItem = traitItem.clone();
            String[] materialSplit = traitBonusItem.getType().name().split("_");
            String material = materialSplit[0] + "_LEGGINGS";
            traitBonusItem.setType(Objects.requireNonNull(Material.getMaterial(material)));
        }
    }

}
