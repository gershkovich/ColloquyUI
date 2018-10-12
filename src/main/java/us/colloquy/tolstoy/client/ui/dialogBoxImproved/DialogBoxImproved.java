package us.colloquy.tolstoy.client.ui.dialogBoxImproved;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

/**
 *PUBLIC SOFTWARE
 *
 *This source code has been placed in the public domain. You can use, modify, and distribute
 *the source code and executable programs based on the source code.
 *
 *However, note the following:
 *
 *DISCLAIMER OF WARRANTY
 *
 * This source code is provided "as is" and without warranties as to performance
 * or merchantability. The author and/or distributors of this source code may
 * have made statements about this source code. Any such statements do not constitute
 * warranties and shall not be relied on by the user in deciding whether to use
 * this source code.This source code is provided without any express or implied
 * warranties whatsoever. Because of the diversity of conditions and hardware
 * under which this source code may be used, no warranty of fitness for a
 * particular purpose is offered. The user is advised to test the source code
 * thoroughly before relying on it. The user must assume the entire risk of
 * using the source code.
 *
 */

/**
 * @author amal
 * @version 1.0
 */
public class DialogBoxImproved extends DialogBox implements HasMaximizeHandlers<DialogBoxImproved>, HasRestoreHandlers<DialogBoxImproved>
{
	int height = 0;

	int width = 0;

	int leftPopUpPosition = 0;

	int topPopUpPosition = 0;

	Image windowCloseButton = new Image();

	Image windowMaximizeButton = new Image();

	Image windowMinimizeButton = new Image();

	Image windowRestoreButton = new Image();

	HTML windowPrintButton = new HTML( "Print" );

	HTML title = new HTML( "" );

	public DialogBoxImproved( boolean autoHide, boolean modal )
	{
		super( autoHide, modal );

		Element td = getCellElement( 0, 1 );

		HorizontalPanel captionPanel = new HorizontalPanel();

		td.removeChild( td.getFirstChildElement() );

		td.appendChild( captionPanel.getElement() );

		captionPanel.setStyleName( "Caption" );//width-100%

		captionPanel.add( title );

		String urlPrefix = "images";

		if ( !GWT.isScript() && GWT.isClient() )
		{
			urlPrefix = "/" + urlPrefix;
		}

		windowCloseButton.setUrl( urlPrefix + "/window-button-close.png" );

		windowMaximizeButton.setUrl( urlPrefix + "/window-button-maximize.png" );

		windowMinimizeButton.setUrl( urlPrefix + "/window-button-minimize.png" );

		windowRestoreButton.setUrl( urlPrefix + "/window-button-restore.png" );

		windowPrintButton.addStyleName( "captionImages" );

		windowPrintButton.addStyleName( "htmlHand" );

		windowMaximizeButton.addStyleName( "captionImages" );

		windowMaximizeButton.addStyleName( "htmlHand" );

		windowMinimizeButton.addStyleName( "captionImages" );

		windowMinimizeButton.addStyleName( "htmlHand" );

		windowRestoreButton.addStyleName( "captionImages" );

		windowRestoreButton.addStyleName( "htmlHand" );

		windowCloseButton.addStyleName( "captionImages" );//float:right

		windowCloseButton.addStyleName( "htmlHand" );

		HorizontalPanel innerCaptionPanel = new HorizontalPanel();

		innerCaptionPanel.add( windowPrintButton );

		innerCaptionPanel.add( windowMaximizeButton );

		innerCaptionPanel.add( windowRestoreButton );

		innerCaptionPanel.add( windowMinimizeButton );

		innerCaptionPanel.add( windowCloseButton );

		windowPrintButton.setVisible( false );

		windowMaximizeButton.setVisible( false );

		windowMinimizeButton.setVisible( false );

		windowCloseButton.setVisible( false );

		windowRestoreButton.setVisible( false );

		captionPanel.setHorizontalAlignment( HasHorizontalAlignment.ALIGN_RIGHT );

		captionPanel.add( innerCaptionPanel );
	}

	public DialogBoxImproved( boolean autoHide )
	{
		this( autoHide, true );
	}

	public DialogBoxImproved()
	{
		this( false );
	}

	@Override
	public String getHTML()
	{
		return this.title.getHTML();
	}

	@Override
	public String getText()
	{
		return this.title.getText();
	}

	@Override
	public void setHTML( String html )
	{
		this.title.setHTML( html );
	}

	@Override
	public void setText( String text )
	{
		this.title.setText( text );
	}


	@Override
	protected void onPreviewNativeEvent( Event.NativePreviewEvent event )  //todo here on mouse up we can make it resizable
	{
		NativeEvent nativeEvent = event.getNativeEvent();

		if ( !event.isCanceled()
			 && ( event.getTypeInt() == Event.ONCLICK )
			 && isCloseEvent( nativeEvent ) )
		{
			this.hide();
		}
		else if ( !event.isCanceled()
				  && ( event.getTypeInt() == Event.ONCLICK )
				  && isMaximizeEvent( nativeEvent ) )
		{
			windowRestoreButton.setVisible( true );

			windowMaximizeButton.setVisible( false );

			leftPopUpPosition = this.getPopupLeft();

			topPopUpPosition = this.getPopupTop();

			width = this.getWidget().getElement().getClientWidth();

			height = this.getWidget().getElement().getClientHeight();

			this.getWidget().setSize( Window.getClientWidth() - 25 + "px", Window.getClientHeight() - 50 + "px" );

			MaximizeEvent.fire( this, this );

			this.setPopupPosition( 0, 0 );
		}
		else if ( !event.isCanceled()
				  && ( event.getTypeInt() == Event.ONCLICK )
				  && isRestoreEvent( nativeEvent ) )
		{
			windowRestoreButton.setVisible( false );

			windowMaximizeButton.setVisible( true );

			this.getWidget().setSize( width + "px", height + "px" );

			this.setPopupPosition( leftPopUpPosition, topPopUpPosition );

			RestoreEvent.fire( this, this );
		}
		else if ( !event.isCanceled() &&
				  event.getTypeInt() == Event.ONKEYDOWN &&
				  event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE &&
				  windowCloseButton.isVisible() )
		{
			this.hide();
		}

		super.onPreviewNativeEvent( event );
	}

	private boolean isCloseEvent( NativeEvent event )
	{
		return event.getEventTarget().equals( windowCloseButton.getElement() );//compares equality of the underlying DOM elements
	}

	private boolean isMaximizeEvent( NativeEvent event )
	{
		return event.getEventTarget().equals( windowMaximizeButton.getElement() );//compares equality of the underlying DOM elements
	}

	private boolean isRestoreEvent( NativeEvent event )
	{
		return event.getEventTarget().equals( windowRestoreButton.getElement() );//compares equality of the underlying DOM elements
	}

	public void setWindowCloseButtonEnabled( boolean use )
	{
		windowCloseButton.setVisible( use );
	}

	public void setWindowMaximizeButtonEnabled( boolean use )
	{
		windowMaximizeButton.setVisible( use );
	}

	public void setWindowPrintButtonEnabled( boolean use )
	{
		windowPrintButton.setVisible( use );
	}

	public HTML getWindowPrintButton()
	{
		return windowPrintButton;
	}

//	public void useWindowRestoreButton( boolean use )
//	{
//		windowRestoreButton.setVisible( use );
//	}

//	public void useWindowMinimizeButton( boolean use )
//	{
//		windowMinimizeButton.setVisible( use );
//	}

	@Override
	public HandlerRegistration addMaximizeHandler(MaximizeHandler<DialogBoxImproved> handler )
	{
		return addHandler(handler, MaximizeEvent.getType());
	}

	@Override
	public HandlerRegistration addRestoreHandler(RestoreHandler<DialogBoxImproved> handler )
	{
		return addHandler(handler, RestoreEvent.getType());
	}
}
