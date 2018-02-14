package us.colloquy.tolstoy.client.ui.dialogBoxImproved;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 8/18/15
 * Time: 4:39 PM
 */
import com.google.gwt.event.shared.EventHandler;

/**
 * Handler interface for {@link RestoreEvent} events.
 *
 * @param <T> the type being Restored
 */
public interface RestoreHandler<T> extends EventHandler
{

	/**
	 * Called when {@link RestoreEvent} is fired.
	 *
	 * @param event the {@link RestoreEvent} that was fired
	 */
	void onRestore(RestoreEvent<T> event);
}
