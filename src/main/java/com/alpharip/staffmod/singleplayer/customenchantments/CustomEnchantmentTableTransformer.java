package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Adds a result hook while leaving the vanilla enchanting GUI and costs intact. */
public final class CustomEnchantmentTableTransformer implements IClassTransformer {
    private static final String TARGET = "net.minecraft.inventory.ContainerEnchantment";
    private static final String HOOK = "com/alpharip/staffmod/singleplayer/customenchantments/CustomEnchantmentTableHooks";
    private static volatile boolean installed;

    public static boolean isInstalled() { return installed; }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET.equals(transformedName)) return basicClass;
        try {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);
            boolean changed = false;
            for (MethodNode method : node.methods) {
                if (!"(Lnet/minecraft/item/ItemStack;II)Ljava/util/List;".equals(method.desc)) continue;
                AbstractInsnNode[] instructions = method.instructions.toArray();
                for (AbstractInsnNode instruction : instructions) {
                    if (instruction.getOpcode() != Opcodes.ARETURN) continue;
                    InsnList hook = new InsnList();
                    hook.add(new InsnNode(Opcodes.DUP));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK, "replaceOne",
                            "(Ljava/util/List;Ljava/lang/Object;Lnet/minecraft/item/ItemStack;)Ljava/util/List;", false));
                    method.instructions.insertBefore(instruction, hook);
                    changed = true;
                }
            }
            if (!changed) return basicClass;
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            installed = true;
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Custom enchantment table transformer failed: " + error.getMessage());
            return basicClass;
        }
    }
}