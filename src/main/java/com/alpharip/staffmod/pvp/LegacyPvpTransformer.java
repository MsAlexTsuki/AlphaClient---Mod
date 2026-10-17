package com.alpharip.staffmod.pvp;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Installs narrowly scoped Legacy PvP hooks for the first-person item renderer:
 * gated sword blockhit swing progress and the optional 1.7.10 bow draw transform.
 * Food, drink, and other item-use rendering remain on their vanilla paths.
 */
public final class LegacyPvpTransformer implements IClassTransformer {
    private static final String ITEM_RENDERER = "net.minecraft.client.renderer.ItemRenderer";
    private static final String MINECRAFT = "net.minecraft.client.Minecraft";
    private static final String HOOK_OWNER = "com/alpharip/staffmod/pvp/LegacyPvpRenderHooks";
    private static volatile boolean installed;
    private static volatile boolean missedSwingDelayInstalled;
    private static volatile boolean bowAnimationInstalled;

    public static boolean isInstalled() {
        return installed;
    }

    public static boolean isMissedSwingDelayInstalled() {
        return missedSwingDelayInstalled;
    }

    public static boolean isBowAnimationInstalled() {
        return bowAnimationInstalled;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return basicClass;
        }
        if (MINECRAFT.equals(transformedName)) {
            return transformMinecraft(basicClass);
        }
        if (!ITEM_RENDERER.equals(transformedName)) {
            return basicClass;
        }

        try {
            ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            boolean changed = false;
            boolean bowChanged = false;
            boolean bowSwingChanged = false;

            for (MethodNode method : classNode.methods) {
                if (!containsFirstPersonBlockTransform(classNode.name, method)) {
                    continue;
                }

                AbstractInsnNode[] instructions = method.instructions.toArray();
                for (AbstractInsnNode instruction : instructions) {
                    if (!(instruction instanceof MethodInsnNode)) {
                        continue;
                    }

                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!classNode.name.equals(call.owner) || !"(FF)V".equals(call.desc)) {
                        continue;
                    }

                    // In 1.8.9 the BLOCK branch calls transformFirstPersonItem
                    // with 0.0F as its swing-progress argument, immediately
                    // followed by doBlockTransformations().
                    AbstractInsnNode next = nextReal(instruction);
                    AbstractInsnNode afterNext = nextReal(next);
                    boolean blockBranch = next instanceof VarInsnNode
                            && next.getOpcode() == Opcodes.ALOAD
                            && ((VarInsnNode) next).var == 0
                            && afterNext instanceof MethodInsnNode
                            && classNode.name.equals(((MethodInsnNode) afterNext).owner)
                            && "()V".equals(((MethodInsnNode) afterNext).desc);
                    if (!blockBranch) {
                        continue;
                    }

                    AbstractInsnNode argument = previousReal(instruction);
                    if (argument == null || argument.getOpcode() != Opcodes.FCONST_0) {
                        continue;
                    }

                    // Local 4 is swingProgress in renderItemInFirstPerson(float).
                    VarInsnNode swingProgress = new VarInsnNode(Opcodes.FLOAD, 4);
                    method.instructions.set(argument, swingProgress);
                    method.instructions.insert(swingProgress, new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HOOK_OWNER,
                            "getBlockhitSwingProgress",
                            "(F)F",
                            false
                    ));
                    changed = true;
                    break;
                }
                if (changed) break;
            }

            // Restore the 1.7.10 swing transform for the bow-use branch. In 1.8.9
            // transformFirstPersonItem is called with 0.0F while using any item.
            // Replace that zero only when the call is immediately followed by the
            // bow-specific transform; a runtime gate keeps vanilla behavior available.
            for (MethodNode method : classNode.methods) {
                if (!"(F)V".equals(method.desc)) {
                    continue;
                }

                AbstractInsnNode[] instructions = method.instructions.toArray();
                boolean methodBowSwingChanged = false;
                for (AbstractInsnNode instruction : instructions) {
                    if (!(instruction instanceof MethodInsnNode)) {
                        continue;
                    }

                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!classNode.name.equals(call.owner) || !"(FF)V".equals(call.desc)) {
                        continue;
                    }

                    AbstractInsnNode next = nextReal(instruction);
                    if (!(next instanceof VarInsnNode) || next.getOpcode() != Opcodes.ALOAD
                            || ((VarInsnNode) next).var != 0) {
                        continue;
                    }

                    AbstractInsnNode lookAhead = nextReal(next);
                    boolean bowBranch = false;
                    for (int step = 0; step < 6 && lookAhead != null; step++) {
                        if (lookAhead instanceof MethodInsnNode) {
                            MethodInsnNode candidate = (MethodInsnNode) lookAhead;
                            if (classNode.name.equals(candidate.owner)
                                    && isBowTransformCall(classNode.name, candidate)) {
                                bowBranch = true;
                                break;
                            }
                        }
                        lookAhead = nextReal(lookAhead);
                    }
                    if (!bowBranch) {
                        continue;
                    }

                    AbstractInsnNode argument = previousReal(instruction);
                    if (argument == null || argument.getOpcode() != Opcodes.FCONST_0) {
                        continue;
                    }

                    VarInsnNode swingProgress = new VarInsnNode(Opcodes.FLOAD, 4);
                    method.instructions.set(argument, swingProgress);
                    method.instructions.insert(swingProgress, new MethodInsnNode(
                            Opcodes.INVOKESTATIC, HOOK_OWNER,
                            "getLegacyBowSwingProgress", "(F)F", false
                    ));
                    methodBowSwingChanged = true;
                    bowSwingChanged = true;
                    break;
                }

                if (!methodBowSwingChanged) {
                    continue;
                }

                // Replace only the bow-transform call, preserving the receiver in
                // the static hook's first parameter to keep the operand stack valid.
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) {
                        continue;
                    }

                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!classNode.name.equals(call.owner)
                            || !isBowTransformCall(classNode.name, call)) {
                        continue;
                    }

                    method.instructions.set(instruction, new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            HOOK_OWNER,
                            "applyBowTransformations",
                            "(Ljava/lang/Object;FLjava/lang/Object;)V",
                            false
                    ));
                    bowChanged = true;
                    break;
                }
                if (bowChanged) break;
            }

            if (!changed && !bowChanged) {
                System.err.println("[AlphaClient] Legacy PvP could not find the expected ItemRenderer render patterns; vanilla rendering was left unchanged.");
                return basicClass;
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            if (changed) {
                installed = true;
                System.out.println("[AlphaClient] Installed Legacy PvP blockhit render hook.");
            } else {
                System.err.println("[AlphaClient] Legacy PvP blockhit render pattern was not found; blockhit remains vanilla.");
            }
            if (bowChanged && bowSwingChanged) {
                bowAnimationInstalled = true;
                System.out.println("[AlphaClient] Installed Legacy PvP legacy bow animation and swing hooks.");
            } else {
                System.err.println("[AlphaClient] Legacy PvP bow render patterns were not both found; bow legacy animation remains unavailable.");
            }
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Legacy PvP blockhit transformer failed; vanilla rendering was left unchanged.");
            error.printStackTrace();
            return basicClass;
        }
    }

    /**
     * Adds a guarded client-side hook at the start of Minecraft.runTick.
     * The hook only clears the missed-swing counter while Legacy PvP is enabled
     * and the crosshair is over air; block/entity targeting is left untouched.
     */
    private byte[] transformMinecraft(byte[] basicClass) {
        try {
            ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            for (MethodNode method : classNode.methods) {
                if (!("runTick".equals(method.name) || "func_71407_l".equals(method.name)
                        || "s".equals(method.name)) || !"()V".equals(method.desc)) {
                    continue;
                }
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (instruction instanceof MethodInsnNode) {
                        MethodInsnNode call = (MethodInsnNode) instruction;
                        if (HOOK_OWNER.equals(call.owner)
                                && "resetMissedSwingDelayIfNeeded".equals(call.name)
                                && "()V".equals(call.desc)) {
                            missedSwingDelayInstalled = true;
                            return basicClass;
                        }
                    }
                }
                org.objectweb.asm.tree.InsnList hook = new org.objectweb.asm.tree.InsnList();
                hook.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_OWNER,
                        "resetMissedSwingDelayIfNeeded", "()V", false));
                method.instructions.insert(hook);
                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);
                missedSwingDelayInstalled = true;
                System.out.println("[AlphaClient] Installed guarded missed-swing delay hook.");
                return writer.toByteArray();
            }
            System.err.println("[AlphaClient] Legacy PvP could not find Minecraft.runTick; missed-swing delay hook was not installed.");
            return basicClass;
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Legacy PvP missed-swing delay transformer failed; vanilla timing was left unchanged.");
            error.printStackTrace();
            return basicClass;
        }
    }

    private static boolean isBowTransformCall(String owner, MethodInsnNode call) {
        // The player class in this descriptor may be deobfuscated or obfuscated
        // depending on the LaunchWrapper transformer order.
        return owner.equals(call.owner)
                && call.desc.startsWith("(F")
                && call.desc.indexOf(';') >= 0
                && call.desc.endsWith(")V")
                && !"(FF)V".equals(call.desc);
    }

    private static boolean containsFirstPersonBlockTransform(String owner, MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (owner.equals(call.owner) && "()V".equals(call.desc)) {
                for (AbstractInsnNode candidate : method.instructions.toArray()) {
                    if (candidate instanceof MethodInsnNode) {
                        MethodInsnNode candidateCall = (MethodInsnNode) candidate;
                        if (owner.equals(candidateCall.owner) && "(FF)V".equals(candidateCall.desc)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static AbstractInsnNode nextReal(AbstractInsnNode instruction) {
        AbstractInsnNode next = instruction == null ? null : instruction.getNext();
        while (next instanceof LineNumberNode || next instanceof FrameNode || next instanceof LabelNode) {
            next = next.getNext();
        }
        return next;
    }

    private static AbstractInsnNode previousReal(AbstractInsnNode instruction) {
        AbstractInsnNode previous = instruction == null ? null : instruction.getPrevious();
        while (previous instanceof LineNumberNode || previous instanceof FrameNode || previous instanceof LabelNode) {
            previous = previous.getPrevious();
        }
        return previous;
    }
}
