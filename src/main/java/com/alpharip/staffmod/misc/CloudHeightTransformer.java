// --- Made By MsAlexTsuki ---
package com.alpharip.staffmod.misc;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

public final class CloudHeightTransformer implements IClassTransformer {
    private static final String RENDER_GLOBAL = "net.minecraft.client.renderer.RenderGlobal";
    private static final String WORLD_PROVIDER = "net/minecraft/world/WorldProvider";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !RENDER_GLOBAL.equals(transformedName)) return basicClass;
        try {
            ClassNode node = new ClassNode();
            new ClassReader(basicClass).accept(node, 0);
            boolean changed = false;
            for (org.objectweb.asm.tree.MethodNode method : node.methods) {
                for (org.objectweb.asm.tree.AbstractInsnNode instruction : method.instructions.toArray()) {
                    if (!(instruction instanceof MethodInsnNode)) continue;
                    MethodInsnNode call = (MethodInsnNode) instruction;
                    if (WORLD_PROVIDER.equals(call.owner) && "()F".equals(call.desc)
                            && ("getCloudHeight".equals(call.name) || "func_76565_k".equals(call.name))) {
                        method.instructions.set(call, new MethodInsnNode(Opcodes.INVOKESTATIC,
                                "com/alpharip/staffmod/misc/CloudHeightSettings",
                                "overrideCloudHeight", "(Lnet/minecraft/world/WorldProvider;)F", false));
                        changed = true;
                    }
                }
            }
            if (!changed) {
                System.err.println("[AlphaClient] Cloud Height could not find the vanilla cloud-height call in RenderGlobal.");
                return basicClass;
            }
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            System.out.println("[AlphaClient] Installed Cloud Height renderer hook.");
            return writer.toByteArray();
        } catch (Throwable error) {
            System.err.println("[AlphaClient] Cloud Height renderer hook failed; vanilla cloud height remains active.");
            error.printStackTrace();
            return basicClass;
        }
    }
}
