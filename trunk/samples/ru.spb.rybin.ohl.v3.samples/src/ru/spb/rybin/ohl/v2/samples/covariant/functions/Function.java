package ru.spb.rybin.ohl.v2.samples.covariant.functions;

public interface Function extends GeneralizedFunction {
  @Override // note, this is a covariant return type
  Value.case getValue(float x);
}
