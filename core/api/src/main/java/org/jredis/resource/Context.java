package org.jredis.resource;

import java.util.Set;

/**
 * {@link Context<T>} provides basic context support for {@link Resource}s,
 * by providing methods for {@link String} parameter name/values and basic
 * namespace operations for generic {@link Object} types. 
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, Apr 16, 2009
 * @since   alpha.0
 * 
 */

public interface Context {
	public String getParam (String key);
	public void setParam (String key, String value);
	Set<String> getParamsKeys();

	public Object get (String name);
	public void bind   (String name, Object value);
	public void rebind   (String name, Object value);
}