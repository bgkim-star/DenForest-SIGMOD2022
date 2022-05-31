package denforest;

import java.util.Objects;

public class Pair<T, V> {
	public T first;
	public V second;

	public Pair(T ta, V tb) {
		first = ta;
		second = tb;
	}

	@Override
	public boolean equals(Object o) {
		// self check
		if (this == o)
			return true;
		// null check
		if (o == null)
			return false;
		// type check and cast
		if (getClass() != o.getClass())
			return false;
		Pair pair = (Pair) o;
		// field comparison
		return (Objects.equals(first, pair.first) && Objects.equals(second,
				pair.second))
				|| (Objects.equals(first, pair.second) && Objects.equals(
						second, pair.first));
	}

}