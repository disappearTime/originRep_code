package com.chineseall.iwanvi.wwlive.pc.common.loginContext;


/**
 *
 * @param
 */
public class BaseThreadLocalContext<T extends BaseThreadLocalContext<?>> {
	private final static ThreadLocal<BaseThreadLocalContext<?>> holder = new ThreadLocal<BaseThreadLocalContext<?>>();

	public static void remove() {
		holder.remove();
	}

	public static BaseThreadLocalContext<?> get() {
		return holder.get();
	}

	protected static void set(BaseThreadLocalContext<?> context) {
		holder.set(context);
	}
}