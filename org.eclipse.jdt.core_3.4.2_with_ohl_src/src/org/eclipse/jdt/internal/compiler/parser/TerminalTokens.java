/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/**
 * IMPORTANT NOTE: These constants are dedicated to the internal Scanner implementation. 
 * It is mirrored in org.eclipse.jdt.core.compiler public package where it is API. 
 * The mirror implementation is using the backward compatible ITerminalSymbols constant 
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens 
 * which constant values reflect the latest parser generation state.
 */
/**
 * Maps each terminal symbol in the java-grammar into a unique integer. 
 * This integer is used to represent the terminal when computing a parsing action. 
 * 
 * Disclaimer : These constant values are generated automatically using a Java 
 * grammar, therefore their actual values are subject to change if new keywords 
 * were added to the language (for instance, 'assert' is a keyword in 1.4).
 */
public interface TerminalTokens {

	// special tokens not part of grammar - not autogenerated
  int TokenNameWHITESPACE = 1000,
  TokenNameCOMMENT_LINE = 1001,
  TokenNameCOMMENT_BLOCK = 1002,
  TokenNameCOMMENT_JAVADOC = 1003;

    public final static int
      TokenNameIdentifier = 24,
      TokenNameabstract = 56,
      TokenNameassert = 75,
      TokenNameboolean = 32,
      TokenNamebreak = 76,
      TokenNamebyte = 33,
      TokenNamecase = 73,
      TokenNamecatch = 102,
      TokenNamechar = 34,
      TokenNameclass = 71,
      TokenNamecontinue = 77,
      TokenNameconst = 108,
      TokenNamedefault = 98,
      TokenNamedo = 78,
      TokenNamedouble = 35,
      TokenNameelse = 103,
      TokenNameenum = 99,
      TokenNameextends = 100,
      TokenNamefalse = 44,
      TokenNamefinal = 57,
      TokenNamefinally = 104,
      TokenNamefloat = 36,
      TokenNamefor = 79,
      TokenNamegoto = 109,
      TokenNameif = 80,
      TokenNameimplements = 106,
      TokenNameimport = 101,
      TokenNameinstanceof = 12,
      TokenNameint = 37,
      TokenNameinterface = 96,
      TokenNamelong = 38,
      TokenNamenative = 58,
      TokenNamenew = 43,
      TokenNamenull = 45,
      TokenNamepackage = 97,
      TokenNameprivate = 59,
      TokenNameprotected = 60,
      TokenNamepublic = 61,
      TokenNamereturn = 81,
      TokenNameshort = 39,
      TokenNamestatic = 54,
      TokenNamestrictfp = 62,
      TokenNamesuper = 41,
      TokenNameswitch = 82,
      TokenNamesynchronized = 55,
      TokenNamethis = 42,
      TokenNamethrow = 83,
      TokenNamethrows = 105,
      TokenNametransient = 63,
      TokenNametrue = 46,
      TokenNametry = 84,
      TokenNamevoid = 40,
      TokenNamevolatile = 64,
      TokenNamewhile = 74,
      TokenNameIntegerLiteral = 47,
      TokenNameLongLiteral = 48,
      TokenNameFloatingPointLiteral = 49,
      TokenNameDoubleLiteral = 50,
      TokenNameCharacterLiteral = 51,
      TokenNameStringLiteral = 52,
      TokenNamePLUS_PLUS = 8,
      TokenNameMINUS_MINUS = 9,
      TokenNameEQUAL_EQUAL = 18,
      TokenNameLESS_EQUAL = 15,
      TokenNameGREATER_EQUAL = 16,
      TokenNameNOT_EQUAL = 19,
      TokenNameLEFT_SHIFT = 17,
      TokenNameRIGHT_SHIFT = 10,
      TokenNameUNSIGNED_RIGHT_SHIFT = 11,
      TokenNamePLUS_EQUAL = 85,
      TokenNameMINUS_EQUAL = 86,
      TokenNameMULTIPLY_EQUAL = 87,
      TokenNameDIVIDE_EQUAL = 88,
      TokenNameAND_EQUAL = 89,
      TokenNameOR_EQUAL = 90,
      TokenNameXOR_EQUAL = 91,
      TokenNameREMAINDER_EQUAL = 92,
      TokenNameLEFT_SHIFT_EQUAL = 93,
      TokenNameRIGHT_SHIFT_EQUAL = 94,
      TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL = 95,
      TokenNameOR_OR = 26,
      TokenNameAND_AND = 25,
      TokenNamePLUS = 1,
      TokenNameMINUS = 2,
      TokenNameNOT = 67,
      TokenNameREMAINDER = 5,
      TokenNameXOR = 21,
      TokenNameAND = 20,
      TokenNameMULTIPLY = 3,
      TokenNameOR = 22,
      TokenNameTWIDDLE = 68,
      TokenNameDIVIDE = 6,
      TokenNameGREATER = 13,
      TokenNameLESS = 7,
      TokenNameLPAREN = 27,
      TokenNameRPAREN = 29,
      TokenNameLBRACE = 66,
      TokenNameRBRACE = 31,
      TokenNameLBRACKET = 14,
      TokenNameRBRACKET = 70,
      TokenNameSEMICOLON = 28,
      TokenNameQUESTION = 23,
      TokenNameCOLON = 65,
      TokenNameCOMMA = 30,
      TokenNameDOT = 4,
      TokenNameEQUAL = 72,
      TokenNameAT = 53,
      TokenNameELLIPSIS = 107,
    TokenNameEOF = 69,
    TokenNameERROR = 110;
}
