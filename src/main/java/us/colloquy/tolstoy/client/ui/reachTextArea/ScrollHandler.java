package us.colloquy.tolstoy.client.ui.reachTextArea;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 11/18/14
 * Time: 4:13 PM
 */

import com.google.gwt.event.shared.EventHandler;

/**
 *
 *
 * @param <T> the type for print
 */
public interface ScrollHandler<T> extends EventHandler
{
	/**
	 *
	 * @param event the {@link com.google.gwt.event.logical.shared.CloseEvent} that was fired
	 */
	void onScroll(ScrollEvent<T> event);
}
