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
 * Adds gated swing progress to the vanilla BLOCK item transform only.
 *
 * The hook does not force a blockhit when right-click is merely held. It only
 * returns swing progress when the runtime hook confirms a sword, a block target,
 * right-click, and an active attack swing. Bow, food and drink rendering remain
 * on their vanilla paths.
 */
public final class LegacyPvpTransformer implements IClassTransformer {
    private static final String ITEM_RENDERER = "net.minecraft.client.renderer.ItemRenderer";
    private static final String HOOK_OWNER = "com/alpharip/staffmod/pvp/LegacyPvpRenderHooks";
    private static volatile boolean installed;

    public static boolean isInstalled() {
        return installed;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !ITEM_RENDERER.equals(transformedName)) {
            return basicClass;
        }

        try {
            ClassNode classNode = new ClassNode();
            new ClassReader(basicClass).accept(classNode, 0);
            boolean changed = false;

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

            if (!changed) {
                System.err.println("[AlphaClient] Legacy PvP blockhit transformer could not find the expected 1.8.9 BLOCK render pattern. Vanilla rendering was left unchanged.");
                return basicClass;
            }

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classNode.accept(writer);
            installed = true;
            System.out.println("[AlphaClient] Installed Legacy PvP blockhit render hook.");
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Legacy PvP blockhit transformer failed; vanilla rendering was left unchanged.");
            error.printStackTrace();
            return basicClass;
        }
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
