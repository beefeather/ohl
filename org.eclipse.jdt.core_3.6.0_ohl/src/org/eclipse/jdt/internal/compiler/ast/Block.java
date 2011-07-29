/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	// empty block
	if (this.statements == null)	return flowInfo;
	int complaintLevel = (flowInfo.reachMode() & FlowInfo.UNREACHABLE) != 0 ? Statement.COMPLAINED_FAKE_REACHABLE : Statement.NOT_COMPLAINED;
	for (int i = 0, max = this.statements.length; i < max; i++) {
		Statement stat = this.statements[i];
		if ((complaintLevel = stat.complainIfUnreachable(flowInfo, this.scope, complaintLevel)) < Statement.COMPLAINED_UNREACHABLE) {
			flowInfo = stat.analyseCode(this.scope, flowContext, flowInfo);
		}
	}
	return flowInfo;
}
/**
 * Code generation for a block
 */
public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	if ((this.bits & IsReachable) == 0) {
		return;
	}
	int pc = codeStream.position;
	if (this.statements != null) {
		for (int i = 0, max = this.statements.length; i < max; i++) {
			this.statements[i].generateCode(this.scope, codeStream);
		}
	} // for local variable debug attributes
	if (this.scope != currentScope) { // was really associated with its own scope
		codeStream.exitUserScope(this.scope);
	}
	codeStream.recordPositionsFrom(pc, this.sourceStart);
}

public boolean isEmptyBlock() {
	return this.statements == null;
}

public StringBuffer printBody(int indent, StringBuffer output) {
	if (this.statements == null) return output;
	for (int i = 0; i < this.statements.length; i++) {
		this.statements[i].printStatement(indent + 1, output);
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
	if (this.statements != null) {
    int alreadyResolvedStatements = 0;
		this.scope =
			this.explicitDeclarations == 0
				? upperScope
				: new BlockScope(upperScope, this.explicitDeclarations);


			if (this.ohlIsSynSwitchBlock) {
				LocalDeclaration declSt = (LocalDeclaration) this.statements[OhlSupport.SwitchLayout.FIRST_DECLARATION_OFFSET];
				LocalDeclaration newDeclSt = (LocalDeclaration) this.statements[OhlSupport.SwitchLayout.SECOND_DECLARATION_OFFSET];
				SwitchStatement switchStatement = (SwitchStatement) this.statements[OhlSupport.SwitchLayout.SWITCH_STATEMENT_OFFSET];
				int switchStatementExpressionPos = switchStatement.sourceStart;
				
				TypeBinding exprType = declSt.resolveRValue(scope);
				if (exprType != null) {
					declSt.type = binding2typeRef(exprType, switchStatementExpressionPos); 
					newDeclSt.type = binding2typeRef(exprType, switchStatementExpressionPos); 
				}
				
				// Resolve to add variable to scope (we use it right now).
				declSt.resolve(scope);
				alreadyResolvedStatements++;

				
				char[] finalVarName = null;
				{
          Expression candidateExpression = declSt.initialization;
  				if (candidateExpression instanceof SingleNameReference) {
  				  SingleNameReference varRef = (SingleNameReference) candidateExpression;
  				  if (varRef.binding instanceof VariableBinding) {
  				    VariableBinding binding = (VariableBinding) varRef.binding;
  				    if (binding.isFinal()) {
  	  			    finalVarName = varRef.token;
  				    }
    			  }
  				}
				}
				
				

      	TypeBinding visitorType = OhlSupport.getVisitorTypeFromEnumCase(exprType);
      	if (visitorType == null) {
      		// Try calling toEnumCase
      		boolean hasToEnumCase = OhlSupport.hasToEnumCaseMethod(exprType, scope);
      		if (hasToEnumCase) {
      			//newDeclSt.type = declSt.type;
      			MessageSend messageSend = new MessageSend();
      			messageSend.receiver = new SingleNameReference(declSt.name, 0);
      			messageSend.selector = OhlSupport.TO_ENUM_CASE_METHOD_NAME;
      			messageSend.arguments = new Expression[0];
      			messageSend.typeArguments = null;
      			messageSend.sourceStart = declSt.sourceStart;
      			messageSend.sourceEnd = declSt.sourceEnd;
      			newDeclSt.initialization = messageSend;

      			exprType = newDeclSt.resolveRValue(scope);
  					newDeclSt.type = binding2typeRef(exprType, switchStatementExpressionPos); 
      			visitorType = OhlSupport.getVisitorTypeFromEnumCase(exprType);
      		}
      	}
      	
				if (visitorType == null) {
				  switchStatement.ohlTodoAnonymousAlloc.type = new SingleTypeReference("unknown_type".toCharArray(), 0);
				} else {
    				switchStatement.ohlTodoAnonymousAlloc.type = binding2typeRef(visitorType, switchStatementExpressionPos);
				}

				for (int i=0; i<switchStatement.ohlCaseBlocks.length; i++) {
					Block block = switchStatement.ohlCaseBlocks[i];
          
					LocalDeclaration decl1 = (LocalDeclaration) block.statements[OhlSupport.SwitchLayout.FIRST_DECLARATION_OFFSET];
          if (finalVarName != null && decl1.name == OhlSupport.NO_TAG_IDENTIFIER) {
            decl1.name = finalVarName;
            decl1.ohlRedefineForCast = true;
          }
          if (decl1.name == OhlSupport.NO_TAG_IDENTIFIER) {
            decl1.name = "caseEnumTempVarNotUsed".toCharArray();
          }
          
					switch (switchStatement.ohlCaseStatements[i].ohlCaseType) {
          case CaseStatement.OHL_TYPE_CASE: {
            
          } break;
          case CaseStatement.OHL_STRUCT_CASE: {
            final char[] selector = ((SingleTypeReference)decl1.type).token;
            final TypeReference typeRef = binding2typeRef(visitorType, 0);
            //visitorType.
            
            // We prefer not to reuse AST elements and create anew each time.
            class TypeReferenceFactory {
              TypeReference create() {
                QualifiedTypeReference memberType = (QualifiedTypeReference) OhlSupport.convertToMemberType(typeRef, selector, true);
                if (memberType == null) {
                  return new SingleTypeReference("<unspecified>".toCharArray(), 0);
                } else {
                  char [] [] tokens = memberType.tokens;
                  //tokens[tokens.length-2] = OhlSupport.CASE_HOLDER_INTERFACE_NAME.toCharArray();
                  return memberType;
                }
              }
              TypeReference createObjectTypeReference() {
                char [][] tokens = new char[][] {
                    "java".toCharArray(),
                    "lang".toCharArray(),
                    "Object".toCharArray()
                };
                return new QualifiedTypeReference(tokens, new long[tokens.length]);
              }
            }
            TypeReferenceFactory typeRefCopies = new TypeReferenceFactory();

            ReferenceBinding subclass;
            if (visitorType instanceof ReferenceBinding) {
              ReferenceBinding visitorRefereceBinding = (ReferenceBinding)visitorType;
              subclass = scope.getMemberType(selector, visitorRefereceBinding);
            } else {
              subclass = null;
            }
            
            if (subclass == null || !subclass.isValidBinding()) {
              decl1.type = typeRefCopies.createObjectTypeReference();
              ((CastExpression)decl1.initialization).type = typeRefCopies.createObjectTypeReference();
            } else {
              decl1.type = typeRefCopies.create();
              ((CastExpression)decl1.initialization).type = typeRefCopies.create();
              FieldBinding[] fields = subclass.fields();
              for (int j=0; j<fields.length; j++) {
                // reverse order of statements/fields
                int statementPos = fields.length - j;
                if (statementPos >= 0 && statementPos < block.statements.length && block.statements[statementPos] instanceof LocalDeclaration) { 
                  LocalDeclaration fieldDecl = (LocalDeclaration) block.statements[statementPos];
                  FieldReference initialization = (FieldReference)fieldDecl.initialization;
                  initialization.token = fields[j].name;
                  if (finalVarName != null) {
                    // what is this?
                    //((SingleNameReference)initialization.receiver).token = finalVarName;
                  }
                }
              }
            }
          } break;
					}
				}
			}


		for (int i = alreadyResolvedStatements, length = this.statements.length; i < length; i++) {
			this.statements[i].resolve(this.scope);
		}
	}
}


	private static TypeReference binding2typeRef(TypeBinding typeBinding, int sourceBegin) {
	  if (typeBinding == null) {
	    return null;
	  }
	  if (typeBinding instanceof CaptureBinding) {
			CaptureBinding captureBinding = (CaptureBinding) typeBinding;
			if (captureBinding.wildcard != null) {
				Wildcard res = new Wildcard(captureBinding.wildcard.boundKind);
				res.bound = binding2typeRef(captureBinding.wildcard.bound, sourceBegin);
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
		String [] packageNameInParts;
		if (packageName.length == 0) {
			packageNameInParts = new String[0];
		} else {
			packageNameInParts = new String(packageName).split("\\.");
		}
		char [] [] tokens = new char[packageNameInParts.length + classNameInParts.length] [];
		long [] pos = new long[packageNameInParts.length + classNameInParts.length];
		{
			for (int i = 0; i < pos.length; i++) {
				long posLong = (sourceBegin + i) << 32 | sourceBegin + i + 1;
				pos[i] = posLong;
			}
		}
		for (int i=0; i<packageNameInParts.length; i++) {
			tokens[i] = packageNameInParts[i].toCharArray();
		}
		for (int i=0; i<classNameInParts.length; i++) {
			tokens[i + packageNameInParts.length] = classNameInParts[i].toCharArray();
		}
		
		if (typeBinding instanceof ParameterizedTypeBinding) {
			ParameterizedTypeBinding genericType = (ParameterizedTypeBinding)typeBinding;
			TypeReference [] [] genericParams = new TypeReference[tokens.length][];
			if (genericType.arguments != null) {
	  			TypeReference [] lastComponentParams = new TypeReference[genericType.arguments.length];
	  			genericParams[genericParams.length-1] = lastComponentParams;
	  			
	  			for (int i=0; i<lastComponentParams.length; i++) {
	  				lastComponentParams[i] = binding2typeRef(genericType.arguments[i], sourceBegin);
	  			}
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
	this.scope = givenScope;
	if (this.statements != null) {
		for (int i = 0, length = this.statements.length; i < length; i++) {
			this.statements[i].resolve(this.scope);
		}
	}
}

public void traverse(ASTVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		if (this.statements != null) {
			for (int i = 0, length = this.statements.length; i < length; i++)
				this.statements[i].traverse(visitor, this.scope);
		}
	}
	visitor.endVisit(this, blockScope);
}

/**
 * Dispatch the call on its last statement.
 */
public void branchChainTo(BranchLabel label) {
	if (this.statements != null) {
		this.statements[this.statements.length - 1].branchChainTo(label);
	}
}
}
