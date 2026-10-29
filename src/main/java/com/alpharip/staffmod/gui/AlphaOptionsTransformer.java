// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.gui;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Adds the Alpha Settings button to Minecraft's vanilla options screen.
 * Kept separate from LegacyPvpTransformer so the Legacy PvP transformer
 * handles only Legacy PvP rendering and timing hooks.
 */
public final class AlphaOptionsTransformer implements IClassTransformer {
    private static final String GUI_OPTIONS = "net.minecraft.client.gui.GuiOptions";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !GUI_OPTIONS.equals(transformedName)) {
            return basicClass;
        }
        return transformGuiOptions(basicClass);
    }

    /**
     * Adds Alpha Settings to the vanilla options screen without replacing its native controls.
     * The action hook is gated to the dedicated button id and leaves all other actions untouched.
     */
    private byte[] transformGuiOptions(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            boolean initHooked = false;
            boolean actionHooked = false;

            for (MethodNode method : classNode.methods) {
                if (("initGui".equals(method.name) || "func_73866_w_".equals(method.name)
                        || "a".equals(method.name)) && "()V".equals(method.desc)) {
                    AbstractInsnNode last = method.instructions.getLast();
                    while (last != null && last.getOpcode() < 0) last = last.getPrevious();
                    if (last != null && last.getOpcode() == Opcodes.RETURN) {
                        org.objectweb.asm.tree.InsnList hook = new org.objectweb.asm.tree.InsnList();
                        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "com/alpharip/staffmod/gui/AlphaOptionsHooks",
                                "addAlphaSettingsButton", "(Ljava/lang/Object;)V", false));
                        method.instructions.insertBefore(last, hook);
                        initHooked = true;
                        break;
                    }
                }
            }

            for (MethodNode method : classNode.methods) {
                boolean actionName = "actionPerformed".equals(method.name)
                        || "func_146284_a".equals(method.name) || "a".equals(method.name);
                if (!actionName || !method.desc.startsWith("(L") || !method.desc.endsWith(")V")) continue;
                org.objectweb.asm.tree.InsnList hook = new org.objectweb.asm.tree.InsnList();
                org.objectweb.asm.tree.LabelNode continueVanilla = new org.objectweb.asm.tree.LabelNode();
                hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                hook.add(new VarInsnNode(Opcodes.ALOAD, 1));
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "com/alpharip/staffmod/gui/AlphaOptionsHooks",
                        "handleAlphaSettingsButton", "(Ljava/lang/Object;Ljava/lang/Object;)Z", false));
                hook.add(new org.objectweb.asm.tree.JumpInsnNode(Opcodes.IFEQ, continueVanilla));
                hook.add(new org.objectweb.asm.tree.InsnNode(Opcodes.RETURN));
                hook.add(continueVanilla);
                method.instructions.insert(hook);
                actionHooked = true;
                break;
            }

            if (!initHooked || !actionHooked) {
                System.err.println("[AlphaClient] Alpha Settings options hook could not find all GuiOptions methods; vanilla options remain available.");
                return basicClass;
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            System.out.println("[AlphaClient] Installed Alpha Settings button in Minecraft options.");
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Alpha Settings options transformer failed; vanilla options remain unchanged.");
            error.printStackTrace();
            return basicClass;
        }
    }

}
