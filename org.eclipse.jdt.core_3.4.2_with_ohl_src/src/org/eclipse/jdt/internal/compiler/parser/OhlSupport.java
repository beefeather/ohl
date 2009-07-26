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
import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;
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
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.ast.SwitchStatement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.ThrowStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

public class OhlSupport {

	private static final String FIELD_PREFIX = "f_";
  private static final String CASE_CLASS_PREFIX = "Case_";
  public static final String VISITOR_TYPE_METHOD_PREFIX = "visit_type_";
  private static final String VISITOR_STRUCT_METHOD_PREFIX = "visit_struct_";
  public static final char [] NO_TAG_IDENTIFIER = "<ohl no tag>".toCharArray();

  static void transformEnumCaseDeclaration(TypeDeclaration enumDeclaration) {

    enumDeclaration.ohlIsEnumCase = true;
    
    TypeReference[] origSuperInterfaces = enumDeclaration.superInterfaces;
    enumDeclaration.superInterfaces = null;
		TypeParameter[] typeParameters = enumDeclaration.typeParameters;
		enumDeclaration.typeParameters = null;


    AbstractMethodDeclaration [] allMethods = enumDeclaration.methods;
    FieldDeclaration[] allFields = enumDeclaration.fields;
		
    // Filter-out methods
    int [] method1Map;
    int [] field1Map;
    int size1fieldx;
    int size1method;
		{
      if (allMethods == null) {
        size1method = 0;
        method1Map = null;
      } else {
        method1Map = new int[allMethods.length];
        
        size1method = 0;
        {
          int [] method2Map = new int[allMethods.length];
          int size2 = 0;
          for (int i=0; i<allMethods.length; i++) {
            if (allMethods[i] instanceof MethodDeclaration) {
              method1Map[size1method] = i;
              size1method++;
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
      if (allFields == null) {
        size1fieldx = 0;
        field1Map = null;
      } else {
        field1Map = new int[allFields.length];
        
        size1fieldx = 0;
        {
          int [] field2Map = new int[allFields.length];
          int size2 = 0;
          for (int i=0; i<allFields.length; i++) {
            if (true /*allFields[i] instanceof FieldDeclaration*/) {
              field1Map[size1fieldx] = i;
              size1fieldx++;
            } else {
              field2Map[size2] = i;
              size2++;
            }
          }
        }
      }
		}
    //boolean size1NotZero = size1field + size1method != 0;
		
		// Case holder (where case classes lies)
		TypeDeclaration caseHolderDeclaration = new TypeDeclaration(enumDeclaration.compilationResult);
    setSourcePositionToPoint(caseHolderDeclaration, enumDeclaration.declarationSourceStart);
		caseHolderDeclaration.enclosingType = enumDeclaration;
		caseHolderDeclaration.modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccInterface;
		caseHolderDeclaration.name = CASE_HOLDER_INTERFACE_NAME.toCharArray();
		if (origSuperInterfaces != null) {
      caseHolderDeclaration.superInterfaces = new TypeReference[origSuperInterfaces.length];
		  for (int i=0; i<origSuperInterfaces.length; i++) {
        caseHolderDeclaration.superInterfaces[i] = 
          convertToMemberType(origSuperInterfaces[i], CASE_HOLDER_INTERFACE_NAME.toCharArray(), false);
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
      
      int superInterfacesLength = size1fieldx;
      if (origSuperInterfaces != null) {
        superInterfacesLength += origSuperInterfaces.length;
      }
      
      if (superInterfacesLength != 0) {
        visitorDeclaration.superInterfaces = new TypeReference[superInterfacesLength];

        for (int i = 0; i<size1fieldx; i++) {
          FieldDeclaration fl = allFields[field1Map[i]];
          TypeReference ref1 = convertToMemberType(fl.type, VISITOR_INTERFACE_NAME.toCharArray(), true);
          visitorDeclaration.superInterfaces[i] = ref1;
        }
        if (origSuperInterfaces != null) {
          for (int i=0; i<origSuperInterfaces.length; i++) {
            TypeReference refOrig = origSuperInterfaces[i];
            TypeReference ref1 = convertToMemberType(refOrig, VISITOR_INTERFACE_NAME.toCharArray(), true);
            visitorDeclaration.superInterfaces[size1fieldx + i] = ref1;
            
  //          if (ref1.typeParameters != null) {
  //            visitorDeclaration.typeParameters = new TypeParameter[typeParameters.length];
  //            System.arraycopy(typeParameters, 0, visitorDeclaration.typeParameters, 0, typeParameters.length);
  //          }
          }
        }
      }
      if (typeParameters != null) {
        visitorDeclaration.typeParameters = Cloner.clone(typeParameters);
      }
      
      if (size1method != 0) {
        visitorDeclaration.methods = new AbstractMethodDeclaration[size1method];
        for (int i=0; i<size1method; i++) {
          MethodDeclaration origMd = (MethodDeclaration) allMethods[method1Map[i]];
          
          MethodDeclaration md = new MethodDeclaration(origMd.compilationResult);
          md.modifiers |= ClassFileConstants.AccAbstract; 

          String oldSelector = new String(origMd.selector);
          md.selector = (VISITOR_STRUCT_METHOD_PREFIX+oldSelector).toCharArray();
          md.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
          md.arguments = origMd.arguments;
          
          setMethodSourcePosition(md, origMd);
          
          
          visitorDeclaration.methods[i] = md;
        }
      }
		}

		{ 
      // Factory methods
      if (size1method != 0) {
        caseHolderDeclaration.memberTypes = new TypeDeclaration[size1method];
        for (int i=0; i<size1method; i++) {
          MethodDeclaration origMd = (MethodDeclaration) allMethods[method1Map[i]];
          MethodDeclaration factoryMethod;
          {
            factoryMethod = new MethodDeclaration(enumDeclaration.compilationResult);
            factoryMethod.selector = origMd.selector;
            factoryMethod.modifiers |= ClassFileConstants.AccStatic | ClassFileConstants.AccPublic;
            setMethodSourcePosition(factoryMethod, origMd);

            if (typeParameters != null) {
              factoryMethod.typeParameters = Cloner.clone(typeParameters);
            }            

            if (origMd.arguments == null) {
            } else {
              int argNumber = origMd.arguments.length;
              factoryMethod.arguments = new Argument[argNumber];
              for (int j=0; j<argNumber; j++) {
                factoryMethod.arguments[j] = 
                  new Argument(origMd.arguments[j].name, 0, origMd.arguments[j].type, 0);
              }
            }

            QualifiedTypeReference caseClassReference;
            if (typeParameters == null) {
              caseClassReference = new QualifiedTypeReference(
                  new char [] [] {
                      CASE_HOLDER_INTERFACE_NAME.toCharArray(),
                      (CASE_CLASS_PREFIX + new String(origMd.selector)).toCharArray()
                  },
                  new long [2]);
            } else {
              TypeReference [][] copyParams = new TypeReference[2][];
              copyParams[1] = convertParamsToRefs(typeParameters);
              
              caseClassReference = new ParameterizedQualifiedTypeReference(
                  new char [] [] {
                      CASE_HOLDER_INTERFACE_NAME.toCharArray(),
                      (CASE_CLASS_PREFIX + new String(origMd.selector)).toCharArray()
                  },
                  copyParams,
                  0,
                  new long [2]);
            }
            factoryMethod.returnType = caseClassReference;

            Expression returnExpression;
            if (origMd.arguments == null) {
              QualifiedNameReference fieldReference = new QualifiedNameReference(
                new char [] [] {
                  CASE_HOLDER_INTERFACE_NAME.toCharArray(),
                  (CASE_CLASS_PREFIX + new String(origMd.selector)).toCharArray(),
                  SINGLETON_FIELD_NAME.toCharArray()
                
                },
                new long [3], 0 ,0);
              returnExpression = fieldReference;
            } else {
              AllocationExpression allocation = new AllocationExpression();
              allocation.type = caseClassReference;
  
              int argNumber = origMd.arguments.length;
              Expression [] allocationArguments = new Expression[argNumber];
              for (int j=0; j<argNumber; j++) {
                allocationArguments[j] = new SingleNameReference(origMd.arguments[j].name, 0);
              }
              
              allocation.arguments = allocationArguments;
              returnExpression = allocation; 
            }
            Statement returnStatement = new ReturnStatement(returnExpression, 0, 0);
            factoryMethod.statements = new Statement[] { returnStatement };
          }
          
          enumDeclaration.methods[method1Map[i]] = factoryMethod;
        }
      }
    }		
		
		
		// case classes 
		{
      if (size1method != 0) {
        caseHolderDeclaration.memberTypes = new TypeDeclaration[size1method];
        for (int i=0; i<size1method; i++) {
          MethodDeclaration origMd = (MethodDeclaration) allMethods[method1Map[i]];
          
          TypeDeclaration subClass = new TypeDeclaration(enumDeclaration.compilationResult);
          subClass.enclosingType = caseHolderDeclaration;
          String oldSelector = new String(origMd.selector);
          subClass.name = (CASE_CLASS_PREFIX+oldSelector).toCharArray();
          subClass.modifiers |= ClassFileConstants.AccStatic | ClassFileConstants.AccPublic;
          subClass.typeParameters = Cloner.clone(typeParameters);
          
          TypeReference visitorRef;
          if (typeParameters == null) {
            visitorRef = new SingleTypeReference(VISITOR_INTERFACE_NAME.toCharArray(), 0);
          } else {
            TypeReference [] refTypeParameters = convertParamsToRefs(typeParameters);
            visitorRef = new ParameterizedSingleTypeReference(VISITOR_INTERFACE_NAME.toCharArray(), refTypeParameters, 0, 0l);
          }
          
          // ru.spb.rybin.ohl.lang.EnumCaseBase<Visitor>
          subClass.superInterfaces = new TypeReference[] {  makeEnumCaseBaseRefernce(visitorRef) };
          subClass.modifiers |= ClassFileConstants.AccStatic;
          
          ConstructorDeclaration constructor;
          // Constructor
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
            acceptMethod.selector = ACCEPT_METHOD_SELECTOR;
            acceptMethod.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
            acceptMethod.arguments = new Argument[] { new Argument("visitor".toCharArray(), 0, visitorRef, 0) };
            acceptMethod.modifiers |= ClassFileConstants.AccPublic;

            MessageSend acceptMessageSend = new MessageSend();
            acceptMessageSend.receiver = new SingleNameReference("visitor".toCharArray(), 0);
            acceptMessageSend.selector = (VISITOR_STRUCT_METHOD_PREFIX+oldSelector).toCharArray();
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
              FieldDeclaration field = new FieldDeclaration();
              field.name = SINGLETON_FIELD_NAME.toCharArray();
              field.type = new SingleTypeReference(subClass.name, 0);
              field.modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccFinal | ClassFileConstants.AccStatic;
              AllocationExpression initializer = new AllocationExpression();
              initializer.type = new SingleTypeReference(subClass.name, 0);
              field.initialization = initializer;
              subClass.fields = new FieldDeclaration[] { field };
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
          subClass.addClinit();

          caseHolderDeclaration.memberTypes[i] = subClass;
        }
      }
		}

    enumDeclaration.memberTypes = new TypeDeclaration [] { caseHolderDeclaration, visitorDeclaration } ;
    
    enumDeclaration.fields = new FieldDeclaration [] {
        createOhlClassField(enumDeclaration.name, enumDeclaration.sourceStart)
    };
		
	}
  
  static FieldDeclaration createOhlClassField(char[] classShortName, int enumPos) {
    FieldDeclaration field = new FieldDeclaration(OHL_CLASS_FIELD_NAME, enumPos, enumPos + 2);
    field.modifiers |= ClassFileConstants.AccPublic | ClassFileConstants.AccFinal | ClassFileConstants.AccStatic;
    field.type = createOhlClassTypeReference(classShortName);
    
    AllocationExpression initializer = new AllocationExpression();
    initializer.type = createOhlClassTypeReference(classShortName);
    ClassLiteralAccess literal = new ClassLiteralAccess(0, new SingleTypeReference(VISITOR_INTERFACE_NAME.toCharArray(), 0));
    initializer.arguments = new Expression[] { literal };
    initializer.sourceStart = enumPos + 1;
    initializer.sourceEnd = enumPos + 2;
    field.initialization = initializer;
    
    field.declarationSourceStart = field.sourceStart;
    field.declarationSourceEnd = field.sourceEnd;
    field.declarationEnd = field.sourceEnd;
    
    return field;
  }
  
  static TypeReference createOhlClassTypeReference(char[] classShortName) {
    TypeReference [][] typeArguments = new TypeReference [OHL_CLASS_TOKENS.length][];
    TypeReference visitorReference = new QualifiedTypeReference(new char[][] { classShortName, VISITOR_INTERFACE_NAME.toCharArray() },
                                                                new long[2]);
    typeArguments[OHL_CLASS_TOKENS.length-1] = new TypeReference[] { visitorReference };
    return new ParameterizedQualifiedTypeReference(OHL_CLASS_TOKENS, typeArguments, 0, new long[OHL_CLASS_TOKENS.length]);
  }

  static ParameterizedQualifiedTypeReference makeEnumCaseBaseRefernce(TypeReference visitorRef) {
    TypeReference [][] typeArguments = new TypeReference [ENUM_CASE_BASE_TOKENS.length][];
    typeArguments[ENUM_CASE_BASE_TOKENS.length-1] = new TypeReference[] { visitorRef };
    return new ParameterizedQualifiedTypeReference(ENUM_CASE_BASE_TOKENS, typeArguments, 0, new long [] {0,0,0,0,0,0});
  }

  private static TypeReference[] convertParamsToRefs(TypeParameter [] typeParameters) {
    if (typeParameters == null) {
      return null;
    }
    TypeReference [] refTypeParameters = new TypeReference[typeParameters.length];
    for (int j=0; j<typeParameters.length; j++) {
      TypeReference tr = new SingleTypeReference(typeParameters[j].name, 0);
      refTypeParameters[j] = tr;
    }
    return refTypeParameters;
  }

  private static void setMethodSourcePosition(MethodDeclaration newMethod,
      MethodDeclaration origMd) {
    newMethod.sourceStart = origMd.sourceStart;
    newMethod.sourceEnd = origMd.sourceEnd;
    newMethod.declarationSourceStart = origMd.sourceStart;
    newMethod.declarationSourceEnd = origMd.sourceEnd;
    // don't touch body positions, parser would try to parse otherwise
  }
  private static void setMethodSourcePosition(MethodDeclaration newMethod,
      CaseStatement orig) {
    newMethod.sourceStart = orig.sourceStart;
    newMethod.sourceEnd = orig.sourceEnd;
    newMethod.declarationSourceStart = orig.sourceStart;
    newMethod.declarationSourceEnd = orig.sourceEnd;
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
          if (caseStatement.ohlCaseType != CaseStatement.OHL_NOT_OHL) {
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
    int ohlNonDefaultCasesNum = 0;
    for (int i=0; i<switchStatement.statements.length; i++) {
      if (switchStatement.statements[i] instanceof CaseStatement) {
        CaseStatement caseStatement = (CaseStatement) switchStatement.statements[i];
        if (caseStatement.ohlCaseType != CaseStatement.OHL_NOT_OHL) {
          if (caseStatement.ohlCaseType == CaseStatement.OHL_DEFAULT_CASE) {
            hasExplicitDefault = true;
          } else {
            caseStatePos[curI++] = i;
          }
        }
      }
    }
    ohlNonDefaultCasesNum = curI;
    
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
    setSourcePositionToPoint(anonymousType, switchStatement.sourceStart);
    
    QualifiedAllocationExpression alloc = new QualifiedAllocationExpression(anonymousType);
    alloc.anonymousType = anonymousType;
    anonymousType.allocation = alloc;
    
    SingleTypeReference anonClassTypeRef = new SingleTypeReference("<not specified>".toCharArray(), 0);
    alloc.type = anonClassTypeRef;
    alloc.statementEnd = -1;
    switchStatement.ohlTodoAnonymousAlloc = alloc;
    
    
    anonymousType.methods = new AbstractMethodDeclaration[ohlNonDefaultCasesNum];
    
    Block [] caseBlocks = new Block[ohlNonDefaultCasesNum];
    CaseStatement[] caseStatements = new CaseStatement[ohlNonDefaultCasesNum];


    
    for (int i=0; i<ohlNonDefaultCasesNum; i++) {
      CaseStatement caseSt = (CaseStatement) switchStatement.statements[caseStatePos[i]];
      
      MethodDeclaration md = new MethodDeclaration(compilationResult);
      setMethodSourcePosition(md, caseSt);
      
      md.annotations = new Annotation [] { 
          new NormalAnnotation(new SingleTypeReference("Override".toCharArray(), 0), 0)
      };
      
      anonymousType.methods[i] = md;
      
      switch (caseSt.ohlCaseType) {
        case CaseStatement.OHL_STRUCT_CASE: {
          md.selector = (VISITOR_STRUCT_METHOD_PREFIX+new String(caseSt.ohlSelector)).toCharArray();
        } break;
        case CaseStatement.OHL_TYPE_CASE: {
          md.selector = VISITOR_TYPE_METHOD_PREFIX.toCharArray();
          
        } break;
        default: throw new RuntimeException("Unexpected type of ohl case statement");
      }
      
      if (caseSt.ohlArgumentProto != null) {
        if (caseSt.ohlArgumentProto.length == 0) {
          md.arguments = null;
        } else {
          md.arguments = new Argument[caseSt.ohlArgumentProto.length];
          for (int j=0; j<md.arguments.length; j++) {
            md.arguments[j] = new Argument(("ignore"+j).toCharArray(), 0, caseSt.ohlArgumentProto[j].type, 0);
          }
        }
      }
      md.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
      md.modifiers |= ClassFileConstants.AccPublic;

      md.statements = new Statement[] {
          new ReturnStatement(new IntLiteral(Integer.toString(i+CASE_CODES_START).toCharArray(), 0, 0), 0, 0)
      };
      
      caseSt.ohlTodoCastExpression.expression = new SingleNameReference(synVarName.toCharArray(), 0);
      caseSt.constantExpression = new IntLiteral(Integer.toString(i+CASE_CODES_START).toCharArray(), 0, 0);
      caseStatements[i] = caseSt;
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
    
    switchStatement.ohlCaseStatements = caseStatements;
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

	
	public static TypeReference convertToMemberType(TypeReference refOrig, char [] memberName, boolean keepGenerics) {
		TypeReference res;
		if (refOrig instanceof SingleTypeReference) {
			char[][] sources = new char [] [] {
      		((SingleTypeReference) refOrig).token,
      		memberName
      };
      long[] positions = new long [] {0, 0 };
      if (keepGenerics && refOrig instanceof ParameterizedSingleTypeReference) {
        ParameterizedSingleTypeReference parameterized = 
            (ParameterizedSingleTypeReference) refOrig;
        TypeReference[] origTypeArguments = Cloner.clone(parameterized.typeArguments);
        TypeReference[][] typeArgs = new TypeReference[2][];
        typeArgs[1] = origTypeArguments;
        res = new ParameterizedQualifiedTypeReference(
            sources,
            typeArgs,
            0,
            positions
            );
      } else {
        res = new QualifiedTypeReference(
            sources,
            positions
            );
      }
		} else if (refOrig instanceof QualifiedTypeReference) {
      char[][] oldTokens = ((QualifiedTypeReference) refOrig).tokens;
      long [] oldPos = ((QualifiedTypeReference) refOrig).sourcePositions;
      char [] [] newTokens = new char[oldTokens.length+1][];
      System.arraycopy(oldTokens, 0, newTokens, 0, oldTokens.length);
      newTokens[oldTokens.length] = memberName;
      
      long [] newPos = new long[oldPos.length+1];
      System.arraycopy(oldPos, 0, newPos, 0, oldPos.length);
      newPos[oldPos.length] = oldPos[oldPos.length-1];
      
		  if (keepGenerics && refOrig instanceof ParameterizedQualifiedTypeReference) {
        ParameterizedQualifiedTypeReference parameterized = 
          (ParameterizedQualifiedTypeReference) refOrig;
        TypeReference[][] origTypeArguments = Cloner.clone(parameterized.typeArguments);
        TypeReference[][] typeArgs = new TypeReference[origTypeArguments.length + 1][];
        System.arraycopy(origTypeArguments, 0, typeArgs, 0, origTypeArguments.length - 1);
        typeArgs[typeArgs.length-1] = origTypeArguments[origTypeArguments.length-1];
        res = new ParameterizedQualifiedTypeReference(
            newTokens,
            origTypeArguments,
            0,
            newPos
            );
      } else {
        res = new QualifiedTypeReference(
            newTokens,
            newPos
            );
		  }
		} else if (refOrig == null) {
		  return null;
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
  public static final char [][] OHL_CLASS_TOKENS = {
    "ru".toCharArray(), 
    "spb".toCharArray(), 
    "rybin".toCharArray(), 
    "ohl".toCharArray(), 
    "lang".toCharArray(), 
    "OhlClass".toCharArray()              
};
	public static final String CASE_HOLDER_INTERFACE_NAME = "C";
  public static final String SINGLETON_FIELD_NAME = "instance";
  public static final char[] ACCEPT_METHOD_SELECTOR = "accept".toCharArray();
	
  static final int CASE_CODES_START = 2;
  public static final char[] IMPLEMENTS_TAG_FIELD_NAME = "ohl_name_tag".toCharArray();
  private static final char[] OHL_CLASS_FIELD_NAME = "ohl_class".toCharArray();
  
  static class Cloner {

    public static TypeParameter[] clone(TypeParameter[] typeParameters) {
      if (typeParameters == null) {
        return null;
      }
      TypeParameter [] copy = new TypeParameter[typeParameters.length];
      for (int i=0; i<copy.length; i++) {
        copy[i] = clone(typeParameters[i]);
      }
      return copy;
    }

    public static TypeReference[][] clone(TypeReference[][] typeArguments) {
      TypeReference[][] copy = new TypeReference[typeArguments.length][];
      for (int i=0; i<copy.length; i++) {
        copy[i] = typeArguments[i];
      }
      return copy;
    }

    public static TypeReference[] clone(TypeReference[] typeArguments) {
      if (typeArguments == null) {
        return null;
      }
      TypeReference[] copy = new TypeReference[typeArguments.length];
      for (int i=0; i<copy.length; i++) {
        copy[i] = typeArguments[i];
      }
      return copy;
    }

    public static TypeReference clone(TypeReference typeArgument) {
      if (typeArgument instanceof SingleTypeReference) {
        if (typeArgument instanceof ParameterizedSingleTypeReference) {
          ParameterizedSingleTypeReference orig =
            (ParameterizedSingleTypeReference)typeArgument;
          ParameterizedSingleTypeReference copy =
            new ParameterizedSingleTypeReference(orig.token, 
                clone(orig.typeArguments), orig.dimensions(), 0);
          copy.sourceStart = orig.sourceStart;
          copy.sourceEnd = orig.sourceEnd;
          return copy;
        } else {
          SingleTypeReference orig =
            (SingleTypeReference)typeArgument;
          SingleTypeReference copy =
            new SingleTypeReference(orig.token, 0);
          copy.sourceStart = orig.sourceStart;
          copy.sourceEnd = orig.sourceEnd;
          return copy;
        }
      } else if (typeArgument instanceof QualifiedTypeReference) {
        if (typeArgument instanceof ParameterizedQualifiedTypeReference) {
          ParameterizedQualifiedTypeReference orig =
            (ParameterizedQualifiedTypeReference)typeArgument;
          ParameterizedQualifiedTypeReference copy =
            new ParameterizedQualifiedTypeReference(orig.tokens, 
                clone(orig.typeArguments), orig.dimensions(), orig.sourcePositions);
          return copy;
        } else {
          QualifiedTypeReference orig =
            (QualifiedTypeReference)typeArgument;
          QualifiedTypeReference copy =
            new QualifiedTypeReference(orig.tokens, 
                orig.sourcePositions);
          return copy;
          
        }
        
      } else {
        throw new RuntimeException();
      }
    }

    private static TypeParameter clone(TypeParameter typeParameter) {
      TypeParameter res = new TypeParameter();
      res.bounds = typeParameter.bounds;
      res.initialization = typeParameter.initialization;
      res.name = typeParameter.name;
      res.type = typeParameter.type;
      
      return res;
    }
    
  }

  public static void addVisitorToClassifier(final TypeDeclaration typeDecl) {
    
    class Util {
      TypeReference getSelfReference() {
        if (typeDecl.typeParameters == null) {
          return new SingleTypeReference(typeDecl.name, 0);
        } else {
          return new ParameterizedSingleTypeReference(typeDecl.name, 
              convertParamsToRefs(typeDecl.typeParameters), 0, 0);
        }
      }
    }
    
    if (typeDecl.superInterfaces != null) {
      for (int i=0; i<typeDecl.superInterfaces.length; i++) {
        if (typeDecl.superInterfaces[i] instanceof ParameterizedQualifiedTypeReference) {
          ParameterizedQualifiedTypeReference ref1 = (ParameterizedQualifiedTypeReference) typeDecl.superInterfaces[i];
          
          {
            ref1.typeArguments[ref1.typeArguments.length - 1] = new TypeReference [] { 
                convertToMemberType(new Util().getSelfReference(), VISITOR_INTERFACE_NAME.toCharArray(), true) };
          }
          
          if (ref1.ohlImplementsTag != null) {
            TypeDeclaration visitorDecl = new TypeDeclaration(typeDecl.compilationResult);
            visitorDecl.enclosingType = typeDecl;
            visitorDecl.name = VISITOR_INTERFACE_NAME.toCharArray();
            visitorDecl.modifiers |= ClassFileConstants.AccInterface | ClassFileConstants.AccStatic 
                | ClassFileConstants.AccPublic | ClassFileConstants.AccAbstract;
            visitorDecl.typeParameters = Cloner.clone(typeDecl.typeParameters);

            TypeDeclaration[] memberTypes = typeDecl.memberTypes;
            TypeDeclaration[] memberTypes2;
            if (memberTypes == null) {
              memberTypes2 = new TypeDeclaration[] { visitorDecl };
            } else {
              memberTypes2 = new TypeDeclaration[memberTypes.length + 1];
              System.arraycopy(memberTypes, 0, memberTypes2, 0, memberTypes.length);
              memberTypes2[memberTypes.length] = visitorDecl;
            }
            
            typeDecl.memberTypes = memberTypes2;
            
            MethodDeclaration md = new MethodDeclaration(typeDecl.compilationResult);
            md.modifiers |= ClassFileConstants.AccPublic; 

            md.selector = VISITOR_TYPE_METHOD_PREFIX.toCharArray();
            md.returnType = TypeReference.baseTypeReference(TypeIds.T_int, 0);
            TypeReference argType = new Util().getSelfReference();
            md.arguments = new Argument[] {
                new Argument("obj".toCharArray(), 0, argType, 0)
            };
            
            visitorDecl.methods = new AbstractMethodDeclaration[] { md };
            
            FieldDeclaration[] fields = typeDecl.fields;
            FieldDeclaration ohlClassField = createOhlClassField(typeDecl.name, typeDecl.sourceStart);
            if (fields == null) {
              fields = new FieldDeclaration[] { ohlClassField };
            } else {
              FieldDeclaration[] fields2 = new FieldDeclaration[fields.length + 1];
              System.arraycopy(fields, 0, fields2, 0, fields.length);
              fields2[fields.length] = ohlClassField;
              fields = fields2;
            }
            typeDecl.fields = fields;
            typeDecl.addClinit();
          }
        }
      }
    }
  }
}
