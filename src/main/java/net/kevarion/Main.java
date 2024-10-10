package net.kevarion;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.event.*;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PickupItemEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerStartSneakingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.time.Duration;

public class Main {
    public static void main(String[] args) {

        System.out.println("Activated. Hello, World!");

        MinecraftServer server = MinecraftServer.init();

        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        instanceContainer.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK);
        });

        instanceContainer.setChunkSupplier(LightingChunk::new);

        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });

        globalEventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
           final Player player = event.getPlayer();
           System.out.println(player.getUsername() + " has broken a block.");

           var material = event.getBlock().registry().material();

           if (material != null) {
               var itemStack = ItemStack.of(material);
               ItemEntity itemEntity = new ItemEntity(itemStack);
               itemEntity.setInstance(event.getInstance(), event.getBlockPosition().add(0.5, 0.5, 0.5));
               itemEntity.setPickupDelay(Duration.ofMillis(500));
           }

        });

        EventNode<Event> allNode = EventNode.all("all");
        allNode.addListener(PickupItemEvent.class, event -> {
            System.out.println("Player picked up an item!");

            var itemStack = event.getItemStack();
            if (event.getLivingEntity() instanceof Player player) {
                player.getInventory().addItemStack(itemStack);
            }
        });

        var playerNode = EventNode.type("players", EventFilter.PLAYER);
        playerNode.addListener(ItemDropEvent.class, event -> {
            System.out.println("Player dropped an item.");
            ItemEntity itemEntity = new ItemEntity(event.getItemStack());
            itemEntity.setInstance(event.getInstance(), event.getPlayer().getPosition());
            itemEntity.setVelocity(event.getPlayer().getPosition().add(0, 5, 0).direction().mul(6));
            itemEntity.setPickupDelay(Duration.ofMillis(500));
        });
        allNode.addChild(playerNode);

        //basically what this does, is it only applies events to players that are on the ground, this is actually very cool.
        //meaning, you can make some events apply to certain people depending on what they're doing, not just if they're on the ground.
        var groundedPlayersNode = EventNode.value("groundedPlayers", EventFilter.PLAYER, Player::isOnGround);
        groundedPlayersNode.addListener(EventListener.builder(PlayerStartSneakingEvent.class)
                //expireCount == basically if you do this event <int> times, it will not run anymore.
                .expireCount(3)
                        //expireWhen == basically, in this case, when you hold a grass block in your main hand, the event will stop listening.
                        .expireWhen(playerStartSneakingEvent -> {
                            if (playerStartSneakingEvent.getPlayer().getInventory().getItemInMainHand().material() == Material.GRASS_BLOCK) {
                                playerStartSneakingEvent.getPlayer().sendMessage("You found grass. Good job!");
                                return true;
                            }
                            return false;
                        })
                        .filter(playerStartSneakingEvent -> {
                            return playerStartSneakingEvent.getPlayer().isEating();
                        })
                .handler(event -> {
                    System.out.println("Player started sneaking on the ground.");
                }).build()
        );
        playerNode.addChild(groundedPlayersNode);

        globalEventHandler.addChild(allNode);

        MojangAuth.init();

        server.start("0.0.0.0", 25565);

    }
}