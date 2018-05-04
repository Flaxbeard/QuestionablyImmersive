package flaxbeard.questionablyimmersive.common.util;

import java.util.Objects;

public class Pair<A, B>
{
	private A a;
	private B b;

	public Pair(A a, B b)
	{
		this.a = a;
		this.b = b;
	}

	public A getFirst()
	{
		return a;
	}

	public B getSecond()
	{
		return b;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(a, pair.a) &&
				Objects.equals(b, pair.b);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(a, b);
	}
}
