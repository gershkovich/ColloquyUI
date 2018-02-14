package us.colloquy.tolstoy.client.ui.dialogBoxImproved;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 8/18/15
 * Time: 4:10 PM
 */

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * A widget that implements this interface is a public source of
 * {@link MaximizeEvent} events.
 *
 * @param <T> the type being maximized
 */
public interface HasMaximizeHandlers<T> extends HasHandlers
{
	/**
	 * Adds a {@link MaximizeEvent} handler.
	 *
	 * @param handler the handler
	 * @return the registration for the event
	 */
	HandlerRegistration addMaximizeHandler(MaximizeHandler<T> handler);
}
