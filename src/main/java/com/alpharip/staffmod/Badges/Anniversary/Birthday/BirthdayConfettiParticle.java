package com.alpharip.staffmod.badges.anniversary.birthday;

/** Renderer-neutral particle description passed to the future Minecraft particle adapter. */
public final class BirthdayConfettiParticle {
    private final double x;
    private final double y;
    private final double z;
    private final double motionX;
    private final double motionY;
    private final double motionZ;
    private final float red;
    private final float green;
    private final float blue;
    private final int lifetimeTicks;

    public BirthdayConfettiParticle(double x, double y, double z,
                                    double motionX, double motionY, double motionZ,
                                    float red, float green, float blue, int lifetimeTicks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.lifetimeTicks = lifetimeTicks;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getMotionX() { return motionX; }
    public double getMotionY() { return motionY; }
    public double getMotionZ() { return motionZ; }
    public float getRed() { return red; }
    public float getGreen() { return green; }
    public float getBlue() { return blue; }
    public int getLifetimeTicks() { return lifetimeTicks; }
}
