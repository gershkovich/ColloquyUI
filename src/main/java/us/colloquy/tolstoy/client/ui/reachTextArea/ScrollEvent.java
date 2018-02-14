package us.colloquy.tolstoy.client.ui.reachTextArea;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 11/18/14
 * Time: 4:13 PM
 */

import com.google.gwt.event.shared.GwtEvent;

/**
 * Represents a print event.
 *
 * @param <T> the type being printed
 */
public class ScrollEvent<T> extends GwtEvent<ScrollHandler<T>>
{
	/**
	 * Handler type.
	 */
	private static Type<ScrollHandler<?>> TYPE;

	/**
	 * Fires a print event on all registered handlers in the handler manager. If
	 * no such handlers exist, this method will do nothing.
	 *
	 * @param <T>    the target type
	 * @param source the source of the handlers
	 * @param target the target
	 */
	public static <T> void fire( HasScrollHandlers<T> source, T target )
	{
		if ( TYPE != null )
		{
			ScrollEvent<T> event = new ScrollEvent<T>( target );

			source.fireEvent( event );
		}
	}

	/**
	 * Gets the type associated with this event.
	 *
	 * @return returns the handler type
	 */
	public static Type<ScrollHandler<?>> getType()
	{
		return TYPE != null ? TYPE : ( TYPE = new Type<ScrollHandler<?>>() );
	}

	private final T target;

	/**
	 * Creates a new print event.
	 *
	 * @param target the target
	 */
	protected ScrollEvent(T target)
	{
		this.target = target;
	}

	// The instance knows its of type T, but the TYPE
	// field itself does not, so we have to do an unsafe cast here.
	@SuppressWarnings( "unchecked" )
	@Override
	public final Type<ScrollHandler<T>> getAssociatedType()
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
	protected void dispatch( ScrollHandler<T> handler )
	{
		handler.onScroll( this );
	}
}
