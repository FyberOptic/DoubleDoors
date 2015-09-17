package net.fybertech.doubledoors;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.fybertech.dynamicmappings.DynamicMappings;
import net.fybertech.meddle.Meddle;
import net.minecraft.launchwrapper.IClassTransformer;

public class DoubleDoorsTransformer implements IClassTransformer 
{

	String blockDoor = DynamicMappings.getClassMapping("net/minecraft/block/BlockDoor");
	
	
	@Override
	public byte[] transform(String arg0, String arg1, byte[] arg2) 
	{
		if (blockDoor != null && arg0.equals(blockDoor)) return transformBlockDoor(arg2);
		else return arg2;
	}
	
	
	private byte[] handleError(String error, byte[] bytes)
	{
		Meddle.LOGGER.error("[DoubleDoors] " + error);
		return bytes;
	}
	
	private byte[] transformBlockDoor(byte[] bytes) 
	{	
		ClassReader reader = new ClassReader(bytes);
		ClassNode cn = new ClassNode();
		reader.accept(cn, 0);
	
		MethodNode onActivated = DynamicMappings.getMethodNodeFromMapping(cn, "net/minecraft/block/Block onBlockActivated (Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/MainOrOffHand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumFacing;FFF)Z");
		if (onActivated == null) return handleError("Couldn't locate onBlockActivated!", bytes);
		
		boolean foundReturn = false;
		AbstractInsnNode targetNode = null;
		
		for (AbstractInsnNode insn = onActivated.instructions.getLast(); insn != null; insn = insn.getPrevious()) {
			if (insn.getOpcode() == Opcodes.IRETURN) { foundReturn = true; continue; }
			if (insn.getOpcode() == Opcodes.ICONST_1 && foundReturn) { 
				targetNode = insn;
				break;
			}
		}		
		if (targetNode == null) return handleError("Couldn't determine onBlockActivated hook location!", bytes);
		
		
		// Create descriptor for hook method
		Type[] args = Type.getMethodType(onActivated.desc).getArgumentTypes();		
		String desc = "(L" + blockDoor + ";";
		for (Type t : args) {
			desc += t.getDescriptor();
		}
		desc += ")V";

		
		InsnList list = new InsnList();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // Block
		list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // World
		list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // BlockPos
		list.add(new VarInsnNode(Opcodes.ALOAD, 3)); // IBlockState
		list.add(new VarInsnNode(Opcodes.ALOAD, 4)); // EntityPlayer
		list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // MainOrOffHand
		list.add(new VarInsnNode(Opcodes.ALOAD, 6)); // ItemStack
		list.add(new VarInsnNode(Opcodes.ALOAD, 7)); // EnumFacing
		list.add(new VarInsnNode(Opcodes.FLOAD, 8)); // float
		list.add(new VarInsnNode(Opcodes.FLOAD, 9)); // float
		list.add(new VarInsnNode(Opcodes.FLOAD, 10)); // float
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/fybertech/doubledoors/DoubleDoorsMod", "onBlockActivatedHook", desc, false));
		
		onActivated.instructions.insertBefore(targetNode,  list);
		
		
		
		String playSFX = DynamicMappings.getMethodMapping("net/minecraft/world/World playAuxSFXAtEntity (Lnet/minecraft/entity/player/EntityPlayer;ILnet/minecraft/util/BlockPos;I)V");
		if (playSFX == null) return handleError("Couldn't locate playAuxSFXAtEntity!", bytes);
		
		MethodNode onNeighbor = DynamicMappings.getMethodNodeFromMapping(cn, "net/minecraft/block/Block onNeighborBlockChange (Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/Block;)V");
		if (onNeighbor == null) return handleError("Couldn't locate onNeighborBlockChange!", bytes);
		
		targetNode = null;
		int powerVar = -1;
		for (AbstractInsnNode insn = onNeighbor.instructions.getLast(); insn != null; insn = insn.getPrevious()) {
			if (targetNode == null && insn.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode mn = (MethodInsnNode)insn;
				if (!playSFX.endsWith(" " + mn.name + " " + mn.desc)) continue;
				targetNode = insn;	
			}
			if (powerVar == -1 && insn.getOpcode() == Opcodes.ILOAD) {
				VarInsnNode vn = (VarInsnNode)insn;
				powerVar = vn.var;
			}
			if (targetNode != null && powerVar != -1) break;
		}		
		if (targetNode == null) return handleError("Couldn't determine onNeighborBlockChange hook location!", bytes);
		
		
		// Create descriptor for hook method
		args = Type.getMethodType(onNeighbor.desc).getArgumentTypes();
		desc = "(L" + blockDoor + ";Z";
		for (Type t : args) {
			desc += t.getDescriptor();
		}
		desc += ")V";
		
		
		list.clear();
		list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // BlockDoor
		list.add(new VarInsnNode(Opcodes.ILOAD, powerVar));
		list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // World
		list.add(new VarInsnNode(Opcodes.ALOAD, 2)); // BlockPos
		list.add(new VarInsnNode(Opcodes.ALOAD, 3)); // IBlockState
		list.add(new VarInsnNode(Opcodes.ALOAD, 4)); // Block
		list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/fybertech/doubledoors/DoubleDoorsMod", "onNeighborBlockChangeHook", desc, false));
		
		onNeighbor.instructions.insert(targetNode, list);
		
		Meddle.LOGGER.info("[DoubleDoors] BlockDoor successfully hooked");
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cn.accept(writer);
		return writer.toByteArray();
	}

}
