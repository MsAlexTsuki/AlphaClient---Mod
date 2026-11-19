package com.alpharip.staffmod.singleplayer.customenchantments;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/** Replaces only the entity-hit arrow removal call; block impacts remain vanilla. */
public final class PiercingArrowTransformer implements IClassTransformer {
    private static final String TARGET = "net.minecraft.entity.projectile.EntityArrow";
    private static final String HOOK = "com/alpharip/staffmod/singleplayer/customenchantments/PiercingArrowHooks";
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
                if (!"onUpdate".equals(method.name) || !"()V".equals(method.desc)) continue;
                int setDeadCalls = 0;
                for (AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (!"setDead".equals(call.name) || !"()V".equals(call.desc)) continue;
                    setDeadCalls++;
                    if (setDeadCalls == 2) {
                        method.instructions.set(instruction, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                HOOK, "handleEntityImpact", "(Ljava/lang/Object;)V", false));
                        changed = true;
                        break;
                    }
                }
            }
            if (!changed) return basicClass;
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            installed = true;
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Piercing arrow transformer failed: " + error.getMessage());
            return basicClass;
        }
    }
}