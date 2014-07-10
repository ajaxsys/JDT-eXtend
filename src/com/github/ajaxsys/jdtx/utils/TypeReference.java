/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.github.ajaxsys.jdtx.utils;

import java.io.Serializable;

/**
 * A class to represent the reference in a class file to some type (class,
 * primitive or array). A type reference is uniquely defined by
 * <ul>
 * <li>an initiating class loader
 * <li>a type name
 * </ul>
 * Resolving a TypeReference to a Type can be an expensive operation. Therefore
 * we canonicalize TypeReference instances and cache the result of resolution.
 */
public final class TypeReference implements Serializable {

	/* Serial version */
	private static final long serialVersionUID = -3256390509887654327L;

	/**
	 * NOTE: initialisation order is important!
	 *
	 * TypeReferences are canonical.
	 */

	/*********************************************************************************************************************
	 * Primitive Dispatch *
	 ********************************************************************************************************************/

	public final static byte BooleanTypeCode = 'Z';

	public final static byte ByteTypeCode = 'B';

	public final static byte CharTypeCode = 'C';

	public final static byte DoubleTypeCode = 'D';

	public final static byte FloatTypeCode = 'F';

	public final static byte IntTypeCode = 'I';

	public final static byte LongTypeCode = 'J';

	public final static byte ShortTypeCode = 'S';

	public final static byte VoidTypeCode = 'V';

	public final static byte OtherPrimitiveTypeCode = 'P';

	public final static byte ClassTypeCode = 'L';

	public final static byte ArrayTypeCode = '[';

	public final static byte PointerTypeCode = '*';

	public final static byte ReferenceTypeCode = '&';

}