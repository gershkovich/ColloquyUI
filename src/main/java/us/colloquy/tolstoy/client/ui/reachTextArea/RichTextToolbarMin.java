package us.colloquy.tolstoy.client.ui.reachTextArea;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import us.colloquy.tolstoy.client.ui.dialogBoxImproved.DialogBoxImproved;

/**
 * Created by IntelliJ IDEA.
 * User: nm343
 * Date: 11/18/14
 * Time: 4:29 PM
 */
public class RichTextToolbarMin extends Composite
{
	private EventHandler handler = new EventHandler();

	private RichTextAreaImproved richText;

	private RichTextAreaImproved.Formatter formatter;

	private VerticalPanel outer = new VerticalPanel();

	private HorizontalPanel topPanel = new HorizontalPanel();

	private ToggleButton bold;

	private ToggleButton italic;

	private ToggleButton underline;

	private PushButton removeFormat;

	private PushButton symbols;

	private PushButton gainOfFunction;

	private PushButton lossOfFunction;

	private String title = "";

	private Label titleLabel =  new Label();

	private DialogBoxImproved dialogBox = new DialogBoxImproved();

	protected void onUnload()
	{
		if ( dialogBox != null )
		{
			if ( dialogBox.isShowing() )
			{
				dialogBox.hide( true );
			}
		}
	}

	/**
	 * Creates a new toolbar that drives the given rich text area.
	 *
	 * @param richText the rich text area to be controlled
	 */
	public RichTextToolbarMin(RichTextAreaImproved richText, String titleIn)
	{
		outer.setVisible(false);

		this.richText = richText;

		this.formatter = richText.getFormatter();

		outer.add(topPanel);
		// outer.add(bottomPanel);
		// topPanel.setWidth("100%");
		outer.setWidth("100%");
		//bottomPanel.setWidth("100%");

		initWidget(outer);

		setStyleName("gwt-RichTextToolbarMin");
		//richText.addStyleName("hasRichTextToolbar");

		topPanel.setHeight( "24px" );

		title = titleIn;

		titleLabel.setText( title );

		titleLabel.setStyleName( "title_label" );

		titleLabel.addClickHandler( handler );

		topPanel.add( titleLabel );

		topPanel.setCellVerticalAlignment( titleLabel, HasVerticalAlignment.ALIGN_BOTTOM );

		if ( formatter != null )
		{
			bold = createToggleButton("b", "bold");

			topPanel.setCellVerticalAlignment( bold, HasVerticalAlignment.ALIGN_BOTTOM );
			topPanel.add(bold);


			italic = createToggleButton("i", "italic");
			topPanel.setCellVerticalAlignment( italic, HasVerticalAlignment.ALIGN_BOTTOM );

			topPanel.add(italic);

			underline = createToggleButton("u","underline");
			topPanel.setCellVerticalAlignment( underline, HasVerticalAlignment.ALIGN_BOTTOM );

			topPanel.add(underline);


			richText.addKeyUpHandler(handler);
			richText.addClickHandler(handler);
		}

		//add symbols handling
		dialogBox = createDialogBox();

		dialogBox.setModal( false );

		dialogBox.setGlassEnabled(false);

		dialogBox.setAnimationEnabled(true);

		// Create a button to show the dialog Box
		final Button symbolButton = new Button( "&zeta;" );

		symbolButton.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				int y = symbolButton.getElement().getAbsoluteTop();

				int x = symbolButton.getElement().getAbsoluteRight();

				dialogBox.setPopupPosition( x + 5, y - 60 );

				dialogBox.show();
			}
		} );

		symbolButton.setWidth("18px");


		symbolButton.addStyleName("gwt-ToggleButton-up");

		topPanel.setCellVerticalAlignment( symbolButton, HasVerticalAlignment.ALIGN_BOTTOM );

		topPanel.add(symbolButton);

		if (titleIn.equalsIgnoreCase("Predicted effects on protein structure/function"))
		{

			topPanel.add(new HTML( "<div style=\"border-left:1px solid #000;height:18px; border-color: white; margin-left: 7px; margin-right: 5px;\"></div>") );

			gainOfFunction = createPushButton("\u27b6", "activating");
			topPanel.setCellVerticalAlignment( gainOfFunction, HasVerticalAlignment.ALIGN_BOTTOM );

			topPanel.add(gainOfFunction);

			lossOfFunction = createPushButton("\u27b4", "loss of function");
			topPanel.setCellVerticalAlignment( lossOfFunction, HasVerticalAlignment.ALIGN_BOTTOM );

			topPanel.add(lossOfFunction);

		}



	}

	public void setVisibleTitle( boolean isVisibleIn )
	{
		outer.setVisible( true );

		for ( int i = 0; i < topPanel.getWidgetCount(); i++ )
		{
			topPanel.getWidget( i ).setVisible( false );

			if ( i == 0 && topPanel.getWidget( i ) instanceof Label)
			{
				topPanel.getWidget( i ).setVisible( isVisibleIn );
			}
		}

		dialogBox.hide( false );
	}

	public void setVisibleToggleButtons( boolean isVisibleIn )
	{
		outer.setVisible( true );

		for ( int i = 0; i < topPanel.getWidgetCount(); i++ )
		{
			topPanel.getWidget( i ).setVisible( isVisibleIn );
		}
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

	private DialogBoxImproved createDialogBox()
	{
		// Create a dialog box and set the caption text
		final DialogBoxImproved dialogBox = new DialogBoxImproved();

//		dialogBox.ensureDebugId("cwDialogBox");

		dialogBox.setHTML( "<b>Special Symbols</b>" );

		dialogBox.setWindowCloseButtonEnabled( true );

		// Create a table to layout the content
		VerticalPanel dialogContents = new VerticalPanel();

		dialogContents.setSpacing(4);

		dialogBox.setWidget(dialogContents);

		Grid symbolGrid = makeSymbolGrid();

		dialogContents.add(symbolGrid);

		dialogContents.setCellHorizontalAlignment( symbolGrid, HasHorizontalAlignment.ALIGN_CENTER );

		// Add a close button at the bottom of the dialog
//		Button closeButton = new Button("close", new ClickHandler()
//		{
//			public void onClick(ClickEvent event)
//			{
//				dialogBox.hide();
//			}
//		});
//
//		dialogContents.add(closeButton);


		// Return the dialog box
		return dialogBox;
	}

	private Grid makeSymbolGrid()
	{
		final String [] greekLetters = {"Alpha","Beta","Gamma","Delta","Epsilon","Zeta","Eta","Theta","Iota",
										"Kappa","Lambda","Mu","Nu","Xi","Omicron","Pi","Rho","Sigma","Tau",
										"Upsilon","Phi","Chi","Psi","Omega"} ;

		Grid grid = new Grid( 1, 24);

		grid.setStyleName( "cw-RichText" );

		grid.setWidth( "100%" );

		grid.setHeight( "100%" );

		int pos = 0;

		for (int i = 0; i < 1; i++)
		{
			for (int j = 0; j < 24; j++)
			{
				pos = addSymbolPushButtons(greekLetters, grid, pos, i, j);
			}
		}

		// grid.getCellFormatter().setStyleName(1, 0, "cell-rta");

		return grid;
	}

	private int addSymbolPushButtons(String[] greekLetters, Grid grid, int pos, int i, int j )
	{
        final PushButton hp = new PushButton();

		final String letterName = greekLetters[pos++];

		hp.setHTML("&" + letterName.toLowerCase()+ ";"  );

		hp.addClickHandler( new ClickHandler()
		{
			@Override
			public void onClick( ClickEvent event )
			{
				richText.insertHTMLatCursorPosition( " &" + letterName.toLowerCase() + ";" );
			}
		} );

		grid.setWidget( i, j, hp );

		return pos;
	}

	private ToggleButton createToggleButton(String label, String tip )
	{
		ToggleButton tb = new ToggleButton( label);
		tb.addClickHandler(handler);
		tb.setWidth( "18px" );
		tb.addStyleName( "toggle_bt" );
		tb.setTitle( tip );
		return tb;
	}


	private PushButton createPushButton(String label, String tip )
	{
		PushButton tb = new PushButton( label);
		tb.addClickHandler(handler);
		tb.setWidth( "18px" );
		tb.addStyleName( "push_bt" );
		tb.setTitle( tip );
		return tb;
	}

	/**
	 * Updates the status of all the stateful buttons.
	 */
	private void updateStatus()
	{

		if ( formatter != null )
		{
			bold.setDown(formatter.isBold());
			italic.setDown(formatter.isItalic());
			underline.setDown(formatter.isUnderlined());

		}

	}

	private class EventHandler implements ClickHandler, ChangeHandler,
            KeyUpHandler, FocusHandler, BlurHandler
	{

		public void onChange( ChangeEvent event )
		{


		}

		public void onClick( ClickEvent event )
		{

			Widget sender = (Widget) event.getSource();

			setVisibleToggleButtons( true );

			if ( sender == bold )
			{
				formatter.toggleBold();
			}
			else if ( sender == italic )
			{
				formatter.toggleItalic();
			}
			else if ( sender == underline )
			{
				formatter.toggleUnderline();
			}
			else if ( sender == removeFormat )
			{
				formatter.removeFormat();
			}
			else if ( sender == richText )
			{
				updateStatus();
			}
			else if ( sender == gainOfFunction )
			{

				richText.setHTML("<b>Oncogenic, activating mutation</b>");
			}
			else if ( sender == lossOfFunction )
			{formatter.toggleBold();
				richText.setHTML("<b>Loss of tumor suppressor function</b>");
			}
//			else if ( sender == titleLabel )
//			{
//				richText.getParent().setHeight( "30px" );
//
//				setVisibleTitle( true );
//			}
		}

		public void onKeyUp( KeyUpEvent event )
		{

			Widget sender = (Widget) event.getSource();
			if ( sender == richText )
			{
				updateStatus();
			}
		}

		@Override
		public void onBlur( BlurEvent event )
		{
			outer.setVisible( false );
		}

		@Override
		public void onFocus( FocusEvent event )
		{
			outer.setVisible( true );

		}
	}
}