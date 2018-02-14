package us.colloquy.tolstoy.client.ui.reachTextArea;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 11/18/14
 * Time: 4:12 PM
 */

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

/**
 * A widget that implements this interface is a public source of
 * {@link ScrollEvent} events.
 *
 * @param <T> the type being printed
 */
public interface HasScrollHandlers<T> extends HasHandlers
{
	/**
	 *
	 * @param handler the handler
	 * @return the registration for the event
	 */
	HandlerRegistration addScrollHandler(ScrollHandler<T> handler);
}

