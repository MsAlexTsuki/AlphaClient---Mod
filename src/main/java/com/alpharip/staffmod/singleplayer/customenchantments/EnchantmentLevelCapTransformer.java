package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Extends vanilla enchantment caps while retaining each enchantment's own effect formula. */
public final class EnchantmentLevelCapTransformer implements IClassTransformer {
    private static final String PREFIX = "net.minecraft.enchantment.";
    private static volatile int patchedMethods;

    public static int getPatchedMethodCount() { return patchedMethods; }

    private static int capFor(String name) {
        if ("EnchantmentDamage".equals(name) || "EnchantmentProtection".equals(name)
                || "EnchantmentDigging".equals(name) || "EnchantmentDurability".equals(name)
                || "EnchantmentArrowDamage".equals(name) || "EnchantmentLootBonus".equals(name)
                || "EnchantmentOxygen".equals(name)) return 10;
        if ("EnchantmentArrowKnockback".equals(name) || "EnchantmentFireAspect".equals(name)
                || "EnchantmentKnockback".equals(name) || "EnchantmentWaterWalker".equals(name)) return 5;
        if ("EnchantmentArrowFire".equals(name) || "EnchantmentArrowInfinite".equals(name)
                || "EnchantmentWaterWorker".equals(name) || "EnchantmentUntouching".equals(name)) return 3;
        return 0;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || transformedName == null || !transformedName.startsWith(PREFIX))
            return basicClass;
        String simpleName = transformedName.substring(PREFIX.length());
        final int cap = capFor(simpleName);
        if (cap <= 0) return basicClass;
        try {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);
            boolean changed = false;
            for (MethodNode method : node.methods) {
                if (!"getMaxLevel".equals(method.name) || !"()I".equals(method.desc)) continue;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() != Opcodes.IRETURN) continue;
                    InsnList replacement = new InsnList();
                    replacement.add(new InsnNode(Opcodes.POP));
                    if (cap >= 0 && cap <= 5) replacement.add(new InsnNode(Opcodes.ICONST_0 + cap));
                    else {
                        replacement.add(new org.objectweb.asm.tree.IntInsnNode(Opcodes.BIPUSH, cap));
                    }
                    method.instructions.insertBefore(instruction, replacement);
                    changed = true;
                }
            }
            if (!changed) return basicClass;
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            patchedMethods++;
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Vanilla enchantment level transformer failed for "
                    + transformedName + ": " + error.getMessage());
            return basicClass;
        }
    }
}