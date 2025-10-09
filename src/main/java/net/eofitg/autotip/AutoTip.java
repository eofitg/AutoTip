package net.eofitg.autotip;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.eofitg.autotip.command.AutoTipCommand;
import net.eofitg.autotip.config.AutoTipConfig;
import net.eofitg.autotip.util.PlayerUtil;
import net.eofitg.autotip.util.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.apache.commons.io.FileUtils;

import java.io.File;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.MOD_VERSION,
        acceptedMinecraftVersions = "[1.8.9]",
        clientSideOnly = true
)
public class AutoTip {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile = null;
    public static AutoTipConfig config;

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static long lastTipTime = -1;
    private static long checkCounter = -1;
    private static String currentServer = "";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        configFile = new File(e.getModConfigurationDirectory(), "auto-tip.json");
        loadConfig();
        Runtime.getRuntime().addShutdownHook(new Thread(AutoTip::saveConfig));
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new AutoTipCommand());
    }

    public static void loadConfig() {
        if (configFile == null) return;
        if (configFile.exists()) {
            try {
                String json = FileUtils.readFileToString(configFile);
                config = gson.fromJson(json, AutoTipConfig.class);
            } catch (Exception ignored) {}
        } else {
            config = new AutoTipConfig();
            saveConfig();
        }
    }

    public static void saveConfig() {
        if (configFile == null) return;
        try {
            String json = gson.toJson(config);
            FileUtils.write(configFile, json);
        } catch (Exception ignored) {}
    }

    @SubscribeEvent
    public void onServerConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        if (mc == null) return;
        ServerData server = mc.getCurrentServerData();
        if (server != null && server.serverIP != null) {
            currentServer = server.serverIP;
        }
    }

    @SubscribeEvent
    public void onServerDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        currentServer = "";
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc == null || mc.thePlayer == null) return;
        if (!serverCheck()) return;

        long timeRemaining = (lastTipTime + config.interval * 1000L) - System.currentTimeMillis();
        int checkInterval;
        if (timeRemaining <= 5000) {
            checkInterval = 20;
        } else if (timeRemaining <= 30000) {
            checkInterval = 40;
        } else if (timeRemaining <= 60000) {
            checkInterval = 200;
        } else {
            checkInterval = 600;
        }

        if (++checkCounter >= checkInterval) {
            checkCounter = 0;
            if (lastTipTime + config.interval * 1000L <= System.currentTimeMillis()) {
                lastTipTime = System.currentTimeMillis();
                PlayerUtil.sendMessage("/tip all");
            }
        }
    }

    private boolean serverCheck() {
        if (currentServer.isEmpty()) return false;
        for (String ip : AutoTip.config.servers) {
            if (currentServer.contains(ip)) return true;
        }
        return false;
    }

}
