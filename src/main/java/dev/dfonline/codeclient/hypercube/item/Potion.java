package dev.dfonline.codeclient.hypercube.item;

import com.google.gson.JsonObject;
import dev.dfonline.codeclient.Utility;
import dev.dfonline.codeclient.hypercube.actiondump.ActionDump;
import dev.dfonline.codeclient.hypercube.actiondump.Icon;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

public class Potion extends VarItem {
    private String potion;
    private int duration;
    private int amplifier;

    public Potion(Item material, JsonObject var) {
        super(material, var);
        this.potion = data.get("pot").getAsString();
        this.duration = data.get("dur").getAsInt();
        this.amplifier = data.get("amp").getAsInt();
    }

    public static String durationToString(int duration) {
        if (duration >= 1000000) return "Infinite";
        if (duration % 20 != 0) return "%d ticks".formatted(duration);
        int seconds = duration / 20;
        return "%d:%d".formatted(seconds / 60, seconds % 60);
    }

    public String getPotion() {
        return potion;
    }

    public void setPotion(String potion) {
        data.addProperty("pot", potion);
        this.potion = potion;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        data.addProperty("dur", duration);
        this.duration = duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public void setAmplifier(int amplifier) {
        data.addProperty("amp", amplifier);
        this.amplifier = amplifier;
    }

    @Override
    public ItemStack toStack() {
        ItemStack stack = super.toStack();
        stack.setCustomName(Text.literal("Potion Effect").setStyle(Style.EMPTY.withItalic(false).withColor(Icon.Type.POTION.color)));
        Text name;
        try {
            ActionDump db = ActionDump.getActionDump();
            var value = Arrays.stream(db.potions).filter(gv -> gv.icon.getCleanName().equals(potion)).findFirst();
            if (value.isEmpty()) throw new Exception("");
            name = Text.literal(value.get().icon.name);
        } catch (Exception e) {
            name = Text.literal(potion).setStyle(Style.EMPTY);
        }
        Utility.addLore(stack,
                name,
                Text.empty(),
                Text.empty().append(Text.literal("Amplifier: ").formatted(Formatting.GRAY)).append(Text.literal(String.valueOf(amplifier + 1))),
                Text.empty().append(Text.literal("Duration: ").formatted(Formatting.GRAY)).append(Text.literal(duration())));
        return stack;
    }

    public String duration() {
        return durationToString(duration);
    }
}
