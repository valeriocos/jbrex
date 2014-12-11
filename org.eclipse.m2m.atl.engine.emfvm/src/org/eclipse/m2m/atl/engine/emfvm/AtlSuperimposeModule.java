/*******************************************************************************
 * Copyright (c) 2008 Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dennis Wagelaar (Vrije Universiteit Brussel) - initial API and implementation
 *******************************************************************************/
package org.eclipse.m2m.atl.engine.emfvm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.m2m.atl.engine.emfvm.lib.ASMModule;
import org.eclipse.m2m.atl.engine.emfvm.lib.ExecEnv;

/**
 * Implements ATL module superimposition by load-time adaptation of the execution environment operations.
 * 
 * @author <a href="mailto:dennis.wagelaar@vub.ac.be">Dennis Wagelaar</a>
 */
public class AtlSuperimposeModule {

	private ExecEnv env;

	private ASM asm;

	private boolean atl2006;

	/**
	 * General exception for applying ATL module superimposition. Used in ASM adaptation sanity check.
	 */
	public class AtlSuperimposeModuleException extends Exception {

		static final long serialVersionUID = 20080205;

		/**
		 * Creates a new AtlSuperimposeModuleException.
		 * 
		 * @param msg
		 *            Exception message
		 */
		public AtlSuperimposeModuleException(String msg) {
			super(msg);
		}
	}

	/**
	 * Creates a new AtlSuperimposeModule object.
	 * 
	 * @param env
	 *            The execution environment to adapt
	 * @param asm
	 *            The ATL module to adapt
	 */
	public AtlSuperimposeModule(ExecEnv env, ASM asm) {
		this.env = env;
		this.asm = asm;
	}

	/**
	 * Adapts any module-specific operations from a library. The matcher and exec operations are adapted to
	 * include any new ATL rules. Rules with the same name as an existing rule will override the existing
	 * rule.
	 * 
	 * @throws AtlSuperimposeModuleException
	 *             if sanity check fails
	 */
	public void adaptModuleOperations() throws AtlSuperimposeModuleException {
		adaptMain();
		removeOperation("main"); //$NON-NLS-1$
		adaptOperation("__matcher__", 2); //$NON-NLS-1$
		removeOperation("__matcher__"); //$NON-NLS-1$
		adaptOperation("__exec__", 10); //$NON-NLS-1$
		removeOperation("__exec__"); //$NON-NLS-1$
	}

	/**
	 * Adapts main method instructions to include helper attribute __init methods.
	 * 
	 * @throws AtlSuperimposeModuleException
	 *             if sanity check fails
	 */
	private void adaptMain() throws AtlSuperimposeModuleException {
		final ASMOperation origOp = (ASMOperation)env.getOperation(ASMModule.class, "main"); //$NON-NLS-1$
		final ASMOperation newOp = asm.getMainOperation();
		if ((origOp != null) && (newOp != null)) {
			mainSanityCrossCheck(origOp, newOp);
			final List<Bytecode> from = Arrays.asList(newOp.getBytecodes());
			final List<Bytecode> into = new ArrayList<Bytecode>(Arrays.asList(origOp.getBytecodes()));
			if (atl2006) {
				insertHelperInits(from, into);
			} else {
				final List<Bytecode> origInit = getInstructions(into, "call A.__init", 20, 1); //$NON-NLS-1$
				final List<Bytecode> newInit = getInstructions(from, "call A.__init", 20, 1); //$NON-NLS-1$
				into.addAll(origInit.size() + 21, newInit);
			}
			origOp.setBytecodes(into.toArray(new Bytecode[0]));
		}
	}

	/**
	 * Performs sanity check and cross-check on main operations.
	 * 
	 * @param main1
	 *            The first main operation to check
	 * @param main2
	 *            The second main operation to check
	 * @throws AtlSuperimposeModuleException
	 *             if sanity check fails
	 */
	private void mainSanityCrossCheck(ASMOperation main1, ASMOperation main2)
			throws AtlSuperimposeModuleException {
		mainSanityCheck(main1);
		mainSanityCheck(main2);
		int preEnd = 21;
		if (atl2006) {
			preEnd = 16;
		}
		for (int i = 0; i < preEnd; i++) {
			String ins1 = main1.getBytecodes()[i].toString();
			String ins2 = main2.getBytecodes()[i].toString();
			if (!ins1.equals(ins2)) {
				throw new AtlSuperimposeModuleException(
						Messages
								.getString(
										"AtlSuperimposeModule.MAINPATTERNNOTEQUAL", ins1, ins2, String.valueOf(i))); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Performs sanity check main operation.
	 * 
	 * @param main
	 *            The main operation to check
	 * @throws AtlSuperimposeModuleException
	 *             if sanity check fails
	 */
	private void mainSanityCheck(ASMOperation main) throws AtlSuperimposeModuleException {
		final List<Bytecode> instructions = Arrays.asList(main.getBytecodes());
		if (instructions.size() < 21) {
			throw new AtlSuperimposeModuleException(
					Messages
							.getString(
									"AtlSuperimposeModule.UNEXPECTEDINSTRUCTIONCOUNT", String.valueOf(instructions.size()))); //$NON-NLS-1$
		}
		final String instr16 = instructions.get(15).toString();
		if (!instr16.equals("set col")) { //$NON-NLS-1$
			throw new AtlSuperimposeModuleException(Messages.getString(
					"AtlSuperimposeModule.UNEXPECTEDINSTRUCTIONSEQUENCE", new Object[] {instr16})); //$NON-NLS-1$
		}
		if (indexOfInstruction(instructions, "set links", 16) == -1) { //$NON-NLS-1$
			throw new AtlSuperimposeModuleException(Messages
					.getString("AtlSuperimposeModule.SETLINKSNOTFOUND")); //$NON-NLS-1$
		}
		final String instr1 = instructions.get(0).toString();
		if (instr1.equals("getasm")) { //$NON-NLS-1$
			atl2006 = true;
		}
	}

	/**
	 * Returns list of instructions with given prefix and context amount of preceding instructions for every
	 * match.
	 * 
	 * @param instr
	 *            The list of instructions to search
	 * @param prefix
	 *            The instruction/operand prefix (startsWith)
	 * @param start
	 *            The instruction index to start searching
	 * @param context
	 *            The number of preceding instructions to include
	 * @return list of instructions with given prefix and context amount of preceding instructions for every
	 *         match
	 */
	private List<Bytecode> getInstructions(List<Bytecode> instr, String prefix, int start, int context) {
		final List<Bytecode> init = new ArrayList<Bytecode>();
		for (int i = start + context; i < instr.size(); i++) {
			Object ins = instr.get(i);
			if (ins.toString().startsWith(prefix)) {
				init.addAll(instr.subList(i - context, i + 1));
			}
		}
		return init;
	}

	/**
	 * Returns The index of the first instruction with given prefix, -1 otherwise.
	 * 
	 * @param instr
	 *            The list of instructions to search
	 * @param prefix
	 *            The instruction/operand prefix (startsWith)
	 * @param start
	 *            The instruction index to start searching
	 * @return The index of the first instruction with given prefix, -1 otherwise
	 */
	private int indexOfInstruction(List<Bytecode> instr, String prefix, int start) {
		for (int i = start; i < instr.size(); i++) {
			Object ins = instr.get(i);
			if (ins.toString().startsWith(prefix)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Adds the instructions of the given operation from asm to the registered operation in env. Checks if
	 * instructions runs of patternLength exist already in the registered operation and only copies the
	 * instructions if not so.
	 * 
	 * @param op
	 *            The operation name
	 * @param patternLength
	 *            The length of the instruction pattern
	 * @throws AtlSuperimposeModuleException
	 *             if sanity check fails
	 */
	private void adaptOperation(String op, int patternLength) throws AtlSuperimposeModuleException {
		final ASMOperation origOp = (ASMOperation)env.getOperation(ASMModule.class, op);
		ASMOperation newOp = null;
		for (Iterator<ASMOperation> i = asm.getOperations(); i.hasNext();) {
			ASMOperation thisOp = i.next();
			if (op.equals(thisOp.getName())) {
				Assert.isTrue(newOp == null);
				newOp = thisOp;
			}
		}
		if ((origOp != null) && (newOp != null)) {
			sanityCrossCheck(origOp, newOp, patternLength);
			final List<Bytecode> from = Arrays.asList(newOp.getBytecodes());
			final List<Bytecode> into = new ArrayList<Bytecode>(Arrays.asList(origOp.getBytecodes()));
			final String origOpRun = serialise(into, 0, into.size());
			for (int i = 0; i < from.size(); i += patternLength) {
				String newOpRun = serialise(from, i, patternLength);
				if (origOpRun.indexOf(newOpRun) == -1) {
					for (int j = i; j < Math.min(i + patternLength, from.size()); j++) {
						into.add(from.get(j));
					}
				}
			}
			origOp.setBytecodes(into.toArray(new Bytecode[0]));
		}
	}

	/**
	 * Copies the "from" helper init instructions to "into".
	 * 
	 * @param from
	 *            The list of instructions to copy the helper init code from.
	 * @param into
	 *            The list of instructions to augment.
	 */
	private void insertHelperInits(List<Bytecode> from, List<Bytecode> into) {
		final int endOfInitCode = indexOfInstruction(from, "set links", 16) - 4; //$NON-NLS-1$
		final List<Bytecode> initInstr = from.subList(16, endOfInitCode);
		final int pos = indexOfInstruction(into, "set links", 16) - 4; //$NON-NLS-1$
		transposeOffsets(into, initInstr.size(), pos);
		transposeOffsets(initInstr, pos - 16, 0);
		into.addAll(pos, initInstr);
	}

	/**
	 * Transposes the offset address of "if" and "goto" instructions.
	 * 
	 * @param instructions
	 *            The instructions to adapt.
	 * @param transpose
	 *            The amount to transpose up/down.
	 * @param start
	 *            The offset from which to start transposing.
	 */
	private void transposeOffsets(List<Bytecode> instructions, int transpose, int start) {
		for (Iterator<Bytecode> i = instructions.iterator(); i.hasNext();) {
			Object instruction = i.next();
			if (instruction instanceof Bytecode) {
				Bytecode instr = (Bytecode)instruction;
				int opcode = instr.getOpcode();
				if ((opcode == Bytecode.IF) || (opcode == Bytecode.GOTO)) {
					int offset = Integer.parseInt((String)instr.getOperand());
					if (offset >= start) {
						offset += transpose;
						instr.setOperand(String.valueOf(offset));
					}
				}
			}
		}
	}

	/**
	 * Performs sanity check and cross-check on pattern repetition and equality in op1 and op2.
	 * 
	 * @param op1
	 *            The first operation to check
	 * @param op2
	 *            The second operation to check
	 * @param patternLength
	 *            The length of the instruction pattern
	 * @throws AtlSuperimposeModuleException
	 *             if sanity check fails
	 */
	private void sanityCrossCheck(ASMOperation op1, ASMOperation op2, int patternLength)
			throws AtlSuperimposeModuleException {
		sanityCheck(op1, patternLength);
		sanityCheck(op2, patternLength);
		final List<Bytecode> instr1 = Arrays.asList(op1.getBytecodes());
		final List<Bytecode> instr2 = Arrays.asList(op2.getBytecodes());
		int limit = Math.min(instr1.size(), instr2.size());
		limit = Math.min(limit, patternLength);
		for (int i = 0; i < limit; i++) {
			int i1 = instr1.get(i).getOpcode();
			int i2 = instr2.get(i).getOpcode();
			if (i1 != i2) {
				throw new AtlSuperimposeModuleException(
						Messages
								.getString(
										"AtlSuperimposeModule.PATTERNNOTEQUAL", op1.getName(), Bytecode.OPCODENAMES[i1], Bytecode.OPCODENAMES[i2], String.valueOf(i))); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Performs sanity check on pattern repetition in op.
	 * 
	 * @param op
	 *            The operation to check
	 * @param patternLength
	 *            The length of the instruction pattern
	 * @throws AtlSuperimposeModuleException
	 *             if sanity check fails
	 */
	private void sanityCheck(ASMOperation op, int patternLength) throws AtlSuperimposeModuleException {
		final List<Bytecode> instr = Arrays.asList(op.getBytecodes());
		if (instr.size() % patternLength > 0) {
			throw new AtlSuperimposeModuleException(
					Messages
							.getString(
									"AtlSuperimposeModule.INSTRUCTIONCOUNTPROBLEM", String.valueOf(patternLength), op.getName())); //$NON-NLS-1$
		}
		for (int i = 0; i < instr.size() - patternLength; i++) {
			int i1 = instr.get(i).getOpcode();
			int i2 = instr.get(i + patternLength).getOpcode();
			if (i1 != i2) {
				throw new AtlSuperimposeModuleException(
						Messages
								.getString(
										"AtlSuperimposeModule.PATTERNDOESNOTREPEAT", String.valueOf(patternLength), op.getName(), Bytecode.OPCODENAMES[i1], Bytecode.OPCODENAMES[i2], String.valueOf(i))); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Serialises a run of instructions.
	 * 
	 * @param instrs
	 *            The list of instructions
	 * @param start
	 *            The starting index of the run
	 * @param length
	 *            The length of the run
	 * @return The semi-colon-separated run of instructions
	 */
	private static String serialise(List<Bytecode> instrs, int start, int length) {
		StringBuffer ser = new StringBuffer();
		for (int i = Math.max(0, start); i < Math.min(instrs.size(), start + length); i++) {
			ser.append(instrs.get(i));
			ser.append(';');
		}
		return ser.toString();
	}

	/**
	 * Removes the operation with the given name from the superimposed module.
	 * 
	 * @param op
	 *            The operation name
	 */
	private void removeOperation(String op) {
		boolean removed = false;
		for (Iterator<ASMOperation> i = asm.getOperations(); i.hasNext();) {
			ASMOperation asmOp = i.next();
			if (op.equals(asmOp.getName())) {
				Assert.isTrue(!removed);
				i.remove();
				removed = true;
			}
		}
	}

}
