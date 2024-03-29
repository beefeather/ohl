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

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

    public final static int

      ERROR_SYMBOL      = 110,
      MAX_NAME_LENGTH   = 41,
      NUM_STATES        = 1005,

      NT_OFFSET         = 110,
      SCOPE_UBOUND      = 141,
      SCOPE_SIZE        = 142,
      LA_STATE_OFFSET   = 12909,
      MAX_LA            = 1,
      NUM_RULES         = 733,
      NUM_TERMINALS     = 110,
      NUM_NON_TERMINALS = 326,
      NUM_SYMBOLS       = 436,
      START_STATE       = 761,
      EOFT_SYMBOL       = 69,
      EOLT_SYMBOL       = 69,
      ACCEPT_ACTION     = 12908,
      ERROR_ACTION      = 12909;
}

