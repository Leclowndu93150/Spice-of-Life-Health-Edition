package com.leclowndu93150.spiceoflifehealthedition.nutrition;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class NutritionReloadListener implements PreparableReloadListener {

    private final ReloadableServerResources serverResources;
    private final RegistryAccess registryAccess;

    public NutritionReloadListener(ReloadableServerResources serverResources, RegistryAccess registryAccess) {
        this.serverResources = serverResources;
        this.registryAccess = registryAccess;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier,
                                           ResourceManager resourceManager,
                                           ProfilerFiller preparationsProfiler,
                                           ProfilerFiller reloadProfiler,
                                           Executor backgroundExecutor,
                                           Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> null, backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(unused -> {
                    NutritionManager.get().recompute(
                            serverResources.getRecipeManager(),
                            registryAccess
                    );
                }, gameExecutor);
    }
}
