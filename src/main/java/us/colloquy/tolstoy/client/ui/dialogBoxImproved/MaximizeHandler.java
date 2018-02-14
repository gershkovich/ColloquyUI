package us.colloquy.tolstoy.client.ui.dialogBoxImproved;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 8/18/15
 * Time: 4:08 PM
 */

import com.google.gwt.event.shared.EventHandler;

/**
 * Handler interface for {@link MaximizeEvent} events.
 *
 * @param <T> the type being Maximized
 */
public interface MaximizeHandler<T> extends EventHandler
{

	/**
	 * Called when {@link MaximizeEvent} is fired.
	 *
	 * @param event the {@link MaximizeEvent} that was fired
	 */
	void onMaximize(MaximizeEvent<T> event);
}
