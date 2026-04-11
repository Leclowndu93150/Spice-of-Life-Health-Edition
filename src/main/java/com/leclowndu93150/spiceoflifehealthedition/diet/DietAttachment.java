package com.leclowndu93150.spiceoflifehealthedition.diet;

import com.leclowndu93150.spiceoflifehealthedition.SpiceOfLifeHealthEdition;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class DietAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SpiceOfLifeHealthEdition.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DietHistory>> DIET =
            ATTACHMENT_TYPES.register("diet", () ->
                    AttachmentType.builder(DietHistory::new)
                            .serialize(DietHistory.CODEC)
                            .copyOnDeath()
                            .sync(DietHistory.STREAM_CODEC)
                            .build());
}
