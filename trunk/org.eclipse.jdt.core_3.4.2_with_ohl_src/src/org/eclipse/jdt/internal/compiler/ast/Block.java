/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.core.internal.jobs.JobStatus;
import org.eclipse.core.internal.localstore.IUnifiedTreeVisitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.OhlSupport;

public class Block extends Statement {
	
	public Statement[] statements;
	public int explicitDeclarations;
	// the number of explicit declaration , used to create scope
	public BlockScope scope;
	
	// OHL
	public boolean ohlIsSynSwitchBlock;
	
	public Block(int explicitDeclarations) {
		this.explicitDeclarations = explicitDeclarations;
	}
	
	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// empty block
		if (statements == null)	return flowInfo;
		boolean didAlreadyComplain = false;
		for (int i = 0, max = statements.length; i < max; i++) {
			Statement stat = statements[i];
			if (!stat.complainIfUnreachable(flowInfo, scope, didAlreadyComplain)) {
				flowInfo = stat.analyseCode(scope, flowContext, flowInfo);
			} else {
				didAlreadyComplain = true;
			}
		}
		return flowInfo;
	}
	/**
	 * Code generation for a block
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachable) == 0) {
			return;
		}
		int pc = codeStream.position;
		if (statements != null) {
			for (int i = 0, max = statements.length; i < max; i++) {
				statements[i].generateCode(scope, codeStream);
			}
		} // for local variable debug attributes
		if (scope != currentScope) { // was really associated with its own scope
			codeStream.exitUserScope(scope);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public boolean isEmptyBlock() {

		return statements == null;
	}

	public StringBuffer printBody(int indent, StringBuffer output) {

		if (this.statements == null) return output;
		for (int i = 0; i < statements.length; i++) {
			statements[i].printStatement(indent + 1, output);
			output.append('\n'); 
		}
		return output;
	}

	public StringBuffer printStatement(int indent, StringBuffer output) {

		printIndent(indent, output);
		output.append("{\n"); //$NON-NLS-1$
		printBody(indent, output);
		return printIndent(indent, output).append('}');
	}

	public void resolve(BlockScope upperScope) {

		if ((this.bits & UndocumentedEmptyBlock) != 0) {
			upperScope.problemReporter().undocumentedEmptyBlock(this.sourceStart, this.sourceEnd);
		}
		if (statements != null) {
			scope =
				explicitDeclarations == 0
					? upperScope
					: new BlockScope(upperScope, explicitDeclarations);

			
			if (this.ohlIsSynSwitchBlock) {
				LocalDeclaration declSt = (LocalDeclaration) this.statements[0];
				TypeBinding exprType = declSt.resolveRValue(scope);
				
				char[] finalVarName = null;
				if (declSt.initialization instanceof SingleNameReference) {
				  SingleNameReference varRef = (SingleNameReference) declSt.initialization;
				  if (varRef.binding instanceof VariableBinding) {
				    VariableBinding binding = (VariableBinding) varRef.binding;
				    if (binding.isFinal()) {
	  			    finalVarName = varRef.token;
				    }
  			  }
				}
				
				if (exprType != null) {
	        declSt.type = binding2typeRef(exprType); 
				}

 		    // ru.spb.rybin.ohl.lang.EnumCaseBase<? super Visitor>
      	char [][] enum_case_base_tokens = OhlSupport.ENUM_CASE_BASE_TOKENS;
		    	
				TypeBinding visitorType = null;
				if (exprType instanceof ParameterizedTypeBinding) {
					ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) exprType;
					if (CharOperation.equals(enum_case_base_tokens, parameterizedTypeBinding.compoundName)) {
						if (parameterizedTypeBinding.arguments.length==1) {
							if (parameterizedTypeBinding.arguments[0] instanceof CaptureBinding) {
							     CaptureBinding binding = (CaptureBinding) parameterizedTypeBinding.arguments[0];
							     if (binding.wildcard != null) {
							    	 if (binding.wildcard.boundKind == Wildcard.SUPER) {
							    		 visitorType = binding.wildcard.bound;
							    	 }
							     }
							}
						}
					}
				}
				SwitchStatement switchSt = (SwitchStatement) this.statements[1];
				
				
				if (visitorType == null) {
				  switchSt.ohlTodoAnonymousAlloc.type = new SingleTypeReference("unknown_type".toCharArray(), 0);
				} else {
    				switchSt.ohlTodoAnonymousAlloc.type = binding2typeRef(visitorType);
				}

				for (int i=0; i<switchSt.ohlCaseBlocks.length; i++) {
					Block block = switchSt.ohlCaseBlocks[i];
          
					LocalDeclaration decl1 = (LocalDeclaration) block.statements[0];
          if (finalVarName != null && decl1.name == OhlSupport.NO_TAG_IDENTIFIER) {
            decl1.name = finalVarName;
            decl1.ohlRedefineForCast = true;
          }
          if (decl1.name == OhlSupport.NO_TAG_IDENTIFIER) {
            decl1.name = "caseEnumTempVarNotUsed".toCharArray();
          }
          
					switch (switchSt.ohlCaseStatements[i].ohlCaseType) {
          case CaseStatement.OHL_TYPE_CASE: {
            
          } break;
          case CaseStatement.OHL_STRUCT_CASE: {
            char[] selector = ((SingleTypeReference)decl1.type).token;
            TypeReference [] typeRefCopies = new TypeReference[3];
            QualifiedTypeReference typeRef = (QualifiedTypeReference) binding2typeRef(visitorType);
            for (int j=0; j<typeRefCopies.length; j++) {
              QualifiedTypeReference memberType = (QualifiedTypeReference) OhlSupport.convertToMemberType(typeRef, selector, true);
              if (memberType == null) {
                typeRefCopies[j] = new SingleTypeReference("<unspecified>".toCharArray(), 0);
              } else {
                char [] [] tokens = memberType.tokens;
                tokens[tokens.length-2] = OhlSupport.CASE_HOLDER_INTERFACE_NAME.toCharArray();
                typeRefCopies[j] = memberType;
              }
            }
            decl1.type = typeRefCopies[0];
            ((CastExpression)decl1.initialization).type = typeRefCopies[1];
            
            ReferenceBinding subclass = (ReferenceBinding)typeRefCopies[2].resolveType(scope);
            if (subclass != null) {
              FieldBinding[] fields = subclass.fields();
              for (int j=0; j<fields.length; j++) {
                // reverse order of statements/fields
                int statementPos = fields.length - j;
                if (statementPos >= 0 && statementPos < block.statements.length && block.statements[statementPos] instanceof LocalDeclaration) { 
                  LocalDeclaration fieldDecl = (LocalDeclaration) block.statements[statementPos];
                  FieldReference initialization = (FieldReference)fieldDecl.initialization;
                  initialization.token = fields[j].name;
                  if (finalVarName != null) {
                    ((SingleNameReference)initialization.receiver).token = finalVarName;
                  }
                }
              }
            }
          } break;
					}
				}
			}
			
			
			for (int i = 0, length = statements.length; i < length; i++) {
				statements[i].resolve(scope);
			}
		}
	}
	
	private static TypeReference binding2typeRef(TypeBinding typeBinding) {
	  if (typeBinding == null) {
	    return null;
	  }
	  if (typeBinding instanceof CaptureBinding) {
			CaptureBinding captureBinding = (CaptureBinding) typeBinding;
			if (captureBinding.wildcard != null) {
				Wildcard res = new Wildcard(captureBinding.wildcard.boundKind);
				res.bound = binding2typeRef(captureBinding.wildcard.bound);
				return res;
			}
			throw new RuntimeException();
		}
    if (typeBinding instanceof TypeVariableBinding) {
      TypeVariableBinding var = (TypeVariableBinding)typeBinding;
      return new SingleTypeReference(var.sourceName, 0);
    }
		
		char[] fullName = typeBinding.qualifiedSourceName();
		char [] packageName = typeBinding.qualifiedPackageName();
		String [] classNameInParts = new String(fullName).split("\\.");
		String [] packageNameInParts = new String(packageName).split("\\.");
		char [] [] tokens = new char[packageNameInParts.length + classNameInParts.length] [];
		long [] pos = new long[packageNameInParts.length + classNameInParts.length];
		for (int i=0; i<packageNameInParts.length; i++) {
			tokens[i] = packageNameInParts[i].toCharArray();
		}
		for (int i=0; i<classNameInParts.length; i++) {
			tokens[i + packageNameInParts.length] = classNameInParts[i].toCharArray();
		}
		
		if (typeBinding instanceof ParameterizedTypeBinding) {
			ParameterizedTypeBinding genericType = (ParameterizedTypeBinding)typeBinding;
			TypeReference [] [] genericParams = new TypeReference[tokens.length][];
			TypeReference [] lastComponentParams = new TypeReference[genericType.arguments.length];
			genericParams[genericParams.length-1] = lastComponentParams;
			
			for (int i=0; i<lastComponentParams.length; i++) {
				lastComponentParams[i] = binding2typeRef(genericType.arguments[i]);
			}
			
			ParameterizedQualifiedTypeReference res = 
				new ParameterizedQualifiedTypeReference(tokens, genericParams, 0, pos);
			return res;
		} else {
			QualifiedTypeReference res = new QualifiedTypeReference(tokens, pos);
			return res;
		}
		
	}

	public void resolveUsing(BlockScope givenScope) {

		if ((this.bits & UndocumentedEmptyBlock) != 0) {
			givenScope.problemReporter().undocumentedEmptyBlock(this.sourceStart, this.sourceEnd);
		}
		// this optimized resolve(...) is sent only on none empty blocks
		scope = givenScope;
		if (statements != null) {
			for (int i = 0, length = statements.length; i < length; i++) {
				statements[i].resolve(scope);
			}
		}
	}

	public void traverse(
		ASTVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			if (statements != null) {
				for (int i = 0, length = statements.length; i < length; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
	
	/**
	 * Dispatch the call on its last statement.
	 */
	public void branchChainTo(BranchLabel label) {
		 if (this.statements != null) {
		 	this.statements[statements.length - 1].branchChainTo(label);
		 }
	}
	
}
