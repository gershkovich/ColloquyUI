package us.colloquy.tolstoy.client.ui.dialogBoxImproved;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 8/18/15
 * Time: 4:39 PM
 */
import com.google.gwt.event.shared.GwtEvent;

/**
 * Represents a restore event.
 *
 * @param <T> the type being closed
 */
public class RestoreEvent<T> extends GwtEvent<RestoreHandler<T>>
{
	/**
	 * Handler type.
	 */
	private static Type<RestoreHandler<?>> TYPE;

	/**
	 * Fires a close event on all registered handlers in the handler manager.
	 *
	 * @param <T>        the target type
	 * @param source     the source of the handlers
	 * @param target     the target
	 */
	public static <T> void fire( HasRestoreHandlers<T> source, T target )
	{
		if ( TYPE != null )
		{
			RestoreEvent<T> event = new RestoreEvent<T>( target );
			source.fireEvent( event );
		}
	}

	/**
	 * Gets the type associated with this event.
	 *
	 * @return returns the handler type
	 */
	public static Type<RestoreHandler<?>> getType()
	{
		return TYPE != null ? TYPE : ( TYPE = new Type<RestoreHandler<?>>() );
	}

	private final T target;

	/**
	 * Creates a new close event.
	 *
	 * @param target         the target
	 */
	protected RestoreEvent( T target )
	{
		this.target = target;
	}

	// The instance knows its of type T, but the TYPE
	// field itself does not, so we have to do an unsafe cast here.
	@SuppressWarnings( "unchecked" )
	@Override
	public final Type<RestoreHandler<T>> getAssociatedType()
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
	protected void dispatch( RestoreHandler<T> handler )
	{
		handler.onRestore( this );
	}
}