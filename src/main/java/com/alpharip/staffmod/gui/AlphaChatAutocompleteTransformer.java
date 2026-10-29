package com.alpharip.staffmod.gui;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/** Applies rank filtering to GuiChat autocomplete responses only. */
public final class AlphaChatAutocompleteTransformer implements IClassTransformer {
    private static final String TARGET = "net.minecraft.client.gui.GuiChat";
    private static final String BRIDGE = "com/alpharip/staffmod/permissions/AlphaChatAutocomplete";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET.equals(transformedName)) return basicClass;
        try {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);
            for (MethodNode method : node.methods) {
                if (!"([Ljava/lang/String;)V".equals(method.desc)) continue;
                if (!("onAutocompleteResponse".equals(method.name)
                        || "func_146406_a".equals(method.name) || "a".equals(method.name))) continue;
                AbstractInsnNode first = method.instructions.getFirst();
                while (first != null && first.getOpcode() < 0) first = first.getNext();
                if (first == null) continue;
                org.objectweb.asm.tree.InsnList hook = new org.objectweb.asm.tree.InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "filter",
                        "([Ljava/lang/String;)[Ljava/lang/String;", false));
                hook.add(new VarInsnNode(Opcodes.ASTORE, 1));
                method.instructions.insertBefore(first, hook);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                node.accept(writer);
                return writer.toByteArray();
            }
            System.err.println("[AlphaClient] GuiChat autocomplete hook not found; suggestions unchanged.");
        } catch (Throwable error) {
            System.err.println("[AlphaClient] GuiChat autocomplete filter failed; suggestions unchanged.");
            error.printStackTrace();
        }
        return basicClass;
    }
}
