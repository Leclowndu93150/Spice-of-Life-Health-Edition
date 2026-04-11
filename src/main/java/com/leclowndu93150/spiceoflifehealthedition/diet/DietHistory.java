package com.leclowndu93150.spiceoflifehealthedition.diet;

import com.leclowndu93150.spiceoflifehealthedition.Config;
import com.leclowndu93150.spiceoflifehealthedition.api.NutritionalProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DietHistory {

    public static final Codec<DietHistory> CODEC = RecordCodecBuilder.create(i -> i.group(
            DietEntry.CODEC.listOf().fieldOf("entries").forGetter(h -> h.entries),
            Codec.FLOAT.optionalFieldOf("weight", 70f).forGetter(h -> h.weight),
            Codec.FLOAT.optionalFieldOf("exerciseBuffer", 0f).forGetter(h -> h.exerciseBuffer)
    ).apply(i, DietHistory::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DietHistory> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public DietHistory decode(RegistryFriendlyByteBuf buf) {
            float weight = buf.readFloat();
            float exercise = buf.readFloat();
            int size = buf.readVarInt();
            List<DietEntry> entries = new ArrayList<>(size);
            for (int j = 0; j < size; j++) {
                ResourceLocation id = ResourceLocation.STREAM_CODEC.decode(buf);
                NutritionalProfile profile = NutritionalProfile.STREAM_CODEC.decode(buf);
                long time = buf.readLong();
                entries.add(new DietEntry(id, profile, time));
            }
            return new DietHistory(entries, weight, exercise);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, DietHistory history) {
            buf.writeFloat(history.weight);
            buf.writeFloat(history.exerciseBuffer);
            buf.writeVarInt(history.entries.size());
            for (DietEntry entry : history.entries) {
                ResourceLocation.STREAM_CODEC.encode(buf, entry.foodId());
                NutritionalProfile.STREAM_CODEC.encode(buf, entry.nutrition());
                buf.writeLong(entry.gameTime());
            }
        }
    };

    private final List<DietEntry> entries;
    private float weight;
    private float exerciseBuffer;
    private NutritionalProfile cachedCumulative;

    public DietHistory() {
        this(new ArrayList<>(), 70f, 0f);
    }

    public DietHistory(List<DietEntry> entries, float weight, float exerciseBuffer) {
        this.entries = new ArrayList<>(entries);
        this.weight = weight;
        this.exerciseBuffer = exerciseBuffer;
        recalcCumulative();
    }

    public void addFood(ResourceLocation foodId, NutritionalProfile nutrition, long gameTime) {
        entries.add(new DietEntry(foodId, nutrition, gameTime));
        while (entries.size() > Config.dietHistorySize) {
            entries.remove(0);
        }
        recalcCumulative();
    }

    public void clearEmptyEntries() {
        entries.removeIf(e -> e.nutrition().isEmpty());
        recalcCumulative();
    }

    public void clear() {
        entries.clear();
        recalcCumulative();
    }

    private static final int BASELINE_WEIGHT = 10;

    public NutritionalProfile getCumulative() {
        return cachedCumulative;
    }

    public NutritionalProfile getRawAverage() {
        if (entries.isEmpty()) return NutritionalProfile.BASELINE;
        return cachedCumulative.divide(entries.size());
    }

    public NutritionalProfile getAverage() {
        NutritionalProfile baselineContribution = NutritionalProfile.BASELINE.scale((float) BASELINE_WEIGHT);
        NutritionalProfile total = cachedCumulative.add(baselineContribution);
        return total.divide(entries.size() + BASELINE_WEIGHT);
    }

    public int getDiversity() {
        Set<ResourceLocation> unique = new HashSet<>();
        for (DietEntry e : entries) {
            unique.add(e.foodId());
        }
        return unique.size();
    }

    public int getEntryCount() {
        return entries.size();
    }

    public List<DietEntry> getRecentEntries(int count) {
        int start = Math.max(0, entries.size() - count);
        return List.copyOf(entries.subList(start, entries.size()));
    }

    public List<GroupedEntry> getGroupedRecent(int maxGroups) {
        List<GroupedEntry> grouped = new ArrayList<>();
        for (int i = entries.size() - 1; i >= 0; i--) {
            DietEntry entry = entries.get(i);
            if (!grouped.isEmpty()) {
                GroupedEntry last = grouped.get(grouped.size() - 1);
                if (last.foodId.equals(entry.foodId())) {
                    last.count++;
                    last.latestTime = Math.max(last.latestTime, entry.gameTime());
                    continue;
                }
            }
            grouped.add(new GroupedEntry(entry.foodId(), entry.nutrition(), entry.gameTime(), 1));
            if (grouped.size() >= maxGroups) break;
        }
        return grouped;
    }

    public static class GroupedEntry {
        public final ResourceLocation foodId;
        public final NutritionalProfile nutrition;
        public long latestTime;
        public int count;

        public GroupedEntry(ResourceLocation foodId, NutritionalProfile nutrition, long latestTime, int count) {
            this.foodId = foodId;
            this.nutrition = nutrition;
            this.latestTime = latestTime;
            this.count = count;
        }
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = Math.max(40f, Math.min(200f, weight));
    }

    public float getExerciseBuffer() {
        return exerciseBuffer;
    }

    public void addExercise(float amount) {
        this.exerciseBuffer += amount;
    }

    public float consumeExerciseBuffer() {
        float val = exerciseBuffer;
        exerciseBuffer = 0;
        return val;
    }

    public void addWeightFromFood(NutritionalProfile profile) {
        float gain = profile.total() * 0.1f;
        gain += profile.fat() * 0.05f;
        gain += profile.sugar() * 0.03f;
        setWeight(weight + gain);
    }

    private void recalcCumulative() {
        NutritionalProfile sum = NutritionalProfile.EMPTY;
        for (DietEntry e : entries) {
            sum = sum.add(e.nutrition());
        }
        cachedCumulative = sum;
    }
}
