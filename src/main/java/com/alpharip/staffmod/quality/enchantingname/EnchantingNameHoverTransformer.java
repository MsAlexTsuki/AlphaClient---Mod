// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.quality.enchantingname;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Suppresses only the vanilla enchanting-option hover tooltip while
 * Enchanting Names is enabled. All other GuiEnchantment rendering remains
 * untouched.
 */
public final class EnchantingNameHoverTransformer implements IClassTransformer {
    private static final String GUI_ENCHANTMENT =
            "net.minecraft.client.gui.GuiEnchantment";
    private static final String HOVER_HOOK =
            "com/alpharip/staffmod/quality/enchantingname/EnchantingNameHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !GUI_ENCHANTMENT.equals(transformedName)) {
            return basicClass;
        }

        try {
            ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            boolean hooked = false;

            for (MethodNode method : classNode.methods) {
                if (!isDrawScreenMethod(method)) {
                    continue;
                }

                for (AbstractInsnNode instruction = method.instructions.getFirst();
                     instruction != null; instruction = instruction.getNext()) {
                    if (!(instruction instanceof MethodInsnNode)) {
                        continue;
                    }

                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!isHoverTextCall(call)) {
                        continue;
                    }

                    LabelNode callVanilla = new LabelNode();
                    LabelNode afterHover = new LabelNode();
                    org.objectweb.asm.tree.InsnList hook =
                            new org.objectweb.asm.tree.InsnList();

                    // Keep the original invocation stack intact unless the
                    // feature is enabled for this enchanting screen.
                    hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                            HOVER_HOOK, "shouldSuppressHover",
                            "(Ljava/lang/Object;)Z", false));
                    hook.add(new JumpInsnNode(Opcodes.IFEQ, callVanilla));

                    // Original stack before drawHoveringText:
                    // GuiScreen, List<String>, mouseX, mouseY.
                    hook.add(new org.objectweb.asm.tree.InsnNode(Opcodes.POP));
                    hook.add(new org.objectweb.asm.tree.InsnNode(Opcodes.POP));
                    hook.add(new org.objectweb.asm.tree.InsnNode(Opcodes.POP));
                    hook.add(new org.objectweb.asm.tree.InsnNode(Opcodes.POP));
                    hook.add(new JumpInsnNode(Opcodes.GOTO, afterHover));
                    hook.add(callVanilla);
                    method.instructions.insertBefore(instruction, hook);
                    method.instructions.insert(instruction, afterHover);
                    hooked = true;
                    break;
                }

                if (hooked) {
                    break;
                }
            }

            if (!hooked) {
                System.err.println("[AlphaClient] Enchanting Names hover hook could not find GuiEnchantment.drawHoveringText.");
                return basicClass;
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            System.out.println("[AlphaClient] Installed Enchanting Names hover suppression.");
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Enchanting Names hover transformer failed; vanilla rendering remains unchanged.");
            error.printStackTrace();
            return basicClass;
        }
    }

    private boolean isDrawScreenMethod(MethodNode method) {
        return "(IIF)V".equals(method.desc)
                && ("drawScreen".equals(method.name)
                || "func_73863_a".equals(method.name)
                || "a".equals(method.name));
    }

    private boolean isHoverTextCall(MethodInsnNode call) {
        return "(Ljava/util/List;II)V".equals(call.desc)
                && ("drawHoveringText".equals(call.name)
                || "func_146283_a".equals(call.name)
                || "a".equals(call.name));
    }
}
