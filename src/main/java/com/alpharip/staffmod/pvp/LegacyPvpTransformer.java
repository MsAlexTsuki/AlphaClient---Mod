package com.alpharip.staffmod.pvp;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Adds the player's swing progress to the vanilla BLOCK item transform only.
 *
 * Minecraft 1.8.9 normally passes 0.0F into transformFirstPersonItem while a
 * sword is blocking, which suppresses the swing animation in that branch.
 * This transformer changes the swing argument in the BLOCK and BOW branches.
 * Food and drink keep their vanilla swing-progress path.
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
                if (!containsBlockTransformCall(classNode.name, method)) {
                    continue;
                }

                AbstractInsnNode[] instructions = method.instructions.toArray();
                for (int i = 0; i < instructions.length; i++) {
                    AbstractInsnNode instruction = instructions[i];
                    if (!(instruction instanceof MethodInsnNode)) {
                        continue;
                    }

                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!classNode.name.equals(call.owner) || !"(FF)V".equals(call.desc)) {
                        continue;
                    }

                    AbstractInsnNode next = nextReal(instruction);
                    AbstractInsnNode afterNext = nextReal(next);
                    boolean blockBranch = next instanceof VarInsnNode
                            && next.getOpcode() == Opcodes.ALOAD
                            && ((VarInsnNode) next).var == 0
                            && afterNext instanceof MethodInsnNode
                            && classNode.name.equals(((MethodInsnNode) afterNext).owner)
                            && "()V".equals(((MethodInsnNode) afterNext).desc);
                    boolean bowBranch = followsBowTransform(instruction, classNode.name);
                    if (!blockBranch && !bowBranch) {
                        continue;
                    }

                    AbstractInsnNode argument = previousReal(instruction);
                    if (argument == null || argument.getOpcode() != Opcodes.FCONST_0) {
                        continue;
                    }

                    // Local 4 is swingProgress in ItemRenderer.renderItemInFirstPerson(float).
                    // Reuse it in the BLOCK and BOW use branches so a swing blends into
                    // the held-item pose instead of snapping instantly into bow draw.
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

    private static boolean followsBowTransform(AbstractInsnNode transformCall, String owner) {
        AbstractInsnNode instruction = transformCall;
        // The bow transform receives partialTicks and ItemStack after transformFirstPersonItem.
        for (int i = 0; i < 5; i++) {
            instruction = nextReal(instruction);
            if (instruction == null) return false;
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                if (owner.equals(call.owner)
                        && "(FLnet/minecraft/item/ItemStack;)V".equals(call.desc)) {
                    return true;
                }
                if (owner.equals(call.owner) && "(FF)V".equals(call.desc)) {
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean containsBlockTransformCall(String owner, MethodNode method) {
        for (AbstractInsnNode instruction : method.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) instruction;
            if (owner.equals(call.owner) && "()V".equals(call.desc)) {
                // The candidate method must also contain the first-person item transform call.
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
