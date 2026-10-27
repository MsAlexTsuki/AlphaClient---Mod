// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.network;


import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import java.lang.reflect.Field;

public class PacketSniffer extends ChannelDuplexHandler {

    public static final String HANDLER_NAME = "alphastaffmod_sniffer";

    private static final int ANIMATION_SWING_ARM = 0;

    private static Field animationEntityIdField = null;
    private static Field animationTypeField = null;
    private static boolean animationFieldsSearched = false;

    private static Field velocityEntityIdField = null;
    private static Field velocityMotionXField = null;
    private static Field velocityMotionYField = null;
    private static Field velocityMotionZField = null;
    private static boolean velocityFieldsSearched = false;

    @Override
    public void channelRead(
            ChannelHandlerContext ctx,
            Object msg)
            throws Exception {

        super.channelRead(ctx, msg);
    }

    private static int getAnimationEntityId(
            S0BPacketAnimation packet) {

        try {

            if (packet == null) {
                return Integer.MIN_VALUE;
            }

            if (!animationFieldsSearched) {
                findAnimationFields();
            }

            if (animationEntityIdField == null) {
                return Integer.MIN_VALUE;
            }

            return animationEntityIdField.getInt(packet);

        } catch (Throwable t) {

            return Integer.MIN_VALUE;
        }
    }

    private static int getAnimationType(
            S0BPacketAnimation packet) {

        try {

            if (packet == null) {
                return -1;
            }

            if (!animationFieldsSearched) {
                findAnimationFields();
            }

            if (animationTypeField == null) {
                return -1;
            }

            return animationTypeField.getInt(packet);

        } catch (Throwable t) {

            return -1;
        }
    }

    private static void findAnimationFields() {

        if (animationFieldsSearched) {
            return;
        }

        animationFieldsSearched = true;

        try {

            Field[] fields =
                    S0BPacketAnimation.class.getDeclaredFields();

            int intFieldCount = 0;

            for (Field field : fields) {

                if (field == null) {
                    continue;
                }

                if (field.getType() != Integer.TYPE) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                } catch (Throwable ignored) {
                }

                intFieldCount++;

                if (intFieldCount == 1) {
                    animationEntityIdField = field;
                }

                if (intFieldCount == 2) {
                    animationTypeField = field;
                    return;
                }
            }

        } catch (Throwable t) {

            animationEntityIdField = null;
            animationTypeField = null;

            System.out.println(
                    "[AlphaStaffMod] Could not find S0B fields."
            );

            t.printStackTrace();
        }
    }

    private static int getVelocityEntityId(
            S12PacketEntityVelocity packet) {

        try {

            if (packet == null) {
                return Integer.MIN_VALUE;
            }

            if (!velocityFieldsSearched) {
                findVelocityFields();
            }

            if (velocityEntityIdField == null) {
                return Integer.MIN_VALUE;
            }

            return velocityEntityIdField.getInt(packet);

        } catch (Throwable t) {

            return Integer.MIN_VALUE;
        }
    }

    private static int getVelocityMotionX(
            S12PacketEntityVelocity packet) {

        try {

            if (packet == null) {
                return Integer.MIN_VALUE;
            }

            if (!velocityFieldsSearched) {
                findVelocityFields();
            }

            if (velocityMotionXField == null) {
                return Integer.MIN_VALUE;
            }

            return velocityMotionXField.getInt(packet);

        } catch (Throwable t) {

            return Integer.MIN_VALUE;
        }
    }

    private static int getVelocityMotionY(
            S12PacketEntityVelocity packet) {

        try {

            if (packet == null) {
                return Integer.MIN_VALUE;
            }

            if (!velocityFieldsSearched) {
                findVelocityFields();
            }

            if (velocityMotionYField == null) {
                return Integer.MIN_VALUE;
            }

            return velocityMotionYField.getInt(packet);

        } catch (Throwable t) {

            return Integer.MIN_VALUE;
        }
    }

    private static int getVelocityMotionZ(
            S12PacketEntityVelocity packet) {

        try {

            if (packet == null) {
                return Integer.MIN_VALUE;
            }

            if (!velocityFieldsSearched) {
                findVelocityFields();
            }

            if (velocityMotionZField == null) {
                return Integer.MIN_VALUE;
            }

            return velocityMotionZField.getInt(packet);

        } catch (Throwable t) {

            return Integer.MIN_VALUE;
        }
    }

    private static void findVelocityFields() {

        if (velocityFieldsSearched) {
            return;
        }

        velocityFieldsSearched = true;

        try {

            Field[] fields =
                    S12PacketEntityVelocity.class.getDeclaredFields();

            int intFieldCount = 0;

            for (Field field : fields) {

                if (field == null) {
                    continue;
                }

                if (field.getType() != Integer.TYPE) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                } catch (Throwable ignored) {
                }

                intFieldCount++;

                if (intFieldCount == 1) {
                    velocityEntityIdField = field;
                }

                if (intFieldCount == 2) {
                    velocityMotionXField = field;
                }

                if (intFieldCount == 3) {
                    velocityMotionYField = field;
                }

                if (intFieldCount == 4) {
                    velocityMotionZField = field;
                    return;
                }
            }

        } catch (Throwable t) {

            velocityEntityIdField = null;
            velocityMotionXField = null;
            velocityMotionYField = null;
            velocityMotionZField = null;

            System.out.println(
                    "[AlphaStaffMod] Could not find S12 fields."
            );

            t.printStackTrace();
        }
    }
}
