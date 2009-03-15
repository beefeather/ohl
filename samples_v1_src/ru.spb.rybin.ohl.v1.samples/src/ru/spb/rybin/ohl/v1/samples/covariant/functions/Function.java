package ru.spb.rybin.ohl.v1.samples.covariant.functions;

public interface Function extends GeneralizedFunction {
  @Override // note, covariant return type
  Value.case getValue(float x);
}
