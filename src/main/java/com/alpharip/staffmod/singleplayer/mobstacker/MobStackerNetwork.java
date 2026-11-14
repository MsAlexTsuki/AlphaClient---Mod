// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.singleplayer.mobstacker;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/** Synchronizes Mob Stacker counts from the integrated server to the client. */
public final class MobStackerNetwork {
    private static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel("alphamobstack");

    static {
        CHANNEL.registerMessage(ClientHandler.class, StackCountMessage.class, 0, Side.CLIENT);
    }

    private MobStackerNetwork() {
    }

    /** Forces this class to initialize while the mod is still starting up. */
    static void initialize() {
    }

    static void sync(EntityLiving entity, int count) {
        if (entity == null || entity.worldObj == null || entity.worldObj.isRemote
                || entity.worldObj.getEntityByID(entity.getEntityId()) != entity) {
            return;
        }

        StackCountMessage message = new StackCountMessage(entity.getEntityId(), count);
        for (EntityPlayer player : entity.worldObj.playerEntities) {
            if (player instanceof EntityPlayerMP) {
                CHANNEL.sendTo(message, (EntityPlayerMP) player);
            }
        }
    }

    public static final class StackCountMessage implements IMessage {
        private int entityId;
        private int count;

        public StackCountMessage() {
        }

        StackCountMessage(int entityId, int count) {
            this.entityId = entityId;
            this.count = count;
        }

        @Override
        public void fromBytes(ByteBuf buffer) {
            entityId = buffer.readInt();
            count = buffer.readInt();
        }

        @Override
        public void toBytes(ByteBuf buffer) {
            buffer.writeInt(entityId);
            buffer.writeInt(count);
        }
    }

    public static final class ClientHandler
            implements IMessageHandler<StackCountMessage, IMessage> {
        @Override
        public IMessage onMessage(final StackCountMessage message, MessageContext context) {
            Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    Minecraft minecraft = Minecraft.getMinecraft();
                    World world = minecraft.theWorld;
                    if (world == null) {
                        return;
                    }

                    Entity entity = world.getEntityByID(message.entityId);
                    if (entity instanceof EntityLiving) {
                        ((EntityLiving) entity).getEntityData().setInteger(
                                MobStackerMod.STACK_COUNT_KEY, Math.max(1, message.count)
                        );
                    }
                }
            });
            return null;
        }
    }
}
