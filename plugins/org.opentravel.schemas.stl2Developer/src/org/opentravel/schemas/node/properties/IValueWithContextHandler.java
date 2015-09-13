package org.opentravel.schemas.node.properties;

/**
 * Interface values that are associated with a context.
 * 
 * @author Dave
 *
 */
public interface IValueWithContextHandler {

	/**
	 * Get count of all
	 */
	public int getCount();

	/**
	 * Get value for a context. Empty string if no value is defined for the context.
	 */
	public String get(String context);

	/**
	 * Set value for a specific context. If the value does not exist one will be created. If value is null or empty the
	 * value will be removed.
	 * 
	 * @return true if set, false if not set because context was not found in library.
	 */
	public boolean set(String value, String context);

	/**
	 * Change the context string for the value specified by context.
	 * 
	 * @return Return false and do nothing if no value is found with source context, the target context is already
	 *         assigned, or the target context is not defined.
	 */
	public boolean change(String sourceContext, String targetContext);

	/**
	 * Fix all contexts. The value with the source context will remain if it exists. All other values will be converted
	 * into documentation.
	 */
	public void fix(String sourceContext);

	/**
	 * Test all values and insure all contexts are declared.
	 */
	public boolean areValid();
}
