package br.com.cas10.oraman.oracle

enum LockMode {

	NONE(0, 'none'),

	NULL(1, 'null (NULL)'),

	SS(2, 'row-S (SS)'),

	SX(3, 'row-X (SX)'),

	S(4, 'share (S)'),

	SSX(5, 'S/Row-X (SSX)'),

	X(6, 'exclusive (X)')

	static final List<LockMode> VALUES = (values() as List).asImmutable()

	static LockMode valueOf(int code) {
		for (LockMode lmode : VALUES) {
			if (lmode.code == code) {
				return lmode
			}
		}
		throw new IllegalArgumentException()
	}

	final int code
	final String label

	private LockMode(int code, String label) {
		this.code = code
		this.label = label
	}
}
