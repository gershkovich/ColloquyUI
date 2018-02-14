package us.colloquy.tolstoy.client.ui.reachTextArea;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.RichTextArea;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 11/18/14
 * Time: 4:12 PM
 */
public class RichTextAreaImproved extends RichTextArea implements HasScrollHandlers<RichTextAreaImproved>
{
	private String title = "";

	public RichTextAreaImproved( String titleIn, String widthIn, String heightIn )
	{
		super();

		title = titleIn;

		setWidth( widthIn );

		setHeight( heightIn );
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public void setTitle( String titleIn )
	{
		title = titleIn;
	}

	private void scroll()
	{
		ScrollEvent.fire(this, this);
	}

	public HandlerRegistration addScrollHandler(ScrollHandler<RichTextAreaImproved> handler )
	{
		return addHandler(handler, ScrollEvent.getType());
	}

	public void insertHTMLatCursorPosition( String html )
	{
		RichTextArea.Formatter formatter = getFormatter();

		formatter.insertHTML( html );
	}
}
