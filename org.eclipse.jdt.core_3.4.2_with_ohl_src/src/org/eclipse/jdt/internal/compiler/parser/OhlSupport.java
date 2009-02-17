package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldReference;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class OhlSupport {

	private static final String FIELD_PREFIX = "f_";
  private static final String CASE_CLASS_PREFIX = "Case_";
  private static final String VISITOR_METHOD_PREFIX = "visit_";

  static void transformEnumCaseDeclaration(TypeDeclaration enumDeclaration) {

    enumDeclaration.ohlIsEnumCase = true;
    
		TypeReference[] superInterfaces = enumDeclaration.superInterfaces;
		enumDeclaration.superInterfaces = null;


    AbstractMethodDeclaration [] allMethods = enumDeclaration.methods;
		
    // Filter-out methods
    int [] method1Map;
    int size1;
		{
      if (allMethods == null) {
        size1 = 0;
        method1Map = null;
      } else {
        method1Map = new int[allMethods.length];
        
        size1 = 0;
        {
          int [] method2Map = new int[allMethods.length];
          int size2 = 0;
          for (int i=0; i<allMethods.length; i++) {
            if (allMethods[i] instanceof MethodDeclaration) {
              method1Map[size1] = i;
              size1++;
            } else {
              method2Map[size2] = i;
              size2++;
            }
          }
          AbstractMethodDeclaration [] newEnumMethods = new AbstractMethodDeclaration[allMethods.length];
          for (int i=0; i<size2; i++) {
            newEnumMethods[method2Map[i]] = allMethods[method2Map[i]];
          }
          enumDeclaration.methods = newEnumMethods;
        }
      }
		}
		
		{
		  int nextN;
		  for (int n = 0; n != 11; n = nextN) {
		    if (n == 3) {
		      nextN = 2;
		    } else {
		      break;
		    }
		  }
		}
		
		
		// Case holder (where case classes lies)
		TypeDeclaration caseHolderDeclaration = new TypeDeclaration(enumDeclaration.compilationResult);
    setSourcePositionToPoint(caseHolderDeclaration, enumDeclaration.declarationSourceStart);
		caseHolderDeclaration.enclosingType = enumDeclaration;
		caseHolderDeclaration.modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccInterface;
		caseHolderDeclaration.name = CASE_HOLDER_INTERFACE_NAME.toCharArray();
		if (superInterfaces != null) {
		  for (int i=0; i<superInterfaces.length; i++) {
		    caseHolderDeclaration.superInterfaces = new TypeReference[superInterfaces.length];
        caseHolderDeclaration.superInterfaces[i] = 
          convertToMemberType(superInterfaces[i], CASE_HOLDER_INTERFACE_NAME.toCharArray());
		  }
		}
		

		TypeDeclaration visitorDeclaration;
		{
		  // Visitor interface
      visitorDeclaration = new TypeDeclaration(enumDeclaration.compilationResult);
      visitorDeclaration.enclosingType = enumDeclaration;
      visitorDeclaration.modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccInterface;
      visitorDeclaration.name = VISITOR_INTERFACE_NAME.toCharArray();
      setSourcePositionToPoint(visitorDeclaration, enumDeclaration.declarationSourceStart);
      
      if (superInterfaces != null) {
        visitorDeclaration.superInterfaces = new TypeReference[superInterfaces.length];
        for (int i=0; i<superInterfaces.length; i++) {
          TypeReference refOrig = superInterfaces[i];
          TypeReference ref1 = convertToMemberType(refOrig, VISITOR_INTERFACE_NAME.toCharArray());
          visitorDeclaration.superInterfaces[i] = ref1;
        }
      }
      
      if (size1 != 0) {
        visitorDeclaration.methods = new AbstractMethodDeclaration[size1];
        for (int i=0; i<size1; i++) {
          MethodDeclaration origMd = (MethodDeclaration) allMethods[method1Map[i]];
          
          MethodDeclaration md = new MethodDeclaration(origMd.compilationResult);
          md.modifiers |= ClassFileConstants.AccAbstract; 

          String oldSelector = new String(origMd.selector);
          md.selector = (VISITOR_METHOD_PREFIX+oldSelector).toCharArray();
          md.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
          md.arguments = origMd.arguments;
          
          
          visitorDeclaration.methods[i] = md;
        }
      }
		}

		{ 
      // Factory methods
      if (size1 != 0) {
        caseHolderDeclaration.memberTypes = new TypeDeclaration[size1];
        for (int i=0; i<size1; i++) {
          MethodDeclaration origMd = (MethodDeclaration) allMethods[method1Map[i]];
          MethodDeclaration factoryMethod;
          {
            factoryMethod = new MethodDeclaration(enumDeclaration.compilationResult);
            factoryMethod.selector = origMd.selector;
            factoryMethod.modifiers |= ClassFileConstants.AccStatic | ClassFileConstants.AccPublic;
            setMethodSourcePosition(factoryMethod, origMd);
            
            if (origMd.arguments == null) {
            } else {
              int argNumber = origMd.arguments.length;
              factoryMethod.arguments = new Argument[argNumber];
              for (int j=0; j<argNumber; j++) {
                factoryMethod.arguments[j] = 
                  new Argument(origMd.arguments[j].name, 0, origMd.arguments[j].type, 0);
              }
            }
            
            factoryMethod.returnType = new QualifiedTypeReference(
                new char [] [] {
                    CASE_HOLDER_INTERFACE_NAME.toCharArray(),
                    (CASE_CLASS_PREFIX + new String(origMd.selector)).toCharArray()
                },
                new long [2]);
            
            AllocationExpression allocation = new AllocationExpression();
            allocation.type = new QualifiedTypeReference(
                new char [] [] {
                    CASE_HOLDER_INTERFACE_NAME.toCharArray(),
                    (CASE_CLASS_PREFIX + new String(origMd.selector)).toCharArray()
                },
                new long [2]);

            Expression [] allocationArguments;
            if (origMd.arguments == null) {
              allocationArguments = null;
            } else {
              int argNumber = origMd.arguments.length;
              allocationArguments = new Expression[argNumber];
              for (int j=0; j<argNumber; j++) {
                allocationArguments[j] = new SingleNameReference(origMd.arguments[j].name, 0);
              }
            }
            
            allocation.arguments = allocationArguments;
            Statement returnStatement = new ReturnStatement(allocation, 0, 0);
            factoryMethod.statements = new Statement[] { returnStatement };
          }
          
          enumDeclaration.methods[method1Map[i]] = factoryMethod;
        }
      }
    }		
		
		
		// case classes 
		{
      if (size1 != 0) {
        caseHolderDeclaration.memberTypes = new TypeDeclaration[size1];
        for (int i=0; i<size1; i++) {
          MethodDeclaration origMd = (MethodDeclaration) allMethods[method1Map[i]];
          
          TypeDeclaration subClass = new TypeDeclaration(enumDeclaration.compilationResult);
          subClass.enclosingType = caseHolderDeclaration;
          String oldSelector = new String(origMd.selector);
          subClass.name = (CASE_CLASS_PREFIX+oldSelector).toCharArray();
          subClass.modifiers |= ClassFileConstants.AccStatic | ClassFileConstants.AccPublic;

          
          TypeReference visitorRef = new SingleTypeReference(VISITOR_INTERFACE_NAME.toCharArray(), 0);
          
          // ru.spb.rybin.ohl.lang.EnumCaseBase<Visitor>
          TypeReference [][] typeArguments = new TypeReference [ENUM_CASE_BASE_TOKENS.length][];
          typeArguments[ENUM_CASE_BASE_TOKENS.length-1] = new TypeReference[] { visitorRef };
          
          subClass.superclass = new ParameterizedQualifiedTypeReference(ENUM_CASE_BASE_TOKENS, typeArguments, 0, new long [] {0,0,0,0,0,0});
          subClass.modifiers |= ClassFileConstants.AccStatic;
          
          // Constructor
          ConstructorDeclaration constructor;
          {
            constructor = new ConstructorDeclaration(enumDeclaration.compilationResult);
            constructor.selector = subClass.name; 
            constructor.modifiers |= ClassFileConstants.AccPublic;
            if (origMd.arguments == null) {
            } else {
              int argNumber = origMd.arguments.length;
              //Expression [] constrArguments = new Expression[argNumber];
              constructor.arguments = new Argument[argNumber];
              Statement[] constrStatements = new Statement[argNumber]; 

              for (int j=0; j<argNumber; j++) {
                constructor.arguments[j] = 
                  new Argument(origMd.arguments[j].name, 0, origMd.arguments[j].type, 0);
                
                ThisReference thisReference = new ThisReference(0, 0);
                FieldReference fieldReference = new FieldReference((FIELD_PREFIX+new String(origMd.arguments[j].name)).toCharArray(), 0);
                fieldReference.receiver = thisReference;
                
                Expression lvalue = fieldReference;
                
                SingleNameReference rvalue = new SingleNameReference(origMd.arguments[j].name, 0);
                constrStatements[j] = new Assignment(lvalue, rvalue, 0);
              }
              constructor.statements = constrStatements;
            }
          }
          
          // Accept method
          MethodDeclaration acceptMethod;
          {
            acceptMethod = new MethodDeclaration(enumDeclaration.compilationResult);
            acceptMethod.selector = "accept".toCharArray();
            acceptMethod.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
            acceptMethod.arguments = new Argument[] { new Argument("visitor".toCharArray(), 0, visitorRef, 0) };
            acceptMethod.modifiers |= ClassFileConstants.AccPublic;

            MessageSend acceptMessageSend = new MessageSend();
            acceptMessageSend.receiver = new SingleNameReference("visitor".toCharArray(), 0);
            acceptMessageSend.selector = (VISITOR_METHOD_PREFIX+oldSelector).toCharArray();
            Statement acceptStatement = new ReturnStatement(acceptMessageSend, 0, 0);
            acceptMethod.statements = new Statement [] { acceptStatement };
            if (origMd.arguments == null) {
            } else {
              int argNumber = origMd.arguments.length;
              Expression [] acceptArguments = new Expression[argNumber];
              for (int j=0; j<argNumber; j++) {
                ThisReference thisReference = new ThisReference(0, 0);
                FieldReference fieldReference = 
                  new FieldReference((FIELD_PREFIX+new String(origMd.arguments[j].name)).toCharArray(), 0);
                fieldReference.receiver = thisReference;
                
                Expression lvalue = fieldReference;
                acceptArguments[j] = lvalue;
              }
              acceptMessageSend.arguments = acceptArguments;
            }
          }
          
          // Fields
          {
            if (origMd.arguments == null) {
            } else {
              int argNumber = origMd.arguments.length;
              FieldDeclaration[] fields = new FieldDeclaration[argNumber];
              for (int j=0; j<argNumber; j++) {
                fields[j] = new FieldDeclaration();
                fields[j].name = (FIELD_PREFIX+new String(origMd.arguments[j].name)).toCharArray();
                fields[j].type = origMd.arguments[j].type;
                fields[j].modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccFinal;
              }
              subClass.fields = fields;
            }
          }
          
          subClass.methods = new AbstractMethodDeclaration[] { constructor, acceptMethod };
          caseHolderDeclaration.memberTypes[i] = subClass;
        }
      }
		}

    enumDeclaration.memberTypes = new TypeDeclaration [] { caseHolderDeclaration, visitorDeclaration } ;
    
		
//		
//		{
//
//			
//			for (int i=0; i<size1; i++) {
//        MethodDeclaration md = (MethodDeclaration) allMethods[method1Map[i]];
//				md.modifiers |= ClassFileConstants.AccAbstract; 
//			    if (md.arguments == null) {
//			    } else {
//				    
//            int argNumber = md.arguments.length;
//						
//						for (int j=0; j<argNumber; j++) {
//						  
//						  //constrArguments[j] = new SingleNameReference(md.arguments[j].name, 0);
//						  
//						}
//						
//						//TODO: duplicate static methods from each base enum-case here
//						
//				    }
//			    }
//			    
//			    
//			    {
//				}
//			}
//		}
//		
	}

  private static void setMethodSourcePosition(MethodDeclaration newMethod,
      MethodDeclaration origMd) {
    newMethod.sourceStart = origMd.sourceStart;
    newMethod.sourceEnd = origMd.sourceEnd;
    newMethod.declarationSourceStart = origMd.sourceStart;
    newMethod.declarationSourceEnd = origMd.sourceEnd;
    // don't touch body positions, parser would try to parse otherwise
  }

  private static void setSourcePositionToPoint(TypeDeclaration typeDeclaration, int pos) {
    typeDeclaration.declarationSourceStart
        = typeDeclaration.declarationSourceEnd
        = typeDeclaration.sourceStart
        = typeDeclaration.sourceEnd 
        = typeDeclaration.bodyStart
        = typeDeclaration.bodyEnd 
        = pos;
  }

  public static Block convertSwitchStatement(SwitchStatement switchStatement, CompilationResult compilationResult) {
    int ohlCount = 0;
    if (switchStatement.statements != null) {
      for (int i=0; i<switchStatement.statements.length; i++) {
        if (switchStatement.statements[i] instanceof CaseStatement) {
          CaseStatement caseStatement = (CaseStatement) switchStatement.statements[i];
          if (caseStatement.ohlTodoCastExpression != null) {
            ohlCount++;
          }
        }
      }
    }
    
    if (ohlCount == 0) {
      return null;
    }

    boolean hasExplicitDefault = false;
    int [] caseStatePos = new int [ohlCount];
    int curI = 0;
    for (int i=0; i<switchStatement.statements.length; i++) {
      if (switchStatement.statements[i] instanceof CaseStatement) {
        CaseStatement caseStatement = (CaseStatement) switchStatement.statements[i];
        if (caseStatement.ohlIsOhlCase) {
          if (caseStatement.constantExpression == null) {
            hasExplicitDefault = true;
          } else {
            caseStatePos[curI++] = i;
          }
        }
      }
    }
    
    
    // TODO: make unique
    String synVarName = "switchExpressionCached";
    Statement [] newSts = new Statement[2];
    LocalDeclaration declSt = new LocalDeclaration(synVarName.toCharArray(), 0, 0);
    declSt.initialization = switchStatement.expression;
    declSt.type = TypeReference.baseTypeReference(TypeIds.T_int, 0);
    declSt.ohlRedefineForCast = true; // No problem it is overloaded
    
    
    TypeDeclaration anonymousType = new TypeDeclaration(compilationResult);
    anonymousType.name = CharOperation.NO_CHAR;
    anonymousType.bits |= (ASTNode.IsAnonymousType|ASTNode.IsLocalType);
    
    QualifiedAllocationExpression alloc = new QualifiedAllocationExpression(anonymousType);
    alloc.anonymousType = anonymousType;
    anonymousType.allocation = alloc;
    
    SingleTypeReference anonClassTypeRef = new SingleTypeReference("<not specified>".toCharArray(), 0);
    alloc.type = anonClassTypeRef;
    alloc.statementEnd = -1;
    switchStatement.ohlTodoAnonymousAlloc = alloc;
    
    
    anonymousType.methods = new AbstractMethodDeclaration[caseStatePos.length];
    
    Block [] caseBlocks = new Block[caseStatePos.length];


    
    for (int i=0; i<caseStatePos.length; i++) {
      CaseStatement caseSt = (CaseStatement) switchStatement.statements[caseStatePos[i]];
      
      MethodDeclaration md = new MethodDeclaration(compilationResult);
      
      md.annotations = new Annotation [] { 
          new NormalAnnotation(new SingleTypeReference("Override".toCharArray(), 0), 0)
      };
      
      anonymousType.methods[i] = md;
      md.selector = ("visit_"+new String(caseSt.ohlSelector)).toCharArray();
      md.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
      md.modifiers |= ClassFileConstants.AccPublic;

      if (caseSt.ohlArgumentProto != null) {
        md.arguments = new Argument[caseSt.ohlArgumentProto.length];
        for (int j=0; j<md.arguments.length; j++) {
          md.arguments[j] = new Argument(("ignore"+j).toCharArray(), 0, caseSt.ohlArgumentProto[j].type, 0);
        }
      }
      
      md.statements = new Statement[] {
          new ReturnStatement(new IntLiteral(Integer.toString(i+CASE_CODES_START).toCharArray(), 0, 0), 0, 0)
      };
      
      caseSt.ohlTodoCastExpression.expression = new SingleNameReference(synVarName.toCharArray(), 0);
      caseSt.constantExpression = new IntLiteral(Integer.toString(i+CASE_CODES_START).toCharArray(), 0, 0);
      caseBlocks[i] = (Block) switchStatement.statements[caseStatePos[i] + 1];
    }
    
    if (hasExplicitDefault) {
      alloc.ohlIsVisitorImpl = true;
    } else {
      
      // this default should be never reached event with separate compilation
      // Java would complain about uninitialized var without this default
      CaseStatement defaultCaseSt = new CaseStatement(null, 0, 0);
      AllocationExpression allocationExpression = new AllocationExpression();
      allocationExpression.type = new QualifiedTypeReference(
          new char [] [] {
              "java".toCharArray(),
              "lang".toCharArray(),
              "RuntimeException".toCharArray()
          }
          , new long [3]
          );
      Statement throwSt = new ThrowStatement(allocationExpression, 0, 0);
      Statement [] sts2 = new Statement[switchStatement.statements.length + 2];
      System.arraycopy(switchStatement.statements, 0,
          sts2, 0, 
          switchStatement.statements.length);

      sts2[sts2.length-2] = defaultCaseSt;
      sts2[sts2.length-1] = throwSt;
      switchStatement.statements = sts2;
    }
    
    switchStatement.ohlCaseBlocks = caseBlocks;
    switchStatement.ohlHasExplicitDefault = hasExplicitDefault;
    
    MessageSend messageSend = new MessageSend();
    messageSend.receiver = new SingleNameReference(synVarName.toCharArray(), 0);
    messageSend.selector = "accept".toCharArray();
    messageSend.arguments = new Expression [] { alloc };
    
    switchStatement.expression = messageSend; 

    newSts[0] = declSt;
    newSts[1] = switchStatement;
    
    Block block = new Block(2);
    block.ohlIsSynSwitchBlock = true;
    block.statements = newSts;

    return block;
  }

	
	public static TypeReference convertToMemberType(TypeReference refOrig, char [] memberName) {
		TypeReference res;
		if (refOrig instanceof SingleTypeReference) {
			res = new QualifiedTypeReference(
					new char [] [] {
							((SingleTypeReference) refOrig).token,
							memberName
					},
					new long [] {0, 0 }
					);
		} else if (refOrig instanceof QualifiedTypeReference) {
			char[][] oldTokens = ((QualifiedTypeReference) refOrig).tokens;
			long [] oldPos = ((QualifiedTypeReference) refOrig).sourcePositions;
			char [] [] newTokens = new char[oldTokens.length+1][];
			System.arraycopy(oldTokens, 0, newTokens, 0, oldTokens.length);
			newTokens[oldTokens.length] = memberName;
			
			long [] newPos = new long[oldPos.length+1];
			System.arraycopy(oldPos, 0, newPos, 0, oldPos.length);
			newPos[oldPos.length] = oldPos[oldPos.length-1];
			
			res = new QualifiedTypeReference(
					newTokens, newPos);
		} else {
			throw new RuntimeException();
		}
		return res;
	}

	public static final String VISITOR_INTERFACE_NAME = "Visitor";
	public static final char [][] ENUM_CASE_BASE_TOKENS = {
      "ru".toCharArray(), 
      "spb".toCharArray(), 
      "rybin".toCharArray(), 
      "ohl".toCharArray(), 
      "lang".toCharArray(), 
      "EnumCaseBase".toCharArray()              
  };
	public static final String CASE_HOLDER_INTERFACE_NAME = "C";
	
  static final int CASE_CODES_START = 2;
}
