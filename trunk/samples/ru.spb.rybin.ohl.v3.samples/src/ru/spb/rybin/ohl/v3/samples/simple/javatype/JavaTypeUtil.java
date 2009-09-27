package ru.spb.rybin.ohl.v2.samples.simple.javatype;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public class JavaTypeUtil {
  /**
   * All dirty work is inside this method.
   * @return type parameter explicitly subtyped
   */
  public static JavaTypes.case subtype(Type type) {
    if (type instanceof Class<?>) {
      return new JavaTypes.clazz((Class<?>)type);
    }
    if (type instanceof GenericArrayType) {
      return new JavaTypes.genericArray((GenericArrayType)type);
    }
    if (type instanceof ParameterizedType) {
      return new JavaTypes.parameterized((ParameterizedType)type);
    }
    if (type instanceof TypeVariable<?>) {
      return new JavaTypes.variable((TypeVariable<?>)type);
    }
    if (type instanceof WildcardType) {
      return new JavaTypes.wildcard((WildcardType)type);
    }
    throw new RuntimeException("Unknown type type");
  }
}
