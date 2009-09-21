package ru.spb.rybin.ohl.v2.samples.simple.javatype;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public enum-case JavaTypes {
  case clazz(Class<?> cl),
  case genericArray(GenericArrayType ar),
  case parameterized(ParameterizedType type),
  case variable(TypeVariable<?> var),
  case wildcard(WildcardType type)
}
