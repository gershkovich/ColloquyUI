package us.colloquy.tolstoy.client.ui.dialogBoxImproved;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 8/18/15
 * Time: 4:09 PM
 */

import com.google.gwt.event.shared.GwtEvent;

/**
 * Represents a close event.
 *
 * @param <T> the type being closed
 */
public class MaximizeEvent<T> extends GwtEvent<MaximizeHandler<T>>
{

	/**
	 * Handler type.
	 */
	private static Type<MaximizeHandler<?>> TYPE;

	/**
	 * Fires a close event on all registered handlers in the handler manager.
	 *
	 * @param <T>        the target type
	 * @param source     the source of the handlers
	 * @param target     the target
	 */
	public static <T> void fire( HasMaximizeHandlers<T> source, T target )
	{
		if ( TYPE != null )
		{
			MaximizeEvent<T> event = new MaximizeEvent<T>( target );
			source.fireEvent( event );
		}
	}

	/**
	 * Gets the type associated with this event.
	 *
	 * @return returns the handler type
	 */
	public static Type<MaximizeHandler<?>> getType()
	{
		return TYPE != null ? TYPE : ( TYPE = new Type<MaximizeHandler<?>>() );
	}

	private final T target;

	/**
	 * Creates a new close event.
	 *
	 * @param target         the target
	 */
	protected MaximizeEvent( T target )
	{
		this.target = target;
	}

	// The instance knows its of type T, but the TYPE
	// field itself does not, so we have to do an unsafe cast here.
	@SuppressWarnings( "unchecked" )
	@Override
	public final Type<MaximizeHandler<T>> getAssociatedType()
	{
		return (Type) TYPE;
	}

	/**
	 * Gets the target.
	 *
	 * @return the target
	 */
	public T getTarget()
	{
		return target;
	}


	@Override
	protected void dispatch( MaximizeHandler<T> handler )
	{
		handler.onMaximize( this );
	}
}
