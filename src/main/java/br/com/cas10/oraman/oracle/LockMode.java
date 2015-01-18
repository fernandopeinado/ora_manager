package br.com.cas10.oraman.oracle;

import java.util.List;

import com.google.common.collect.ImmutableList;

public enum LockMode {

  NONE(0, "none"),

  NULL(1, "null (NULL)"),

  SS(2, "row-S (SS)"),

  SX(3, "row-X (SX)"),

  S(4, "share (S)"),

  SSX(5, "S/Row-X (SSX)"),

  X(6, "exclusive (X)");

  private static final List<LockMode> VALUES = ImmutableList.copyOf(values());

  static LockMode valueOf(int code) {
    for (LockMode lmode : VALUES) {
      if (lmode.code == code) {
        return lmode;
      }
    }
    throw new IllegalArgumentException();
  }

  private final int code;
  private final String label;

  private LockMode(int code, String label) {
    this.code = code;
    this.label = label;
  }

  String getLabel() {
    return label;
  }
}
