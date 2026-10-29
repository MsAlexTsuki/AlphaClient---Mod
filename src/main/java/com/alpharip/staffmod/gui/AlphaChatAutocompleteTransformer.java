package com.alpharip.staffmod.gui;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

/** Applies rank filtering to both autocomplete responses and the final local suggestion list. */
public final class AlphaChatAutocompleteTransformer implements IClassTransformer {
    private static final String TARGET = "net.minecraft.client.gui.GuiChat";
    private static final String BRIDGE = "com/alpharip/staffmod/permissions/AlphaChatAutocomplete";
    private static final String STRING_ARRAY = "[Ljava/lang/String;";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !TARGET.equals(transformedName)) return basicClass;
        try {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);
            boolean changed = false;

            // Filter server responses when Forge routes them through GuiChat.
            for (MethodNode method : node.methods) {
                if (!"([Ljava/lang/String;)V".equals(method.desc)) continue;
                if (!("onAutocompleteResponse".equals(method.name)
                        || "func_146406_a".equals(method.name) || "a".equals(method.name))) continue;
                if (containsFilterHook(method)) continue;
                AbstractInsnNode first = firstReal(method);
                if (first == null) continue;
                InsnList hook = new InsnList();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "filter",
                        "([Ljava/lang/String;)[Ljava/lang/String;", false));
                hook.add(new VarInsnNode(Opcodes.ASTORE, 1));
                method.instructions.insertBefore(first, hook);
                changed = true;
            }

            // Forge client commands are also collected locally. Filter the stored
            // suggestion array immediately before GuiChat cycles/displays it.
            String suggestionsField = null;
            for (FieldNode field : node.fields) {
                if (STRING_ARRAY.equals(field.desc)) {
                    suggestionsField = field.name;
                    if ("foundPlayerNames".equals(field.name) || "field_146412_t".equals(field.name)) break;
                }
            }
            if (suggestionsField != null) {
                for (MethodNode method : node.methods) {
                    if (!"()V".equals(method.desc)) continue;
                    if (!("autocompletePlayerNames".equals(method.name)
                            || "func_146404_p".equals(method.name) || "p".equals(method.name))) continue;
                    if (containsFilterHook(method)) continue;
                    AbstractInsnNode first = firstReal(method);
                    if (first == null) continue;
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, suggestionsField, STRING_ARRAY));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "filter",
                            "([Ljava/lang/String;)[Ljava/lang/String;", false));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    // Swap filtered array and this so PUTFIELD receives (object, array).
                    hook.add(new org.objectweb.asm.tree.InsnNode(Opcodes.SWAP));
                    hook.add(new FieldInsnNode(Opcodes.PUTFIELD, node.name, suggestionsField, STRING_ARRAY));
                    method.instructions.insertBefore(first, hook);
                    changed = true;
                }
            }

            if (!changed) {
                System.err.println("[AlphaClient] GuiChat autocomplete hooks were not found; suggestions unchanged.");
                return basicClass;
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] GuiChat autocomplete filter failed; suggestions unchanged.");
            error.printStackTrace();
            return basicClass;
        }
    }

    private static AbstractInsnNode firstReal(MethodNode method) {
        AbstractInsnNode instruction = method.instructions.getFirst();
        while (instruction != null && instruction.getOpcode() < 0) instruction = instruction.getNext();
        return instruction;
    }

    private static boolean containsFilterHook(MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (BRIDGE.equals(call.owner) && "filter".equals(call.name)) return true;
            }
        }
        return false;
    }
}
