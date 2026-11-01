package com.alpharip.staffmod.gui;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Filters AlphaClient command suggestions and requires a leading slash for
 * Forge client commands. GuiChat's server response hook is retained.
 */
public final class AlphaChatAutocompleteTransformer implements IClassTransformer {
    private static final String GUI_CHAT = "net.minecraft.client.gui.GuiChat";
    private static final String CLIENT_COMMAND_HANDLER = "net.minecraftforge.client.ClientCommandHandler";
    private static final String BRIDGE = "com/alpharip/staffmod/permissions/AlphaChatAutocomplete";
    private static final String ARRAY_DESC = "[Ljava/lang/String;";
    private static final String COMMAND_DESC =
            "(Lnet/minecraft/command/ICommandSender;Ljava/lang/String;)I";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return basicClass;
        if (GUI_CHAT.equals(transformedName)) return transformGuiChat(basicClass);
        if (CLIENT_COMMAND_HANDLER.equals(transformedName)) return transformClientCommandHandler(basicClass);
        return basicClass;
    }

    private byte[] transformGuiChat(byte[] basicClass) {
        try {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);
            boolean changed = false;
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
            return changed ? write(node) : basicClass;
        } catch (Throwable error) {
            System.err.println("[AlphaClient] GuiChat autocomplete filter failed; suggestions unchanged.");
            error.printStackTrace();
            return basicClass;
        }
    }

    private byte[] transformClientCommandHandler(byte[] basicClass) {
        try {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);
            boolean changed = false;
            boolean autocompleteHookApplied = false;
            boolean slashGuardApplied = false;

            for (MethodNode method : node.methods) {
                // Forge 1.8.9 calls executeCommand for ordinary chat as well as slash commands.
                // Convert non-slash input into an unknown command so Forge sends it as chat.
                if (COMMAND_DESC.equals(method.desc)
                        && ("executeCommand".equals(method.name) || "func_71556_a".equals(method.name))
                        && !containsCommandGuardHook(method)) {
                    AbstractInsnNode first = firstReal(method);
                    if (first != null) {
                        InsnList guard = new InsnList();
                        guard.add(new VarInsnNode(Opcodes.ALOAD, 2));
                        guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "prepareCommandMessage",
                                "(Ljava/lang/String;)Ljava/lang/String;", false));
                        guard.add(new VarInsnNode(Opcodes.ASTORE, 2));
                        method.instructions.insertBefore(first, guard);
                        changed = true;
                        slashGuardApplied = true;
                    }
                }

                // Forge 1.8.9 builds the local client-command list in autoComplete(String, String).
                if (!"(Ljava/lang/String;Ljava/lang/String;)V".equals(method.desc)) continue;
                if (!("autoComplete".equals(method.name) || "func_".equals(method.name))) continue;
                if (containsFilterHook(method)) continue;

                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction.getOpcode() != Opcodes.RETURN) continue;
                    InsnList hook = new InsnList();
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new FieldInsnNode(Opcodes.GETFIELD, node.name, "latestAutoComplete", ARRAY_DESC));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, BRIDGE, "filter",
                            "([Ljava/lang/String;)[Ljava/lang/String;", false));
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new InsnNode(Opcodes.SWAP));
                    hook.add(new FieldInsnNode(Opcodes.PUTFIELD, node.name, "latestAutoComplete", ARRAY_DESC));
                    method.instructions.insertBefore(instruction, hook);
                    changed = true;
                    autocompleteHookApplied = true;
                }
            }

            if (slashGuardApplied) {
                System.out.println("[AlphaClient] Applied slash-only guard to Forge client commands.");
            } else {
                System.err.println("[AlphaClient] Forge client-command slash guard not found or already applied.");
            }
            if (autocompleteHookApplied) {
                System.out.println("[AlphaClient] Applied rank filter to Forge client-command autocomplete.");
            }
            return changed ? write(node) : basicClass;
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Forge client-command transformation failed; client commands unchanged.");
            error.printStackTrace();
            return basicClass;
        }
    }

    private static byte[] write(ClassNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
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

    private static boolean containsCommandGuardHook(MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (BRIDGE.equals(call.owner) && "prepareCommandMessage".equals(call.name)) return true;
            }
        }
        return false;
    }
}
