package gigaherz.jsonthings.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.resources.*;
import net.minecraft.util.Unit;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class ThingResourceManager
{
    private static final Method M_CREATE = ObfuscationReflectionHelper.findMethod(ResourcePackType.class, "create", String.class, String.class);
    private static final ResourcePackType PACK_TYPE_THINGS;
    static {
        try
        {
            PACK_TYPE_THINGS = (ResourcePackType) M_CREATE.invoke(null, "JSONTHINGS_THINGS", "things");
        }
        catch (IllegalAccessException | InvocationTargetException e)
        {
            throw new RuntimeException("Error calling private method", e);
        }
    }
    public static void staticInit()
    {
        /* do nothing */
    }

    public static final ThingResourceManager INSTANCE = new ThingResourceManager();

    private final IReloadableResourceManager resourceManager;
    private final IPackFinder folderPackFinder;
    private final ResourcePackList packList;
    public final BlockParser blockParser = new BlockParser();
    public final ItemParser itemParser = new ItemParser();

    public ThingResourceManager()
    {
        resourceManager = new SimpleReloadableResourceManager(PACK_TYPE_THINGS);
        folderPackFinder = new FolderPackFinder(getResourcepacksLocation(), IPackNameDecorator.PLAIN);
        packList = new ResourcePackList(folderPackFinder);
        resourceManager.addReloadListener(blockParser);
        resourceManager.addReloadListener(itemParser);
    }

    public IPackFinder getFolderPackFinder()
    {
        return (infoConsumer, infoFactory) -> folderPackFinder.findPacks(info -> {
            if (!disabledPacks.contains(info.getName()))
                infoConsumer.accept(info);
        }, (a,b,c,d,e,f,g) -> infoFactory.create(a,true,c,d,e,f,g));
    }

    public File getResourcepacksLocation()
    {
        return FMLPaths.GAMEDIR.get().resolve("thingpacks").toFile();
    }

    /**
     *  Call during mod construction **without enqueueWork**!
     */
    public synchronized void addPackFinder(IPackFinder finder)
    {
        packList.addPackFinder(finder);
    }

    /**
     *  Call during mod construction **without enqueueWork**!
     */
    public synchronized void addResourceReloadListener(IFutureReloadListener listener)
    {
        resourceManager.addReloadListener(listener);
    }

    private static final Set<String> disabledPacks = Sets.newHashSet();

    private static final CompletableFuture<Unit> COMPLETED_FUTURE = CompletableFuture.completedFuture(Unit.INSTANCE);
    public static CompletableFuture<ThingResourceManager> init(Executor backgroundExecutor, Executor gameExecutor)
    {
        INSTANCE.packList.reloadPacksFromFinders();

        List<String> enabledPacks = Lists.newArrayList();
        for(ResourcePackInfo s : INSTANCE.packList.getAllPacks())
        {
            if (!disabledPacks.contains(s.getName()))
                enabledPacks.add(s.getName());
        }
        INSTANCE.packList.setEnabledPacks(enabledPacks);

        CompletableFuture<Unit> completablefuture = INSTANCE.resourceManager.reloadResourcesAndThen(backgroundExecutor, gameExecutor, INSTANCE.packList.func_232623_f_(), COMPLETED_FUTURE);
        return completablefuture.whenComplete((unit, throwable) -> {
            if (throwable != null) {
                INSTANCE.resourceManager.close();
            }
        }).thenApply((unit) -> INSTANCE);
    }

    public ResourcePackList getResourcePackList()
    {
        return packList;
    }
}
