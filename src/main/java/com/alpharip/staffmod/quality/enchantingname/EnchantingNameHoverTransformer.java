// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.quality.enchantingname;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Replaces the vanilla Standard Galactic option text at its original draw
 * call, removes the option icon while the feature is enabled, and suppresses
 * the duplicate hover tooltip.
 */
public final class EnchantingNameHoverTransformer implements IClassTransformer {
    private static final String GUI_ENCHANTMENT =
            "net.minecraft.client.gui.GuiEnchantment";
    private static final String HOOK =
            "com/alpharip/staffmod/quality/enchantingname/EnchantingNameHooks";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !GUI_ENCHANTMENT.equals(transformedName)) {
            return basicClass;
        }

        try {
            ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);

            boolean textHooked = false;
            boolean textureHooked = false;
            boolean hoverHooked = false;

            for (MethodNode method : classNode.methods) {
                if ("(FII)V".equals(method.desc)) {
                    for (AbstractInsnNode instruction = method.instructions.getFirst();
                         instruction != null; ) {
                        AbstractInsnNode next = instruction.getNext();
                        if (instruction instanceof MethodInsnNode) {
                            MethodInsnNode call = (MethodInsnNode) instruction;

                            if (isSplitStringCall(call)) {
                                method.instructions.set(call, new MethodInsnNode(
                                        Opcodes.INVOKESTATIC, HOOK,
                                        "drawEnchantingOptionText",
                                        "(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;IIII)V",
                                        false));
                                textHooked = true;
                            } else if (isOptionTextureCall(call)) {
                                method.instructions.set(call, new MethodInsnNode(
                                        Opcodes.INVOKESTATIC, HOOK,
                                        "drawEnchantingOptionTexture",
                                        "(Lnet/minecraft/client/gui/GuiEnchantment;IIIIII)V",
                                        false));
                                textureHooked = true;
                            }
                        }
                        instruction = next;
                    }
                }

                if (isDrawScreenMethod(method)) {
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
                        InsnList hook = new InsnList();

                        hook.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                HOOK, "shouldSuppressHover",
                                "(Ljava/lang/Object;)Z", false));
                        hook.add(new JumpInsnNode(Opcodes.IFEQ, callVanilla));

                        // Original stack: GuiScreen, List<String>, mouseX, mouseY.
                        hook.add(new InsnNode(Opcodes.POP));
                        hook.add(new InsnNode(Opcodes.POP));
                        hook.add(new InsnNode(Opcodes.POP));
                        hook.add(new InsnNode(Opcodes.POP));
                        hook.add(new JumpInsnNode(Opcodes.GOTO, afterHover));
                        hook.add(callVanilla);

                        method.instructions.insertBefore(instruction, hook);
                        method.instructions.insert(instruction, afterHover);
                        hoverHooked = true;
                        break;
                    }
                }
            }

            if (!textHooked || !textureHooked || !hoverHooked) {
                System.err.println("[AlphaClient] Enchanting Names transformer incomplete: text="
                        + textHooked + ", icon=" + textureHooked + ", hover=" + hoverHooked);
                return basicClass;
            }

            ClassWriter writer = new ClassWriter(
                    ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            classNode.accept(writer);
            System.out.println("[AlphaClient] Installed Enchanting Names text, icon and hover hooks.");
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Enchanting Names transformer failed; vanilla rendering remains unchanged.");
            error.printStackTrace();
            return basicClass;
        }
    }

    private boolean isSplitStringCall(MethodInsnNode call) {
        return "(Ljava/lang/String;IIII)V".equals(call.desc)
                && call.owner != null
                && call.owner.endsWith("/FontRenderer");
    }

    private boolean isOptionTextureCall(MethodInsnNode call) {
        return "(IIIIII)V".equals(call.desc)
                && ("drawTexturedModalRect".equals(call.name)
                || "func_73729_b".equals(call.name)
                || "b".equals(call.name));
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
